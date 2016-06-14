package de.bwl.bwfla.common.interfaces;

import java.util.List;

import javax.ejb.Remote;
import javax.jws.WebMethod;
import javax.jws.WebService;

import de.bwl.bwfla.common.datatypes.SoftwarePackage;
import de.bwl.bwfla.common.datatypes.SoftwareDescription;

@Remote
@WebService
public interface SoftwareArchiveWSRemote
{
	@WebMethod
	public int getNumSoftwareSeatsById(String id);
	
	@WebMethod
	public SoftwarePackage getSoftwarePackageById(String id);
	
	@WebMethod
	public List<String> getSoftwarePackages();
	
	@WebMethod
	public SoftwareDescription getSoftwareDescriptionById(String id);
	
	@WebMethod
	public List<SoftwareDescription> getSoftwareDescriptions();
	
	@WebMethod
	public String getName();
}
