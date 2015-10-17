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

package de.bwl.bwfla.workflows.forms;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import de.bwl.bwfla.workflows.beans.common.LanguageConverter;

public class FormNewSoftwarePackage {
	private String name;
	private String description;
	private Date releaseDate;
	private String infoSource;
	private String location;
	private String licence;
	private String language[] = LanguageConverter.languages[122];
	private String documentation;
	private List<File> files = new ArrayList<File>();
	
	private FormHardware hardware;
	private FormOs os;
	private File thumbnail_file;

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

	public String[] getLanguage() {
		return language;
	}

	public String getDocumentation() {
		return documentation;
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

	public void setLanguage(String[] language) {
		this.language = language;
	}

	public void setDocumentation(String documentation) {
		this.documentation = documentation;
	}

	public List<File> getFiles() {
		return files;
	}

	public void setFiles(List<File> files) {
		this.files = files;
	}

	public FormHardware getHardware() {
		return hardware;
	}

	public void setHardware(FormHardware hardware) {
		this.hardware = hardware;
	}

	public FormOs getOs() {
		return os;
	}

	public void setOs(FormOs os) {
		this.os = os;
	}

	public File getThumbnail_file() {
		return thumbnail_file;
	}

	public void setThumbnail_file(File thumbnail_file) {
		this.thumbnail_file = thumbnail_file;
	}
}
