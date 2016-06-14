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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.xml.bind.JAXBException;

import de.bwl.bwfla.api.eaas.EaasWS;
import de.bwl.bwfla.common.datatypes.AbstractDataResource;
import de.bwl.bwfla.common.datatypes.ArchiveBinding;
import de.bwl.bwfla.common.datatypes.Binding;
import de.bwl.bwfla.common.datatypes.Drive;
import de.bwl.bwfla.common.datatypes.FileCollection;
import de.bwl.bwfla.common.datatypes.Drive.DriveType;
import de.bwl.bwfla.common.datatypes.EmulationEnvironment;
import de.bwl.bwfla.common.datatypes.Environment;
import de.bwl.bwfla.common.datatypes.FileCollectionEntry;
import de.bwl.bwfla.common.datatypes.VolatileResource;
import de.bwl.bwfla.common.datatypes.Binding.AccessType;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.services.container.helpers.ContainerHelper;
import de.bwl.bwfla.common.services.container.helpers.ContainerHelperFactory;
import de.bwl.bwfla.common.services.container.helpers.ImageFileHelper;
import de.bwl.bwfla.common.services.container.types.Container;
import de.bwl.bwfla.common.services.container.types.Container.Filesystem;
import de.bwl.bwfla.common.utils.ObjectArchiveHelper;
import de.bwl.bwfla.common.utils.Pair;

public class EmulationEnvironmentHelper 
{
	protected final static Logger log = Logger.getLogger(EmulationEnvironmentHelper.class.getName());

	/** List of beans, that support media-changing. */
	private static final Set<String> BEANS_WITH_MEDIACHANGE_SUPPORT = new HashSet<String>();
	static {
		// Add all supported bean names...
		Set<String> beans = BEANS_WITH_MEDIACHANGE_SUPPORT;
		beans.add("Kegs");
		beans.add("PceAtariSt");
		beans.add("PceIbmPc");
		beans.add("PceMacPlus");
		beans.add("Qemu");
		beans.add("VirtualBox");
	}

	/** List of beans, that support media-changing from internal UI. */
	private static final Map<String, String> BEANS_WITH_MEDIACHANGE_UI = new HashMap<String, String>();
	static {
		final String helpPrefix = "Media can be changed from emulator's internal UI.\n\n"
		                        + "To open emulator's menu, please press ";
		
		final String beebemHelpMsg = helpPrefix + "the F11 key. Then select 'Discs' entry, "
		                           + "followed by 'Change disc in drive 0/1'.";
		
		final String viceHelpMsg = helpPrefix + "the F12 key. Then select 'Drive' entry, "
		                         + "followed by 'Attach disk image to drive 8-11'.";
		
		// Add all supported bean names...
		Map<String, String> beans = BEANS_WITH_MEDIACHANGE_UI;
		beans.put("Beebem", beebemHelpMsg);
		beans.put("ViceC64", viceHelpMsg);
	}
	
	public static Drive findEmptyDrive(EmulationEnvironment env, Drive.DriveType type)
	{
		for(Drive d: env.getDrive()) 
			if(d.getType().equals(type) && (d.getData() == null || d.getData().isEmpty())) 
				return d;

		log.info("can't find empty drive for type " + type);

		return null;
	}

	public static EmulationEnvironment clean(final EmulationEnvironment original)
	{
		EmulationEnvironment env = original.copy();

		// remove all removable drives from the environment
		Map<String, Boolean> resourcesRemoveMap = new HashMap<>();

		for (Drive d : env.getDrive()) {
			String resourceUrl = d.getData();
			if (resourceUrl.startsWith("binding://")) {
				resourceUrl = resourceUrl.substring("binding://".length());
				resourceUrl = resourceUrl.substring(0,
						resourceUrl.indexOf("/") > 0 ? resourceUrl.indexOf("/")
								: resourceUrl.length());
			} else {
				resourceUrl = "";
			}

			if (d.getType() == Drive.DriveType.CDROM
					|| d.getType() == Drive.DriveType.FLOPPY) {
				resourcesRemoveMap.put(resourceUrl, true);
				d.setData("");
			} else {
				resourcesRemoveMap.put(resourceUrl, false);
			}
		}

		// remove all spurious resources
		for (Iterator<AbstractDataResource> it = env.getAbstractDataResource().iterator(); it.hasNext();) {
			AbstractDataResource r = it.next();

			// resource was only used for a removable drive, so remove it
			Boolean remove = resourcesRemoveMap.get(r.getId());
			if (remove != null && remove.booleanValue()) {
				it.remove();
				continue;
			}

			// resource was volatile (but not in use by a drive), remove it, too
			for (AbstractDataResource origRes : original.getAbstractDataResource()) {
				
				if (origRes.getId().equals(r.getId())
						&& origRes instanceof VolatileResource) {
					it.remove();
					break;
				}
			}
		}
		// the copied environment should now be "clean" of any removable
		// drives and formerly volatile resources
		return env;
	}

	private static String _registerDataSource(String conf, String ref, String type)
	{
		EmulationEnvironment env = EmulationEnvironment.fromValue(conf);
		if(env == null)
		{
			log.severe("invalid config format, got " + conf);
			return null;
		}

		DriveType t = DriveType.valueOf(type);
		Drive d = findEmptyDrive(env, t);  
		if(d != null)
		{
			Binding r = new Binding();
			String id = UUID.randomUUID().toString();
			r.setId(id);
			r.setUrl(ref);
			r.setAccess(AccessType.COW);
			env.getAbstractDataResource().add(r);
			d.setData("binding://" + id);
		}
		return env.toString();
	}


	/** 
	 * Drives capable to accept ready made images.
	 * @return
	 */
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

	/**
	 * Drives require a file system helper (FS annotation is required)
	 * @return
	 */
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


	public static Environment registerDataSource(Environment env, String ref, Drive.DriveType type) throws BWFLAException, IllegalArgumentException
	{
		if(env == null || ref == null || type == null)
			throw new IllegalArgumentException();
		
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
		Binding res = new Binding();
		res.setId(uuid);
		res.setUrl(url);
		res.setAccess(accessType);
		env.getAbstractDataResource().add(res);

		return uuid;
	}

	public static String getMainHddRef(EmulationEnvironment env)
	{
		if(env == null)
			return null;
		for (AbstractDataResource ab : env.getAbstractDataResource()) {
			if (ab.getId().equals("main_hdd")) { 
				Binding b = (Binding)ab;
				return b.getUrl();
			}
		}
		return null;
	}

	public static EmulationEnvironment setMainHdRef(EmulationEnvironment env, String ref, boolean cow) throws BWFLAException
	{
		if(env == null)
			return null;

		for (AbstractDataResource ab : env.getAbstractDataResource()) {
			if (ab.getId().equals("main_hdd")) { 
				Binding b = (Binding)ab;
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
	
	public static boolean beanSupportsMediaChange(String bean, DriveType type)
	{
		return BEANS_WITH_MEDIACHANGE_SUPPORT.contains(bean);
	}
	
	public static boolean beanSupportsMediaChangeUi(String bean, DriveType type)
	{
		return BEANS_WITH_MEDIACHANGE_UI.containsKey(bean);
	}
	
	public static String getMediaChangeHelp(String bean)
	{
		return BEANS_WITH_MEDIACHANGE_UI.get(bean);
	}
	
	public static int addArchiveBinding(EmulationEnvironment env, String host, 
			String archive, String objectId, DriveType type) throws BWFLAException
	{
		if(host == null || objectId == null || type == null)
		{
			throw new BWFLAException("addArchiveBinding::invalid parameter: " + host + " " + archive + " " + objectId);
			
		}
		
		ArchiveBinding ab = new ArchiveBinding(host, archive, objectId);
		ObjectArchiveHelper helper = new ObjectArchiveHelper(host);
		FileCollection fc = helper.getObjectReference(archive, objectId);
		ab.setId(objectId);
		env.getAbstractDataResource().add(ab);
		
		final String bean = env.getEmulator().getBean();
		if (EmulationEnvironmentHelper.beanSupportsMediaChange(bean, type)) {
			FileCollectionEntry fce = fc.files.get(0);
			return EmulationEnvironmentHelper.registerDrive(env, objectId, fce.getId(), type);
		}
		else if (EmulationEnvironmentHelper.beanSupportsMediaChangeUi(bean, type)) {
			FileCollectionEntry fce = fc.files.get(0);
			EmulationEnvironmentHelper.registerDrive(env, objectId, fce.getId(), type);
			return -1;
		}
		else // greedy allocation
		{
			for(FileCollectionEntry fce : fc.files)
			{
				log.info("adding fce to drive: " + fce.getId());
				EmulationEnvironmentHelper.registerDrive(env, objectId, fce.getId(), type);
			}
			return -2;
		}
	}
	
	private static int registerDrive(EmulationEnvironment env, String binding, String path, Drive.DriveType driveType) 
	{
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
	
	public static Container createFilesContainer(EmulationEnvironment _environment, String _dev, List<File> files)
	{
		Container container = null;			
		ContainerHelper helper = null;


		if(_dev.equals(Drive.DriveType.CDROM_IMG.name()) || _dev.equals(Drive.DriveType.DISK_IMG.name()) || _dev.equals(Drive.DriveType.FLOPPY_IMG.name()))
		{
			helper = new ImageFileHelper();
			Drive.DriveType type = Drive.DriveType.valueOf(_dev);
			Drive.DriveType realType = Drive.DriveType.convert(type);
			_dev = realType.toString().toUpperCase();
		}
		else
		{
			List<Pair<String,String>> devices = EmulationEnvironmentHelper.getHelperDrives((EmulationEnvironment) _environment);

			Filesystem fs = null;
			for(Pair<String, String> device: devices)
				if(device.getA().equalsIgnoreCase(_dev))
					fs = Filesystem.valueOf(device.getB().toUpperCase());

			if(fs == null)
			{
				log.severe("could not determine filesystem to uploaded attach files for the device (skipping): " + _dev);
				return null;
			}

			helper = ContainerHelperFactory.getContainerHelper(_dev, fs);
		}

		if(helper == null)
		{
			log.severe("container helper is null, make sure to check whether helper factory supports this device/filesystem combination");
			return null;
		}

		container = helper.createEmptyContainer();
		if(container == null)
		{
			log.severe("container is null, make sure to check whether corresponding helper is properly configured: " + helper.getClass().getSimpleName());
			return null;
		}

		if(!helper.insertIntoContainer(container, files))
		{
			log.warning("data attachment failed for the following container: " + container.getClass().getSimpleName());
			return null;
		}

		return container;

	}

	public static int attachContainer(EaasWS _eaas, String _sessionId, String _dev, Container container)
	{
		try
		{
			DataSource ds = new FileDataSource(container.getFile());
			DataHandler dh = new DataHandler(ds);
			if(_sessionId == null)
				return -1;
			
			log.severe("got sessionid : ++" + _sessionId + "++");
			
			return _eaas.attachMedium(_sessionId, dh, _dev.toUpperCase());
		}
		catch(Throwable t)
		{
			t.printStackTrace();
			return -1;
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

	/*
	public static List<Resource> getIndirectBindings(EmulationEnvironment env)
	{
		ArrayList<Resource> l = new ArrayList<>();
		
		for (Resource r : env.getBinding())
		{
			try 
			{
				URI uri = new URI(r.getUrl());
				if (uri.isOpaque() && uri.getScheme().equalsIgnoreCase("objectarchive"))
				{
					l.add(r);
				}
			}
			catch(Exception e)
			{
				log.warning("uri creation faild. skipping: " + r.getUrl());
				continue;
			}
		}
		return l;
	}
	*/
}
