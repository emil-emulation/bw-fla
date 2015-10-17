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

package de.bwl.bwfla.workflows.beans.record;

import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import de.bwl.bwfla.workflows.beans.common.BwflaEmulatorViewBean;
import de.bwl.bwfla.workflows.beans.common.RemoteSessionRecorder;


@ManagedBean
@ViewScoped
public class WF_REC_2 extends BwflaEmulatorViewBean implements Serializable
{
	// Injected members
	@Inject private WF_REC_Data wfdata;
	
	// Member fields
	private RemoteSessionRecorder recorder;
	private String statusmsg;
	private String btnlabel;
	private boolean recflag;

	
	@Override
	public void initialize()
	{
		// Due to a bug in JBOSS, this method can be called multiple times!
		// This happens mostly when we are redirecting to a new page.
		
		// Was this method already called?
		if (wfdata.getRemoteSessionRecorder() != null) {
			log.warning("An attempt made to construct the bean multiple times! Skip the redundant construction.");
			return;
		}
		
		super.initialize();
		
		super.emuHelper = wfdata.getRemoteEmulatorHelper();
		
		// Create and initialize the remote recorder
		this.recorder = new RemoteSessionRecorder(emuHelper.getEaasWS(), emuHelper.getSessionId());
		if (!recorder.prepare())
			this.panic("Initialization of remote SessionRecorder failed!");
		
		// For later usage in following webpages
		wfdata.setRemoteSessionRecorder(recorder);
		
		this.btnlabel = "STOP";
		this.recflag = false;
		
		this.startOrStopRecording();
	}
	
	public String getStatusMessage()
	{
		return statusmsg;
	}
	
	public String getButtonLabel()
	{
		return btnlabel;
	}
	
	public void startOrStopRecording()
	{
		// Recording already finished?
		if (recflag)
			return;  // Yes
		
		final String winid = this.getWindowId();
		
		// Start recording?
		if (!recorder.isRecordModeEnabled()) {
			log.info("Start session-recording for window " + winid + ".");
			statusmsg = "Recording...";
			btnlabel = "Stop";
			
			if (!recorder.start())
				this.panic("Starting the recorder failed!");
		}
		else {
			log.info("Stop session-recording for window " + winid + ".");
			statusmsg = "Recording stopped!";
			recflag = true;
			recorder.stop();
		}
	}
	
	public boolean isRecording()
	{
		return recorder.isRecordModeEnabled();
	}
	
	public boolean isRecordingFinished()
	{
		return recflag;
	}
	
	public void addActionFinishedMark()
	{
		recorder.addActionFinishedMark();
	}
	
	@Override
	public String forward()
	{
		if (this.isRecording())
			this.startOrStopRecording();
		
		// Construct the next page's URL
		return WF_REC_Data.getPageUrl(3, true);
	}

	
	/* =============== Internal Stuff =============== */
	
	private static final long serialVersionUID = -5517465078595633083L;
}
