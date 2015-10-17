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

package de.bwl.bwfla.eaas.proxy;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.activation.DataHandler;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.MTOM;
import javax.xml.ws.soap.MTOMFeature;
import javax.xml.ws.soap.SOAPBinding;
import de.bwl.bwfla.api.emucomp.AbstractCredentials;
import de.bwl.bwfla.api.emucomp.ConnectionType;
import de.bwl.bwfla.api.emucomp.EmulatorWS;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.eaas.allocation.EmuSession;
import de.bwl.bwfla.eaas.allocation.IAllocator;



@MTOM
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@WebService(targetNamespace = "http://bwfla.bwl.de/api/emucomp", serviceName = "EmulatorWSService", portName = "EmulatorWSPort")
public class EmulatorProxyWS implements EmulatorWS
{
	private final static Map<String, EmulatorWS> portsMap = new ConcurrentHashMap<>();
	private final static Map<String, EmulatorWS> mtomPortsMap = new ConcurrentHashMap<>();
    @EJB private IAllocator resourceAllocator;
    
    private static EmulatorWS getInternalPortConnection(EmuSession emuSession) throws BWFLAException
    {
        try
        {	
        	URL wsdl = new URL(emuSession.nodeLocation + "/EmulatorWS?wsdl");
        	EmulatorWS port = portsMap.get(wsdl.toString());
            if(port != null)
            	return port;
            	
	        Service service = Service.create(wsdl, new QName("http://bwfla.bwl.de/api/emucomp", "EmulatorWSService"));
	        EmulatorWS emuComp = service.getPort(new QName("http://bwfla.bwl.de/api/emucomp", "EmulatorWSPort"), EmulatorWS.class, new MTOMFeature());
	        BindingProvider bp = (BindingProvider) emuComp;
			bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, emuSession.nodeLocation + "/EmulatorWS");
	        portsMap.put(wsdl.toString(), emuComp);
	        return emuComp;
        }
        catch(WebServiceException | MalformedURLException e)
        {
        	e.printStackTrace();
        	throw new BWFLAException("internal EAAS-proxy error occured, requests cannot be further forwarded to the destination EAAS-node"); 
        }
    }
    
    private static EmulatorWS getInternaMTOMPortConnection(EmuSession emuSession) throws BWFLAException
    {
        try
        {	
        	URL wsdl = new URL(emuSession.nodeLocation + "/EmulatorWS?wsdl");
        	EmulatorWS port = mtomPortsMap.get(wsdl.toString());
            if(port != null)
            	return port;
            	
	        Service service = Service.create(wsdl, new QName("http://bwfla.bwl.de/api/emucomp", "EmulatorWSService"));
	        EmulatorWS emuComp = service.getPort(new QName("http://bwfla.bwl.de/api/emucomp", "EmulatorWSPort"), EmulatorWS.class, new MTOMFeature());
	        
	        BindingProvider bp = (BindingProvider) emuComp;
	    	SOAPBinding binding = (SOAPBinding) bp.getBinding();
	    	bp.getRequestContext().put("javax.xml.ws.client.receiveTimeout", "0");
	    	bp.getRequestContext().put("javax.xml.ws.client.connectionTimeout", "0");
	    	bp.getRequestContext().put("com.sun.xml.internal.ws.transport.http.client.streaming.chunk.size", 8192);
	    	bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, emuSession.nodeLocation + "/EmulatorWS");
	    	binding.setMTOMEnabled(true);
	        
	        mtomPortsMap.put(wsdl.toString(), emuComp);
	        return emuComp;
        }
        catch(WebServiceException | MalformedURLException e)
        {
        	e.printStackTrace();
        	throw new BWFLAException("internal EAAS-proxy error occured, requests cannot be further forwarded to the destination EAAS-node"); 
        }
    }
    
    @Override
    public void start(String clientSessionId) throws BWFLAException
    {
    	EmuSession internalSession = resourceAllocator.getInternalSession(clientSessionId);
    	EmulatorWS port = EmulatorProxyWS.getInternalPortConnection(internalSession);
    	port.start(internalSession.internalSessionId);
    }

    @Override
    public void stop(String clientSessionId) throws BWFLAException
    {
    	EmuSession internalSession = resourceAllocator.getInternalSession(clientSessionId);
    	EmulatorWS port = EmulatorProxyWS.getInternalPortConnection(internalSession);
    	port.stop(internalSession.internalSessionId);
    }
    
    @Override
	public int changeMedium(String arg0, int arg1, String arg2) throws BWFLAException
	{
    	EmuSession internalSession = resourceAllocator.getInternalSession(arg0);
    	EmulatorWS port = EmulatorProxyWS.getInternalPortConnection(internalSession);
    	return port.changeMedium(internalSession.internalSessionId, arg1, arg2);
	}
    
    @Override
    public int attachMedium(String arg0, @XmlMimeType("application/octet-stream") DataHandler arg1, String arg2) throws BWFLAException
    {
    	EmuSession internalSession = resourceAllocator.getInternalSession(arg0);
    	EmulatorWS port = EmulatorProxyWS.getInternaMTOMPortConnection(internalSession);
    	return port.attachMedium(internalSession.internalSessionId, arg1, arg2);
    }

    @Override
    public @XmlMimeType("application/octet-stream") DataHandler detachMedium(String clientSessionId, int handle) throws BWFLAException 
    {
    	EmuSession internalSession = resourceAllocator.getInternalSession(clientSessionId);
    	EmulatorWS port = EmulatorProxyWS.getInternaMTOMPortConnection(internalSession);
    	return port.detachMedium(internalSession.internalSessionId, handle);
    }

    @Override
    public String getRuntimeConfiguration(String clientSessionId) throws BWFLAException 
    {
    	EmuSession internalSession = resourceAllocator.getInternalSession(clientSessionId);
    	EmulatorWS port = EmulatorProxyWS.getInternalPortConnection(internalSession);
    	return port.getRuntimeConfiguration(internalSession.internalSessionId);
    }

    @Override
    public List<String> getColdplugableDrives(String clientSessionId) throws BWFLAException 
    {
    	EmuSession internalSession = resourceAllocator.getInternalSession(clientSessionId);
    	EmulatorWS port = EmulatorProxyWS.getInternalPortConnection(internalSession);
    	return port.getColdplugableDrives(internalSession.internalSessionId);
    }

    @Override
    public List<String> getHotplugableDrives(String clientSessionId) throws BWFLAException 
    {
    	EmuSession internalSession = resourceAllocator.getInternalSession(clientSessionId);
    	EmulatorWS port = EmulatorProxyWS.getInternalPortConnection(internalSession);
    	return port.getHotplugableDrives(internalSession.internalSessionId);
    }

    @Override
    public String saveEnvironment(String clientSessionId, String wsHost, String name, String type) throws BWFLAException 
    {
    	EmuSession internalSession = resourceAllocator.getInternalSession(clientSessionId);
    	EmulatorWS port = EmulatorProxyWS.getInternalPortConnection(internalSession);
    	return port.saveEnvironment(internalSession.internalSessionId, wsHost, name, type);
    }

    @Override
    public String getEmulatorState(String clientSessionId) throws BWFLAException 
    {
    	EmuSession internalSession = resourceAllocator.getInternalSession(clientSessionId);
    	EmulatorWS port = EmulatorProxyWS.getInternalPortConnection(internalSession);
    	return port.getEmulatorState(internalSession.internalSessionId);
    }

    @Override
    public void connectNic(String clientSessionId, String arg1, String arg2) throws BWFLAException 
    {
    	EmuSession internalSession = resourceAllocator.getInternalSession(clientSessionId);
    	EmulatorWS port = EmulatorProxyWS.getInternalPortConnection(internalSession);
    	port.connectNic(internalSession.internalSessionId, arg1, arg2);
    }

    @Override
    public String getControlURL(String clientSessionId, ConnectionType type, AbstractCredentials credentials) throws BWFLAException 
    {
	    EmuSession internalSession = resourceAllocator.getInternalSession(clientSessionId);
	    EmulatorWS port = EmulatorProxyWS.getInternalPortConnection(internalSession);
	    return port.getControlURL(internalSession.internalSessionId, type, credentials);
    }

    /* ==================== Session recording API ==================== */

	@Override
	public boolean prepareSessionRecorder(String clientSessionId) throws BWFLAException 
	{

		EmuSession internalSession = resourceAllocator.getInternalSession(clientSessionId);
		EmulatorWS port = EmulatorProxyWS.getInternalPortConnection(internalSession);
		return port.prepareSessionRecorder(internalSession.internalSessionId);

	}

	@Override
	public void startSessionRecording(String clientSessionId) throws BWFLAException 
	{
		EmuSession internalSession = resourceAllocator.getInternalSession(clientSessionId);
		EmulatorWS port = EmulatorProxyWS.getInternalPortConnection(internalSession);
		port.startSessionRecording(internalSession.internalSessionId);
	}

	@Override
	public void stopSessionRecording(String clientSessionId) throws BWFLAException 
	{
		EmuSession internalSession = resourceAllocator.getInternalSession(clientSessionId);
		EmulatorWS port = EmulatorProxyWS.getInternalPortConnection(internalSession);
		port.stopSessionRecording(internalSession.internalSessionId);
	}

	@Override
	public boolean isRecordModeEnabled(String clientSessionId) throws BWFLAException 
	{
		EmuSession internalSession = resourceAllocator.getInternalSession(clientSessionId);
		EmulatorWS port = EmulatorProxyWS.getInternalPortConnection(internalSession);
		return port.isRecordModeEnabled(internalSession.internalSessionId);
	}

	@Override
	public void addActionFinishedMark(String clientSessionId) throws BWFLAException 
	{
		EmuSession internalSession = resourceAllocator.getInternalSession(clientSessionId);
		EmulatorWS port = EmulatorProxyWS.getInternalPortConnection(internalSession);
		port.addActionFinishedMark(internalSession.internalSessionId);
	}

	@Override
	public void defineTraceMetadataChunk(String clientSessionId, String tag, String comment) throws BWFLAException 
	{
		EmuSession internalSession = resourceAllocator.getInternalSession(clientSessionId);
		EmulatorWS port = EmulatorProxyWS.getInternalPortConnection(internalSession);
		port.defineTraceMetadataChunk(internalSession.internalSessionId, tag, comment);
	}

	@Override
	public void addTraceMetadataEntry(String clientSessionId, String ctag, String key, String value) throws BWFLAException 
	{
		EmuSession internalSession = resourceAllocator.getInternalSession(clientSessionId);
		EmulatorWS port = EmulatorProxyWS.getInternalPortConnection(internalSession);
		port.addTraceMetadataEntry(internalSession.internalSessionId, ctag, key, value);
	}

	@Override
	public String getSessionTrace(String clientSessionId) throws BWFLAException 
	{
		EmuSession internalSession = resourceAllocator.getInternalSession(clientSessionId);
		EmulatorWS port = EmulatorProxyWS.getInternalPortConnection(internalSession);
		return port.getSessionTrace(internalSession.internalSessionId);
	}


	/* ==================== Session replay API ==================== */

	@Override
	public boolean prepareSessionPlayer(String clientSessionId, String trace, boolean headless) throws BWFLAException 
	{
		EmuSession internalSession = resourceAllocator.getInternalSession(clientSessionId);
		EmulatorWS port = EmulatorProxyWS.getInternalPortConnection(internalSession);
		return port.prepareSessionPlayer(internalSession.internalSessionId, trace, headless);
	}

	@Override
	public int getSessionPlayerProgress(String clientSessionId) throws BWFLAException 
	{
		EmuSession internalSession = resourceAllocator.getInternalSession(clientSessionId);
		EmulatorWS port = EmulatorProxyWS.getInternalPortConnection(internalSession);
		return port.getSessionPlayerProgress(internalSession.internalSessionId);
	}

	@Override
	public boolean isReplayModeEnabled(String clientSessionId) throws BWFLAException 
	{
		EmuSession internalSession = resourceAllocator.getInternalSession(clientSessionId);
		EmulatorWS port = EmulatorProxyWS.getInternalPortConnection(internalSession);
		return port.isReplayModeEnabled(internalSession.internalSessionId);
	}


	/* ==================== Monitoring API ==================== */

	@Override
	public boolean updateMonitorValues(String clientSessionId) throws BWFLAException
	{
		EmuSession internalSession = resourceAllocator.getInternalSession(clientSessionId);
		EmulatorWS port = EmulatorProxyWS.getInternalPortConnection(internalSession);
		return port.updateMonitorValues(internalSession.internalSessionId);
	}

	@Override
	public String getMonitorValue(String clientSessionId, Integer vid) throws BWFLAException 
	{
		EmuSession internalSession = resourceAllocator.getInternalSession(clientSessionId);
		EmulatorWS port = EmulatorProxyWS.getInternalPortConnection(internalSession);
		return port.getMonitorValue(internalSession.internalSessionId, vid);
	}

	@Override
	public List<String> getMonitorValues(String clientSessionId, List<Integer> vids) throws BWFLAException 
	{
		EmuSession internalSession = resourceAllocator.getInternalSession(clientSessionId);
		EmulatorWS port = EmulatorProxyWS.getInternalPortConnection(internalSession);
		return port.getMonitorValues(internalSession.internalSessionId, vids);
	}

	@Override
	public List<String> getAllMonitorValues(String clientSessionId) throws BWFLAException 
	{
		EmuSession internalSession = resourceAllocator.getInternalSession(clientSessionId);
		EmulatorWS port = EmulatorProxyWS.getInternalPortConnection(internalSession);
		return port.getAllMonitorValues(internalSession.internalSessionId);
	}
	
	
	/* ==================== Screenshot API ==================== */

	@Override
	public void takeScreenshot(String clientSessionId) throws BWFLAException
	{
		EmuSession internalSession = resourceAllocator.getInternalSession(clientSessionId);
		EmulatorWS port = EmulatorProxyWS.getInternalPortConnection(internalSession);
		port.takeScreenshot(internalSession.internalSessionId);
	}
	
	@Override
	public @XmlMimeType("application/octet-stream") DataHandler getNextScreenshot(String clientSessionId) throws BWFLAException
	{
		EmuSession internalSession = resourceAllocator.getInternalSession(clientSessionId);
		EmulatorWS port = EmulatorProxyWS.getInternalPortConnection(internalSession);
		return port.getNextScreenshot(internalSession.internalSessionId);
	}
}