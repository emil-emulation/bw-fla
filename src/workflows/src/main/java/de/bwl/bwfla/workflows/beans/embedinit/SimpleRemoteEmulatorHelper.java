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
		super();
		this.sessid = sessid;
	}
	
	public void initialize()
	{
		super.initialize();

		boolean initialized = false;
		
		// Wait for the session to become ready.
		for (int i = 1500; !initialized && (i > 0); --i) {
			final EaasState state = this.getEaasState();
			switch(state)
			{
				case SESSION_UNDEFINED:
				case SESSION_RUNNING:
				case SESSION_STOPPED:
				case SESSION_BUSY:
					throw new WFPanicException("EAAS is in an illegal state at this point");

				case SESSION_ALLOCATING:
					break;

				case SESSION_READY:
					initialized = true;
					continue;

				case SESSION_CLIENT_FAULT:
					throw new WFPanicException("Client has specified illegal input data to EAAS");

				case SESSION_FAILED:
					throw new WFPanicException("An internal error occured in EAAS, remote state");

				case SESSION_OUT_OF_RESOURCES:
					throw new WFPanicException("EAAS is out of resources, please try again later");
			}
			
			try {
				Thread.sleep(100);
			}
			catch (InterruptedException exception) {
				exception.printStackTrace();
			}	
		}
		
		if (!initialized)
			throw new WFPanicException("EAAS session was not ready after a predefined timeout");
	}
}
