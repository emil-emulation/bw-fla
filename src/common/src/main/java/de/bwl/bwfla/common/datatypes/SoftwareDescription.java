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

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.transform.stream.StreamSource;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "softwareDescription", namespace="http://bwfla.bwl.de/common/datatypes",
	propOrder = { "softwareId", "label" })
@XmlRootElement(namespace = "http://bwfla.bwl.de/common/datatypes")
public class SoftwareDescription
{
	@XmlElement(required = true)
	private String softwareId;

	@XmlElement(required = true)
	private String label;

	public SoftwareDescription()
	{
		this(null, null);
	}
	
	public SoftwareDescription(String id, String label)
	{
		this.softwareId = id;
		this.label = label;
	}
	
	public String getSoftwareId()
	{
		return softwareId;
	}
	
	public String getLabel()
	{
		return label;
	}

	public void setSoftwareId(String id)
	{
		this.softwareId = id;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	public String value() throws JAXBException
	{
    	JAXBContext jc = JAXBContext.newInstance(this.getClass());
    	Marshaller marshaller = jc.createMarshaller();
    	StringWriter writer = new StringWriter();
    	marshaller.marshal(this, writer);
    	return writer.toString();
    }
	
	public static SoftwareDescription fromValue(String data)
    {
		return SoftwareDescription.fromValue(new StringReader(data));
    }
	
	public static SoftwareDescription fromValue(Reader reader)
    {
		try {
			JAXBContext jc = JAXBContext.newInstance(SoftwareDescription.class);
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			StreamSource stream = new StreamSource(reader);
			return (SoftwareDescription) unmarshaller.unmarshal(stream);
		} catch (Throwable t) {
			throw new IllegalArgumentException("passed 'data' metadata cannot be parsed by 'JAX-B', check input contents");
		}
    }
}
