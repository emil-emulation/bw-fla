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

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;

import de.bwl.bwfla.common.datatypes.EmulationEnvironment;
import de.bwl.bwfla.common.datatypes.EmulationNode;
import de.bwl.bwfla.common.datatypes.Environment;
import de.bwl.bwfla.common.datatypes.Network;
import de.bwl.bwfla.common.datatypes.NetworkEnvironment;
import de.bwl.bwfla.common.datatypes.Resource;
import de.bwl.bwfla.imagearchive.ImageArchiveConfig.ImageType;

public class ImageHandler {

	protected static final Logger	log	= Logger.getLogger(ImageHandler.class.getName());
	ImageArchiveConfig iaConfig;
	Map<String,EmulationEnvironment> envs;

	enum ExportType { NBD, HTTP };

	public ImageHandler(ImageArchiveConfig conf)
	{
		iaConfig = conf;
		envs = new ConcurrentHashMap<String,EmulationEnvironment>();
	}

	protected boolean publishImage(File img, String exportId, ExportType unused)
	{
		File exportFile = new File(iaConfig.exportPath + "/" + exportId);
		if(exportFile.exists())
			exportFile.delete();
		try {
			Files.createSymbolicLink(exportFile.toPath(), img.toPath());
		} catch (IOException e) {
			log.info("publication of image " + exportId + " failed: " + e.getMessage());
			return false;
		}
		return true;
	}

	private File getImageTargetPath(String exportId, String type)
	{
		File target = new File(iaConfig.imagePath, type);
		if(target.isDirectory())
			return target;
		else return null;
	}

	private File importImageFile(File image, String exportId, String type, boolean delete)
	{
		
		File target = getImageTargetPath(exportId, type);
		File destImgFile = new File(target, exportId);

		if(destImgFile.exists())
		{
			log.severe("the following file already exists, will not overwrite: " + destImgFile.getAbsolutePath());
			return null;
		}
			
		try {
			if(delete)
				Files.move(image.toPath(), destImgFile.toPath(), REPLACE_EXISTING);
			else
				Files.copy(image.toPath(), destImgFile.toPath(), REPLACE_EXISTING);
		} catch (IOException e1) {
			log.info("failed moving incoming image to " + target + " reason " + e1.getMessage());
			return null;
		}

		return destImgFile;
	}

	private File importImageStream(DataHandler image, String exportId, String type, boolean delete)
	{
		File target = getImageTargetPath(exportId, type);
		File destImgFile = new File(target, exportId);
		if(destImgFile.exists())
		{
			log.severe("the following file already exists, will not overwrite: " + destImgFile.getAbsolutePath());
			return null;
		}

		if(!DataUtil.writeData(image, destImgFile))
		{
			log.severe("failed wrting image");
			return null;
		}
		return destImgFile;
	}

	private boolean writeMetaData(String conf, String id, String type)
	{
		File metaDataDir = new File(iaConfig.metaDataPath, type);
		if(!metaDataDir.isDirectory())
			return false;

		String confFullName = id + ".xml";

		File destConfFile = new File(metaDataDir + File.separator + confFullName);

		if(destConfFile.exists())
		{
			log.severe("the following file already exists, will not overwrite: " + destConfFile.getAbsolutePath());
			return false;
		}

		return DataUtil.writeString(conf, destConfFile);
	}

	protected String loadMetaDataFile(File mf)
	{
		String env;
		try {
			env = FileUtils.readFileToString(mf);
		} catch (IOException e) {
			log.info("failed loading " + mf + " - " + e.getMessage());
			return null;
		}

		return exportConfig(env);	
	}
	
	protected ConcurrentHashMap<Path,String> loadMetaData(File path)
	{
		ConcurrentHashMap <Path,String> md = new ConcurrentHashMap<Path,String>();

		if(!path.exists() || !path.isDirectory())
		{
			log.info("path " + path + " not a valid meta-data directory");
			return md;
		}

		for(final File fileEntry : path.listFiles())
		{
			if(fileEntry.isDirectory())
				continue;
				
				String env;
				if((env = loadMetaDataFile(fileEntry)) != null)
					md.put(fileEntry.toPath(), env);
		}
		return md;
	}

	protected boolean importImage(String id, String conf, File image, String type, boolean delete)
	{		
		File exportFile = null;
		
		if(image != null)
			if((exportFile = importImageFile(image, id, type, delete)) == null)
				return false;
		
		if(writeMetaData(conf, id, type))
			if(exportFile != null)
				if(this.publishImage(exportFile, id, null))
					return true;
		
		return false;
	}

	protected boolean importImage(String id, String conf, DataHandler image, String type)
	{
		File exportFile = importImageStream(image, id, type, false);
		
		if(exportFile != null)
			if(writeMetaData(conf, id, type))
				if(this.publishImage(exportFile, id, null))
					return true;

		return false;
	}

	public EmulationEnvironment exportConfig(EmulationEnvironment env) {
		String prefix = null;
		if(iaConfig.nbdPrefix != null && !iaConfig.nbdPrefix.isEmpty())
		{
			prefix = iaConfig.nbdPrefix;
		}
		else if(iaConfig.httpPrefix != null && !iaConfig.httpPrefix.isEmpty())
		{
			prefix = iaConfig.httpPrefix;
		}

		
		for(Resource r : env.getBinding())
		{
			try 
			{
				URI uri = new URI(r.getUrl());
				if (uri.isOpaque()
						&& uri.getScheme().equalsIgnoreCase(ImageArchiveConfig.IA_URI_SCHEME)) {
					if (prefix != null) {
						r.setUrl(prefix + uri.getSchemeSpecificPart());
					} else {
						log.warning("using internal ref and no transport prefix set in property files, exiting");
						return null;
					}
				}
			} 
			catch (URISyntaxException e) 
			{
				log.severe("URL parsing failed: " + r.getUrl());
				return null;
			}
		}
		envs.put(env.getId(), env);
		return env;
	}
	
	protected EmulationEnvironment getEnvById(String id)
	{
		return envs.get(id);
	}
	
	protected EmulationEnvironment removeEnvironmentIndex(String id)
	{
		return envs.remove(id);
	}
	
	public String exportConfig(String env)
	{
		try {
			Environment emuEnv = Environment.fromValue(env);
			
			if(emuEnv == null) {
				return null;
			}

			if (emuEnv instanceof EmulationEnvironment) {
				return exportConfig((EmulationEnvironment)emuEnv).value();
			} else if (emuEnv instanceof NetworkEnvironment) {
				NetworkEnvironment netEnv = (NetworkEnvironment)emuEnv;
				
				for (Network network : netEnv.getNetwork()) {
					for (EmulationNode node : network.getEmulator()) {
						node.setEmulationEnvironment(exportConfig(node.getEmulationEnvironment()));
					}
				}
				return netEnv.value();
			}
		} catch (JAXBException e) {
			log.severe("Could not unmarshal environment:" + e.getMessage());
			e.getStackTrace();
			return null;
		}
		log.severe("Environment is neither NetworkEnvironment nor EmulationEnvironment. Aborting.");
		return null;
	}

	protected ConcurrentHashMap<Path, String> getImages(String type)
	{
		try {
			ImageType t = ImageType.valueOf(type);
			File dir = new File(iaConfig.metaDataPath, t.name());
			if(!dir.exists())
			{
				log.info("dir not found: " + dir);
				return new ConcurrentHashMap<Path,String>();
			}
			return loadMetaData(dir);
		}
		catch(IllegalArgumentException e)
		{
			log.warning("unknown ImageType " + e.getMessage());
			return new ConcurrentHashMap<Path,String>();
		}
	}

	protected List<String> getIncomingImageList()
	{
		List<String> imageList = new ArrayList<String>();

		File incomingDir = iaConfig.incomingPath;
		if(!incomingDir.exists())
			return imageList;
		for (final File fileEntry : incomingDir.listFiles()) {
			if (!fileEntry.isDirectory()) {
				imageList.add(fileEntry.getName());	
			}
		}
		return imageList;
	}

	/*private void deleteMetaData(String id)
	{
		File userImagesMetaDataDir = new File(iaConfig.userImagesMetaDataPath);
		if(!userImagesMetaDataDir.exists() || !userImagesMetaDataDir.isDirectory())
		{
			log.severe("make sure that the repository directory exists and user has sufficient permissions: " 
					+ userImagesMetaDataDir.getAbsolutePath());
			return;
		}

		String confFullName = id + ".xml";

		File destConfFile = new File(userImagesMetaDataDir + File.separator + confFullName);
		if(destConfFile.exists())
		{
			destConfFile.delete();
		}
	}*/



	//	private String searchDir(String id, String path)
	//	{
	//		File dir = new File(path);
	//		if(!dir.exists())
	//			return null;
	//
	//		for (final File fileEntry : dir.listFiles()) {
	//			if (!fileEntry.isDirectory()) {
	//
	//				try {
	//					String conf = FileUtils.readFileToString(fileEntry);
	//					EmulationEnvironment env = EmulatorUtils.unmarshalEmuEnvironment(conf);
	//					if(env.getId().equals(id))
	//						return conf;
	//				} catch (Exception e) {
	//					if(e.getMessage() != null)
	//						log.info("failed parsing env: " + fileEntry + " " + e.getMessage());
	//				}	
	//			}
	//		}
	//		return null;
	//	}



}
