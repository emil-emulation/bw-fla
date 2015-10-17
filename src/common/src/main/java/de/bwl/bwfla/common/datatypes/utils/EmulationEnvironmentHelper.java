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

package de.bwl.bwfla.common.datatypes.utils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import de.bwl.bwfla.common.datatypes.Drive;
import de.bwl.bwfla.common.datatypes.EmulationEnvironment;
import de.bwl.bwfla.common.datatypes.Environment;
import de.bwl.bwfla.common.datatypes.Resource;
import de.bwl.bwfla.common.datatypes.Resource.AccessType;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.Pair;

public class EmulationEnvironmentHelper 
{
	protected final static Logger log = Logger.getLogger(EmulationEnvironmentHelper.class.getName());
	
	private static String _registerDataSource(String conf, String ref, String type)
	{
		EmulationEnvironment env = EmulationEnvironment.fromValue(conf);
		if(env == null)
		{
			log.severe("invalid config format, got " + conf);
			return null;
		}
		
		Iterator<Drive> iterator = env.getDrive().iterator();
		while (iterator.hasNext()) {
			Drive d = iterator.next();    
			if(d.getType().name().equals(type) && (d.getData() == null || d.getData().isEmpty()))
			{
				Resource r = new Resource();
				String id = UUID.randomUUID().toString();
				r.setId(id);
				r.setUrl(ref);
				r.setAccess(AccessType.COW);
				env.getBinding().add(r);
				d.setData("binding://" + id);
			}
		}
		
		return env.toString();
	}
	
	public static List<String> getImageDrives(EmulationEnvironment env)
	{
		List<String> emptyDrives = new ArrayList<>();
		Iterator<Drive> iterator = env.getDrive().iterator();
		
		while (iterator.hasNext()) 
		{
			Drive d = iterator.next();
			if(d.getData() == null || d.getData().isEmpty())
			{
				Drive.DriveType type = d.getType();
				Drive.DriveType imageType = Drive.DriveType.convert(type); 
				emptyDrives.add(imageType.name());
			}
		}
		
		return emptyDrives;
	}
	
	
	public static List<Pair<String,String>> getHelperDrives(EmulationEnvironment env)
	{
		ArrayList<Pair<String,String>> emptyDrives = new ArrayList<Pair<String,String>>();
		Iterator<Drive> iterator = env.getDrive().iterator();
		
		while (iterator.hasNext()) 
		{
			Drive d = iterator.next();    
			
			if((d.getData() == null || d.getData().isEmpty()) && (d.getFilesystem() != null && !d.getFilesystem().isEmpty()))
			{
				Pair<String,String> p = new Pair<String,String>(d.getType().name().toUpperCase(), d.getFilesystem());
				emptyDrives.add(p);
			}
		}
		
		return emptyDrives;
	}
	
	
	public static Environment registerDataSource(Environment env, String ref, Drive.DriveType type) throws BWFLAException
	{
		String xml = env.toString();
		xml = _registerDataSource(xml, ref, type.name());
		if(xml == null)
			return null;
		try {
			return Environment.fromValue(xml);
		} catch (JAXBException e) {
			throw new BWFLAException("register data source failed: " + e.getMessage(), e);
		}
	}
	
	public static String addBinding(EmulationEnvironment env, String url) {
		return addBinding(env, url, AccessType.COW);
	}
	
	public static String addBinding(EmulationEnvironment env, String url, AccessType accessType) {
		if (env == null) {
			log.info("given environment is null");
			return null;
		}
		if (url == null || url.isEmpty()) {
			log.info("given binding url is null or empty");
			return null;
		}
		String uuid = UUID.randomUUID().toString();
		Resource res = new Resource();
		res.setId(uuid);
		res.setUrl(url);
		res.setAccess(accessType);
		env.getBinding().add(res);
		
		return uuid;
	}
	
	public static EmulationEnvironment registerDrive(EmulationEnvironment env, String binding, Path path, Drive.DriveType driveType) {
		if (env == null) {
			log.info("given environment is null");
			return null;
		}
		// construct URL
		String dataUrl = "binding://" + binding + "/" + path.toString();
		for (Drive drive : env.getDrive()) {
			if(drive.getType().equals(driveType) && (drive.getData() == null || drive.getData().isEmpty())) {
				drive.setData(dataUrl);
				break;
			}
		}
		
		return env;
	}
	
	public static EmulationEnvironment setMainHdRef(EmulationEnvironment env, String ref, boolean cow) throws BWFLAException
	{
		if(env == null)
			return null;
		
		for (Resource b : env.getBinding()) {
			if (b.getId().equals("main_hdd")) { 
				b.setUrl(ref);
				if (cow) {
					b.setAccess(AccessType.COW);
				} else {
					b.setAccess(AccessType.COPY);
				}
				return env;
			}
		}
		throw new BWFLAException("updating image source failed");
	}
	
	public static EmulationEnvironment setMainHdRef(String config, String ref, boolean cow) throws BWFLAException
	{
		EmulationEnvironment env = EmulationEnvironment.fromValue(config);
		if(env== null)
			return null;
		
		return setMainHdRef(env, ref, cow);
	}
}
