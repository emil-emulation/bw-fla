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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import de.bwl.bwfla.api.emucomp.EmulatorWS;
import de.bwl.bwfla.api.emucomp.EmulatorWSService;
import de.bwl.bwfla.api.emucomp.NetworkSwitchWS;
import de.bwl.bwfla.api.emucomp.NetworkSwitchWSService;
import de.bwl.bwfla.common.datatypes.EmulationNode;
import de.bwl.bwfla.common.datatypes.Network;
import de.bwl.bwfla.common.datatypes.NetworkEnvironment;
import de.bwl.bwfla.common.datatypes.NetworkNode;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.exceptions.OutOfResourcesException;
import de.bwl.bwfla.common.utils.NetworkUtils;


public class NetworkBuilder implements Callable<NetworkBuildResult>
{	
	private static final Logger			LOG 			= Logger.getLogger(Network.class.getName());
	private static ExecutorService	 	threadPool 		= Executors.newCachedThreadPool();
	private static EmulatorWS    		emulatorPort;
	private static NetworkSwitchWS 		switchPort;

	private final  NetworkEnvironment	env;
	private final  Network				network;
	private final  String				clientTemplate;
	
	static
	{
		try
		{
			final String THIS_HOST 	= NetworkUtils.getHostWSAddress().getHostAddress();
			final String emulWsdl   = "http://" + THIS_HOST + ":8080/eaas-ejb/EmulatorWSService/EmulatorProxyWS?wsdl";
			final String switchWsdl = "http://" + THIS_HOST + ":8080/eaas-ejb/NetworkSwitchWSService/NetworkSwitchProxyWS?wsdl";
			
			EmulatorWSService emulService = new EmulatorWSService(new URL(emulWsdl));
			emulatorPort = emulService.getEmulatorWSPort();
			 
			NetworkSwitchWSService netService = new NetworkSwitchWSService(new URL(switchWsdl));
			switchPort = netService.getNetworkSwitchWSPort();
		}
		catch(MalformedURLException e)
		{
			LOG.severe("url of network switch and/or emulator is malformed, behavior of this object will be undefined, check the value of 'emulatorPort' and/or 'switchPort' member-field");
		} catch (Exception e) {
            LOG.severe(e.getMessage());
            e.printStackTrace();
        }
	}

	public NetworkBuilder(NetworkEnvironment env, Network network, String clientTemplate)
	{
		this.env = env;
		this.network = network;
		this.clientTemplate = clientTemplate;
	}
	
	public NetworkBuildResult buildSync() throws OutOfResourcesException, IllegalArgumentException, BWFLAException
	{	
		return this.call();
	}
	
	public Future<NetworkBuildResult> buildAsync()
	{
		return threadPool.submit(this);
	}
	
	@Override
	public NetworkBuildResult call() throws OutOfResourcesException, IllegalArgumentException, BWFLAException
	{
		NetworkBuildResult netBuildResult = new NetworkBuildResult(); 
		
		NodeBuilder switchNode = new NodeBuilder("networkswitch", env);
		String switchId = switchNode.buildSync();
		netBuildResult.componentIds.add(switchId);
		
		Map<Future<String>, NetworkNode> components = new HashMap<>();
		ClientPair	 					 client	    = null;
		
		for(NetworkNode node: network.getEmulator())
		{
			if(!(node instanceof EmulationNode))
			{
				LOG.warning("unknown node type " + node.getClass().getCanonicalName());
				continue;
			}
	
			EmulationNode emuNode = (EmulationNode) node;
			boolean isTemplate = emuNode.isTemplate() != null && emuNode.isTemplate().booleanValue();
			
			if(!isTemplate || emuNode.getEmulationEnvironment().getId().equals(clientTemplate))
			{										
				Future<String> emuNodeId = (new NodeBuilder("emulator", emuNode.getEmulationEnvironment())).buildAsync();
					
				if(isTemplate)
					client = new ClientPair(emuNodeId, node);
				else
					components.put(emuNodeId, node);
			}
		}
		
		try
		{
			String clientId = client.id.get();
			emulatorPort.connectNic(clientId, client.node.getHwaddress(), switchPort.getNetworkEndpoint(switchId));
			netBuildResult.clientId = clientId;
		
	        for(Map.Entry<Future<String>, NetworkNode> entry: components.entrySet())
	        {
	        	Future<String> component = entry.getKey();
	            String componentId = component.get();
	            NetworkNode compNode = entry.getValue();
	            emulatorPort.connectNic(componentId, compNode.getHwaddress(), switchPort.getNetworkEndpoint(switchId));
	            emulatorPort.start(componentId);
	            netBuildResult.componentIds.add(componentId);
	        }
        
		}
		catch(BWFLAException | InterruptedException e)
		{
			e.printStackTrace();
			throw new BWFLAException();
		}
		catch(ExecutionException e)
		{
			Throwable cause = e.getCause();
			
			if(cause instanceof OutOfResourcesException)
				throw (OutOfResourcesException) cause;
			
			if(cause instanceof IllegalArgumentException)
				throw (IllegalArgumentException) cause;
			
			if(cause instanceof BWFLAException)
				throw (BWFLAException) cause;
			
			e.printStackTrace();
			throw new BWFLAException();
		} 
		
		return netBuildResult;
	}
}