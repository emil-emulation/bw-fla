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
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.transform.stream.StreamSource;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "environment", namespace = "http://bwfla.bwl.de/common/datatypes", propOrder = {
    "id",
    "description"
})
@XmlSeeAlso({
    EmulationEnvironment.class,
    NetworkEnvironment.class
})
public class Environment {

    @XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes", required = true)
    protected String id;
    @XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes", required = true)
    protected EnvironmentDescription description;

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

    public EnvironmentDescription getDescription() {
        return description;
    }

    public void setDescription(EnvironmentDescription value) {
        this.description = value;
    }

    
    public static Environment fromValue(String data) throws JAXBException {
    	JAXBContext jc = JAXBContext.newInstance(NetworkEnvironment.class, EmulationEnvironment.class, 
    			AbstractDataResource.class, 
    			Binding.class, ArchiveBinding.class);
    	Unmarshaller unmarshaller = jc.createUnmarshaller();
    	return (Environment)unmarshaller.unmarshal(new StreamSource(new StringReader(data)));
    }
    
    public String toString()
    {
    	try {
			return value();
		} catch (JAXBException e) {
			e.printStackTrace();
			return null;
		}
    }
    
    public String value() throws JAXBException {
    	JAXBContext jc = JAXBContext.newInstance(this.getClass());
    	Marshaller marshaller = jc.createMarshaller();
    	StringWriter w = new StringWriter();
    	marshaller.marshal(this, w);
    	return w.toString();
    }
    
	public Environment copy() {
		try {
			return Environment.fromValue(this.value());
		} catch (JAXBException e) {
			e.printStackTrace();
			return null;
		}
	}
}
