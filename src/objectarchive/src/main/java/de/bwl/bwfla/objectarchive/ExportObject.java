package de.bwl.bwfla.objectarchive;

import java.io.File;

import de.bwl.bwfla.common.services.net.HttpExport;
import de.bwl.bwfla.objectarchive.conf.ObjectArchiveSingleton;
import de.bwl.bwfla.objectarchive.impl.DigitalObjectFileArchive;


public class ExportObject extends HttpExport 
{ 
	private static final long serialVersionUID = 1L;

	@Override
	public File resolveRequest(String reqStr) 
	{
		String archive = null; // archive is the first element of the path
		String objPath = null; // objPath is the rest of the path without the first element
		if(reqStr.startsWith("/"))
		{
			archive = reqStr.substring(1, reqStr.indexOf("/", 2));
			objPath = reqStr.substring(reqStr.indexOf("/", 2) + 1);
		}

		// System.out.println("archive: " + archive + "-" + objPath);

		// System.out.println("http-object-archive: " + requestedObject);
		if(ObjectArchiveSingleton.archiveMap == null)
			return null;
		
		DigitalObjectArchive doa = ObjectArchiveSingleton.archiveMap.get(archive);
		if(!(doa instanceof DigitalObjectFileArchive))
			return null;

		DigitalObjectFileArchive dofa = (DigitalObjectFileArchive)doa;
	
		// System.out.println("http-object-archive: filePath: " + filePath);
		return new File(dofa.getLocalPath(), objPath);
	}
	
}
