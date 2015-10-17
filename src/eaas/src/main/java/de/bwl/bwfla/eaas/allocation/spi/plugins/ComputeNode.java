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

package de.bwl.bwfla.eaas.allocation.spi.plugins;

import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import de.bwl.bwfla.eaas.allocation.ComputeInstance;
import de.bwl.bwfla.eaas.allocation.ResourceSpecs;

/* Package-Private */

@XmlRootElement
@XmlAccessorType(value = XmlAccessType.NONE)
public class ComputeNode
{
	@XmlElement
	protected URL									address;
	
	protected ResourceSpecs							nodespecs;
	
	@XmlElement
	protected String								name;
	
	protected ComputeNodeResources resources;
	protected Map<ComputeInstance, ResourceSpecs> instances;
	protected volatile boolean running;
	
	
	public ComputeNode(URL address, ResourceSpecs specs)
	{
		final int capacity = 2 * ((int) specs.getNumCpuCores() + 1);
		
		this.name = "bwFLA Machine";
		this.address = address;
		this.nodespecs = specs;
		this.resources = new ComputeNodeResources(specs);
		this.instances = new ConcurrentHashMap<ComputeInstance, ResourceSpecs>(capacity);
		this.running = true;
	}
	
	public ComputeNode(String name, URL address, ResourceSpecs specs)
	{
		this(address, specs);
		this.name = name;
	}
	
	public String getName()
	{
		return name;
	}
	
	public URL getAddress()
	{
		return address;
	}
	
	public boolean hasAddress(URL target)
	{
		return target.equals(this.address);
	}
	
	public ResourceSpecs getNodeSpecs()
	{
		return nodespecs;
	}
	
	public Set<ComputeInstance> getComputeInstances()
	{
		return instances.keySet();
	}
	
	public boolean isRunning()
	{
		return running;
	}
	
	public void markAsRunning()
	{
		running = true;
	}
	
	public void markAsFailed()
	{
		running = false;
	}
	
	public ComputeInstance reserveResources(ResourceSpecs specs)
	{
		// Check the free resources
		synchronized (resources) {
			boolean outOfResources = (resources.getNumCpuCores() < specs.getNumCpuCores())
							|| (resources.getRamCapacity() < specs.getRamCapacity())
							|| (resources.getDiskCapacity() < specs.getDiskCapacity());
			
			if (outOfResources)
				return null;  // Can't reserve resources!
			
			resources.reserve(specs);
		}
		
		ComputeInstance instance = new ComputeInstance(address);
		instances.put(instance, specs);
		return instance;
	}
	
	public void releaseResources(ComputeInstance instance)
	{
		ResourceSpecs specs = instances.remove(instance);
		if (specs == null)
			return;  // Not found!
		
		synchronized (resources) {
			resources.release(specs);
		}
	}
	
	public void reset()
	{
		instances.clear();
		
		synchronized (resources) {
			resources.set(nodespecs);
		}
	}
	
	
	/* =============== JAXB Stuff =============== */
	
	@SuppressWarnings("unused")
	private ComputeNode()
	{
		this.address = null;
		this.nodespecs = null;
		this.resources = null;
		this.instances = null;
		this.running = true;
	}
	
	@XmlElement
    private ResourceSpecs getNodespecs()
    {
        return this.nodespecs;
    }
	
	@SuppressWarnings("unused")
	private void setNodespecs(ResourceSpecs specs)
	{
		final int capacity = 2 * ((int) specs.getNumCpuCores() + 1);
		
	    this.nodespecs = specs;
	    this.resources = new ComputeNodeResources(specs);
        this.instances = new ConcurrentHashMap<ComputeInstance, ResourceSpecs>(capacity);
	}
}
