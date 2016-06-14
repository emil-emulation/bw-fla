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

import java.io.StringReader;
import java.io.StringWriter;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.transform.stream.StreamSource;

@XmlType(namespace = "http://bwfla.bwl.de/common/datatypes")
@XmlSeeAlso({VdeNetworkEndpoint.Mutable.class})
public abstract class NetworkEndpoint {
	public abstract boolean connect(NetworkEndpoint endpoint);
	public abstract boolean disconnect();
	
	public static NetworkEndpoint fromValue(String value) throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(NetworkEndpoint.class, VdeNetworkEndpoint.Mutable.class);
		Unmarshaller unmarshaller = jc.createUnmarshaller();
		Object obj = unmarshaller.unmarshal(new StreamSource(new StringReader(value)));
		
		NetworkEndpoint result = null;
		if (obj instanceof VdeNetworkEndpoint.Mutable) {
			result = new VdeNetworkEndpoint.Adapter().unmarshal((VdeNetworkEndpoint.Mutable)obj);
		} else {
			result = (NetworkEndpoint)obj;
		}
		return result;
    }
	
    public String value() throws JAXBException {
    	Logger log = Logger.getLogger(this.getClass().getName());

    	log.info("marshalling for " + this.getClass().getCanonicalName());
    	JAXBContext jc = JAXBContext.newInstance(this.getClass());
    	Marshaller marshaller = jc.createMarshaller();
    	StringWriter w = new StringWriter();
    	marshaller.marshal(this, w);
    	return w.toString();
    }
}

