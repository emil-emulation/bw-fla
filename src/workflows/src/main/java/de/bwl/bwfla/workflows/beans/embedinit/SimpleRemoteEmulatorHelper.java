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

package de.bwl.bwfla.workflows.beans.embedinit;

import de.bwl.bwfla.common.datatypes.EaasState;
import de.bwl.bwfla.workflows.beans.common.AbstractRemoteEmulatorHelper;
import de.bwl.bwfla.workflows.beans.common.WFPanicException;


public class SimpleRemoteEmulatorHelper extends AbstractRemoteEmulatorHelper
{
	public SimpleRemoteEmulatorHelper(String sessid)
	{
		super(null);
		this.sessId = sessid;
	}
	
	public void initialize()
	{
		final int TIMEOUT_IN_SEC = 15;
		EaasState state = null;
		
		try
		{
			super.initialize();
			
			for(int i = 0, DELAY_MS = 100; i < TIMEOUT_IN_SEC * DELAY_MS; ++i)
			{
				state = this.getEaasState();
				switch(state)
				{
					case SESSION_UNDEFINED:
					case SESSION_RUNNING:
					case SESSION_STOPPED:
						throw new WFPanicException("EAAS is in an illegal state at this point: " + state.value());

					case SESSION_CLIENT_FAULT:
						throw new WFPanicException("client has specified illegal input data to EAAS");

					case SESSION_FAILED:
						throw new WFPanicException("an internal error occured on the server side");

					case SESSION_OUT_OF_RESOURCES:
						throw new WFPanicException("EAAS infrastructure is out of resources, please try again later");
						
					case SESSION_ALLOCATING:
					case SESSION_BUSY:
						Thread.sleep(DELAY_MS);
						continue;
						
					case SESSION_READY:
						return;
				}			
			}

			throw new WFPanicException("EAAS session not allocated after a predefined workflow timeout, last known remote state: " + state.value());			
		}
		catch(Throwable t)
		{
			t.printStackTrace();
			throw new WFPanicException("web-interface module has experienced an internal error, please try again later");
		}
		finally
		{
			if(state != EaasState.SESSION_READY && this.sessId != null && this.eaas != null)
				this.eaas.releaseSession(this.sessId);
		}
	}
}
