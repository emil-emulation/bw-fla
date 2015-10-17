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

public class FormHardware {
	private long hardwareid;	
	private String name;
	private String description;
	private Date releaseDate;
	private String infoSource;
	private String location;
	private String licence;
	private String documentation;
	
	private long architectureid;
	private String architecturename;
	private String architecturetype;
	private String architectureplatform;

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

	public String getArchitecturename() {
		return architecturename;
	}

	public String getArchitecturetype() {
		return architecturetype;
	}

	public String getArchitectureplatform() {
		return architectureplatform;
	}

	public void setArchitecturename(String architecturename) {
		this.architecturename = architecturename;
	}

	public void setArchitecturetype(String architecturetype) {
		this.architecturetype = architecturetype;
	}

	public void setArchitectureplatform(String architectureplatform) {
		this.architectureplatform = architectureplatform;
	}

	public long getHardwareid() {
		return hardwareid;
	}

	public long getArchitectureid() {
		return architectureid;
	}

	public void setHardwareid(long hardwareid) {
		this.hardwareid = hardwareid;
	}

	public void setArchitectureid(long architectureid) {
		this.architectureid = architectureid;
	}
	
	@Override
	public boolean equals(Object obj) {
		FormHardware typecast = Utils.typecast(obj, this.getClass());
		
		if (typecast != null) {
			return typecast.getHardwareid() == this.getHardwareid();
		}
			
		return false;
	}
}
