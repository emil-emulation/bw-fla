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
import javax.xml.bind.annotation.XmlValue;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "emulatorSpec", namespace = "http://bwfla.bwl.de/common/datatypes", propOrder = {
    "machine"
})
public class EmulatorSpec {

    @XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes")
    protected EmulatorSpec.Machine machine;
    @XmlAttribute(name = "bean", required = true)
    protected String bean;
    @XmlAttribute(name = "version")
    protected String version;

    public EmulatorSpec.Machine getMachine() {
        return machine;
    }

    public void setMachine(EmulatorSpec.Machine value) {
        this.machine = value;
    }

    public String getBean() {
        return bean;
    }

    public void setBean(String value) {
        this.bean = value;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String value) {
        this.version = value;
    }


    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Machine {
    	
        @XmlValue
        protected String value;
        @XmlAttribute(name = "base")
        protected String base;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getBase() {
            return base;
        }

        public void setBase(String value) {
            this.base = value;
        }

    }

}
