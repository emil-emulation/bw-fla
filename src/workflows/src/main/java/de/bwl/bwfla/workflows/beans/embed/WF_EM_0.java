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

package de.bwl.bwfla.workflows.beans.embed;

import java.io.IOException;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.workflows.beans.common.BwflaEmulatorViewBean;
import de.bwl.bwfla.workflows.beans.common.CitationUrlHelper;
import de.bwl.bwfla.workflows.beans.common.WorkflowResources;
import de.bwl.bwfla.workflows.catalogdata.Description;



@ManagedBean
@ViewScoped
public class WF_EM_0 extends BwflaEmulatorViewBean 
{
	private static final long	serialVersionUID	= -2323912800593361682L;
	private boolean				sessionObtained		= false;
	private boolean				sessionInitialised	= false;

	@Override
	public void monitorState(ActionEvent event)
	{
		if(sessionInitialised)
			super.monitorState(event);
		else
			log.warning("will not monitor state, remote emulator helper has to be initialized first");
	}
	
	@Override
	public void initialize()
	{	
		if(jsf.isPostback())
			return;
		
		super.initialize();
		
		String paramString = "";
		for(Map.Entry<String, String> param:  FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().entrySet())
		{
			paramString += param.getKey() + "=" + param.getValue() + "&";
			if(param.getKey().equals("sessionOk"))
				sessionObtained = true;
		}
				
		if(!sessionObtained)
			try 
			{
				jsf.getExternalContext().getSession(true);
				FacesContext.getCurrentInstance().getExternalContext().redirect("/faces/pages/workflow-embed/WF_EM_0.xhtml?" + paramString + "sessionOk=1&faces-redirect=true");
				return;
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
				panic("unable to perform redirection in order to obtain user session");
			}
		
		Description	desc = CitationUrlHelper.getDescription(jsf);
		if(desc == null)
			panic("user has specified wrong environment description, check passed URL parameter string");
		
		try {
			this.emuHelper = desc.getEmulatorHelper();
		} catch (BWFLAException e) {
			panic(e.getMessage());
		}
		if(this.emuHelper == null)
			panic("emulator helper could not be created for the corresponding platform");
			
		try
		{	
			this.resourceManager.disableTimeout();
			this.emuHelper.initialize();
		}
		finally
		{
			this.resourceManager.restartTimeout();
		}
		
		this.resourceManager.register(WorkflowResources.WF_RES.EMU_COMP, this.emuHelper);
		this.sessionInitialised = true;
	}

	@Override
	public String forward()
	{
		return "";
	}

	@Override
	public boolean isAutostart()
	{
		return true;
	}
	
	@Override
	public boolean isInsideIFrame()
	{
		return true;
	}
}
