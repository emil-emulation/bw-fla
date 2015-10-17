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

package de.bwl.bwfla.common.datatypes;

import java.io.StringWriter;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import de.bwl.bwfla.common.utils.ProcessRunner;


@XmlJavaTypeAdapter(VdeNetworkEndpoint.Adapter.class)
public class VdeNetworkEndpoint extends NetworkEndpoint {

	protected final Logger log = Logger.getLogger(this.getClass().getName());
	
	// hostname and socket path that this network endpoint
	// connects to
	protected String hostname = null;
	protected String socketPath = null;
	
	// ssh key required to connect to hostname
	protected byte[] key = null;
	
	// resource management
	protected ProcessRunner runner = null;
	
	public VdeNetworkEndpoint(String hostname, String socketPath, byte[] key) {
		this.hostname = hostname;
		this.socketPath = socketPath;
		this.key = key;
	}
	
	@Override
	public boolean connect(NetworkEndpoint endpoint) {
		if (this.runner != null) {
			log.severe("vde endpoint already connected. Abort.");
			return false;
		}
		
		if (!(endpoint instanceof VdeNetworkEndpoint)) {
			log.severe("Endpoint instance is not a vde endpoint, don't know what to do with it. Abort.");
			return false;
		}
		
		// TODO for performance reasons, we could leave the local ssh connection
		//      if we first determine the current host
		
		// use dpipe to connect 
		String command = String.format("dpipe ssh %s vde_plug %s = ssh %s vde_plug %s",
				                       this.hostname,
				                       this.socketPath,
				                       ((VdeNetworkEndpoint)endpoint).hostname,
				                       ((VdeNetworkEndpoint)endpoint).socketPath);

		log.info("Initiating vde endpoint connection: " + command);
		runner = new ProcessRunner(command);
		if (!runner.start()) {
			log.severe("Connecting the vde-endpoint failed!");
			runner = null;
		}
		
		return false;
	}
	
	@Override
	public boolean disconnect() {
		if (runner != null) {
			runner.stop();
			runner.cleanup();
			runner = null;
		}
		return true;
	}
	
	@Override
	public String value() throws JAXBException {
    	JAXBContext jc = JAXBContext.newInstance(VdeNetworkEndpoint.Mutable.class);
    	VdeNetworkEndpoint.Adapter adapter = new VdeNetworkEndpoint.Adapter();
    	Marshaller marshaller = jc.createMarshaller();
    	StringWriter w = new StringWriter();
    	try {
			marshaller.marshal(adapter.marshal(this), w);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return w.toString();

	}

	
	public static class Adapter extends XmlAdapter<VdeNetworkEndpoint.Mutable, VdeNetworkEndpoint> {

		@Override
		public VdeNetworkEndpoint.Mutable marshal(VdeNetworkEndpoint unmarshalled) {
			VdeNetworkEndpoint.Mutable result = new VdeNetworkEndpoint.Mutable();
			result.hostname = unmarshalled.hostname;
			result.socketPath = unmarshalled.socketPath;
			result.key = unmarshalled.key;
			return result;
		}

		@Override
		public VdeNetworkEndpoint unmarshal(VdeNetworkEndpoint.Mutable marshalled) {
			return new VdeNetworkEndpoint(marshalled.hostname, marshalled.socketPath, marshalled.key);
		}
		
	}
	
	@XmlType(name="vdeNetworkEndpoint", namespace = "http://bwfla.bwl.de/common/datatypes")
	@XmlRootElement(name = "vdeNetworkEndpoint")
	public static class Mutable {
		@XmlElement
		private String hostname = null;

		@XmlElement
		private String socketPath = null;

		@XmlElement
		private byte[] key = null;
	}
}
