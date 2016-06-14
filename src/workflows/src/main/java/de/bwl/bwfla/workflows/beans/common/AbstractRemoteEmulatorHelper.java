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
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.activation.DataHandler;

import de.bwl.bwfla.api.eaas.ConnectionType;
import de.bwl.bwfla.api.eaas.EaasWS;
import de.bwl.bwfla.common.datatypes.Drive;
import de.bwl.bwfla.common.datatypes.EaasState;
import de.bwl.bwfla.common.datatypes.EmulationEnvironment;
import de.bwl.bwfla.common.datatypes.Environment;
import de.bwl.bwfla.common.datatypes.Drive.DriveType;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.EmulatorUtils;
import de.bwl.bwfla.workflows.api.WorkflowResource;
import de.bwl.bwfla.workflows.conf.WorkflowSingleton;


public abstract class AbstractRemoteEmulatorHelper implements WorkflowResource
{
	protected static final Logger LOG = Logger.getLogger(AbstractRemoteEmulatorHelper.class.getName());
	protected String 	sessId;
	protected EaasWS 	eaas;
	protected boolean	cleanedUp;
	protected EmulatorMediaManager mediaManager;
	protected final Environment environment;
	

	public AbstractRemoteEmulatorHelper(Environment env)
	{
		this.sessId = null;
		this.eaas = null;
		this.cleanedUp = false;
		this.mediaManager = null;
		environment = env;
	}
	
	public void initialize()
	{			
		try
		{
			URL wsdl = new URL(WorkflowSingleton.CONF.eaasGw);
			
			eaas = EmulatorUtils.getEaas(wsdl);
			if(eaas == null)
				throw new WFPanicException("EAAS is null");
		}
		catch(MalformedURLException exception)
		{
			exception.printStackTrace();
			throw new WFPanicException("Eaas wsdl property is malformed");
		}
	}

	public void cleanup()
	{
		if(eaas == null)
		{
			LOG.warning("cleanup: eaas is null, will not proceed (session either was removed before or not initialized yet), cleanedUp is " + cleanedUp);
			return;
		}
		
		this.eaas.releaseSession(sessId);
		this.sessId 	 = null;
		this.eaas 		 = null;
		this.cleanedUp   = true;
	}
	
	public void startEmulator() throws BWFLAException
	{
		if(this.cleanedUp)
			throw new BWFLAException("cannot perform operation, since this session was already removed");
		
		eaas.start(sessId);
	}
	
	public void stop() throws BWFLAException
	{
		if(this.cleanedUp)
			throw new BWFLAException("cannot perform operation, since this session was already removed");
		
		try 
		{
			final String state = eaas.getSessionState(sessId);
			if (EaasState.fromValue(state) != EaasState.SESSION_RUNNING)
				return;
			
			eaas.stop(sessId);
		}
		catch(BWFLAException exception) 
		{
			exception.printStackTrace();
		}
	}

	public synchronized EaasState getEaasState()
	{	
		if(this.cleanedUp)
		{
			LOG.warning("cannot perform this operation, since this session was already removed");
			return null;
		}
		
		EaasState state = null;
		if (eaas != null) {
			try {
				state = EaasState.fromValue(eaas.getSessionState(sessId));
			}
			catch (BWFLAException exception) {
				exception.printStackTrace();
			}
		}
		
		return state;
	}
	
	public String getControlUrl() throws BWFLAException
	{
		if(this.cleanedUp)
			throw new BWFLAException("cannot perform operation, since this session was already removed (timeout?)");
		
		try 
		{
			return eaas.getControlURL(sessId, ConnectionType.HTTP, null);
		}
		catch(Throwable e)
		{
			throw new BWFLAException(e);
		}
	}

	public String saveEnvironment(String host, String name, String type) throws BWFLAException
	{
		if(this.cleanedUp)
			throw new BWFLAException("cannot perform operation, since this session was already removed (timeout?)");
		
		String environment = null;
		environment = eaas.saveEnvironment(sessId, host, name, type);
		return environment;
	}
		
	public void takeScreenshot() throws BWFLAException
	{
		if(this.cleanedUp)
			throw new BWFLAException("cannot perform operation, since this session was already removed (timeout?)");
		
		eaas.takeScreenshot(sessId);
	}
	
	public DataHandler getNextScreenshot() throws BWFLAException
	{
		if(this.cleanedUp)
			throw new BWFLAException("cannot perform operation, since this session was already removed (timeout?)");
		
		return eaas.getNextScreenshot(sessId);
	}
	
	public EaasWS getEaasWS()
	{
		return eaas;
	}
	
	public String getSessionId()
	{
		return sessId;
	}
	
	public EmulatorMediaManager getMediaManager()
	{
		return mediaManager;
	}
	
	public EmulationEnvironment getEmulationEnvironment()
	{
		if(environment == null)
			return null;
		
		if(environment instanceof EmulationEnvironment)
			return (EmulationEnvironment)environment;
		
		return null;
	}
	
	public boolean requiresUserPrefs()
	{
		if(environment == null)
			return false;
		
		if(!(environment instanceof EmulationEnvironment))
			return false;
		
		EmulationEnvironment env = (EmulationEnvironment)environment;
		if(env != null && env.getUiOptions() != null && env.getUiOptions().getInput() != null && env.getUiOptions().getInput().isRequired())
			return true;
		return false;
	}
}
