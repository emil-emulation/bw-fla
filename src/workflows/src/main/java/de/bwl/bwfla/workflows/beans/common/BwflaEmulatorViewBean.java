/*
 * This file is part of the Emulation-as-a-Service framework.
 *
 * The Emulation-as-a-Service framework is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * The Emulation-as-a-Service framework is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Emulation-as-a-Software framework.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package de.bwl.bwfla.workflows.beans.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
	protected boolean emulatorReady = false;
	protected File screenshot = null;
	private String hdlStr = "";
	private String embedGw;
	private String imageArchiveHost;
	private int connectTries = 0;
	
	private EaasState lastEmucompState;
	
	@Override
	protected void initialize()
	{
		super.initialize();
		embedGw = WorkflowSingleton.CONF.embedGw;
		imageArchiveHost = WorkflowSingleton.CONF.archiveGw;
		lastEmucompState = EaasState.SESSION_UNDEFINED;
	}
	
	public void monitorState(ActionEvent event)
	{
		EaasState state = this.emuHelper.getEaasState();
		if(state == null)
		{
			log.info("remote emulation session missing, state is 'null'");
			return;
		}
		
		switch(state)
		{
			case SESSION_UNDEFINED:
			case SESSION_ALLOCATING:
				if(!this.emulatorReady);
					panic("EAAS is in an illegal state at this point: " + state.value());
				break;

			case SESSION_READY:
				break;

			case SESSION_BUSY:
				final int MAX_CONNECT_TRIES = 300;
				if(connectTries++ > MAX_CONNECT_TRIES)
					panic("unable to connect to server after multiple attempts, current state: " + state.value());
				break;

			case SESSION_RUNNING:
				this.emulatorReady = true;
				break;

			case SESSION_STOPPED:
				// XXX: decide whether this has to be handled
				connectTries = 0;
				if(lastEmucompState != EaasState.SESSION_RUNNING && lastEmucompState != EaasState.SESSION_STOPPED)
					panic("failed starting remote emulator, check configuration");
				break;

			case SESSION_OUT_OF_RESOURCES:
				panic("emulation-as-a-service is out of resources, please try again later");
				break;

			case SESSION_CLIENT_FAULT:
				panic("emulation-as-a-service message: client has supplied incorrect parameters");
				break;

			case SESSION_FAILED:
				panic("an internal error occured on the server-side");
				break;
		}
		
		if(lastEmucompState != state)
		{
			log.info("change of remote state to: " + state.value());
			lastEmucompState = state;
		}
	}
	
	public boolean isAutostart()
	{
		return true;
	}
	
	public boolean isEmulatorReady()
	{	
		return this.emulatorReady;
	}
	
	public String getControlUrl() 
	{
		
		try {
			return emuHelper.getControlUrl();
		} catch (Throwable e) {
			panic(e.getMessage(), e);
		}
		return null;
	}
	
	public void start() throws BWFLAException
	{	
		emuHelper.startEmulator();
	}
	
	public void stop()
	{
		emuHelper.stop();
	}
	
	public void takeScreenshot() throws BWFLAException
	{
		if (!this.isEmulatorReady()) {
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
}
