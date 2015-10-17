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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.transform.stream.StreamSource;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "emulationEnvironment", namespace = "http://bwfla.bwl.de/common/datatypes", propOrder = {
    "arch",
    "emulator",
    "uiOptions",
    "drive",
    "nic",
    "binding",
    "nativeConfig"
})

@XmlRootElement(namespace = "http://bwfla.bwl.de/common/datatypes")
public class EmulationEnvironment
    extends Environment
{

    @XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes", required = true)
    protected String arch;
    @XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes", required = true)
    protected EmulatorSpec emulator;
    @XmlElement(name = "ui_options", namespace = "http://bwfla.bwl.de/common/datatypes")
    protected UiOptions uiOptions;
    @XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes")
    protected List<Drive> drive;
    @XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes")
    protected List<Nic> nic;
    @XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes")
    protected List<Resource> binding;
    @XmlElement(namespace = "http://bwfla.bwl.de/common/datatypes")
    protected EmulationEnvironment.NativeConfig nativeConfig;

    public String getArch() {
        return arch;
    }

    public void setArch(String value) {
        this.arch = value;
    }

    public EmulatorSpec getEmulator() {
        return emulator;
    }

    public void setEmulator(EmulatorSpec value) {
        this.emulator = value;
    }

    public UiOptions getUiOptions() {
        return uiOptions;
    }

    public void setUiOptions(UiOptions value) {
        this.uiOptions = value;
    }

    public List<Drive> getDrive() {
        if (drive == null) {
            drive = new ArrayList<Drive>();
        }
        return this.drive;
    }

    public List<Nic> getNic() {
        if (nic == null) {
            nic = new ArrayList<Nic>();
        }
        return this.nic;
    }

    public List<Resource> getBinding() {
        if (binding == null) {
            binding = new ArrayList<Resource>();
        }
        return this.binding;
    }

    public EmulationEnvironment.NativeConfig getNativeConfig() {
        return nativeConfig;
    }

    public void setNativeConfig(EmulationEnvironment.NativeConfig value) {
        this.nativeConfig = value;
    }


    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "value"
    })
    public static class NativeConfig {

        @XmlValue
        protected String value;
        @XmlAttribute(name = "linebreak")
        protected String linebreak;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getLinebreak() {
            return linebreak;
        }

        public void setLinebreak(String value) {
            this.linebreak = value;
        }

    }
    
    public static EmulationEnvironment fromValue(String data)
    {
		try {
			JAXBContext jc = JAXBContext
					.newInstance(EmulationEnvironment.class);
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			EmulationEnvironment result = (EmulationEnvironment) unmarshaller
					.unmarshal(new StreamSource(new StringReader(data)));
			return result;
		} catch (Throwable t) {
			throw new IllegalArgumentException("passed 'data' metadata cannot be parsed by 'JAX-B', check input contents");
		}
    }

	public EmulationEnvironment copy() {
		try {
			return EmulationEnvironment.fromValue(this.value());
		} catch (JAXBException e) {
			e.printStackTrace();
			return null;
		}
	}
}
