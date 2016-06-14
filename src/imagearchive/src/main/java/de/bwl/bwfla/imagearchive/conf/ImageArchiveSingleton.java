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

package de.bwl.bwfla.imagearchive.conf;

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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import de.bwl.bwfla.common.utils.config.ConfigurationManager;
import de.bwl.bwfla.imagearchive.IWDArchive;
import de.bwl.bwfla.imagearchive.ImageArchiveConfig;
import de.bwl.bwfla.imagearchive.ImageHandler;
import de.bwl.bwfla.imagearchive.ImageArchiveConfig.ImageType;


@Singleton
@Startup
public class ImageArchiveSingleton
{
	protected static final Logger			LOG	= Logger.getLogger(ImageArchiveSingleton.class.getName());
	public static volatile boolean 			confValid = false;
	public static volatile ImageArchiveConf	CONF = null;
	private static AtomicBoolean	constructed	= new AtomicBoolean(false);

	private static WatchService watcher;
	public static volatile ImageArchiveConfig iaConfig;
	public static volatile IWDArchive iwdArchive;
	public static volatile ImageHandler imageHandler;

	public static volatile ConcurrentHashMap<Path,String> templates;
	public static volatile ConcurrentHashMap<Path,String> base; 
	public static volatile ConcurrentHashMap<Path,String> derivate;
	public static volatile ConcurrentHashMap<Path,String> system;
	public static volatile ConcurrentHashMap<Path,String> user;
	public static volatile ConcurrentHashMap<Path,String> netenv;
	public static volatile Map<ImageType, Map<Path, String>> imagesCache = new ConcurrentHashMap<>();

	@PostConstruct
    public void init() 
	{
		LOG.info("initializing ImageArchiveSingleton");
		ImageArchiveSingleton.loadConf();
	}

	public static boolean validate(ImageArchiveConf conf)
	{
		if(conf != null)
			if(conf.archiveBase != null  && !conf.archiveBase.isEmpty())
				// if((conf.nbdPrefix != null  && !conf.nbdPrefix.isEmpty()))
					return true;

		return false;
	}

	synchronized public static void loadConf()
	{ 
		if(constructed.compareAndSet(false, true))
		{	
			LOG.info("load Conf");
			CONF = ConfigurationManager.load(ImageArchiveConf.class);
			confValid = ImageArchiveSingleton.validate(CONF);
			if(!confValid)
				return;

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

				watcher = FileSystems.getDefault().newWatchService();
				registerWatchRecursively(iaConfig.metaDataPath.toPath(), watcher);
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	private static void updateImageMetadata(Path path, String filename)
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

	private static void removeImageMetadata(Path path, String filename)
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

	private static void registerWatchRecursively(final Path root, final WatchService watcher) throws IOException
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

							WatchEvent<Path> ev = (WatchEvent<Path>) event;
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
}