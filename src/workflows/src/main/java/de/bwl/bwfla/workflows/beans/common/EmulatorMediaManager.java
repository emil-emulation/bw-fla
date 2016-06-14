package de.bwl.bwfla.workflows.beans.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.sun.xml.bind.v2.schemagen.episode.Bindings;

import de.bwl.bwfla.api.eaas.EaasWS;
import de.bwl.bwfla.common.datatypes.ArchiveBinding;
import de.bwl.bwfla.common.datatypes.Drive;
import de.bwl.bwfla.common.datatypes.EmulationEnvironment;
import de.bwl.bwfla.common.datatypes.Environment;
import de.bwl.bwfla.common.datatypes.FileCollection;
import de.bwl.bwfla.common.datatypes.FileCollectionEntry;
import de.bwl.bwfla.common.datatypes.Drive.DriveType;
import de.bwl.bwfla.common.datatypes.EaasState;
import de.bwl.bwfla.common.datatypes.utils.EmulationEnvironmentHelper;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.services.container.helpers.ContainerHelper;
import de.bwl.bwfla.common.services.container.helpers.ContainerHelperFactory;
import de.bwl.bwfla.common.services.container.helpers.ImageFileHelper;
import de.bwl.bwfla.common.services.container.types.CdromContainer;
import de.bwl.bwfla.common.services.container.types.Container;
import de.bwl.bwfla.common.services.container.types.HddZipContainer;
import de.bwl.bwfla.common.services.container.types.ImageFileContainer;
import de.bwl.bwfla.common.services.container.types.Container.Filesystem;
import de.bwl.bwfla.common.utils.ObjectArchiveHelper;
import de.bwl.bwfla.common.utils.Pair;
import de.bwl.bwfla.common.utils.Zip32Utils;
import de.bwl.bwfla.workflows.softwarearchive.SoftwareDescription;
import de.bwl.bwfla.workflows.softwarearchive.datatypes.BundledFile;

public class EmulatorMediaManager 
{
	protected static final Logger LOG = Logger.getLogger(AbstractRemoteEmulatorHelper.class.getName());
	// protected Map<Integer, String> containerDeviceTypes = new HashMap<>();

	protected List<ChosenMediaForDeviceBean> chosenMediaForDevices = null;

	private Map<String, List<File>> localFiles;
	private Map<String, List<File>> uploads;
	private Map<Integer, Container> containerIDs;
	private final Environment environment;
	private EaasWS eaas;
	private String sessionId = null;

	// we allow only one binding per session! 
	private BindingHandle binding = null;

	EmulatorMediaManager(Environment env)
	{
		this.localFiles = new HashMap<>();
		this.uploads = new HashMap<>();
		this.containerIDs = new HashMap<>();
		this.environment = env;

	}

	public void setEmulatorConnection(EaasWS eaas, String sessionId)
	{
		this.eaas = eaas;
		this.sessionId = sessionId;
	}

	public boolean addArchiveBinding(String host, String archive, String objectId, DriveType type) throws BWFLAException
	{
		if(!(environment instanceof EmulationEnvironment))
			return false;

		EmulationEnvironment env = (EmulationEnvironment)environment;
		int driveId = EmulationEnvironmentHelper.addArchiveBinding(env, host, archive, objectId, type);
		
		if(host == null || objectId == null || type == null)
		{
			LOG.severe("addArchiveBinding::invalid parameter: " + host + " " + archive + " " + objectId);
			return false;
		}

		ObjectArchiveHelper helper = new ObjectArchiveHelper(host);
		FileCollection fc = helper.getObjectReference(archive, objectId);
		
		ArchiveBindingHandle aHandle = new ArchiveBindingHandle();
		aHandle.bindingId = objectId;
		aHandle.driveId = driveId;
		aHandle.type = type;
		aHandle.fc = fc;
		aHandle.defaultFile = fc.files.get(0).getId();

		binding = aHandle;
		return true;
	}

	private int registerDrive(String binding, String path, Drive.DriveType driveType) 
	{
		if(!(environment instanceof EmulationEnvironment))
			return -1;

		EmulationEnvironment env = (EmulationEnvironment)environment;

		// construct URL
		String subres = "";
		if(path != null)
			subres += "/" + path.toString();

		String dataUrl = "binding://" + binding + subres;
		int driveId = -1;
		for (Drive drive : env.getDrive()) {
			++driveId; // hack: fix me

			if(drive.getType().equals(driveType) && (drive.getData() == null || drive.getData().isEmpty())) {
				drive.setData(dataUrl);
				break;
			}
		}
		return driveId;
	}

	public boolean attachSoftwarePackage(SoftwareDescription sw, URL bundle)
	{
		BundledFile file = null;
		try 
		{
			file = sw.getFiles().get(0);
		}
		catch (IndexOutOfBoundsException e) {
			LOG.severe("Software contains no bundled media files.");
			return false;
		}
		Drive.DriveType type = Drive.DriveType.valueOf(file.getType().name().toUpperCase());
		String bindingId = EmulationEnvironmentHelper.addBinding((EmulationEnvironment)this.environment, 
				bundle.toString());
		if(bindingId == null)
			return false;

		int driveId = registerDrive(bindingId, file.getPath().toString(), type);
		if(driveId < 0)
			return false;
		// this.containerDeviceTypes.put(driveId, type.value());

		SoftwareBindingHandle swBinding = new SoftwareBindingHandle();
		swBinding.bindingId = bindingId; 
		swBinding.bundleUrl = bundle;
		swBinding.selectedSoftware = sw;
		swBinding.driveId = driveId;
		swBinding.type = type;
		this.binding = swBinding;
		return true;
	}

	private void changeMedium(String deviceType, String objectRef) throws BWFLAException {
		if(eaas == null || sessionId == null)
			throw new BWFLAException("changeMedium: can only be called if emulator is running");

		Drive.DriveType type = Drive.DriveType.valueOf(deviceType);
		if (type == Drive.DriveType.CDROM_IMG || type == Drive.DriveType.DISK_IMG || type == DriveType.FLOPPY_IMG) {
			type = Drive.DriveType.convert(type);
		}

		if(binding == null)
			throw new BWFLAException("change medium: binding undefined.");

		int deviceId = binding.driveId;
		if (deviceId < 0) {
			throw new BWFLAException("Could not find a corresponding device id for type " + deviceType + "/" + type.value());
		}

		eaas.changeMedium(sessionId, deviceId, objectRef);
	}

	public List<Pair<WorkflowsFile, String>> detachAndDownloadContainers() throws BWFLAException
	{
		List<Pair<WorkflowsFile, String>> downloads = new ArrayList<>();

		if(eaas == null || sessionId == null)
			throw new BWFLAException("detachAndDownloadContainers: eaas is null, will not proceed (session either was remove before or not initialized yet)");

        for (int i = 0, DELAY_MS = 100; i < 5 * DELAY_MS; ++i) // wait for 5 sec
        {
            String state = eaas.getSessionState(sessionId);
            if (state.equalsIgnoreCase(EaasState.SESSION_STOPPED.value()))
                break;

            try {
                Thread.sleep(DELAY_MS);
            } catch (InterruptedException e) {
                throw new BWFLAException(e);
            }
        }

		if(containerIDs != null)
			for(Map.Entry<Integer, Container> containerID: containerIDs.entrySet())
			{
				File resultingFile = null;
				File detachedContainerFile = null;
				File filesDir = null;
				boolean success = false;

				try
				{ 
					// XXX: unsupported container types
					if(containerID.getValue() instanceof CdromContainer || containerID.getValue() instanceof ImageFileContainer || containerID.getValue() instanceof HddZipContainer)
						continue;

					Integer id = containerID.getKey();				
					detachedContainerFile = File.createTempFile("detached_medium_", null);
					DataHandler dh = eaas.detachMedium(sessionId, id);

					try(InputStream is = dh.getInputStream(); OutputStream os = new FileOutputStream(detachedContainerFile))
					{
						IOUtils.copy(is, os);
						os.flush();
					}

					Container container = containerID.getValue();
					container.setFile(detachedContainerFile);

					ContainerHelper helper = ContainerHelperFactory.getContainerHelper(container);
					if(helper == null)
					{
						LOG.severe("could not acquire helper for the following container type: " + container.getClass().getSimpleName());
						continue;
					}

					filesDir = helper.extractFromContainer(container);
					if(filesDir == null)
					{
						LOG.severe("either an error occured during extraction or container has no files in it: " + container.getClass().getSimpleName());
						continue;
					}

					File[] files = filesDir.listFiles();

					resultingFile = File.createTempFile("extracted_container_", ".zip");
					resultingFile.delete();
					String resultingTitle = null;

					if(files.length == 0)
					{
						LOG.warning("skipping, detached container seems to be empty, container ID: " + id);
						continue;
					}
					else
					{		
						Zip32Utils.zip(resultingFile, filesDir);
						resultingTitle = container.getClass().getSimpleName().toLowerCase() + ".zip";						
					}

					downloads.add(new Pair<WorkflowsFile, String>(new WorkflowsFile(resultingFile.getAbsolutePath()), resultingTitle));
					success = true;

				}
				catch(IOException | BWFLAException e)
				{
					e.printStackTrace();
				}
				finally
				{
					if(detachedContainerFile != null && detachedContainerFile.isFile())
						detachedContainerFile.delete();

					if(filesDir != null && filesDir.isDirectory())
						try
					{
							FileUtils.cleanDirectory(filesDir);
							filesDir.delete();
					}
					catch(IOException e)
					{
						e.printStackTrace();
					}

					if(!success && resultingFile != null && resultingFile.isFile())
						resultingFile.delete();
				}
			}

		return downloads;
	}

	public void setFilesToInject(Map<String, List<File>> upFiles)
	{
		uploads = upFiles;
	}

	void attachLocalFiles() throws BWFLAException
	{
		if(eaas == null || sessionId == null)
			throw new BWFLAException("attachLocalFiles: emulator is not initialized yet");

		Map<String, List<File>> injFiles = new HashMap<>();	
		injFiles.putAll(uploads);
		injFiles.putAll(localFiles);

		for(Map.Entry<String, List<File>> upload: uploads.entrySet())
		{
			String dev = upload.getKey();
			List<File> files = upload.getValue();

			Container container = EmulationEnvironmentHelper.createFilesContainer((EmulationEnvironment)environment, dev, files);
			int id = EmulationEnvironmentHelper.attachContainer(eaas, sessionId, dev, container);
			containerIDs.put(id, container);
			//	containerDeviceTypes.put(id, dev.toUpperCase());
		}
	}


	public void addLocalFilesToInject(Map<String, List<File>> files)
	{
		localFiles.putAll(files);
	}

	/**
	 * Drives require a file system helper (FS annotation is required)
	 * @return
	 */
	public List<Pair<String,String>> getHelperDevices()
	{
		if (environment instanceof EmulationEnvironment) {
			return EmulationEnvironmentHelper.getHelperDrives((EmulationEnvironment) environment);
		}
		return new ArrayList<Pair<String,String>>();
	}

	/** 
	 * Drives capable to accept ready made images.
	 * @return
	 */
	public List<String> getImageDevices()
	{
		if (environment instanceof EmulationEnvironment) {
			return EmulationEnvironmentHelper.getImageDrives((EmulationEnvironment) environment);
		}
		return new ArrayList<String>();
	}

	void cleanup()
	{
		this.uploads.clear();
		this.localFiles.clear();
		this.containerIDs.clear();
	}

	List<ChosenMediaForDeviceBean> getChosenMedia()
	{
		return this.chosenMediaForDevices;
	}

	void setChosenMedia(List<ChosenMediaForDeviceBean> cm)
	{
		this.chosenMediaForDevices = cm;
	}

	void updateChosenMedia() throws BWFLAException 
	{
		if(binding == null)
			throw new BWFLAException("updateChosenMedia: binding undefined");

		for (ChosenMediaForDeviceBean chosenMedium : this.chosenMediaForDevices) {
			if (!chosenMedium.isChanged()) {
				continue;
			}
			chosenMedium.resetChanged();


			if (chosenMedium.getChosenMedia().equalsIgnoreCase("(none)")) {
				changeMedium(chosenMedium.getDeviceName(), null);
			} else {
				changeMedium(chosenMedium.getDeviceName(), "binding://" + binding.bindingId + "/" + chosenMedium.getChosenMedia());
			}

		}
	}

	List<ChosenMediaForDeviceBean> getChosenMediaForDevices() {
		if (this.chosenMediaForDevices != null) {
			return this.chosenMediaForDevices;
		}

		this.chosenMediaForDevices = new ArrayList<ChosenMediaForDeviceBean>();

		if(binding == null)
			return chosenMediaForDevices;


		if(binding instanceof SoftwareBindingHandle)
		{
			SoftwareBindingHandle swBinding = (SoftwareBindingHandle)binding;
			String selectedDefaultFile = null;
			Drive.DriveType type = null;
			try {
				// select the same file by default that was used
				// to initialize the environment in WF_M_1
				selectedDefaultFile = swBinding.selectedSoftware.getFiles().get(0).getPath().toString();
				// this is a hack. we use the type of the first file...
				type = Drive.DriveType.valueOf(swBinding.selectedSoftware.getFiles().get(0).getType().name());
				type = Drive.DriveType.convert(type);
			}
			catch (IndexOutOfBoundsException e) {
				// software bundle does not contain any files
				return chosenMediaForDevices;
			}

			List<String> filenames = new ArrayList<String>();
			filenames.add("(none)");

			for (BundledFile file : swBinding.selectedSoftware.getFiles()) 
			{
				filenames.add(file.getPath().toString());
			}

			this.chosenMediaForDevices.add(new ChosenMediaForDeviceBean(type.name(), selectedDefaultFile, filenames));
			return this.chosenMediaForDevices;
		}
		else if(binding instanceof ArchiveBindingHandle)
		{
			LOG.info("managing ArchiveBindingHandle");
			ArchiveBindingHandle aBinding = (ArchiveBindingHandle)binding;

			List<String> filenames = new ArrayList<String>();
			filenames.add("(none)");

			for (FileCollectionEntry link : aBinding.fc.files) {
				if (link.getId() == null || link.getUrl() == null)
					continue;

				filenames.add(link.getId());
			}

			this.chosenMediaForDevices.add(new ChosenMediaForDeviceBean(aBinding.type.name(), aBinding.defaultFile, filenames));
			return chosenMediaForDevices;
		}
		else
			return new ArrayList<ChosenMediaForDeviceBean>();
	}

	abstract class BindingHandle{ 
		String bindingId;
		int driveId;
		Drive.DriveType type;
	}

	class SoftwareBindingHandle extends BindingHandle
	{
		URL bundleUrl;
		SoftwareDescription selectedSoftware;
	}

	class ArchiveBindingHandle extends BindingHandle
	{
		FileCollection fc;
		String defaultFile; 
	}
}
