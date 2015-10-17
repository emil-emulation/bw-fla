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

package de.bwl.bwfla.eaas.allocation.spi.plugins.googlecloud;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.compute.Compute;
import com.google.api.services.compute.ComputeScopes;
import com.google.api.services.compute.model.AccessConfig;
import com.google.api.services.compute.model.AttachedDisk;
import com.google.api.services.compute.model.AttachedDiskInitializeParams;
import com.google.api.services.compute.model.Firewall;
import com.google.api.services.compute.model.Firewall.Allowed;
import com.google.api.services.compute.model.Instance;
import com.google.api.services.compute.model.MachineType;
import com.google.api.services.compute.model.Network;
import com.google.api.services.compute.model.NetworkInterface;
import com.google.api.services.compute.model.NetworkList;
import com.google.api.services.compute.model.Operation;
import com.google.api.services.compute.model.Operation.Error;
import com.google.api.services.compute.model.Scheduling;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.exceptions.OutOfResourcesException;
import de.bwl.bwfla.eaas.allocation.ComputeInstance;
import de.bwl.bwfla.eaas.allocation.IPlugin;
import de.bwl.bwfla.eaas.allocation.ResourceSpecs;
import de.bwl.bwfla.eaas.allocation.spi.plugins.ComputeNode;
import de.bwl.bwfla.eaas.conf.GoogleConnection;


@XmlRootElement
@XmlSeeAlso(GoogleConnection.class)
public class GoogleCloudPlugin extends IPlugin
{	
	private static final long	serialVersionUID	= 9016584055249714496L;
	private static final Logger						LOG				= Logger.getLogger(GoogleCloudPlugin.class.getSimpleName());
	
	private GoogleConnection						googleConnection;
	private Compute									googleCompute;
	private Network									network;

	private final AtomicLong						machineIndx		= new AtomicLong(0);
	private final List<ComputeNode>					activeNodes		= Collections.synchronizedList(new ArrayList<ComputeNode>());
	private final Map<ComputeInstance, ComputeNode>	instanceToNode	= Collections.synchronizedMap(new HashMap<ComputeInstance, ComputeNode>());
	private final Timer								healthChecker	= new Timer();
	
	@Override
	public void run()
	{	
		Iterator<ComputeNode> it = GoogleCloudPlugin.this.activeNodes.iterator();
		ComputeNode node = null;
		
		while(it.hasNext())
			try
			{
				node = it.next();
				URL wsdl = new URL(node.getAddress() + "/EaasComponentWS?wsdl");
				
				boolean reachable = GoogleCloudPlugin.wsReachable(wsdl);
				for(int i = 0, MAX_TRIES = 10; !reachable && i < MAX_TRIES; ++i)
				{
					reachable = GoogleCloudPlugin.wsReachable(wsdl);
					Thread.sleep(200);
				}
				
				if(!reachable)
				{
					this.unregisterNode(node);

					for(ComputeInstance instance : node.getComputeInstances())
						this.instanceToNode.remove(instance);
					
					it.remove();
					LOG.warning("removed malfunctioning node from the node-pool: " + node.getAddress());
				}
			}
			catch(BWFLAException e)
			{
				LOG.warning("unable to unregister the node, perhaps it has been shutdown previously and/or doesn't exist anymore " + node.getAddress());
			}
			catch(MalformedURLException e)
			{
				e.printStackTrace();
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
	}
	
	private static boolean wsReachable(URL wsdl) throws BWFLAException
	{
		HttpURLConnection httpConn = null;
		
		try
		{
			httpConn = (HttpURLConnection) wsdl.openConnection();
			httpConn.setConnectTimeout(1000);
			return httpConn.getResponseCode() == HttpURLConnection.HTTP_OK;
		}
		catch(IOException e)
		{
			return false;
		}
		finally
		{
			if(httpConn != null)
				httpConn.disconnect();
		}
	}

	public GoogleCloudPlugin(Object connection)
	{	
		if(!(connection instanceof GoogleConnection))
			throw new IllegalArgumentException("passed connection object is of type: '" + connection.getClass().getCanonicalName() + "' , whereas expected type is: '" + GoogleConnection.class.getCanonicalName() + "'");
		
		try
		{	
			this.googleConnection = (GoogleConnection) connection;
			NetHttpTransport transporter = new NetHttpTransport();
			JsonFactory	JSON_FACTORY = JacksonFactory.getDefaultInstance();
			GoogleCredential credentials = new GoogleCredential.Builder().setTransport(transporter).setJsonFactory(JSON_FACTORY).setServiceAccountId(googleConnection.accountId).setServiceAccountScopes(Collections.singleton(ComputeScopes.COMPUTE)).setServiceAccountPrivateKeyFromP12File(new File(googleConnection.certificate)).build();
			credentials.refreshToken();
			this.googleCompute = new Compute.Builder(transporter, JSON_FACTORY, null).setApplicationName(GoogleCloudPlugin.class.getSimpleName()).setHttpRequestInitializer(credentials).build();
			this.network = this.createBwFlaNetworkIfAbsent();
			
			int HEALTH_CHECK_PERIOD_MILLIS = 30 * 1000;
			this.healthChecker.scheduleAtFixedRate(this, HEALTH_CHECK_PERIOD_MILLIS, HEALTH_CHECK_PERIOD_MILLIS);
		}
		catch(GeneralSecurityException | IOException | BWFLAException e)
		{			
			e.printStackTrace();
		}
	}

	private void waitForRemoteOperation(Operation operation, int TIMEOUT_SEC) throws BWFLAException
	{
		final String OP_NAME = operation.getName();
		final boolean zoneIsSet = operation.getZone() != null && !operation.getZone().isEmpty();
		
		for(int i = 0; i < TIMEOUT_SEC; ++i)
			try
			{	
				if(operation == null)
					throw new BWFLAException("failed to query operation status, returned object is null, exiting");
				else if(operation.getStatus().equals("DONE"))
					break;
				
				if(zoneIsSet)
					operation = googleCompute.zoneOperations().get(googleConnection.project, googleConnection.zone, OP_NAME).execute();
				else
					operation = googleCompute.globalOperations().get(googleConnection.project, OP_NAME).execute();
				
				Thread.sleep(1000);
			}
			catch(IOException | InterruptedException e)
			{
				throw new BWFLAException(e.getMessage());
			}
		
			if(!operation.getStatus().equals("DONE"))
				throw new BWFLAException("gave-up waiting for the " + operation.getName() + "' operation, after predefined timeout");
		
			Error error = operation.getError();
			if(error != null)
				throw new BWFLAException("failed to execute the '" + operation.getName() + "' operation" + ", reason: " + error.toString());				
	}
	
	synchronized private Network createBwFlaNetworkIfAbsent() throws BWFLAException
	{
		try
		{
			final String BWFLA_NETWORK = "bwfla-network";
			NetworkList networks = googleCompute.networks().list(googleConnection.project).execute();
			
			if(networks != null && networks.getItems() != null)
				for(Network network: networks.getItems())
					if(network.getName().equalsIgnoreCase(BWFLA_NETWORK))
						return network;
			
			Network newNetwork = new Network();
			newNetwork.setName(BWFLA_NETWORK);
			newNetwork.setIPv4Range("10.240.0.0/16");
			Operation networkOp = googleCompute.networks().insert(googleConnection.project, newNetwork).execute();
			this.waitForRemoteOperation(networkOp, 400);
			newNetwork = googleCompute.networks().get(googleConnection.project, BWFLA_NETWORK).execute();
					
			Allowed allowed = new Allowed();
			allowed.setIPProtocol("tcp");
			allowed.setPorts(new ArrayList<String>(Arrays.asList("22", "80", "443", "8080", "4822")));
			Firewall firewall = new Firewall().setName(BWFLA_NETWORK + "-firewall");
			firewall.setSourceRanges(new ArrayList<String>(Arrays.asList("0.0.0.0/0")));
			firewall.setAllowed(new ArrayList<Allowed>(Arrays.asList(allowed)));
			firewall.setNetwork(newNetwork.getSelfLink());
			Operation firewallOp = googleCompute.firewalls().insert(googleConnection.project, firewall).execute();
			this.waitForRemoteOperation(firewallOp, 400);
			
			return newNetwork;
		}
		catch(IOException e)
		{
			throw new BWFLAException(e.getMessage());
		}
	}

	private String genUniqueMachineName()
	{	
		String uuid = UUID.randomUUID().toString();
		return "bwfla-machine-" + uuid + "-" + machineIndx.getAndIncrement();
	}

	private ComputeNode registerNode() throws BWFLAException
	{		
		final long HDD_SIZE_GB = 10;
		String VM_NAME = this.genUniqueMachineName();
		Instance instance = new Instance().setName(VM_NAME);
		
		AttachedDiskInitializeParams iParams = new AttachedDiskInitializeParams();
		iParams.setDiskName(VM_NAME).setDiskSizeGb(HDD_SIZE_GB).setSourceImage(googleConnection.osImage);
		AttachedDisk attDisk = new AttachedDisk().setBoot(true).setType("PERSISTENT").setAutoDelete(Boolean.TRUE).setMode("READ_WRITE").setInitializeParams(iParams);
		instance.setDisks(new ArrayList<AttachedDisk>(Arrays.asList(attDisk)));
		
		Scheduling scheduling = new Scheduling();
		scheduling.setAutomaticRestart(true);
		scheduling.setOnHostMaintenance("MIGRATE");
		instance.setScheduling(scheduling);
		
		try
		{
			MachineType machineType = googleCompute.machineTypes().get(googleConnection.project, googleConnection.zone, googleConnection.machine).execute();
			instance.setMachineType(machineType.getSelfLink());
			
			NetworkInterface networkInterface = (new NetworkInterface()).setNetwork(network.getSelfLink());
			AccessConfig accessConfig = new AccessConfig();
			accessConfig.setType("ONE_TO_ONE_NAT");
			networkInterface.setAccessConfigs(new ArrayList<AccessConfig>(Arrays.asList(accessConfig)));
			
			instance.setCanIpForward(false).setNetworkInterfaces(new ArrayList<NetworkInterface>(Arrays.asList(networkInterface)));
			Operation createInstance = googleCompute.instances().insert(googleConnection.project, googleConnection.zone, instance).execute();
			this.waitForRemoteOperation(createInstance, 400);
			instance = googleCompute.instances().get(googleConnection.project, googleConnection.zone, instance.getName()).execute();

			final String PROTOCOL = "http";
			final int PORT = 8080;
			final String IP = instance.getNetworkInterfaces().get(0).getAccessConfigs().get(0).getNatIP();
			URL serverUrl = new URL(PROTOCOL + "://" + IP + ":" + PORT + "/emucomp");
			this.waitUntilServerIsUp(serverUrl, 400);
			
			ResourceSpecs specs = new ResourceSpecs();
			specs.setNumCpuCores(machineType.getGuestCpus());
			specs.setRamCapacity(machineType.getMemoryMb());
			specs.setDiskCapacity(attDisk.getInitializeParams().getDiskSizeGb().intValue());
			return new ComputeNode(VM_NAME, serverUrl, specs);
		}
		catch(IOException e)
		{
			throw new BWFLAException(e.getMessage());
		}
	}
	
	private void waitUntilServerIsUp(URL serverUrl, int TIMEOUT_SEC) throws BWFLAException
	{		
		final int SLEEP_TIME_MS = 100;
		final int MAX_TRIES = ((int) ((TIMEOUT_SEC * 1000) / (float) SLEEP_TIME_MS));
		
		for(int i = 0; i < MAX_TRIES; ++i)
           	try
			{
				if(GoogleCloudPlugin.wsReachable(new URL(serverUrl + "/EaasComponentWS?wsdl")))
					return;
				else
					Thread.sleep(SLEEP_TIME_MS);
			}
			catch(MalformedURLException | InterruptedException e)
			{
				e.printStackTrace();
				throw new BWFLAException("a code error occured leading to operation failure");
			}
           	
		
		throw new BWFLAException("gave up waiting for the application server at this address to boot-up: " + serverUrl);
	}
	
	synchronized private void unregisterNode(ComputeNode node) throws BWFLAException
	{
		try
		{	
			googleCompute.instances().delete(googleConnection.project, googleConnection.zone, node.getName()).execute();
		}
		catch(IOException e)
		{
			throw new BWFLAException(e.getMessage());
		}
	}
	
	@Override
	public ComputeInstance getComputeInstance(ResourceSpecs specs) throws OutOfResourcesException
	{
		try
		{
			ComputeNode node = this.registerNode();
			this.activeNodes.add(node);
			
			specs.setNumCpuCores(0f);
			specs.setRamCapacity(0);
			specs.setDiskCapacity(0);
			
			ComputeInstance instance = node.reserveResources(specs);
			this.instanceToNode.put(instance, node);			
			return instance;
		}
		catch(BWFLAException e)
		{
			e.printStackTrace();
		}
		
 		throw new OutOfResourcesException();
	}

	@Override
	public void freeComputeInstance(ComputeInstance computeInstance)
	{
		try
		{
			ComputeNode node = instanceToNode.remove(computeInstance);
			
			if(node != null)
			{
				this.activeNodes.remove(node);
				this.unregisterNode(node);
			}				
		}
		catch(BWFLAException e)
		{
			e.printStackTrace();
		}
	}
	
	
	//////////// *********** NEXT THREE METHODS ARE FOR JAX-B ONLY, WILL ALL BE REMOVED WHEN USER-MANAGEMENT IS IMPLEMENTED *********** /////////// 
	
	@SuppressWarnings("unused")
	private GoogleCloudPlugin()
	{
		// XXX: no code, please leave empty
	}
	
	@XmlElement
	public GoogleConnection getGoogleConnection()
	{
		return googleConnection;
	}

	public void setGoogleConnection(GoogleConnection googleConnection)
	{
		this.googleConnection = googleConnection;
		this.initialize(this.googleConnection);
	}

	private void initialize(GoogleConnection googleConnection)
	{	 
		try
		{	
			NetHttpTransport transporter = new NetHttpTransport();
			JsonFactory	JSON_FACTORY = JacksonFactory.getDefaultInstance();
			GoogleCredential credentials = new GoogleCredential.Builder().setTransport(transporter).setJsonFactory(JSON_FACTORY).setServiceAccountId(googleConnection.accountId).setServiceAccountScopes(Collections.singleton(ComputeScopes.COMPUTE)).setServiceAccountPrivateKeyFromP12File(new File(googleConnection.certificate)).build();
			credentials.refreshToken();
			this.googleCompute = new Compute.Builder(transporter, JSON_FACTORY, null).setApplicationName(GoogleCloudPlugin.class.getSimpleName()).setHttpRequestInitializer(credentials).build();
			this.network = this.createBwFlaNetworkIfAbsent();

			int HEALTH_CHECK_PERIOD_MILLIS = 30 * 1000;
			this.healthChecker.scheduleAtFixedRate(this, HEALTH_CHECK_PERIOD_MILLIS, HEALTH_CHECK_PERIOD_MILLIS);
		}
		catch(GeneralSecurityException | IOException | BWFLAException e)
		{			
			e.printStackTrace();
		}
	}

	//////////// ******************************************************************************************************************** /////////////
}