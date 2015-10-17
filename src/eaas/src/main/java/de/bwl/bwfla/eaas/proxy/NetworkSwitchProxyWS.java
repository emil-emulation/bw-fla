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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.MTOM;
import de.bwl.bwfla.api.emucomp.NetworkSwitchWS;
import de.bwl.bwfla.api.emucomp.NetworkSwitchWSService;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.eaas.allocation.EmuSession;
import de.bwl.bwfla.eaas.allocation.IAllocator;



@MTOM
@Stateless
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@WebService(targetNamespace = "http://bwfla.bwl.de/api/emucomp", serviceName = "NetworkSwitchWSService", portName = "NetworkSwitchWSPort")
public class NetworkSwitchProxyWS implements NetworkSwitchWS
{
	private final static Map<String, NetworkSwitchWS>	portsMap			= new ConcurrentHashMap<>();
	@EJB private IAllocator								resourceAllocator;

	private static NetworkSwitchWS getInternalPortConnection(EmuSession emuSession) throws BWFLAException
	{
		try
		{
			URL wsdl = new URL(emuSession.nodeLocation + "/NetworkSwitchWS?wsdl");
			NetworkSwitchWS port = portsMap.get(wsdl.toString());
			if(port != null)
				return port;

			NetworkSwitchWSService netComp = new NetworkSwitchWSService(wsdl);
			port = netComp.getNetworkSwitchWSPort();
			portsMap.put(wsdl.toString(), port);
			return port;
		}
		catch(WebServiceException | MalformedURLException e)
		{
			e.printStackTrace();
			throw new BWFLAException("internal EAAS-proxy error occured, requests cannot be further forwarded to the destination EAAS-node");
		}
	}

	@Override
	public String getNetworkEndpoint(String clientSessionId) throws BWFLAException
	{
		EmuSession internalSession = resourceAllocator.getInternalSession(clientSessionId);
		NetworkSwitchWS port = NetworkSwitchProxyWS.getInternalPortConnection(internalSession);
		return port.getNetworkEndpoint(internalSession.internalSessionId);
	}
}