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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.bwl.bwfla.api.eaas.EaasWS;


public class RemoteSessionPlayer
{
	/** Logger instance. */
	private final Logger log = LoggerFactory.getLogger(RemoteSessionPlayer.class);

	// Member fields
	private final EaasWS eaas;
	private final String sid;
	private boolean prepared;
	
	
	public RemoteSessionPlayer(EaasWS eaas, String sid)
	{
		if (eaas == null)
			throw new IllegalArgumentException("Invalid EaasWS service specified!");
		
		if (sid == null || sid.isEmpty())
			throw new IllegalArgumentException("Invalid SessionID specified!");
		
		this.eaas = eaas;
		this.sid = sid;
		this.prepared = false;
	}

	public boolean prepare(String trace, boolean headless)
	{
		log.info("Initializing remote session-player service");
		try {
			prepared = eaas.prepareSessionPlayer(sid, trace, headless);
		}
		catch (Exception exception) {
			exception.printStackTrace();
			prepared = false;
		}
		
		return prepared;
	}

	public boolean isPrepared()
	{
		return prepared;
	}
	
	public boolean isReplayModeEnabled()
	{
		try {
			return eaas.isReplayModeEnabled(sid);
		}
		catch (Exception exception) {
			exception.printStackTrace();
			return false;
		}
	}
	
	public int getProgress()
	{
		try {
			return eaas.getSessionPlayerProgress(sid);
		}
		catch (Exception exception) {
			exception.printStackTrace();
			return 0;
		}
	}
}
