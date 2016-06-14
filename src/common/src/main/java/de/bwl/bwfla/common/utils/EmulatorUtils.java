package de.bwl.bwfla.common.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPBinding;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;

import de.bwl.bwfla.api.eaas.EaasWS;
import de.bwl.bwfla.api.eaas.EaasWSService;
import de.bwl.bwfla.common.datatypes.Binding;
import de.bwl.bwfla.common.exceptions.BWFLAException;

public class EmulatorUtils 
{
	protected static final Logger log = Logger.getLogger("EmulatorUtils");

	public enum XmountOutputFormat {
		RAW("raw"),
		VDI("vdi"),
		VHD("vhd"),
		VMDK("vmdk");

		private final String format;

		private XmountOutputFormat(String s) {
			this.format = s;
		}
		public String toString() {
			return this.format;
		}
	}


	public static String computeMd5(File file) 
	{
		String result = new String();

		try 
		{
			OutputStream out = new NullOutputStream();
			MessageDigest md = MessageDigest.getInstance("MD5");
			InputStream is = new DigestInputStream(new FileInputStream(file), md);
			IOUtils.copy(is, out);
			out.flush();
			out.close();
			is.close();
			byte[] digest = md.digest();
			for (int i = 0; i < digest.length; i++) {
				result += Integer.toString((digest[i] & 0xff) + 0x100, 16)
						.substring(1);
			}
		} 
		catch (NoSuchAlgorithmException | IOException e) 
		{
			log.warning(e.getMessage());
		}

		return result;
	}

    public static String connectBinding(Binding resource, Path resourceDir,
            XmountOptions xmountOpts)
                    throws BWFLAException, IllegalArgumentException {
        String resUrl = resource.getUrl();

        // TODO handle.net resolution according to used transport

        if (resource == null || resource.getId() == null
                || resource.getId().isEmpty()) {
            throw new IllegalArgumentException(
                    "Given resource is null or has invalid id.");
        }

        if (resource.getAccess() == null)
            resource.setAccess(Binding.AccessType.COW);

        if (resource.getId() == null || resUrl == null
                || resource.getId().isEmpty() || resUrl.isEmpty()) {
            throw new IllegalArgumentException(
                    "Given id is null or has invalid id.");
        }

        String resFile = null;
        switch (resource.getAccess()) {
        case COW:
            // create cow container
            // Qemu's block layer driver handles many transport protocols,
            // as long as we don't want to support yet another one, we can
            // safely ignore transports here and just pass the url to Qemu.
            Path cowPath = resourceDir.resolve(resource.getId() + ".cow");

            EmulatorUtils.createCowFile(resUrl, cowPath);

            Path fuseMountpoint = cowPath
                    .resolveSibling(cowPath.getFileName() + ".fuse");
            try {
                resFile = mountCowFile(cowPath, fuseMountpoint, xmountOpts)
                        .toString();
            } catch (IOException e) {
                throw new BWFLAException("Could not fuse-mount image file.", e);
            }
            break;
        case COPY:
            // use qemu-imgs convert feature to create a new local raw copy
            Path imgCopy = resourceDir.resolve(resource.getId() + ".copy");
            ProcessRunner process = new ProcessRunner("qemu-img");
            process.addArgument("convert");
            process.addArgument("-fraw");
            process.addArgument("-O" + xmountOpts.getOutFmt().toString().toLowerCase());
            process.addArgument(resUrl);
            process.addArgument(imgCopy.toString());
            if (!process.execute()) {
                throw new BWFLAException(
                        "Cannot create local copy of the binding's data, connecting binding cancelled.");
            }

            resFile = imgCopy.toString();
            break;
        default:
            log.severe("This should never happen!");
        }

        return resFile;
    }

	public static Path prepareSoftwareCollection(String handle, Path tempPath) {
		try {

			Path image = handleToPath(handle, tempPath);

			Path mountpoint = java.nio.file.Files.createTempDirectory(tempPath,	"fuse-");
			ProcessRunner process = new ProcessRunner("fuseiso");
			process.addArgument(image.toString());
			process.addArgument(mountpoint.toString());
			if (!process.execute())
				return null;

			return mountpoint;

		} catch (Exception e) {
			log.severe("Temporary mountpoint cannot be created: " + e.getMessage());
		}
		return null;
	}

	private static Path handleToPath(String handle, Path tempPath) throws Exception
	{
		ProcessRunner process = new ProcessRunner();

		// creating local cow image file
		Path cowFile = java.nio.file.Files.createTempFile(tempPath, "cow-", ".qcow2");
		process.setCommand("qemu-img");
		process.addArguments("create", "-f", "qcow2", "-o");
		process.addArgument("backing_file=", handle, ",backing_fmt=raw");
		process.addArgument(cowFile.toString());
		process.execute();

		// loop-mounting cow image to produce raw block stream
		// representation
		Path mountpoint = Files.createTempDirectory(tempPath, "fuse-");
		process.setCommand("qemu-fuse");
		process.addArguments("-o", "kernel_cache",
				             "-o", "noforget",
				             "-o", "large_read",
				             "-o", "max_readahead=131072",
				             "-o", "allow_root");
		process.addArgument(cowFile.toString());
		process.addArgument(mountpoint.toString());
		process.execute();

		return mountpoint.resolve(cowFile.getFileName());
	}

	/**
	 * Creates a copy-on-write wrapper (in qcow2 file format) for imgUrl at the
	 * specified directory.
	 * 
	 * @param imgUrl
	 *            The image url to be wrapped (any valid Qemu url)
	 * @param cowPath
	 *            Path where the qcow2 file will be created at.
	 * @return true if the cow file could be created successfully, false
	 *         otherwise.
	 */
	public static void createCowFile(String imgUrl, Path cowPath) throws BWFLAException {
		ProcessRunner process = new ProcessRunner();

		// determine if image is derivative
		process.setCommand("qemu-img");
		process.addArgument("info");
		process.addArgument(imgUrl);
		if (!process.start()) {
			throw new BWFLAException("qemu-img invocation failed. Check log output for ProcessRunner's information.");
		}

		process.waitUntilFinished();

		// if the image has a backing file, it already is a derivative
		boolean isDerivative = false;
		try {
			String output = process.getStdOutString();
			isDerivative = output.contains("backing file");
		}
		catch (IOException exception) {
			log.warning("Could not determine if an image is derivative! Assuming non-derivative.");
		}

		// Should this go in a try..finally statement?
		process.cleanup();

		process.setCommand("qemu-img");
		process.addArguments("create", "-f", "qcow2", "-o");
		process.addArgument("backing_file=", imgUrl);
		process.addArgument(cowPath.toString());

		if (!process.execute()) {
			try {
				Files.deleteIfExists(cowPath);
			} catch (Exception e) {
				log.severe("Created a temporary file but cannot delete it after error. This is bad.");
			}
			throw new BWFLAException("Could not create local COW file. See log output for more information (maybe).");
		}
	}

	/**
	 * (FUSE-)mounts a file to the specified mountpoint. The mountpoint is
	 * created if it does not exist.
	 * 
	 * @param imagePath
	 *            The file to be mounted
	 * @param mountpoint
	 *            The mountpoint the file is mounted at
	 * @param command
	 *            The commandline used to mount imagePath at mountpoint
	 * @return true if the file could successfully be mounted, false otherwise
	 */
	protected static void mountFile(Path imagePath, Path mountpoint,
			String command) throws IllegalArgumentException, BWFLAException {
		if (imagePath == null) {
			throw new IllegalArgumentException("Given image path was null");
		}
		if (!Files.isRegularFile(imagePath)) {
			throw new IllegalArgumentException("Given image path \"" + imagePath + "\" is not a regular file.");
		}

		try {
			// create mountpoint if necessary
			Files.createDirectories(mountpoint);

			ProcessRunner process = new ProcessRunner(command);
			if (!process.execute()) {
				try {
					Files.deleteIfExists(mountpoint);
				} catch (IOException e) {
					log.severe("Created a temporary file but cannot delete it after error. This is bad.");
				}
				throw new BWFLAException("Error mounting " + imagePath.getFileName()
						+ ". See log output for more information (maybe).");
			}
		} catch (IOException e) {
			throw new BWFLAException("Error mounting " + imagePath.getFileName() + ".", e);
		}
	}

	public static Path mountCowFile(Path image, Path mountpoint)
			throws IllegalArgumentException, IOException, BWFLAException {
		return mountCowFile(image, mountpoint, null);
	}
	
	/**
	 * Mounts a QEMU qcow file (or any QEMU image file) to the specified
	 * mountpoint. The mountpoint is created if it does not exist.
	 * 
	 * @param image
	 *            Path to the image file to be mounted.
	 * @param mountpoint
	 *            Path where the qcow2 file will be mounted to.
	 * @param readonly
	 *            Whether the image should be mounted read-only (default false)
	 * @param outputFormat
	 *            Determines the output format of the mount operation (default raw)
	 * @return Path to the image within the mountpoint
	 * @throws BWFLAException if the mounting fails (see cause for further info)
	 * @throws IllegalArgumentException if the image cannot be used as a mount source
	 * @throws IOException If readonly is false and the image cannot be mounted read/write
	 */
	public static Path mountCowFile(Path image, Path mountpoint, XmountOptions xmountOpts) throws IllegalArgumentException,
			IOException, BWFLAException {
		if (image == null) {
			throw new IllegalArgumentException("Given image path was null");
		}
		if (!Files.isRegularFile(image)) {
			throw new IllegalArgumentException("Given image path \"" + image
					+ "\" is not a regular file.");
		}
		if (!xmountOpts.isReadonly() && !Files.isWritable(image)) {
			throw new IOException("Given image path \"" + image
					+ "\" is not writable but rw access was requested.");
		}
		
		return xmount(image.toAbsolutePath().toString(), 
				mountpoint, xmountOpts);	
	}
	
	public static Path xmount(String imagePath, Path mountpoint, 
				XmountOptions xmountOpts)
		throws IllegalArgumentException,IOException, BWFLAException
	{
		if(xmountOpts == null)
			xmountOpts = new XmountOptions();
		
		// This mimicks the behavior of xmount which looks for the
	    // last slash in the string and takes everything up until
	    // the last dot in the string as the base name for the image
        String baseName = imagePath.substring(imagePath.lastIndexOf('/') + 1);
        if (baseName.lastIndexOf('.') > 0)
            baseName = baseName.substring(0, baseName.lastIndexOf('.'));
	    
	    try {
			// create mountpoint if necessary
			Files.createDirectories(mountpoint);
			log.info("created subdirectories up to " + mountpoint.toString());

			ProcessRunner process = new ProcessRunner("xmount");
			process.addArguments("--in", "qemu", imagePath);
			xmountOpts.setXmountOptions(process);
			process.addArguments(mountpoint.toAbsolutePath().toString());

			if (!process.execute()) {
				try {
					Files.deleteIfExists(mountpoint);
				} catch (IOException e) {
					log.severe(
							"Created a temporary file but cannot delete it after error. This is bad.");
				}
				throw new BWFLAException("Error mounting " + imagePath
						+ ". See log output for more information (maybe).");
			}

			switch (xmountOpts.getOutFmt()) {
			case RAW:
				return mountpoint.resolve(baseName + ".dd");
			case VDI:
			case VHD:
			case VMDK:
				return mountpoint.resolve(baseName + "."
						+ xmountOpts.getOutFmt().toString().toLowerCase());
			default:
				return null;
			}
		} catch (IOException e) {
			throw new BWFLAException(
					"Error mounting " + imagePath + ".", e);
		}
	}
	
    
    
	/**
	 * Mounts a UDF or ISO file to the specified mountpoint. The mountpoint is
	 * created if it does not exist.
	 * 
	 * @param imagePath
	 *            The image filename to be mounted.
	 * @param mountpoint
	 *            Path where the udf file will be mounted to.
	 * @return true if the file could be mounted successfully, false otherwise
	 * @throws BWFLAException if the mounting fails (see cause for further info)
	 * @throws IllegalArgumentException if the imagePath cannot be used as a mount source
	 */
	public static void mountUdfFile(Path imagePath, Path mountpoint) throws IllegalArgumentException, BWFLAException {
		String command = "fuseiso " + imagePath.toString() + " "
				+ mountpoint.toString() + " -o allow_root";
		mountFile(imagePath, mountpoint, command);
	}

	public static Path mountUdfFile(Path imagePath) 
			throws IllegalArgumentException, IOException, BWFLAException 
	{
		Path mountpoint = Files.createTempDirectory("iso-mount-");
		try {
			mountUdfFile(imagePath, mountpoint);
		} catch (BWFLAException e) {
			Files.deleteIfExists(mountpoint);
			throw new BWFLAException(e);
		}
		return mountpoint;
	}

	public static EaasWS getEaas(URL wsdl) {
		if (wsdl == null)
			return null;

		EaasWSService service = new EaasWSService(wsdl);
		EaasWS comp = service.getEaasWSPort();

		BindingProvider bp = (BindingProvider) comp;
		SOAPBinding binding = (SOAPBinding) bp.getBinding();
		bp.getRequestContext().put("javax.xml.ws.client.receiveTimeout", "0");
		bp.getRequestContext()
		.put("javax.xml.ws.client.connectionTimeout", "0");
		binding.setMTOMEnabled(true);
		bp.getRequestContext()
		.put("com.sun.xml.internal.ws.transport.http.client.streaming.chunk.size",
				8192);
		return comp;
	}
	
	public static File mount(Path path, String fsType) throws BWFLAException, IOException
	{
		if(path == null)
			return null;
		
		ProcessRunner process = new ProcessRunner();
		File tempDir = Files.createTempDirectory("mount-").toFile();

		process.setCommand("sudo");
		process.addArgument("/bin/mount");
		if(fsType != null)
		{
			process.addArgument("-t");
			process.addArgValue(fsType);
		}
		process.addArgument(path.toString());
		process.addArgument(tempDir.getAbsolutePath());
		if(!process.execute())
		{	
			FileUtils.deleteDirectory(tempDir);
			throw new BWFLAException("mount failed");
		}
		return tempDir;
	}
	
	public static void unmount(File tempDir) throws BWFLAException, IOException
	{
		if(tempDir == null)
			return;
		ProcessRunner process = new ProcessRunner();
		process.setCommand("sudo");
		process.addArgument("/bin/umount");
		process.addArgument(tempDir.getAbsolutePath());
		if(!process.execute())
		{
			throw new BWFLAException("unmounting " + tempDir.getAbsolutePath() + "failed");
		}
		FileUtils.deleteDirectory(tempDir);
	}
	
	public static void unmountFuse(File tempDir) throws BWFLAException, IOException
	{
		if(tempDir == null)
			return;
		
		ProcessRunner process = new ProcessRunner();
		process.setCommand("fusermount");
		process.addArguments("-u", "-z");
		process.addArgument(tempDir.toString());
		if(!process.execute())
		{
			throw new BWFLAException("unmounting " + tempDir.getAbsolutePath() + " failed");
		}
		
		FileUtils.deleteDirectory(tempDir);
	}
	
	public static boolean padFile(File f, int blocksize)
	{
		if(!f.exists())
			return false;
		
		long fileSize = f.length();
		if(fileSize == 0 || fileSize % blocksize == 0)
			return false;
		
		int padding = (int) (blocksize - (fileSize % blocksize)) % blocksize;
		log.info("Warn: padding file: " + f.getName());
		
		byte[] bytes = new byte[padding];
		Arrays.fill( bytes, (byte) 0 );
		try {
			FileOutputStream output = new FileOutputStream(f, true);
			output.write(bytes);
			output.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	
}

