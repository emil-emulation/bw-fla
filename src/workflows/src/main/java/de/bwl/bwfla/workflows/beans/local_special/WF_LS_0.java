package de.bwl.bwfla.workflows.beans.local_special;

import java.io.Serializable;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.SystemEnvironmentHelper;
import de.bwl.bwfla.workflows.beans.common.BwflaEmulatorViewBean;
import de.bwl.bwfla.workflows.beans.common.RedirectionObserver;
import de.bwl.bwfla.workflows.beans.common.WorkflowResources;
import de.bwl.bwfla.workflows.catalogdata.SystemEnvironmentDescription;
import de.bwl.bwfla.workflows.conf.WorkflowSingleton;


@ManagedBean
@ViewScoped
public class WF_LS_0 extends BwflaEmulatorViewBean implements Serializable
{
	private static final long serialVersionUID = 4110115919235408667L;

	private SystemEnvironmentHelper envHelper = null;
	private String envid = null;
	
	@Inject private WF_LS_data wfData;
	
	@Override
	public void initialize()
	{
		Map<String, String> parameters = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		envid = parameters.get("envid");
		if (envid == null || envid.isEmpty()) {
			this.panic("No Environment-ID specified!");
			return;
		}
		
		wfData.setEnvironmentID(envid);
		
		envHelper = WorkflowSingleton.envHelper;
		description = new SystemEnvironmentDescription(envHelper, envid);
		try {
			emuHelper = description.getEmulatorHelper();
			if (emuHelper == null) {
				this.panic("Failed to obtain technical meta-data for ID " + envid);
				return;
			}
		}
		catch (BWFLAException exception) {
			this.panic("Creating an emulator helper failed!", exception);
		}
		super.initialize();
		resourceManager.cleanupAll();
		resourceManager.register(WorkflowResources.WF_RES.EMU_COMP, emuHelper);
		
		emuHelper.initialize();
		
		this.setEaasStateObserver(RedirectionObserver.create(jsf, "/faces" + this.forward()));
	}
	
	public String forward()
	{
		// Redirect to itself
		final String params = "faces-redirect=true&envid=" + envid;
		return "/pages/workflow-local-special/WF_LS_0.xhtml?" + params;
	}
}
