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

package de.bwl.bwfla.workflows.softwarearchive;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

@XmlRootElement(name = "swArchiveRegistry")
@XmlAccessorType(XmlAccessType.FIELD)
public class SwArchiveRegistry {
	@XmlElementRef(required=true)
	protected List<SoftwareArchive> archives;

	public List<SoftwareArchive> getArchives() {
		if (archives == null) {
			return Collections.unmodifiableList(new ArrayList<SoftwareArchive>());
		}
		return Collections.unmodifiableList(archives);
	}

	public static SwArchiveRegistry unmarshal(Source source)
			throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(SwArchiveRegistry.class, FileSoftwareArchive.class);
		Unmarshaller unmarshaller = jc.createUnmarshaller();
		return (SwArchiveRegistry) unmarshaller.unmarshal(source);
	}

	public static SwArchiveRegistry fromValue(String data) throws JAXBException {
		return unmarshal(new StreamSource(new StringReader(data)));
	}

	public static SwArchiveRegistry fromFile(File data) throws JAXBException,
			FileNotFoundException {
		return unmarshal(new StreamSource(new FileReader(data)));
	}

	public String toString() {
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
}
