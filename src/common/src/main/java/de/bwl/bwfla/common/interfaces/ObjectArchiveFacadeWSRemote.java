package de.bwl.bwfla.common.interfaces;

import java.util.List;

import javax.ejb.Remote;
import javax.jws.WebMethod;
import javax.jws.WebService;

@Remote
@WebService
public interface ObjectArchiveFacadeWSRemote {

	@WebMethod
	public List<String> getObjectList(String archive);
	
	@WebMethod
	public List<String> getArchives(); 
	
	@WebMethod
	public String getObjectReference(String archive, String id);
}
