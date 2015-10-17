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
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.annotation.PostConstruct;
import javax.ejb.Remote;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlMimeType;

import de.bwl.bwfla.common.datatypes.EmulationEnvironment;
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

@WebService
@Singleton
@Startup
@Remote(ImageArchiveWSRemote.class)
public class ImageArchiveWS implements ImageArchiveWSRemote
{
	protected static final Logger		LOG			= Logger.getLogger(ImageArchiveWS.class.getName());
	private static final AtomicBoolean	constructed	= new AtomicBoolean(false);
	
	private ImageArchiveConfig iaConfig;
	private IWDArchive iwdArchive;
	private ImageHandler imageHandler;
	private WatchService watcher;
	
	volatile private ConcurrentHashMap<Path,String> templates;
	volatile private ConcurrentHashMap<Path,String> base; 
	volatile private ConcurrentHashMap<Path,String> derivate;
	volatile private ConcurrentHashMap<Path,String> system;
	volatile private ConcurrentHashMap<Path,String> user;
	volatile private ConcurrentHashMap<Path,String> netenv;
	volatile private Map<ImageType, Map<Path, String>> imagesCache = new ConcurrentHashMap<>();
	
	private void registerWatchRecursively(final Path root, final WatchService watcher) throws IOException
	{
		final Map<WatchKey, Path> keys = new HashMap<>();
		
		Files.walkFileTree(root, new SimpleFileVisitor<Path>() 
		{
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
			{
				if(!dir.equals(root))
				{
					WatchKey key = dir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.OVERFLOW);
					LOG.info("watching :" + dir);
					keys.put(key, dir);
				}
				
				return FileVisitResult.CONTINUE;
			}
		});
		
		new Thread()
		{	
			@Override
			public void run()
			{	
				while(true)
		            try 
		            {
		            	WatchKey key = watcher.take();
		            	Path dir = keys.get(key);
		            	// key.pollEvents();
		            	for (WatchEvent<?> event: key.pollEvents()) 
		            	{
		            		WatchEvent.Kind<?> kind = event.kind();
		            	        
		            	        if (kind == StandardWatchEventKinds.OVERFLOW) 
		            	            continue;
		            	        
		            	        WatchEvent<Path> ev = (WatchEvent<Path>)event;
		            	        Path filename = ev.context();
		            	        if(filename != null)
		            	        {
//		            	        	LOG.info("dir " + dir + ", filename : " + filename + " has changed " + kind);
		            	        	if(kind == StandardWatchEventKinds.ENTRY_MODIFY || kind == StandardWatchEventKinds.ENTRY_CREATE)
		            	        		updateImageMetadata(dir, filename.toString());
		            	        	else if(kind == StandardWatchEventKinds.ENTRY_DELETE)
		            	        		removeImageMetadata(dir, filename.toString());
		            	        }
				            	else
				            		LOG.info("unable to retrieve information about the changed watch key while recursively watching directory: " + root.toString());
		            	 }
		            	key.reset();   	
		            	
		            }
		            catch(ClosedWatchServiceException e)
		    		{
		            	return;
		    		}
		            catch(Throwable e) 
		            {
		            	e.printStackTrace();
		            	LOG.info("disabling recursive watching of the directory: " + root.toString());
		            	return;
		            }
			}
		}
		.start();
	}
	
	private void updateImageMetadata(Path path, String filename)
	{
		String[] elems = path.toString().split("/");
		ImageType t;
		try {
			t = ImageType.valueOf(elems[elems.length-1]);
		}
		catch(IllegalArgumentException e)
		{
			LOG.warning("unknown ImageType " + e.getMessage());
			return;
		}
		
		Map<Path,String> map = imagesCache.get(t);
		if(map == null)
			return;
		String env = imageHandler.loadMetaDataFile(new File(path.toFile(), filename));
		if(env != null)
		{
			// LOG.info("updating " + new File(path.toFile(), filename).toPath() + " " + env);
			map.put(new File(path.toFile(), filename).toPath(), env);
		}
	}
	
	private void removeImageMetadata(Path path, String filename)
	{
		String[] elems = path.toString().split("/");
		ImageType t;
		try {
			t = ImageType.valueOf(elems[elems.length-1]);
		}
		catch(IllegalArgumentException e)
		{
			LOG.warning("unknown ImageType " + e.getMessage());
			return;
		}
		
		Map<Path,String> map = imagesCache.get(t);
		if(map == null)
			return;
		// LOG.info("removing " + new File(path.toFile(), filename).toPath().toString());
		map.remove(new File(path.toFile(), filename).toPath());
	}
	
	@PostConstruct
	private void initialize()
	{
		if(constructed.compareAndSet(false, true))
			this.reloadProperties();
	}
	
	synchronized private boolean reloadProperties()
	{	
		ImageArchiveSingleton.loadConf();
		if(!ImageArchiveSingleton.confValid)
		{
			LOG.severe("module configuration is invalid, without proper configuration all methods will fail");
			return false;
		}
		
		String basePath   = ImageArchiveSingleton.CONF.archiveBase;
		String nbdPrefix  = ImageArchiveSingleton.CONF.nbdPrefix;
		String httpPrefix = ImageArchiveSingleton.CONF.httpPrefix;

		iaConfig     = new ImageArchiveConfig(basePath, nbdPrefix, httpPrefix);
		iwdArchive   = new IWDArchive(iaConfig);
		imageHandler = new ImageHandler(iaConfig);
		
		base 		= imageHandler.getImages(ImageType.base.toString());
		derivate 	= imageHandler.getImages(ImageType.derivate.toString());
		system 	= imageHandler.getImages(ImageType.system.toString());
		user		= imageHandler.getImages(ImageType.user.toString());
		netenv 	= imageHandler.getImages(ImageType.netenv.toString());
		
		imagesCache     = new HashMap<>();
		imagesCache.put(ImageType.base, base);
		imagesCache.put(ImageType.derivate, derivate);
		imagesCache.put(ImageType.system, system);
		imagesCache.put(ImageType.user, user);
		imagesCache.put(ImageType.netenv, netenv);
		
		templates 		= imageHandler.loadMetaData(iaConfig.templatesPath);
		
		try
		{	
			if(watcher != null)
				watcher.close();
						
			this.watcher = FileSystems.getDefault().newWatchService();
			this.registerWatchRecursively(iaConfig.metaDataPath.toPath(), watcher);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		return true;
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
		if(image == null)
		{
			LOG.severe("image data handler is null, aborting");
			return null;
		}

		if(conf == null)
		{
			LOG.severe("image configuration dara is null, aborting");
			return null;
		}

		ImageType imageType;
		try
		{
			imageType = ImageType.valueOf(type);
		}
		catch(IllegalArgumentException e)
		{
			LOG.severe("incorrect image type specified: " + type != null ? type : "(null value)");
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
	public String registerImage(String conf, String image, boolean delete, String type) 
	{
		if(conf == null)
		{
			LOG.severe("image configuration dara is null, aborting");
			return null;
		}
		
		ImageType imageType;
		try
		{
			imageType = ImageType.valueOf(type);
		}
		catch(IllegalArgumentException e)
		{
			LOG.severe("incorrect image type specified: " + type != null ? type : "(null value)");
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
		// TODO: implement
		return false;
	}
	
	@Override
	public List<String> getIncomingImageList()
	{	
		return imageHandler.getIncomingImageList();
	}
	
	@Override
	public List<String> getTemplates()
	{
		return new ArrayList<String>(templates.values());
	}
	
	@Override
	public List<String> getImages(String type) 
	{	
		List<String> images = null;
		
		try
		{
			Map<Path,String> iMap = imagesCache.get(ImageType.valueOf(type));
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
		return iwdArchive.getRecording(envId, traceId);
	}

	@Override
	public List<IWDMetaData> getRecordings(String envId)
	{	
		return iwdArchive.getRecordings(envId);
	}
	
	@Override
	public boolean addRecordingFile(String envId, String traceId, String data)
	{		
		return iwdArchive.addRecordingFile(envId, traceId, data);
	}

	@Override
	public String getImageById(String id) {
		return imageHandler.getEnvById(id).toString();
	}
}
