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

package de.bwl.bwfla.workflows.softwarearchive.datatypes;

import java.io.File;
import java.nio.file.Path;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import de.bwl.bwfla.common.datatypes.Drive.DriveType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "file", propOrder = { "path", "type", "description", "platform" }, namespace="miep")
public class BundledFile {
	public BundledFile() {

	}

	@XmlElement(required = false, namespace="miep")
	protected String description;

	@XmlElement(required = true, namespace="miep")
	protected File path;

	@XmlElement(required = true, namespace="miep")
	protected DriveType type;
	
	@XmlElement(required = true, namespace="miep")
	protected String platform;

	//public BundledFile(Path file) {
	//	this(file, MediumType.UNKNOWN, null);
	//}

	public BundledFile(Path file, String platform, DriveType type) {
		this(file, platform, type, null);
	}

	public BundledFile(Path path, String platform, DriveType type, String description) {
		this.path = path.toFile();
		this.type = type;
		this.platform = platform;
		this.description = description;
	}

	// public BundledFile(Path file, String description) {
	//	this(file, MediumType.UNKNOWN, description);
	// }

	public String getDescription() {
		return description;
	}

	public Path getPath() {
		return path.toPath();
	}

	public DriveType getType() {
		return type;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setPath(Path file) {
		this.path = file.toFile();
	}

	public void setType(DriveType type) {
		this.type = type;
	}
	
	public String getPlatform()
	{
		return platform;
	}
}
