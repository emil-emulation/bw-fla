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

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SoftwarePackage {
	private String name;
	private String description;
	private Date releaseDate;
	private String infoSource;
	private String location;
	private String licence;
	private String language;
	private String documentation;
	private List<File> files = new ArrayList<File>();

	private long hardwareid;
	private long osid;
	private String thumbnailFilepath;

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

	public String getLanguage() {
		return language;
	}

	public String getDocumentation() {
		return documentation;
	}

	public List<File> getFiles() {
		return files;
	}

	public long getHardwareid() {
		return hardwareid;
	}

	public long getOsid() {
		return osid;
	}

	public String getThumbnailFilepath() {
		return thumbnailFilepath;
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

	public void setLanguage(String language) {
		this.language = language;
	}

	public void setDocumentation(String documentation) {
		this.documentation = documentation;
	}

	public void setFiles(List<File> files) {
		this.files = files;
	}

	public void setHardwareid(long hardwareid) {
		this.hardwareid = hardwareid;
	}

	public void setOsid(long osid) {
		this.osid = osid;
	}

	public void setThumbnailFilepath(String thumbnailFilepath) {
		this.thumbnailFilepath = thumbnailFilepath;
	}
}
