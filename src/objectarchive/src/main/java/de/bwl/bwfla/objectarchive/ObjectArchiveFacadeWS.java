package de.bwl.bwfla.objectarchive;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Remote;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.jws.WebService;
import javax.xml.bind.JAXBException;

import de.bwl.bwfla.common.datatypes.FileCollection;
import de.bwl.bwfla.common.datatypes.FileCollectionEntry;
import de.bwl.bwfla.common.interfaces.ObjectArchiveFacadeWSRemote;
import de.bwl.bwfla.objectarchive.conf.ObjectArchiveConf;
import de.bwl.bwfla.objectarchive.conf.ObjectArchiveSingleton;

@WebService
@Remote(ObjectArchiveFacadeWSRemote.class)
public class ObjectArchiveFacadeWS implements ObjectArchiveFacadeWSRemote
{
	protected static final Logger LOG = Logger.getLogger(ObjectArchiveFacadeWS.class.getName());
	
	@PostConstruct
	private void initialize()
	{
		
	}
	
	/**
	 * @return list of object IDs
	 */
	@Override
	public List<String> getObjectList(String archive)
	{
		if(!ObjectArchiveSingleton.confValid)
		{
			LOG.severe("ObjectArchive not configured");
			return null;
		}
		DigitalObjectArchive a = ObjectArchiveSingleton.archiveMap.get(archive);
		return a.getObjectList();
	}

	/**
	 * @param id object-id
	 * @return object reference as PID / PURL
	 */
	@Override
	public String getObjectReference(String archive, String id)
	{
		if(!ObjectArchiveSingleton.confValid)
		{
			LOG.severe("ObjectArchive not configured");
			return null;
		}
		
		DigitalObjectArchive a = ObjectArchiveSingleton.archiveMap.get(archive);
		try {
			FileCollection fc = a.getObjectReference(id);
			if(fc == null)
			{
				LOG.severe("could not find object");
				return null;
			}
			return fc.value();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public List<String> getArchives() {
		if(!ObjectArchiveSingleton.confValid)
		{
			LOG.severe("ObjectArchive not configured");
			return null;
		}
		
		Set<String> keys = ObjectArchiveSingleton.archiveMap.keySet();
		return new ArrayList<String>(keys);
	}
}