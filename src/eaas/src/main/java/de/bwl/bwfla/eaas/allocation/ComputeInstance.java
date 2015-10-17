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

package de.bwl.bwfla.eaas.allocation;

import java.net.URL;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;



@XmlRootElement
@XmlAccessorType(value = XmlAccessType.NONE)
public class ComputeInstance
{
    @XmlElement 
    private final URL url;

    @SuppressWarnings("unused")
    private ComputeInstance()
    {
        this.url = null;
    }

    public ComputeInstance(URL location)
    {
    	if(location == null)
    		throw new IllegalArgumentException("compute-instance location cannot be set as 'null'");
    	
        this.url = location;
    }
    
    public URL getUrl()
    {
        return this.url;
    }
}