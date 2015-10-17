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

import java.io.Serializable;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.WindowContext;
import de.bwl.bwfla.workflows.beans.common.WorkflowResources.ResourceManager;



public abstract class BwflaFormBean implements Serializable
{
	private static final long	serialVersionUID	= 5120189956975251734L;
	protected final Logger		log					= Logger.getLogger(this.getClass().getName());
	
	@Inject
	protected UserSession		session;
	
	@Inject
	protected FacesContext		jsf;
	
	@Inject
	protected WindowContext		winCtx;
	
	@Inject
	protected WorkflowResources resources; 
	
	protected ResourceManager resourceManager; 
	
	public String getWindowId()
	{
		return winCtx.getId();
	}
	
	synchronized public void ping(javax.faces.event.ActionEvent e)
	{
		resourceManager.keepAlive();
	}

	@PostConstruct
	synchronized protected void initialize()
	{
		resourceManager = resources.getResMngr();
	}

	protected void panic(String msg)
	{
		WFPanicException ex = new WFPanicException(msg);
		throw ex;
	}
	
	protected void panic(String msg, Throwable throwable)
	{
		throwable.printStackTrace();
		throw new WFPanicException(msg, throwable);
	}
	
	@PreDestroy
	public void cleanup() { }
	
	abstract public String forward();
}
