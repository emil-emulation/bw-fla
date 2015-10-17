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
import java.net.URL;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.workflows.softwarearchive.datatypes.BundledFile;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({ FileSoftwareArchive.class })
public abstract class SoftwareArchive {
	@XmlElement(required=true)
	protected String name;
	@XmlElement(required=true, nillable=false)
	protected String description;

	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public abstract List<String> getSupportedPlatforms() throws BWFLAException; 
	public abstract List<SoftwareDescription> getSoftwareList();
	public abstract void saveSoftware(SoftwareDescription desc) throws BWFLAException;
	public abstract URL getSoftwareBundleUrl(SoftwareDescription desc);
	public abstract List<BundledFile> getAvailableFiles(String platform);
	
	public static SoftwareArchive unmarshal(Source source)
			throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(SoftwareArchive.class);
		Unmarshaller unmarshaller = jc.createUnmarshaller();
		return (SoftwareArchive) unmarshaller.unmarshal(source);
	}
	
	public static SoftwareArchive fromValue(String data) throws JAXBException {
		return unmarshal(new StreamSource(new StringReader(data)));
	}

	public static SoftwareArchive fromFile(File data) throws JAXBException,
			FileNotFoundException {
		return unmarshal(new StreamSource(new FileReader(data)));
	}
}
