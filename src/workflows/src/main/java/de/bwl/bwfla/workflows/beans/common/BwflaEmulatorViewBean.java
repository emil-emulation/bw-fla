package de.bwl.bwfla.workflows.beans.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.activation.DataHandler;
import javax.faces.event.ActionEvent;

import de.bwl.bwfla.common.datatypes.EaasState;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.services.handle.HandleService;
import de.bwl.bwfla.common.utils.SystemEnvironmentHelper;
import de.bwl.bwfla.workflows.catalogdata.Description;
import de.bwl.bwfla.workflows.catalogdata.DescriptionSerializer;
import de.bwl.bwfla.workflows.catalogdata.SystemEnvironmentDescription;
import de.bwl.bwfla.workflows.conf.WorkflowSingleton;



public abstract class BwflaEmulatorViewBean extends BwflaFormBean
{
	private static final long serialVersionUID = 1L;
	protected AbstractRemoteEmulatorHelper emuHelper = null;
	protected Description description = null;
	protected File screenshot = null;
	private String hdlStr = "";
	private String embedGw;
	private String imageArchiveHost;
	private EmulatorMediaManager mediaManager = null;
	private EaasStateObserver eaasStateObserver = null;
	private EaasState lastEmucompState;
	protected boolean eaasReady = false;
	protected boolean eaasRunning = false;
	private int connectionChecks = 0;

	@Override
	protected void initialize()
	{
		if(emuHelper == null)
		{
			log.severe("emuHelper not set before super.initialize was called");
			panic("invalid workflow template");
		}
		super.initialize();
		embedGw = WorkflowSingleton.CONF.embedGw;
		lastEmucompState = EaasState.SESSION_UNDEFINED;
		// Prevent autostart by checking if the user set his preferences yet
		mediaManager = emuHelper.getMediaManager();
	}
	
	synchronized public void observeReadiness(ActionEvent event) throws BWFLAException
	{	
		if(this.eaasRunning)
		{
			log.warning("emulation-as-a-service is running, no need to proceed observation");
			return;
		}
		
		EaasState state = this.emuHelper.getEaasState();
		if(state == null)
		{
			log.severe("illegal state at this workflow step, unable to get emulation state, value is 'null'");
			return;
		}
		
		if(lastEmucompState != state)
		{
			log.info("Remote state changed: " + lastEmucompState.value() + " -> " + state.value());
			this.lastEmucompState = state;
		}
		
		final int MAX_CONNECT_TRIES = 300;
		if(connectionChecks++ > MAX_CONNECT_TRIES)
		{
			panic("unable to connect to server after multiple attempts, last known state: " + state.value());
			return;
		}
		
		switch(state)
		{
			case SESSION_ALLOCATING:
			case SESSION_BUSY:
				break;
				
			case SESSION_READY:
				this.eaasReady = true;
				break;
				
			case SESSION_RUNNING:
				this.eaasReady = true;
				this.eaasRunning = true;
				this.connectionChecks = 0;
				break;
	
			case SESSION_OUT_OF_RESOURCES:
				panic("emulation-as-a-service is out of resources, please try again later");
				break;

			case SESSION_CLIENT_FAULT:
				panic("client has supplied incorrect input parameters to emulation-as-a-service ");
				break;

			case SESSION_FAILED:
				panic("an internal error occured on the server-side of emulation-as-a-service");
				break;
				
			default:
				panic("emulation-as-a-service for this session is in an illegal state at this workflow-point: " + state.value());
				break;
		}
	}
	
	synchronized public void observeConnection(ActionEvent event) throws BWFLAException
	{	
		if(!this.eaasRunning)
		{
			log.warning("emulation-as-a-service is not running, no need to proceed observation");
			return;
		}
		
		EaasState state = this.emuHelper.getEaasState();
		if(state == null)
		{
			log.severe("illegal state at this workflow step, unable to get emulation state, value is 'null'");
			return;
		}
		
		if (lastEmucompState != state) {
			log.info("Remote state changed: " + lastEmucompState.value() + " -> " + state.value());
			
			// Notify state observer
			if (eaasStateObserver != null) {
				try {
					eaasStateObserver.onStateChanged(lastEmucompState, state);
				}
				catch (Exception exception) {
					this.panic("Executing EaasStateObserver failed!", exception);
				}
			}
			
			this.lastEmucompState = state;
		}
		
		switch(state)
		{
			case SESSION_RUNNING:
			case SESSION_BUSY:
			case SESSION_INACTIVE:
				// XXX: no action required
				break;
				
			case SESSION_STOPPED:
				this.eaasRunning = false;
				break;
				
			default:
				panic("emulation-as-a-service is in an illegal state at this point: " + state.value());
				break;
		}

	}
	
	public boolean isAutostart()
	{
		return true;
	}
	
	public boolean isEaasReady()
	{	
		return this.eaasReady;
	}
	
	public boolean isEaasRunning()
	{	
		return this.eaasRunning;
	}
	
	public EaasStateObserver getEaasStateObserver()
	{
		return eaasStateObserver;
	}
	
	public void setEaasStateObserver(EaasStateObserver observer)
	{
		this.eaasStateObserver = observer;
	}
	
	public String getControlUrl() throws BWFLAException 
	{	
		return emuHelper.getControlUrl();
	}
	
	public void start() throws BWFLAException
	{	
		emuHelper.startEmulator();
	}
	
	public void stop() throws BWFLAException
	{
		emuHelper.stop();
	}
	
	public void takeScreenshot() throws BWFLAException
	{
		if (!this.eaasRunning) {
			log.warning("Emulator is not running, cannot take screenshot!");
			return;
		}
		
		emuHelper.takeScreenshot();

		DataHandler data = null;
		int numRetries = 20;
		
		// Wait for the screenshot to become available
		while ((data = emuHelper.getNextScreenshot()) == null) {
			try {
				Thread.sleep(250L);
			}
			catch (InterruptedException exception) {
				exception.printStackTrace();
				return;
			}
			
			if (--numRetries < 0) {
				log.warning("Timeout occured while waiting for screenshot!");
				return;
			}
		}
		
		try {
			if (screenshot == null)
				screenshot = File.createTempFile("screenshot-", ".png");
			
			// Write screenshot data to a temporary-file
			FileOutputStream output = new FileOutputStream(screenshot);
			data.writeTo(output);
			output.flush();
			output.close();
			
			log.info("SCREENSHOT FILE: " + screenshot);
		}
		catch (IOException exception) {
			log.warning("An error occured during screenshot file creation/modification!");
		}
	}
	
	/** Returns true, when the website is embedded using an iframe. */
	public boolean isInsideIFrame()
	{
		return false;
	}
	
	public void createHdlCodeModifed() throws BWFLAException
	{
		if(description == null)
		{
			log.severe("description not set. configure propperly!");
			UINotify.error("wf-error: description not set");
			return;
		}
		
		Description d = DescriptionSerializer.copy(description);
		
		if(embedGw == null)
		{
			log.info("embedGw nt set");
			UINotify.error("Gateway for embedded URLs not configured");
			return;
		}
		
		SystemEnvironmentHelper dstArchive = WorkflowSingleton.envHelper;
		String uuid = emuHelper.saveEnvironment(imageArchiveHost, "WF_I_2 generated env", "user");
		log.info("got new id " + uuid);
		((SystemEnvironmentDescription) d).updateEmulationEnvironmentId(dstArchive, uuid);
		
		String id = UUID.randomUUID().toString();
		String url = embedGw + CitationUrlHelper.urlString(d);
		if(HandleService.createUrlHandle("11270/" + id, url))
		{
			hdlStr =  "http://hdl.handle.net/11270/" + id;
			log.info("created handle " + hdlStr);
		}
	}
	
	public String getIFrameCode()
	{
		if(description == null)
		{
			log.severe("description not set. configure propperly!");
			UINotify.error("wf-error: description not set");
			return "";
		}
		
		if(embedGw == null)
		{
			log.info("embedGw nt set");
			return "not configured";
		}
		
		return "<iframe width=\"800\" height=\"600\" src=\"" + embedGw + CitationUrlHelper.urlString(description) + "\"" + " frameborder=\"0\"></iframe>";
	}
	
	public void createHdl()
	{
		if(description == null)
		{
			log.severe("description not set. configure propperly!");
			UINotify.error("wf-error: description not set");
			hdlStr = "not configured";
			return;
		}
		
		if(embedGw == null)
		{
			log.info("embedGw not set");
			hdlStr = "not configured";
			UINotify.error("Gateway for embedded URLs not configured");
			return;
		}
		
		String id = UUID.randomUUID().toString();
		String url = embedGw + CitationUrlHelper.urlString(description);
		if(HandleService.createUrlHandle("11270/" + id, url))
		{
			hdlStr =  "http://hdl.handle.net/11270/" + id;
			log.info("created handle " + hdlStr);
		}
	}
	
	public String getHandleStr()
	{
		return hdlStr;
	}

	public List<ChosenMediaForDeviceBean> getChosenMediaForDevices() {
		if(mediaManager == null)
			return null;
        return mediaManager.getChosenMediaForDevices();
    }
	
	public void updateChosenMedia() {
		if(mediaManager == null)
			return;
	    try {
			mediaManager.updateChosenMedia();
		} catch (Exception e) {
            UINotify.error("Change medium failed: " + e.getMessage());
        }
	}
	
	public void setChosenMediaForDevices(List<ChosenMediaForDeviceBean> chosenMediaForDevices) {
		if(mediaManager == null)
			return;
		mediaManager.setChosenMedia(chosenMediaForDevices);
	}
}
