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

package de.bwl.bwfla.eaas.sessions.internal;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import de.bwl.bwfla.api.cluster.EmuSession;
import de.bwl.bwfla.api.cluster.ResourceAllocatorWS;
import de.bwl.bwfla.api.cluster.ResourceAllocatorWSService;
import de.bwl.bwfla.api.cluster.ResourceState;
import de.bwl.bwfla.common.datatypes.Environment;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.exceptions.OutOfResourcesException;
import de.bwl.bwfla.common.utils.NetworkUtils;



public class NodeBuilder implements Callable<String>
{
	private static final Logger			LOG 			= Logger.getLogger(NodeBuilder.class.getName());
	private static ResourceAllocatorWS 	resourcePort;
	private static ExecutorService	 	threadPool;
	
	private final  String		componentType;
	private final  Environment	env;
	
	static
	{
		try
		{
			final String THIS_HOST 	= NetworkUtils.getHostWSAddress().getHostAddress();
			final String resWsdl = "http://" + THIS_HOST + ":8080/eaas-ejb/ResourceAllocatorWS?wsdl";
			ResourceAllocatorWSService resService = new ResourceAllocatorWSService(new URL(resWsdl));
			resourcePort = resService.getResourceAllocatorWSPort();
			threadPool = Executors.newCachedThreadPool();
		}
		catch(MalformedURLException e)
		{
			LOG.severe("url of resource allocator is malformed, behavior of this object will be undefined, check the value of 'resWsdl' member-field");
		} catch (Exception e) {
		    LOG.severe(e.getMessage());
		    e.printStackTrace();
		}
	}

	public NodeBuilder(String componentType, Environment env)
	{
		this.componentType = componentType;
		this.env = env;
	}
	
	public String buildSync() throws OutOfResourcesException, IllegalArgumentException, BWFLAException
	{	
		return this.call();
	}
	
	public Future<String> buildAsync()
	{
		return threadPool.submit(this);
	}
	
	@Override
	public String call() throws OutOfResourcesException, IllegalArgumentException, BWFLAException
	{		
		String clusterId;
		
		try
		{
			clusterId = resourcePort.allocate(componentType, env.value());
		}
		catch(JAXBException e)
		{
			throw new IllegalArgumentException();
		}
		
		EmuSession clusterSession = resourcePort.getInternalSession(clusterId);
		
		ResourceState lastState = ResourceState.RESOURCE_ALLOCATING;
		for(int tries = 0, MAX_TRIES = 1500; tries < MAX_TRIES; ++tries)
		{
			ResourceState state = clusterSession.getState();
			lastState = state;
			switch(state)
			{
				case RESOURCE_ALLOCATING:
					break;
					
				case RESOURCE_READY:
					return clusterId;
					
				case RESOURCE_RELEASED:
					throw new IllegalStateException();
					
				case OUT_OF_RESOURCES:
					throw new OutOfResourcesException();
					
				case CLIENT_FAULT:
					throw new IllegalArgumentException();
				
				default:
					LOG.info("state: " + state);
			}
			
			try
			{
				Thread.sleep(100);
				clusterSession = resourcePort.getInternalSession(clusterId);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		
		if(clusterId != null)
			resourcePort.release(clusterId);
		
		throw new BWFLAException("resource allocation timed out, aborting operation: " + lastState);
	}
}
