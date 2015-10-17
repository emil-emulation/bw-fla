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

package de.bwl.bwfla.workflows.beans.common;

import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Named;
import javax.swing.Timer;
import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.WindowScoped;
import de.bwl.bwfla.workflows.api.WorkflowResource;
import de.bwl.bwfla.workflows.beans.common.WorkflowResources.WF_RES;



@Named
@WindowScoped
public class WorkflowResources implements Serializable 
{
	public enum 					WF_RES 				{ EMU_COMP, FILE }
	protected static final Logger	LOG					= Logger.getLogger(WorkflowResources.class.getName());
	private static final long		serialVersionUID	= 4999783155398488727L;
	private ResourceManager			resMngr;

	
	@PostConstruct
	private void initialize()
	{
		this.resMngr = new ResourceManager();
	}
	
	public ResourceManager getResMngr()
	{
		return resMngr;
	}
	
	@PreDestroy
	private void cleanup()
	{
		this.resMngr.cleanupAll();
	}
	
	public class ResourceManager implements ActionListener
	{
		private static final int											TIMEOUT	= 30 * 1000;
		protected Timer														timer	= new Timer(TIMEOUT, this);
		public Map<WorkflowResources.WF_RES, ArrayList<WorkflowResource>>	res		= new HashMap<>();
		
		public ResourceManager()
		{
			timer.setCoalesce(false);
			
			if(!timer.isRunning())
				timer.start();
		}
		
		synchronized public void actionPerformed(java.awt.event.ActionEvent e)
		{
			if(timer.isRunning())
			{
				timer.stop();
				this.cleanupAll();
			}
		}
		
		public void keepAlive()
		{
			if(timer.isRunning())
				timer.restart();
		}
		
		public void disableTimeout()
		{
			timer.stop();
		}
		
		public void restartTimeout()
		{
			timer.restart();
		}
		
		synchronized  public void register(WorkflowResources.WF_RES key, WorkflowResource wres)
		{
			ArrayList<WorkflowResource> list = res.get(key);
			
			if(list == null)
			{
				list = new ArrayList<WorkflowResource>();
				res.put(key, list);
			}
			
			list.add(wres);
		}
		
		synchronized public void cleanupResource(WorkflowResource wres)
		{	
			for(List<WorkflowResource> list: res.values())
				if(list.remove(wres))
				{
					wres.cleanup();
					return;
				}
			
			LOG.warning("requested resource not found in the list of registered resources");
		}
		
		synchronized  public void cleanupResources(WorkflowResources.WF_RES key)
		{
			ArrayList<WorkflowResource> list = res.remove(key);

			if(list == null)
				return;

			for(WorkflowResource wres : list)
				wres.cleanup();
		}

		synchronized public void cleanupAll()
		{
			if(timer.isRunning())
				timer.stop();
			
			for(ArrayList<WorkflowResource> resources: res.values())
				for(WorkflowResource resource: resources)
					resource.cleanup();
			
			res.clear();
			this.restartTimeout();
		}

		public boolean hasResource(WF_RES emuComp)
		{
			return res.containsKey(emuComp);
		}
	}
}
