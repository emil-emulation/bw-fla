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

package de.bwl.bwfla.eaas.sessions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.MTOM;
import javax.xml.ws.soap.SOAPBinding;
import de.bwl.bwfla.api.cluster.EmuSession;
import de.bwl.bwfla.api.cluster.ResourceAllocatorWS;
import de.bwl.bwfla.api.cluster.ResourceAllocatorWSService;
import de.bwl.bwfla.api.emucomp.AbstractCredentials;
import de.bwl.bwfla.api.emucomp.ConnectionType;
import de.bwl.bwfla.api.emucomp.EmulatorWS;
import de.bwl.bwfla.api.emucomp.EmulatorWSService;
import de.bwl.bwfla.common.datatypes.EaasState;
import de.bwl.bwfla.common.datatypes.EmulationEnvironment;
import de.bwl.bwfla.common.datatypes.Environment;
import de.bwl.bwfla.common.datatypes.Network;
import de.bwl.bwfla.common.datatypes.NetworkEnvironment;
import de.bwl.bwfla.common.datatypes.ProcessMonitorVID;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.exceptions.OutOfResourcesException;
import de.bwl.bwfla.common.utils.NetworkUtils;
import de.bwl.bwfla.eaas.sessions.internal.NetworkBuildResult;
import de.bwl.bwfla.eaas.sessions.internal.NetworkBuilder;
import de.bwl.bwfla.eaas.sessions.internal.NodeBuilder;


@MTOM
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@WebService(targetNamespace = "http://bwfla.bwl.de/api/eaas")
public class EaasWS
{
	private static final Logger			LOG	= Logger.getLogger(EaasWS.class.getName());	
	private static EmulatorWS			emulatorPort;
    private static ResourceAllocatorWS	resourcePort;
	
	@EJB
	private SessionBean sessions;
	
	@PostConstruct
	private void postConstruct()
	{
		final String THIS_HOST   = NetworkUtils.getHostAddress().getHostAddress();
		final String emulWsdl    = "http://" + THIS_HOST + ":8080/eaas-ejb/EmulatorWSService/EmulatorProxyWS?wsdl";
		final String resWsdl     = "http://" + THIS_HOST + ":8080/eaas-ejb/ResourceAllocatorWS?wsdl";
	
		try
		{
			EmulatorWSService emulService = new EmulatorWSService(new URL(emulWsdl));
			emulatorPort = emulService.getEmulatorWSPort();
		                       
			ResourceAllocatorWSService resService = new ResourceAllocatorWSService(new URL(resWsdl ));
			resourcePort = resService.getResourceAllocatorWSPort();
		}
		catch(MalformedURLException e)
		{
			e.printStackTrace();
		}
	}
	
	@WebMethod
	public String createSession(final String xmlConfig, final String sessionName, final String clientTemplate)
	{
		final Environment config;
		
		try
		{
			config = Environment.fromValue(xmlConfig);
		}
		catch(JAXBException e)
		{
			e.printStackTrace();
			throw new IllegalArgumentException();
		}

		final Session session = sessions.allocateSession(sessionName);
		session.state = EaasState.SESSION_ALLOCATING.value();
		
		new Thread()
		{
			@Override
			public void run()
			{	
				try
				{
					if(config instanceof EmulationEnvironment)
					{
						NodeBuilder node = new NodeBuilder("emulator", (EmulationEnvironment) config);
						Future<String> clusterIdFuture = node.buildAsync();
						String clusterId = clusterIdFuture.get();
						session.addClient(clusterId);
					}
					else if(config instanceof NetworkEnvironment)
					{ 	
						NetworkEnvironment env = (NetworkEnvironment) config;
						List<Future<NetworkBuildResult>> networkBuildResults = new ArrayList<>();
						
						for(final Network network: env.getNetwork())
						{
							NetworkBuilder net = new NetworkBuilder(env, network, clientTemplate);
							networkBuildResults.add(net.buildAsync());
						}
						
						for(Future<NetworkBuildResult> networkBuildResult: networkBuildResults)
						{
							NetworkBuildResult result = networkBuildResult.get();
							session.addClient(result.clientId);
									
							for(String componentId: result.componentIds)
								session.addComponent(componentId); 
						}
					}
					else
						throw new IllegalArgumentException("unsupported configuration type: " + config.getClass().getSimpleName());
					
					session.state = EaasState.SESSION_READY.value();
					LOG.info("Session " + session.getSessionId() + " created.");
				}
				catch(ExecutionException | InterruptedException e)
				{
					Throwable cause = e.getCause();
					
					if(cause == null)
					{	
						e.printStackTrace();
						session.state = EaasState.SESSION_FAILED.value();
					}
					else 
						if(cause instanceof OutOfResourcesException)
							session.state = EaasState.SESSION_OUT_OF_RESOURCES.value();
					else 
						if(cause instanceof IllegalArgumentException)
							session.state = EaasState.SESSION_CLIENT_FAULT.value();
					else
					{
						cause.printStackTrace();
						session.state = EaasState.SESSION_FAILED.value();
					}
					
					LOG.severe("Failed to allocate components for session " + session.getSessionId());
				}
			}
		}
		.start();
		
		return session.getSessionId(); 
	}

	@WebMethod
	public void releaseSession(String clientSessionId)
	{
		Session session = this.sessions.getSession(clientSessionId);
		if(session == null)
			throw new IllegalStateException("session for this client id has not been found in EAAS: " + clientSessionId);
		
		sessions.releaseSession(session, EaasWS.resourcePort);
		
		LOG.info("Session " + clientSessionId + " released. All components deallocated.");
		LOG.info("Session " + clientSessionId + " was active for " + session.getDurationInSeconds() + " second(s).");
	}

	public int changeMedium(String clientSessionId, int containerId, String reference) throws BWFLAException
	{
		Session session = sessions.getSession(clientSessionId);
		EmulatorWS port = EaasWS.emulatorPort;
		return port.changeMedium(session.getClientId(), containerId, reference);
	}
	
	public int attachMedium(String clientSessionId, @XmlMimeType("application/octet-stream") DataHandler data, String mediumType) throws BWFLAException
	{	
		Session session = sessions.getSession(clientSessionId);
		EmulatorWS port = EaasWS.emulatorPort;

		BindingProvider bp = (BindingProvider) port;
		SOAPBinding binding = (SOAPBinding) bp.getBinding();
		bp.getRequestContext().put("javax.xml.ws.client.receiveTimeout", "0");
		bp.getRequestContext().put("javax.xml.ws.client.connectionTimeout", "0");
		binding.setMTOMEnabled(true);
		bp.getRequestContext().put("com.sun.xml.internal.ws.transport.http.client.streaming.chunk.size", 8192);

		return port.attachMedium(session.getClientId(), data, mediumType);
	}

	public @XmlMimeType("application/octet-stream") DataHandler detachMedium(String clientSessionId, int handle) throws BWFLAException
	{
		Session session = sessions.getSession(clientSessionId);
		EmulatorWS port = EaasWS.emulatorPort;

		BindingProvider bp = (BindingProvider) port;
		SOAPBinding binding = (SOAPBinding) bp.getBinding();
		bp.getRequestContext().put("javax.xml.ws.client.receiveTimeout", "0");
		bp.getRequestContext().put("javax.xml.ws.client.connectionTimeout", "0");
		binding.setMTOMEnabled(true);
		bp.getRequestContext().put("com.sun.xml.internal.ws.transport.http.client.streaming.chunk.size", 8192);

		return port.detachMedium(session.getClientId(), handle);
	}

	public boolean start(final String clientSessionId) throws BWFLAException
	{
		Session session = sessions.getSession(clientSessionId); 
		EaasWS.emulatorPort.start(session.getClientId());		
		return true;
	}

	public boolean stop(final String clientSessionId) throws BWFLAException
	{
		Session session = sessions.getSession(clientSessionId);
		EaasWS.emulatorPort.stop(session.getClientId());
		return true;
	}

	public String getRuntimeConfiguration(String clientSessionId) throws BWFLAException
	{
		Session session = sessions.getSession(clientSessionId);
		EmulatorWS port = EaasWS.emulatorPort;
		return port.getRuntimeConfiguration(session.getClientId());
	}

	public List<String> getColdplugableDrives(String clientSessionId) throws BWFLAException
	{
		Session session = sessions.getSession(clientSessionId);
		EmulatorWS port = EaasWS.emulatorPort;
		return port.getColdplugableDrives(session.getClientId());
	}

	public List<String> getHotplugableDrives(String clientSessionId) throws BWFLAException
	{
		Session session = sessions.getSession(clientSessionId);
		EmulatorWS port = EaasWS.emulatorPort;
		return port.getHotplugableDrives(session.getClientId());
	}

	public String saveEnvironment(String clientSessionId, String wsHost, String name, String type) throws BWFLAException
	{
		Session session = sessions.getSession(clientSessionId);
		EmulatorWS port = EaasWS.emulatorPort;
		return port.saveEnvironment(session.getClientId(), wsHost, name, type);
	}

	public String getSessionState(String clientSessionId) throws BWFLAException
	{
		Session session = sessions.getSession(clientSessionId);

		if(session == null)
		{
			LOG.warning("client checking state of a non-existing session, specified client id: " + clientSessionId);
			return EaasState.SESSION_UNDEFINED.value();
		}
		
		if(!session.state.equalsIgnoreCase(EaasState.SESSION_READY.value()))
			return session.state;

		EmulatorWS port = EaasWS.emulatorPort;
		String emuState = port.getEmulatorState(session.getClientId()).toLowerCase();
		return EaasState.fromValue(emuState).value();
	}

	public void connectNic(String clientSessionId, String arg1, String arg2) throws BWFLAException
	{
		Session session = sessions.getSession(clientSessionId);
		EmulatorWS port = EaasWS.emulatorPort;
		port.connectNic(session.getClientId(), arg1, arg2);
	}

	public String getControlURL(String clientSessionId, ConnectionType type, AbstractCredentials credentials) throws BWFLAException
	{	
		Session session = sessions.getSession(clientSessionId);
		EmuSession internalSessoin = EaasWS.resourcePort.getInternalSession(session.getClientId());
		String nodeLocation = internalSessoin.getNodeLocation();
		EmulatorWS port = EaasWS.emulatorPort;
		return nodeLocation + port.getControlURL(session.getClientId(), type, credentials);
	}

	/* =============== Session recording API =============== */

	public boolean prepareSessionRecorder(String clientSessionId) throws BWFLAException
	{
		Session session = sessions.getSession(clientSessionId);
		EmulatorWS port = EaasWS.emulatorPort;
		return port.prepareSessionRecorder(session.getClientId());
	}

	public void startSessionRecording(String clientSessionId) throws BWFLAException
	{
		Session session = sessions.getSession(clientSessionId);
		EmulatorWS port = EaasWS.emulatorPort;
		port.startSessionRecording(session.getClientId());
	}

	public void stopSessionRecording(String clientSessionId) throws BWFLAException
	{
		Session session = sessions.getSession(clientSessionId);
		EmulatorWS port = EaasWS.emulatorPort;
		port.stopSessionRecording(session.getClientId());
	}

	public boolean isRecordModeEnabled(String clientSessionId) throws BWFLAException
	{
		Session session = sessions.getSession(clientSessionId);
		EmulatorWS port = EaasWS.emulatorPort;
		return port.isRecordModeEnabled(session.getClientId());
	}

	public void addActionFinishedMark(String clientSessionId) throws BWFLAException
	{
		Session session = sessions.getSession(clientSessionId);
		EmulatorWS port = EaasWS.emulatorPort;
		port.addActionFinishedMark(session.getClientId());
	}

	public void defineTraceMetadataChunk(String clientSessionId, String tag, String comment) throws BWFLAException
	{
		Session session = sessions.getSession(clientSessionId);
		EmulatorWS port = EaasWS.emulatorPort;
		port.defineTraceMetadataChunk(session.getClientId(), tag, comment);
	}

	public void addTraceMetadataEntry(String clientSessionId, String ctag, String key, String value) throws BWFLAException
	{
		Session session = sessions.getSession(clientSessionId);
		EmulatorWS port = EaasWS.emulatorPort;
		port.addTraceMetadataEntry(session.getClientId(), ctag, key, value);
	}

	public String getSessionTrace(String clientSessionId) throws BWFLAException
	{
		Session session = sessions.getSession(clientSessionId);
		EmulatorWS port = EaasWS.emulatorPort;
		return port.getSessionTrace(session.getClientId());
	}

	/* =============== Session replay API =============== */

	public boolean prepareSessionPlayer(String clientSessionId, String trace, boolean headless) throws BWFLAException
	{
		Session session = sessions.getSession(clientSessionId);
		EmulatorWS port = EaasWS.emulatorPort;
		return port.prepareSessionPlayer(session.getClientId(), trace, headless);
	}

	public int getSessionPlayerProgress(String clientSessionId) throws BWFLAException
	{
		Session session = sessions.getSession(clientSessionId);
		EmulatorWS port = EaasWS.emulatorPort;
		return port.getSessionPlayerProgress(session.getClientId());
	}

	public boolean isReplayModeEnabled(String clientSessionId) throws BWFLAException
	{
		Session session = sessions.getSession(clientSessionId);
		EmulatorWS port = EaasWS.emulatorPort;
		return port.isReplayModeEnabled(session.getClientId());
	}

	/* ==================== Monitoring API ==================== */

	public boolean updateMonitorValues(String clientSessionId) throws BWFLAException
	{
		Session session = sessions.getSession(clientSessionId);
		EmulatorWS port = EaasWS.emulatorPort;
		return port.updateMonitorValues(session.getClientId());
	}

	public String getMonitorValue(String clientSessionId, ProcessMonitorVID vid) throws BWFLAException
	{
		Session session = sessions.getSession(clientSessionId);
		EmulatorWS port = EaasWS.emulatorPort;
		return port.getMonitorValue(session.getClientId(), vid.value());
	}

	public List<String> getMonitorValues(String clientSessionId, Collection<ProcessMonitorVID> vids) throws BWFLAException
	{
		Session session = sessions.getSession(clientSessionId);
		EmulatorWS port = EaasWS.emulatorPort;

		ArrayList<Integer> values = new ArrayList<Integer>(vids.size());
		for(ProcessMonitorVID vid : vids)
			values.add(vid.value());

		return port.getMonitorValues(session.getClientId(), values);
	}

	public List<String> getAllMonitorValues(String clientSessionId) throws BWFLAException
	{
		Session session = sessions.getSession(clientSessionId);
		EmulatorWS port = EaasWS.emulatorPort;
		return port.getAllMonitorValues(session.getClientId());
	}
	
	/* ==================== Screenshot API ==================== */

	public void takeScreenshot(String clientSessionId) throws BWFLAException
	{
		Session session = sessions.getSession(clientSessionId);
		EmulatorWS port = EaasWS.emulatorPort;
		port.takeScreenshot(session.getClientId());
	}
	
	public @XmlMimeType("application/octet-stream") DataHandler getNextScreenshot(String clientSessionId) throws BWFLAException
	{
		Session session = sessions.getSession(clientSessionId);
		EmulatorWS port = EaasWS.emulatorPort;
		return port.getNextScreenshot(session.getClientId());
	}
}