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


package de.bwl.bwfla.api.emucomp;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for filesystem.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="filesystem">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="FAT12"/>
 *     &lt;enumeration value="FAT16"/>
 *     &lt;enumeration value="HFS"/>
 *     &lt;enumeration value="ISO"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "filesystem")
@XmlEnum
public enum Filesystem {

    @XmlEnumValue("FAT12")
    FAT_12("FAT12"),
    @XmlEnumValue("FAT16")
    FAT_16("FAT16"),
    HFS("HFS"),
    ISO("ISO");
    private final String value;

    Filesystem(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static Filesystem fromValue(String v) {
        for (Filesystem c: Filesystem.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
