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
import java.util.Date;

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
@XmlType(name = "softwarePackage", namespace="http://bwfla.bwl.de/common/datatypes",
	propOrder = { "name", "description", "releaseDate", "infoSource", "location",
	              "licence", "numSeats", "language", "documentation", "archive", "objectId" })
@XmlRootElement(namespace = "http://bwfla.bwl.de/common/datatypes")
public class SoftwarePackage
{
	@XmlElement(required = true)
	private String name;
	
	private String description;
	private Date releaseDate;
	private String infoSource;
	private String location;
	private String licence;
	private int numSeats;
	private String language;
	private String documentation;
	
	@XmlElement(required = true)
	private String archive;
	
	@XmlElement(required = true)
	private String objectId;

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Date getReleaseDate() {
		return releaseDate;
	}

	public String getInfoSource() {
		return infoSource;
	}

	public String getLocation() {
		return location;
	}

	public String getLicence() {
		return licence;
	}
	
	public int getNumSeats() {
		return numSeats;
	}

	public String getLanguage() {
		return language;
	}

	public String getDocumentation() {
		return documentation;
	}

	public String getArchive() {
		return archive;
	}
	
	public String getObjectId() {
		return objectId;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}

	public void setInfoSource(String infoSource) {
		this.infoSource = infoSource;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public void setLicence(String licence) {
		this.licence = licence;
	}

	public void setNumSeats(int numSeats) {
		this.numSeats = numSeats;
	}
	
	public void setLanguage(String language) {
		this.language = language;
	}

	public void setDocumentation(String documentation) {
		this.documentation = documentation;
	}

	public void setArchive(String archive) {
		this.archive = archive;
	}
	
	public void setObjectId(String id) {
		this.objectId = id;
	}
	
	public String value() throws JAXBException {
    	JAXBContext jc = JAXBContext.newInstance(this.getClass());
    	Marshaller marshaller = jc.createMarshaller();
    	StringWriter writer = new StringWriter();
    	marshaller.marshal(this, writer);
    	return writer.toString();
    }
	
	public static SoftwarePackage fromValue(String data)
    {
		return SoftwarePackage.fromValue(new StringReader(data));
    }
	
	public static SoftwarePackage fromValue(Reader reader)
    {
		try {
			JAXBContext jc = JAXBContext.newInstance(SoftwarePackage.class);
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			StreamSource stream = new StreamSource(reader);
			return (SoftwarePackage) unmarshaller.unmarshal(stream);
		} catch (Throwable t) {
			throw new IllegalArgumentException("passed 'data' metadata cannot be parsed by 'JAX-B', check input contents");
		}
    }
}
