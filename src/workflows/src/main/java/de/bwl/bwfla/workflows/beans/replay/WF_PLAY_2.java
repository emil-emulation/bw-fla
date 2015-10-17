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

package de.bwl.bwfla.workflows.beans.replay;

import java.io.IOException;
import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import de.bwl.bwfla.workflows.beans.common.BwflaEmulatorViewBean;
import de.bwl.bwfla.workflows.beans.common.RemoteSessionPlayer;
import de.bwl.bwfla.workflows.beans.common.WorkflowResources;


@ManagedBean
@ViewScoped
public class WF_PLAY_2 extends BwflaEmulatorViewBean implements Serializable
{
	private static final long serialVersionUID = -7931658858286045584L;

	// Injected members
	@Inject private WF_PLAY_Data wfdata;
	
	// Member fields
	private String statusmsg;
	private RemoteSessionPlayer player;

	
	@Override
	public void initialize()
	{
		// Due to a bug in JBOSS, this method can be called multiple times!
		// This happens mostly when we are redirecting to a new page.
		
		// Was this method already called?
		if (wfdata.getRemoteSessionPlayer() != null) {
			log.warning("An attempt made to construct the bean multiple times! Skip the redundant construction.");
			return;
		}

		super.initialize();
		
		super.emuHelper = wfdata.getRemoteEmulatorHelper();
		
		// Initialize the remote session-player
		player = new RemoteSessionPlayer(emuHelper.getEaasWS(), emuHelper.getSessionId());
		wfdata.setRemoteSessionPlayer(player);
		
		// Send the session-trace to the remote session-player
		String trace = wfdata.getSystemEnvironmentHelper().getRecording(wfdata.getEmulatorEnvId(), wfdata.getTrace().getUuid());
		if (!player.prepare(trace, false))
			this.panic("Preparing remote session-player failed!");
		
		this.statusmsg = "Replaying...";
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
	public void cleanup()
	{
		resourceManager.cleanupResources(WorkflowResources.WF_RES.EMU_COMP);
		super.cleanup();
	}
	
	@Override
	public String forward()
	{
		// Construct the next page's URL
		return WF_PLAY_Data.getPageUrl(3, true);
	}
}
