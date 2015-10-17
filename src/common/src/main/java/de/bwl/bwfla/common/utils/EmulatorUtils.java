/*
 * This file is part of the Emulation-as-a-Service framework.
 *
 * The Emulation-as-a-Service framework is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * The Emulation-as-a-Service framework is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Emulation-as-a-Software framework.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package de.bwl.bwfla.common.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPBinding;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import de.bwl.bwfla.api.eaas.EaasWS;
import de.bwl.bwfla.api.eaas.EaasWSService;
import de.bwl.bwfla.common.datatypes.EmulationEnvironment;
import de.bwl.bwfla.common.datatypes.Environment;
import de.bwl.bwfla.common.datatypes.NetworkEnvironment;
import de.bwl.bwfla.common.datatypes.Resource;
import de.bwl.bwfla.common.services.container.helpers.ContainerHelper;
import de.bwl.bwfla.common.services.container.helpers.ContainerHelperFactory;
import de.bwl.bwfla.common.services.container.types.Container;
import de.bwl.bwfla.common.services.container.types.Container.Filesystem;
import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.DigitalObjectContent;
import eu.planets_project.services.datatypes.ImmutableDigitalObject;

public class EmulatorUtils 
{
	protected static final Logger log = Logger.getLogger("EmulatorUtils");

	
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

	public static Map<String, ImmutableDigitalObject> prepareFilesForInjection(
			Map<String, List<File>> uploads) {
		Map<String, ImmutableDigitalObject> objsInj = new HashMap<>();

		for (Map.Entry<String, List<File>> upload : uploads.entrySet()) {
			String dev = upload.getKey();
			List<File> files = upload.getValue();
			Filesystem fs;

			switch (dev) {
			case "disk":
				fs = Filesystem.FAT16;
				break;

			case "floppy":
				fs = Filesystem.FAT12;
				break;

			case "cdrom":
			case "iso":
				fs = Filesystem.ISO;
				break;

			default:
				log.warning("the supplied device type is either invalid or is currently unsupported, skipping: "
						+ dev);
				continue;
			}

			ContainerHelper helper = ContainerHelperFactory.getContainerHelper(
					dev, fs);
			Container container = helper.createEmptyContainer();
			helper.insertIntoContainer(container, files);

			DigitalObjectContent doContent = Content.byReference(container
					.getFile());
			DigitalObject.Builder builder = new DigitalObject.Builder(doContent);
			builder.title("do_" + dev + "_container_");

			ImmutableDigitalObject obj = (ImmutableDigitalObject) builder
					.build();
			objsInj.put(dev, obj);
		}

		return objsInj;
	}

	public static String connectBinding(Resource resource, Path resourceDir) {
		// TODO: in Java, this function should throw exceptions
		// instead of returning failure states

		String resUrl = resource.getUrl();
		log.info("resource: " + resUrl);

		// TODO handle.net resolution according to used transport

		if (resource.getAccess() == null)
			resource.setAccess(Resource.AccessType.COPY);

		String resFile = null;
		switch (resource.getAccess()) {
		case COW:
			// create cow container
			// Qemu's block layer driver handles many transport protocols,
			// as long as we don't want to support yet another one, we can
			// safely ignore transports here and just pass the url to Qemu.
			Path cowPath = resourceDir.resolve(resource.getId() + ".cow");
			if (!EmulatorUtils.createCowFile(resUrl, cowPath)) {
				log.info("Cannot create cow file, connecting binding cancelled: "
						+ cowPath.toString());
				return null;
			}
			Path fuseMountpoint = cowPath.resolveSibling(cowPath.getFileName()
					+ ".fuse");
			if (!EmulatorUtils.mountCowFile(cowPath,
					cowPath.resolveSibling(cowPath.getFileName() + ".fuse"))) {
				log.info("Cannot create cow file, connecting binding cancelled: "
						+ cowPath.toString());
				return null;
			}

			resFile = fuseMountpoint.resolve(cowPath.getFileName()).toString();
			break;
		case COPY:
			// use qemu-imgs convert feature to create a new local raw copy
			Path imgCopy = resourceDir.resolve(resource.getId() + ".copy");
			ProcessRunner process = new ProcessRunner("qemu-img");
			process.addArgument("convert");
			process.addArgument("-fraw");
			process.addArgument("-Oraw");
			process.addArgument(resUrl);
			process.addArgument(imgCopy.toString());
			if (!process.execute()) {
				log.severe("Cannot create local copy of the binding's data, connecting binding cancelled.");
				return null;
			}

			resFile = imgCopy.toString();
			break;
		default:
			log.info("This should never happen!");
		}

		// resFile is now a local file that is either a file directly usable
		// by the emulator, or an EWF image containing such a file
		// So we need to wrap the EWF in a qcow2 container again in order
		// to access it.

		Path ewfLink = resourceDir.resolve(Paths.get(resFile).getFileName()
				+ ".E01");
		try {
			Files.createSymbolicLink(ewfLink, Paths.get(resFile));
		} catch (IOException e) {
			log.info("failed to create link");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ProcessRunner process = new ProcessRunner();

		// determine if image is derivative
		process.setCommand("qemu-img");
		process.addArgument("info");
		process.addArgument(ewfLink.toString());
		if (!process.start())
			return null;

		process.waitUntilFinished();

		// if the image has a backing file, it already is a derivative
		boolean isEWF = false;
		try {
			String output = process.getStdOutString();
			isEWF = output.contains("file format: ewf");
			log.info(output);
		} catch (IOException exception) {
			log.severe("Could not determine if an image is EWF!");
			exception.printStackTrace();
		}
		process.cleanup();

		if (isEWF) {
			log.info("unwrapping EWF file");
			Path unEwfPath = resourceDir.resolve(resource.getId() + ".unewf");

			process.setCommand("qemu-img");
			process.addArguments("create", "-f", "qcow2", "-o");
			process.addArgument("backing_file=", ewfLink.toString());
			process.addArgValue(",backing_fmt=ewf");

			process.addArgument(unEwfPath.toString());
			if (!process.execute()) {
				return null;
			}
			Path fuseMountpoint = unEwfPath.resolveSibling(unEwfPath
					.getFileName() + ".fuse");
			if (!EmulatorUtils.mountCowFile(unEwfPath, fuseMountpoint)) {
				log.info("Cannot create unEWF file, connecting binding cancelled: "
						+ unEwfPath.toString());
				return null;
			}

			resFile = fuseMountpoint.resolve(unEwfPath.getFileName())
					.toString();
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
				             "-o", "max_readahead=131072");
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
	public static boolean createCowFile(String imgUrl, Path cowPath) {
		// TODO: in Java, this method should throw exceptions instead
		// of returning failure states

		ProcessRunner process = new ProcessRunner();
		
		// determine if image is derivative
		process.setCommand("qemu-img");
		process.addArgument("info");
		process.addArgument(imgUrl);
		if (!process.start())
			return false;
		
		process.waitUntilFinished();
		
		// if the image has a backing file, it already is a derivative
		boolean isDerivative = false;
		try {
			String output = process.getStdOutString();
			isDerivative = output.contains("backing file");
		}
		catch (IOException exception) {
			log.severe("Could not determine if an image is derivative!");
			exception.printStackTrace();
		}
		
		process.cleanup();
		
		process.setCommand("qemu-img");
		process.addArguments("create", "-f", "qcow2", "-o");
		process.addArgument("backing_file=", imgUrl);

		if (!isDerivative) {
			// for base images, enforce the backing_fmt to
			// play nice with emulator-specific formats
			process.addArgValue(",backing_fmt=raw");
		}
		// else:
		//       for derivatives, use auto-detection of backing_fmt
		//       to accept the backing_file as containing diff data

		process.addArgument(cowPath.toString());
		
		return process.execute();
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
	protected static boolean mountFile(Path imagePath, Path mountpoint,
			String command) {
		// TODO: in Java, this method should throw exceptions instead
		// of returning failure states

		if (imagePath == null || !Files.isRegularFile(imagePath)) {
			log.severe("Image file cannot be accessed, mounting cancelled: "
					+ imagePath.toString());
			return false;
		}

		try {
			// create mountpoint if necessary
			Files.createDirectories(mountpoint);

			ProcessRunner process = new ProcessRunner(command);
			if (!process.execute())
				return false;

		} catch (IOException e) {
			log.severe("Temporary mountpoint cannot be created: "
					+ e.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * Mounts a QEMU qcow file (or any QEMU image file) to the specified
	 * mountpoint. The mountpoint is created if it does not exist.
	 * 
	 * @param imagePath
	 *            The image filename to be mounted.
	 * @param mountpoint
	 *            Path where the qcow2 file will be mounted to.
	 * @return true if the file could be mounted successfully, false otherwise
	 */
	public static boolean mountCowFile(Path imagePath, Path mountpoint) {
		// TODO: in Java, this method should throw exceptions instead
		// of returning failure states

		String command = "qemu-fuse -o kernel_cache -o noforget -o large_read -o max_readahead=131072 " + imagePath.toString() + " "
				+ mountpoint.toString();
		return mountFile(imagePath, mountpoint, command);
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
	 */
	public static boolean mountUdfFile(Path imagePath, Path mountpoint) {
		// TODO: in Java, this method should throw exceptions instead
		// of returning failure states

		String command = "fuseiso " + imagePath.toString() + " "
				+ mountpoint.toString();
		return mountFile(imagePath, mountpoint, command);
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
}

