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

import java.util.Date;
import de.bwl.bwfla.workflows.beans.common.Utils;

public class FormOs {
	private long osid;
	private String name;
	private String description;
	private Date releaseDate;
	private String infoSource;
	private String location;
	private String licence;
	private String documentation;

	private long versionid;
	private String versionname;
	private String versiondescription;
	private Date versionreleasedate;

	public long getOsid() {
		return osid;
	}

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

	public String getDocumentation() {
		return documentation;
	}

	public long getVersionid() {
		return versionid;
	}

	public String getVersionname() {
		return versionname;
	}

	public String getVersiondescription() {
		return versiondescription;
	}

	public Date getVersionreleasedate() {
		return versionreleasedate;
	}

	public void setOsid(long osid) {
		this.osid = osid;
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

	public void setDocumentation(String documentation) {
		this.documentation = documentation;
	}

	public void setVersionid(long versionid) {
		this.versionid = versionid;
	}

	public void setVersionname(String versionname) {
		this.versionname = versionname;
	}

	public void setVersiondescription(String versiondescription) {
		this.versiondescription = versiondescription;
	}

	public void setVersionreleasedate(Date versionreleasedate) {
		this.versionreleasedate = versionreleasedate;
	}

	@Override
	public boolean equals(Object obj) {
		FormOs typecast = Utils.typecast(obj, this.getClass());

		if (typecast != null) {
			return typecast.getOsid() == this.getOsid();
		}

		return false;
	}
}
