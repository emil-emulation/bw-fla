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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "kbdOptions", namespace = "http://bwfla.bwl.de/common/datatypes", propOrder = {
    "clientKbdLayout",
    "clientKbdModel",
    "emulatorKbdLayout",
    "emulatorKbdModel"
})
public class InputOptions {

	@XmlAttribute(name = "required", required = false)
	protected boolean required = false;;
	
	@XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes")
	protected String clientKbdLayout;
	@XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes")
	protected String clientKbdModel;
	@XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes")
	protected String emulatorKbdLayout;
	@XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes")
	protected String emulatorKbdModel;
	public String getClientKbdLayout() {
		return clientKbdLayout;
	}
	
	public void setClientKbdLayout(String clientKbdLayout) {
		this.clientKbdLayout = clientKbdLayout;
	}
	public String getClientKbdModel() {
		return clientKbdModel;
	}
	public void setClientKbdModel(String clientKbdModel) {
		this.clientKbdModel = clientKbdModel;
	}
	public String getEmulatorKbdLayout() {
		return emulatorKbdLayout;
	}
	public void setEmulatorKbdLayout(String emulatorKbdLayout) {
		this.emulatorKbdLayout = emulatorKbdLayout;
	}
	public String getEmulatorKbdModel() {
		return emulatorKbdModel;
	}
	public void setEmulatorKbdModel(String emulatorKbdModel) {
		this.emulatorKbdModel = emulatorKbdModel;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}
}
