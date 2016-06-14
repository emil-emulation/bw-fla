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

package de.bwl.bwfla.imagearchive;

import java.io.File;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.annotation.PostConstruct;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlMimeType;

import de.bwl.bwfla.common.datatypes.EmulationEnvironment;
import de.bwl.bwfla.common.datatypes.Environment;
import de.bwl.bwfla.common.datatypes.utils.EmulationEnvironmentHelper;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.interfaces.ImageArchiveWSRemote;
import de.bwl.bwfla.common.services.guacplay.replay.IWDMetaData;
import de.bwl.bwfla.imagearchive.ImageArchiveConfig.ImageType;
import de.bwl.bwfla.imagearchive.conf.ImageArchiveSingleton;


/**
 * @author Isgandar Valizada, bwFLA project, University of Freiburg, Germany
 * 
 */

@Stateless 
@WebService
@Remote(ImageArchiveWSRemote.class)
public class ImageArchiveWS implements ImageArchiveWSRemote
{
	protected static final Logger		LOG			= Logger.getLogger(ImageArchiveWS.class.getName());
	private boolean configured = false;
	
	private ImageArchiveConfig iaConfig = null;
	private IWDArchive iwdArchive = null;
	private ImageHandler imageHandler = null;
	
	@PostConstruct
	private void initialize()
	{
		this.reloadProperties();
	}
	
	synchronized private void reloadProperties()
	{	
		ImageArchiveSingleton.loadConf();
		if(!ImageArchiveSingleton.confValid)
		{
			LOG.severe("module configuration is invalid, without proper configuration all methods will fail");
			return;
		}
		iaConfig = ImageArchiveSingleton.iaConfig;
		iwdArchive = ImageArchiveSingleton.iwdArchive;
		imageHandler = ImageArchiveSingleton.imageHandler;
		configured = true;
	}

	private String updateConfig(String conf, String newId, boolean setIdOnly) throws BWFLAException
	{
		EmulationEnvironment env = EmulationEnvironment.fromValue(conf);
		if(env == null)
		{
			LOG.severe("invalid config format, got " + conf);
			return null;
		}

		if(!setIdOnly)
		{
			String newHandle = ImageArchiveConfig.IA_URI_SCHEME + ":" + newId;
			env = EmulationEnvironmentHelper.setMainHdRef(env, newHandle, true);
			if(env == null)
				return null;
		}
		
		env.setId(newId);
		
		return env.toString();
	}
	
	@Override
	public String registerImage(String conf, @XmlMimeType("application/octet-stream") DataHandler image, String type)
	{	
		if(!configured)
		{
			LOG.severe("ImageArchive is not configured");
			return null;
		}
					
		if(image == null)
		{
			LOG.severe("image data handler is null, aborting");
			return null;
		}

		if(conf == null)
		{
			LOG.severe("image configuration data is null, aborting");
			return null;
		}
		
		String id = UUID.randomUUID().toString() + String.valueOf(System.currentTimeMillis()).substring(0, 2);
		String modifiedConf;
		try {
			modifiedConf = updateConfig(conf, id, false);
		} catch (BWFLAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		if(modifiedConf == null)
			return null;
		
		if(!imageHandler.importImage(id, modifiedConf, image, type))
		{
			LOG.severe("unable to register image with the id: " + id);
			return null;
		}	
		
//		modifiedConf = imageHandler.exportConfig(modifiedConf);
//		imagesCache.get(imageType).add(modifiedConf);
		LOG.info("registred new image " + id + " with type: " + type.toString());
		return id;
	}
	
	@Override
	public String registerImageUsingFile(String conf, String image, boolean delete, String type) 
	{
		if(!configured)
		{
			LOG.severe("ImageArchive is not configured");
			return null;
		}
		
		if(conf == null)
		{
			LOG.severe("image configuration data is null, aborting");
			return null;
		}
		
		String id = UUID.randomUUID().toString() + String.valueOf(System.currentTimeMillis()).substring(0, 2);
		String modifiedConf;
		try {
			modifiedConf = updateConfig(conf, id, image == null);
		} catch (BWFLAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		if(modifiedConf == null)
			return null;

		if(image == null)
		{
			if(!imageHandler.importImage(id, modifiedConf, null, type, delete))
				return null;
				
			LOG.info("registred new image " + id);
			return id;
		}
		
		File img = new File(iaConfig.incomingPath, image);
		if(!img.exists())
		{
			LOG.severe("incoming image " + image + " does not exist");
			return null;
		}
	
		if(!imageHandler.importImage(id, modifiedConf, img, type, delete))
			return null;

		modifiedConf = imageHandler.exportConfig(modifiedConf);	
		LOG.info("registred new image " + id + " with type: " + type.toString());		
		return id;
	}

	@Override
	public String publishImage(String image, String exportId)
	{
		if(!configured)
		{
			LOG.severe("ImageArchive is not configured");
			return null;
		}
		
		File img = new File(iaConfig.incomingPath, image);
		if(!img.exists())
		{
			LOG.severe("incoming image " + image + " does not exist");
			return null;
		}

		imageHandler.publishImage(img, exportId, null);
	
		String url = null;
		if(iaConfig.nbdPrefix != null && !iaConfig.nbdPrefix.isEmpty())
		{
			url = iaConfig.nbdPrefix + exportId;
			//	log.info("new url: " + r.getUrl());
		}
		else if(iaConfig.httpPrefix != null && !iaConfig.httpPrefix.isEmpty())
			url = iaConfig.httpPrefix + exportId;
		else 
		{
			LOG.severe("using internal ref and no transport prefix set in property files");
		}
		return url;
	}
	
	@Override
	public boolean releaseImage(String url)
	{
		if(!configured)
		{
			LOG.severe("ImageArchive is not configured");
			return false;
		}
		// TODO: implement
		return false;
	}
	
	@Override
	public List<String> getIncomingImageList()
	{	
		if(!configured)
		{
			LOG.severe("ImageArchive is not configured");
			return null;
		}
		return imageHandler.getIncomingImageList();
	}
	
	@Override
	public List<String> getTemplates()
	{
		if(!configured)
		{
			LOG.severe("ImageArchive is not configured");
			return null;
		}
		return new ArrayList<String>(ImageArchiveSingleton.templates.values());
	}
	
	@Override
	public List<String> getImages(String type) 
	{	
		if(!configured)
		{
			LOG.severe("ImageArchive is not configured");
			return null;
		}
		
		List<String> images = null;
		
		try
		{
			Map<Path,String> iMap = ImageArchiveSingleton.imagesCache.get(ImageType.valueOf(type));
			images = new ArrayList<String>(iMap.values());
		}
		catch(IllegalArgumentException e)
		{
			LOG.warning("client has specified an illegal argument as an image type: " +  type != null ? type : "(null value)");
		}
			
		return images;
	}
	
	@Override
	public String getRecording(String envId, String traceId)
	{
		if(!configured)
		{
			LOG.severe("ImageArchive is not configured");
			return null;
		}
		return iwdArchive.getRecording(envId, traceId);
	}

	@Override
	public List<IWDMetaData> getRecordings(String envId)
	{	
		if(!configured)
		{
			LOG.severe("ImageArchive is not configured");
			return null;
		}
		return iwdArchive.getRecordings(envId);
	}
	
	@Override
	public boolean addRecordingFile(String envId, String traceId, String data)
	{	
		if(!configured)
		{
			LOG.severe("ImageArchive is not configured");
			return false;
		}
		return iwdArchive.addRecordingFile(envId, traceId, data);
	}

	@Override
	public String getImageById(String id) 
	{
		if(!configured)
		{
			LOG.severe("ImageArchive is not configured");
			return null;
		}
		Environment env = imageHandler.getEnvById(id);
		return env == null ? null : env.toString(); 
	}
}
