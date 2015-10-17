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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import de.bwl.bwfla.api.eaas.ConnectionType;
import de.bwl.bwfla.api.eaas.EaasWS;
import de.bwl.bwfla.common.datatypes.EaasState;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.EmulatorUtils;
import de.bwl.bwfla.workflows.api.WorkflowResource;
import de.bwl.bwfla.workflows.conf.WorkflowSingleton;


public abstract class AbstractRemoteEmulatorHelper implements WorkflowResource
{
	protected static final Logger LOG = Logger.getLogger(AbstractRemoteEmulatorHelper.class.getName());

	protected String sessid;
	protected EaasWS eaas;

	public AbstractRemoteEmulatorHelper()
	{
		this.sessid = null;
		this.eaas = null;
	}
	
	public void initialize()
	{			
		try {
			URL wsdl = new URL(WorkflowSingleton.CONF.eaasGw);
			eaas = EmulatorUtils.getEaas(wsdl);
			if (eaas == null)
				throw new WFPanicException("EAAS is null");
		}
		catch (MalformedURLException exception) {
			exception.printStackTrace();
			throw new WFPanicException("Eaas wsdl property is malformed");
		}
	}

	public void cleanup()
	{
		if(eaas == null)
		{
			LOG.severe("cleanup: eaas is null: this is a bug!");
			return;
		}
		
		eaas.releaseSession(sessid);
		
		this.sessid  = null;
		this.eaas 	= null;
	}

	public void startEmulator() throws BWFLAException
	{
		eaas.start(sessid);
	}
	
	public void stop()
	{
		try {
			final String state = eaas.getSessionState(sessid);
			if (EaasState.fromValue(state) != EaasState.SESSION_RUNNING)
				return;
			
			eaas.stop(sessid);
		}
		catch(BWFLAException exception) {
			exception.printStackTrace();
		}
	}

	public boolean isRunning() throws BWFLAException
	{	
		final String state = eaas.getSessionState(sessid);
		return state.equalsIgnoreCase(EaasState.SESSION_RUNNING.value());
	}

	public synchronized EaasState getEaasState()
	{
		EaasState state = null;
		if (eaas != null) {
			try {
				state = EaasState.fromValue(eaas.getSessionState(sessid));
			}
			catch (BWFLAException exception) {
				exception.printStackTrace();
			}
		}
		
		return state;
	}
	
	public String getSessionId()
	{
		return sessid;
	}

	public String getControlUrl() throws BWFLAException
	{
		try {
			return eaas.getControlURL(sessid, ConnectionType.HTML, null);
		}
		catch(Throwable e)
		{
			throw new BWFLAException(e);
		}
	}

	public EaasWS getEaasWS()
	{
		return eaas;
	}
	
	public boolean isOutOfResources()
	{
		return false;
	}
	
	public String saveEnvironment(String host, String name, String type) throws BWFLAException
	{
		String environment = null;
		environment = eaas.saveEnvironment(sessid, host, name, type);
		return environment;
	}
	
	
	/* =============== Screenshot API =============== */
	
	public void takeScreenshot() throws BWFLAException
	{
		eaas.takeScreenshot(sessid);
	}
	
	public DataHandler getNextScreenshot() throws BWFLAException
	{
		return eaas.getNextScreenshot(sessid);
	}
}
