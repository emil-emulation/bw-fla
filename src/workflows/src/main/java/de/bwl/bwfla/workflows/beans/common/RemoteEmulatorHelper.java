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
import javax.activation.DataHandler;
import javax.xml.bind.JAXBException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import de.bwl.bwfla.common.datatypes.Drive;
import de.bwl.bwfla.common.datatypes.EaasState;
import de.bwl.bwfla.common.datatypes.EmulationEnvironment;
import de.bwl.bwfla.common.datatypes.Environment;
import de.bwl.bwfla.common.datatypes.RemoteProcessMonitor;
import de.bwl.bwfla.common.datatypes.utils.EmulationEnvironmentHelper;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.services.container.helpers.ContainerHelper;
import de.bwl.bwfla.common.services.container.helpers.ContainerHelperFactory;
import de.bwl.bwfla.common.services.container.helpers.ImageFileHelper;
import de.bwl.bwfla.common.services.container.types.CdromContainer;
import de.bwl.bwfla.common.services.container.types.Container;
import de.bwl.bwfla.common.services.container.types.Container.Filesystem;
import de.bwl.bwfla.common.services.container.types.HddZipContainer;
import de.bwl.bwfla.common.services.container.types.ImageFileContainer;
import de.bwl.bwfla.common.utils.Pair;
import de.bwl.bwfla.common.utils.Zip32Utils;
import eu.planets_project.services.datatypes.Content;
import eu.planets_project.services.datatypes.DigitalObject;
import eu.planets_project.services.datatypes.DigitalObjectContent;
import eu.planets_project.services.datatypes.ImmutableDigitalObject;


public class RemoteEmulatorHelper extends AbstractRemoteEmulatorHelper
{
	private final Environment environment;
	private RemoteProcessMonitor monitor;
	private Map<String, List<File>> localFiles;
	private Map<String, List<File>> uploads;
	private Map<Integer, Container> containerIDs;

	public static RemoteEmulatorHelper createRemoteEmuFromConfig(String config)
	{
		try 
		{
			Environment env = Environment.fromValue(config);
			return new RemoteEmulatorHelper(env);
		} 
		catch(JAXBException e) 
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public RemoteEmulatorHelper(Environment env)
	{
		super();

		this.environment = env;
		this.monitor = null;
		this.localFiles = new HashMap<>();
		this.uploads = new HashMap<>();
		this.containerIDs = new HashMap<>();
	}

	public void setFilesToInject(Map<String, List<File>> upFiles)
	{
		uploads = upFiles;
	}

	public void addLocalFilesToInject(Map<String, List<File>> files)
	{
		localFiles.putAll(files);
	}

	public void addBundledFile(URL bundle, Path file, Drive.DriveType type) 
	{
		// LOG.info(this.environment.toString());
		String bindingId = EmulationEnvironmentHelper.addBinding((EmulationEnvironment)this.environment, bundle.toString());
		EmulationEnvironmentHelper.registerDrive((EmulationEnvironment)this.environment, bindingId, file, type);
		//LOG.info("\n\n\n");
		//LOG.info(this.environment.toString());
	}
	
	public void initialize()
	{
		super.initialize();
		
		try
		{
			sessid = eaas.createSession(environment.value(), null, "9999");
			if (sessid == null)
				throw new WFPanicException("session id is null");
		}
		catch(JAXBException e)
		{
			e.printStackTrace();
			throw new WFPanicException("environmnent metadata is malformed");
		}
		
		boolean initialized = false;
		for(int i = 0, TIMEOUT = 1500; !initialized && i < TIMEOUT; ++i)
		{
			EaasState state = this.getEaasState();

			switch(state)
			{
				case SESSION_UNDEFINED:
				case SESSION_RUNNING:
				case SESSION_STOPPED:
				case SESSION_BUSY:
					throw new WFPanicException("EAAS is in an illegal state at this point");

				case SESSION_ALLOCATING:
					break;

				case SESSION_READY:
					initialized = true;
					continue;

				case SESSION_CLIENT_FAULT:
					throw new WFPanicException("client has specified illegal input data to EAAS");

				case SESSION_FAILED:
					throw new WFPanicException("an internal error occured in EAAS, remote state");

				case SESSION_OUT_OF_RESOURCES:
					throw new WFPanicException("EAAS is out of resources, please try again later");
			}
			
			try
			{
				Thread.sleep(100);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}			
		}

		if(!initialized)
			throw new WFPanicException("EAAS session not allocated after a predefined workflow timeout");

		monitor = new RemoteProcessMonitor(sessid, eaas);
	}

	public List<Pair<String,String>> getHelperDevices()
	{
		if (environment instanceof EmulationEnvironment) {
			return EmulationEnvironmentHelper.getHelperDrives((EmulationEnvironment) environment);
		}
		return new ArrayList<Pair<String,String>>();
	}
	
	public List<String> getImageDevices()
	{
		if (environment instanceof EmulationEnvironment) {
			return EmulationEnvironmentHelper.getImageDrives((EmulationEnvironment) environment);
		}
		return new ArrayList<String>();
	}

	public List<Pair<WorkflowsFile, String>> detachAndDownloadContainers()
	{
		List<Pair<WorkflowsFile, String>> downloads = new ArrayList<>();
		
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
					DataHandler dh = eaas.detachMedium(sessid, id);

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

	public RemoteProcessMonitor getProcessMonitor()
	{
		return monitor;
	}
	
	synchronized public void cleanup()
	{
		super.cleanup();
		
		this.monitor = null;
		this.uploads.clear();
		this.localFiles.clear();
		this.containerIDs.clear();
	}

	synchronized public void startEmulator() throws BWFLAException
	{
		Map<String, List<File>> injFiles = new HashMap<>();	
		injFiles.putAll(uploads);
		injFiles.putAll(localFiles);
			
		for(Map.Entry<String, List<File>> upload: uploads.entrySet())
		{
			String dev = upload.getKey();
			List<File> files = upload.getValue();
			
			Container container = null;			
			ContainerHelper helper = null;
			
			try
			{
				if(dev.equals(Drive.DriveType.CDROM_IMG.name()) || dev.equals(Drive.DriveType.DISK_IMG.name()) || dev.equals(Drive.DriveType.FLOPPY_IMG.name()))
				{
					helper = new ImageFileHelper();
					Drive.DriveType type = Drive.DriveType.valueOf(dev);
					Drive.DriveType realType = Drive.DriveType.convert(type);
					dev = realType.toString().toUpperCase();
				}
				else
				{
					List<Pair<String,String>> devices = this.getHelperDevices();
					
					Filesystem fs = null;
					for(Pair<String, String> device: devices)
						if(device.getA().equalsIgnoreCase(dev))
							fs = Filesystem.valueOf(device.getB());
				
					if(fs == null)
					{
						LOG.severe("could not determine filesystem to uploaded attach files for the device (skipping): " + dev);
						continue;
					}
				
					helper = ContainerHelperFactory.getContainerHelper(dev, fs);
				}
				
				if(helper == null)
				{
					LOG.severe("container helper is null, make sure to check whether helper factory supports this device/filesystem combination");
					continue;
				}
				
				container = helper.createEmptyContainer();
				if(container == null)
				{
					LOG.severe("container is null, make sure to check whether corresponding helper is properly configured: " + helper.getClass().getSimpleName());
					continue;
				}
				
				if(!helper.insertIntoContainer(container, files))
				{
					LOG.warning("data attachment failed for the following container: " + container.getClass().getSimpleName());
					continue;
				}
				
				DigitalObjectContent doContent = Content.byReference(container.getFile());
				DigitalObject.Builder builder = new DigitalObject.Builder(doContent);
				builder.title(dev + "_container_do_");
				ImmutableDigitalObject obj = (ImmutableDigitalObject) builder.build();
				
				int id = eaas.attachMedium(sessid, obj.getContent().getHandler(), dev.toUpperCase());
				containerIDs.put(id, container);
			}
			catch(Throwable t)
			{
				t.printStackTrace();
			}
			finally
			{
				if(container != null)
				{
					File fl = container.getFile();
					if(fl != null && fl.isFile())
						fl.delete();
				}
			}
		}
		
		super.startEmulator();
	}
}
