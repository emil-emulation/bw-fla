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

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(namespace="miep")
@XmlEnum
public enum MediumType {
	@XmlEnumValue("unknown")
	UNKNOWN("unknown"),
    @XmlEnumValue("cdrom")
    CDROM("cdrom"),
    @XmlEnumValue("hdd")
    HARDDISK("hdd"),
    @XmlEnumValue("floppy")
    FLOPPY("floppy");
    
    private final String value;
    
    MediumType(String value) {
    	this.value = value;
    }
    
    public String value() {
    	return value;
    }
    
    public static MediumType fromValue(String value) {
    	if (value != null) {
    		for (MediumType type : MediumType.values()) {
    			if (value.equalsIgnoreCase(type.value())) {
    				return type;
    			}
    		}
    	}
    	throw new IllegalArgumentException("No constant with text " + value + " found");
    }
}
