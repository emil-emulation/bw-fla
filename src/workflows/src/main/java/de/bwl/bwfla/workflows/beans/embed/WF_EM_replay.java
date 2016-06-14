package de.bwl.bwfla.workflows.beans.embed;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.SystemEnvironmentHelper;
import de.bwl.bwfla.workflows.beans.common.BwflaEmulatorViewBean;
import de.bwl.bwfla.workflows.beans.common.CitationUrlHelper;
import de.bwl.bwfla.workflows.beans.common.RemoteSessionPlayer;
import de.bwl.bwfla.workflows.beans.common.WorkflowResources;
import de.bwl.bwfla.workflows.catalogdata.Description;
import de.bwl.bwfla.workflows.conf.WorkflowSingleton;


@ManagedBean
@ViewScoped
public class WF_EM_replay extends BwflaEmulatorViewBean implements Serializable
{
	private static final long serialVersionUID = 5185442473194817339L;

	// Member fields
	private String statusmsg;
	private RemoteSessionPlayer player;

	
	@Override
	public void initialize()
	{
		// Due to a bug in JBOSS, this method can be called multiple times!
		// This happens mostly when we are redirecting to a new page.
		
		// Was this method already called?
		if (resourceManager.hasResource(WorkflowResources.WF_RES.EMU_COMP)) {
			log.warning("An attempt made to construct the bean multiple times! Skip the redundant construction.");
			return;
		}

		final ExternalContext extcontext = FacesContext.getCurrentInstance().getExternalContext();
		final Map<String, String> parameters = extcontext.getRequestParameterMap();
		
		final String traceid = parameters.get("traceid");
		if (traceid == null || traceid.isEmpty())
			this.panic("Trace-ID is wrong or missing! Check passed URL parameters.");
		
		// Initialize emulator-helper
		final Description envDescription = CitationUrlHelper.getDescription(jsf);
		if (envDescription == null)
			this.panic("Environment description is wrong or missing! Check passed URL parameters.");

		try {
			emuHelper = envDescription.getEmulatorHelper();
		} catch (BWFLAException exception) {
			this.panic(exception.getMessage());
		}

		if (emuHelper == null)
			this.panic("Umulator helper could not be created for the corresponding platform.");

		super.initialize();
		
		try {
			resourceManager.register(WorkflowResources.WF_RES.EMU_COMP, emuHelper);
			resourceManager.disableTimeout();
			emuHelper.initialize();
		}
		finally {
			resourceManager.restartTimeout();
		}
		
		// Initialize the remote session-player
		player = new RemoteSessionPlayer(emuHelper.getEaasWS(), emuHelper.getSessionId());
		
		// Send the session-trace to the remote session-player
		try {
			final SystemEnvironmentHelper envHelper = WorkflowSingleton.envHelper;
			String trace = envHelper.getRecording(envDescription.getId(), traceid);
			if (!player.prepare(trace, false))
				this.panic("Preparing remote session-player failed!");
		}
		catch (Exception exception) {
			this.panic("Loading the specified session-trace failed!", exception);
		}
		
		statusmsg = "Replaying...";
	}
	
	public String getStatusMessage()
	{
		return statusmsg;
	}
	
	public int getProgress() throws IOException
	{
		if (player == null)
			return 0;
		
		final int progress = player.getProgress();
		if (progress == 100)
			statusmsg = "Replay finished!";
		
		return progress;
	}
	
	public boolean isPlaying()
	{
		return ((player != null) && player.isReplayModeEnabled());
	}
	
	@Override
	public String forward()
	{
		return "";
	}
	
	@Override
	public boolean isAutostart()
	{
		return true;
	}
	
	@Override
	public boolean isInsideIFrame()
	{
		return true;
	}
}
