package de.bwl.bwfla.workflows.beans.miniwf;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;

import de.bwl.bwfla.common.datatypes.EaasState;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.workflows.beans.common.BwflaEmulatorViewBean;
import de.bwl.bwfla.workflows.beans.common.RedirectionObserver;
import de.bwl.bwfla.workflows.beans.common.WorkflowResources;
import de.bwl.bwfla.workflows.beans.miniwf.WF_M_data.Storage;
import de.bwl.bwfla.workflows.conf.WorkflowSingleton;

@ManagedBean
@ViewScoped
public class WF_M_2 extends BwflaEmulatorViewBean implements Serializable
{
	private static final long	serialVersionUID	= -2323912800593361682L;
	
	@Inject private WF_M_data	wfData;
	protected Storage			storage;
	private String 				derivateName;
	
	
	public String forward()
	{
		try
		{
			storage.emuHelper.stop();
		}
		catch(BWFLAException e)
		{
			e.printStackTrace();
			panic("unable to stop the remote EAAS session correctly, exiting");
		}
		
		return "/pages/workflow-miniwf/WF_M_3.xhtml?faces-redirect=true";
	}
	
	@Override
	public void initialize()
	{
		storage = wfData.getStorage();
		emuHelper = storage.emuHelper;
		description = wfData.getStorage().description;
		super.initialize();
		if(resourceManager.hasResource(WorkflowResources.WF_RES.EMU_COMP))
			return;
		
		this.storage.emuHelper.initialize();
		resourceManager.register(WorkflowResources.WF_RES.EMU_COMP, storage.emuHelper);
		
		final String startpage = "/faces/pages/bwfla.xhtml";
		final String nextpage = "/faces/pages/workflow-miniwf/WF_M_3.xhtml?faces-redirect=true";
		
		RedirectionObserver observer = new RedirectionObserver(jsf);
		observer.addEntry(EaasState.SESSION_INACTIVE, startpage, "Session inactivity detected.");
//		observer.addEntry(EaasState.SESSION_STOPPED , nextpage, "Session stopped unexpectedly!");
		this.setEaasStateObserver(observer);
	}
	
	public String save()
	{
		if(derivateName.trim().isEmpty())
			return "";
		
		try {
			storage.emuHelper.saveEnvironment(WorkflowSingleton.CONF.imageArchive, derivateName, "derivate");
		} catch (BWFLAException e) {
			panic("Failed saving derivate: " + e.getMessage(), e);
		}
		return "/pages/workflow-miniwf/WF_M_3.xhtml?faces-redirect=true";
	}
	
	public String getDerivateName() {
		return derivateName;
	}

	public void setDerivateName(String derivateName) {
		this.derivateName = derivateName;
	}
}
