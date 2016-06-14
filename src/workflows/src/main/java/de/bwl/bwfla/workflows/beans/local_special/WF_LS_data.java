package de.bwl.bwfla.workflows.beans.local_special;

import java.io.Serializable;
import javax.inject.Named;
import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.WindowScoped;

@Named
@WindowScoped
public class WF_LS_data implements Serializable
{
	private static final long serialVersionUID = -2185210506088159957L;

	private String envid = "";

	public void setEnvironmentID(String id)
	{
		this.envid = id;;
	}
	
	public String getEnvironmentID() 
	{
		return envid;
	}
}
