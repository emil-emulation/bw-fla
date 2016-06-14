package de.bwl.bwfla.common.utils;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPBinding;

import de.bwl.bwfla.common.datatypes.FileCollection;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.jaxwsstubs.objectarchive.ObjectArchiveFacadeWSService;
import de.bwl.bwfla.common.jaxwsstubs.objectarchive.ObjectArchiveFacadeWSRemote;

public class ObjectArchiveHelper {

	protected final Logger	log	= Logger.getLogger(this.getClass().getName());
	
	private ObjectArchiveFacadeWSRemote archive = null; 
	private final String wsHost;
	
	public ObjectArchiveHelper(String wsHost)
	{
		this.wsHost = wsHost;
	}
	
	private static ObjectArchiveFacadeWSRemote getImageArchiveCon(String host)
	{
		URL wsdl;
		ObjectArchiveFacadeWSRemote archive;
		try 
		{
			wsdl = new URL("http://" + host + "/object-archive/ObjectArchiveFacadeWS?wsdl");
			ObjectArchiveFacadeWSService service = new ObjectArchiveFacadeWSService(wsdl);
			archive = service.getObjectArchiveFacadeWSPort();
		} 
		catch (Throwable t) 
		{
			// TODO Auto-generated catch block
			Logger.getLogger(SystemEnvironmentHelper.class.getName()).info("Can not initialize wsdl from http://" + host + "/object-archive/ObjectArchiveFacadeWS?wsdl");
			return null;
		}

		BindingProvider bp = (BindingProvider)archive;
		SOAPBinding binding = (SOAPBinding) bp.getBinding();
		binding.setMTOMEnabled(true);
		bp.getRequestContext().put("javax.xml.ws.client.receiveTimeout", "0");
		bp.getRequestContext().put("javax.xml.ws.client.connectionTimeout", "0");
		bp.getRequestContext().put("com.sun.xml.internal.ws.transport.http.client.streaming.chunk.size", 8192);
		bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "http://" + host + "/object-archive/ObjectArchiveFacadeWS?wsdl");

		return archive;
	}
	
	private void connectArchive() throws BWFLAException
	{
		if(archive != null)
			return;
		
		archive = getImageArchiveCon(wsHost);
		if(archive == null)
			throw new BWFLAException("could not connect to object archive @ " + wsHost);	
	}
	
	public List<String> getArchives() throws BWFLAException 
	{
		connectArchive();
		return archive.getArchives();
	}
	
	public List<String> getObjectList(String _archive) throws BWFLAException
	{
		connectArchive();
		List<String> objs = archive.getObjectList(_archive);
		
		if(objs == null)
		{
			log.warning("archive  " + _archive + " is empty");
			return new ArrayList<>();
		}
		
		log.info(_archive + ": found " + objs.size() + " objects");
		List<String> uniqueList = new ArrayList<String>(
				new HashSet<String>(objs));
		java.util.Collections.sort(uniqueList);
		return uniqueList;
	}
	
	public FileCollection getObjectReference(String _archive, String id) throws BWFLAException
	{
		connectArchive();
		String colStr = archive.getObjectReference(_archive, id);
		if(colStr == null)
		{
			log.severe("could not get metadata for ID: " + id);
			return null;
		}
		
		FileCollection fc = null;
		try {
			fc = FileCollection.fromValue(colStr);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (fc == null || fc.label == null)
			return null;
		
		if(fc.files.size() == 0)
			return null;
		
		return fc;
	}
	
	public String getHost()
	{
		return wsHost;
	}
	
	
}
