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
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import de.bwl.bwfla.common.utils.Pair;
import de.bwl.bwfla.workflows.forms.FormHardware;
import de.bwl.bwfla.workflows.forms.FormOs;
import de.bwl.bwfla.workflows.softwarearchive.datatypes.BundledFile;
import de.bwl.bwfla.workflows.softwarearchive.datatypes.SoftwareBundle;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="softwareDescription", namespace="miep")
@XmlRootElement(namespace="miep")
public class SoftwareDescription {
	@XmlElement(required = true, nillable = false, namespace="miep")
	private String id;
	@XmlElement(required = true, nillable = false, namespace="miep")
	private String name;
	@XmlElement(required = true, nillable = false, namespace="miep")
	private String description;
	@XmlElement(namespace="miep")
	private String version;

	@XmlElement(namespace="miep")
	private String licensesText;
	@XmlElement(namespace="miep")
	private int licenseInstanceCount = 1;
	
	@XmlElement(required = false, namespace="miep")
	private SoftwareBundle softwareBundle;


	public SoftwareBundle getSoftwareBundle() {
		return softwareBundle;
	}

	public void setSoftwareBundle(SoftwareBundle softwareBundle) {
		this.softwareBundle = softwareBundle;
	}

	@XmlElement(namespace="miep")
	private List<String> natives = new ArrayList<String>();
	@XmlElement(namespace="miep")
	private List<String> imports = new ArrayList<String>();
	@XmlElement(namespace="miep")
	private List<String> exports = new ArrayList<String>();

	
	public String getVersion() {
		return version;
	}

	public String getLicensesText() {
		return licensesText;
	}

	public int getLicenseInstanceCount() {
		return licenseInstanceCount;
	}

	public List<BundledFile> getFiles() {
		return this.softwareBundle.files;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public void setLicensesText(String licensesText) {
		this.licensesText = licensesText;
	}

	public void setLicenseInstanceCount(int licenseInstanceCount) {
		this.licenseInstanceCount = licenseInstanceCount;
	}

	public void setFiles(List<BundledFile> list) {
		this.softwareBundle.files = list;
	}

	public static SoftwareDescription unmarshal(Source source)
			throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(SoftwareDescription.class);
		Unmarshaller unmarshaller = jc.createUnmarshaller();
		return (SoftwareDescription) unmarshaller.unmarshal(source);
	}

	public static SoftwareDescription fromValue(String data) throws JAXBException {
		return unmarshal(new StreamSource(new StringReader(data)));
	}

	public static SoftwareDescription fromFile(File data) throws JAXBException,
			FileNotFoundException {
		return unmarshal(new StreamSource(new FileReader(data)));
	}
	
	public String value() throws JAXBException {
    	return value(false);
	}
	
	public String value(boolean prettyPrint) throws JAXBException {
    	JAXBContext jc = JAXBContext.newInstance(this.getClass());
    	Marshaller marshaller = jc.createMarshaller();
    	marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, prettyPrint);
    	StringWriter w = new StringWriter();
    	marshaller.marshal(this, w);
    	return w.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		SoftwareDescription typecast = de.bwl.bwfla.workflows.beans.common.Utils.typecast(obj, SoftwareDescription.class);
		return typecast != null && this.getId().equals(typecast.getId());
	}
	public List<String> getNatives() {
		if (this.natives == null) {
			this.natives = new ArrayList<String>();
		}

		return natives;
	}

	public void setNatives(List<String> natives) {
		this.natives = natives;
	}

	public List<String> getImports() {
		return imports;
	}

	public void setImports(List<String> imports) {
		this.imports = imports;
	}

	public List<String> getExports() {
		if (this.exports == null) {
			this.exports = new ArrayList<String>();
		}
		return exports;
	}

	public void setExports(List<String> exports) {
		this.exports = exports;
	}
}
