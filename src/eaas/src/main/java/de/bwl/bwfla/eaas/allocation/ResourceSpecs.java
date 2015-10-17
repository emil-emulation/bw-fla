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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
@XmlAccessorType(value = XmlAccessType.FIELD)
public class ResourceSpecs
{
	@XmlElement(name="cpucores", required=true)
	protected float numCpuCores;
	
	@XmlElement(name="memory", required=true)
	protected int ramCapacity;
	
	@XmlElement(name="disk", required=false)
	protected int diskCapacity;

	
	public ResourceSpecs()
	{
		this.numCpuCores = 0.0F;
		this.ramCapacity = 0;
		this.diskCapacity = 0;
	}
	
	public ResourceSpecs(ResourceSpecs other)
	{
		this.numCpuCores = other.numCpuCores;
		this.ramCapacity = other.ramCapacity;
		this.diskCapacity = other.diskCapacity;
	}
	
	public void setNumCpuCores(float number)
	{
		this.numCpuCores = number;
	}
	
	public float getNumCpuCores()
	{
		return numCpuCores;
	}
	
	public void setRamCapacity(int capacity)
	{
		this.ramCapacity = capacity;
	}
	
	public int getRamCapacity()
	{
		return ramCapacity;
	}
	
	public void setDiskCapacity(int capacity)
	{
		this.diskCapacity = capacity;
	}
	
	public int getDiskCapacity()
	{
		return diskCapacity;
	}
}
