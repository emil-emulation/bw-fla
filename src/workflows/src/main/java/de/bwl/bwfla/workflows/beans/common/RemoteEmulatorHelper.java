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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import de.bwl.bwfla.common.datatypes.Drive;
import de.bwl.bwfla.common.datatypes.EaasState;
import de.bwl.bwfla.common.datatypes.EmulationEnvironment;
import de.bwl.bwfla.common.datatypes.Environment;
import de.bwl.bwfla.common.datatypes.RemoteProcessMonitor;
import de.bwl.bwfla.common.datatypes.Drive.DriveType;
import de.bwl.bwfla.common.datatypes.utils.EmulationEnvironmentHelper;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.services.container.helpers.ContainerHelper;
import de.bwl.bwfla.common.services.container.helpers.ContainerHelperFactory;
import de.bwl.bwfla.common.services.container.helpers.ImageFileHelper;
import de.bwl.bwfla.common.services.container.types.CdromContainer;
import de.bwl.bwfla.common.services.container.types.Container;
import de.bwl.bwfla.common.services.container.types.Container.Filesystem;
import de.bwl.bwfla.common.services.container.types.HddZipContainer;
import de.bwl.bwfla.common.services.container.types.ImageFileContainer;
import de.bwl.bwfla.common.utils.Pair;
import de.bwl.bwfla.common.utils.Zip32Utils;


public class RemoteEmulatorHelper extends AbstractRemoteEmulatorHelper
{
	
	private RemoteProcessMonitor monitor;

	public static RemoteEmulatorHelper createRemoteEmuFromConfig(String config)
	{
		try 
		{
			Environment env = Environment.fromValue(config);
			return new RemoteEmulatorHelper(env);
		} 
		catch(JAXBException e) 
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public RemoteEmulatorHelper(Environment env)
	{
		super(env);

	
		this.monitor = null;
		this.mediaManager = new EmulatorMediaManager(env);
	}
	
	public void initialize()
	{
		final int TIMEOUT_IN_SEC = 15;
		
		try
		{
			super.initialize();
			
			this.sessId = this.eaas.createSession(environment.value(), null, "9999");
			if(sessId == null)
				throw new WFPanicException("EAAS has answered with a newly created session id as 'null', will not proceed");
			
			mediaManager.setEmulatorConnection(eaas, sessId);
			
			EaasState state = null; 
			loop: for(int i = 0, DELAY_MS = 100; i < TIMEOUT_IN_SEC * DELAY_MS; ++i)
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
						break loop;
				}			
			}

			if(state != EaasState.SESSION_READY)
				throw new WFPanicException("EAAS session not allocated after a predefined workflow timeout, last known remote state: " + state.value());
			
			this.monitor = new RemoteProcessMonitor(sessId, eaas);
		}
		catch(Throwable t)
		{
			t.printStackTrace();
			
			if(this.sessId != null && this.eaas != null)
				this.eaas.releaseSession(this.sessId);
			
			if(t instanceof JAXBException)
				throw new WFPanicException("environmnent metadata is malformed");
			
			throw new WFPanicException("web-interface module has experienced an internal error, please try again later");
		}
	}	

	public RemoteProcessMonitor getProcessMonitor()
	{
		return monitor;
	}
	
	synchronized public void cleanup()
	{
		super.cleanup();
		
		this.monitor = null;
		mediaManager.cleanup();
	}

	synchronized public void startEmulator() throws BWFLAException
	{	
		if(this.cleanedUp)
			throw new BWFLAException("cannot perform operation, since this session was already removed");
		
		if(eaas == null)
		{
			LOG.warning("saveEnvironment: eaas is null, will not proceed (session either was remove before or not initialized yet)");
			return;
		}
		mediaManager.attachLocalFiles();
		super.startEmulator();
	}
}
