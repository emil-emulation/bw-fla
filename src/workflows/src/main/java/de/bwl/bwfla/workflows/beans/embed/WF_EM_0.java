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
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import de.bwl.bwfla.common.datatypes.EmulationEnvironment;
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
	private boolean				sessionInitialised	= false;
	
	@Inject
	private WF_EM_data wfData;

	@Override
	public void observeReadiness(ActionEvent event) throws BWFLAException
	{
		if(sessionInitialised)
			super.observeReadiness(event);
		else
			log.warning("will not monitor state, remote emulator helper has to be initialized first");
	}
	
	@Override
	public void initialize()
	{	
		if(jsf.isPostback())
			return;
		
		final ExternalContext extcontext = FacesContext.getCurrentInstance().getExternalContext();
		final Map<String, String> parameters = extcontext.getRequestParameterMap();
		String urlPrefix = null;
		
		if(wfData.getStorage().initialURL == null)
		{
			StringBuffer uri = ((HttpServletRequest) (extcontext.getRequest())).getRequestURL();
			String query =  ((HttpServletRequest) (extcontext.getRequest())).getQueryString();
			wfData.getStorage().initialURL = uri.toString() + "?" + query;
			System.out.println("original url: " + wfData.getStorage().initialURL);
		}
		
		final String traceid = parameters.get("traceid");
		if (traceid != null && !traceid.isEmpty())
			urlPrefix = "/faces/pages/workflow-embed/WF_EM_replay.xhtml?";
		else {
			final String sessok = parameters.get("sessionOk");
			if (sessok == null || !sessok.contentEquals("1")) 
				urlPrefix = "/faces/pages/workflow-embed/WF_EM_0.xhtml?";
		}
		
		if (urlPrefix != null) {
			// Reconstruct the original URL
			final StringBuilder url = new StringBuilder(1024);
			url.append(urlPrefix);
			
			// ... append all parameters
			for (Map.Entry<String, String> parameter : parameters.entrySet()) {
				url.append(parameter.getKey());
				url.append('=');
				url.append(parameter.getValue());
				url.append('&');
			}
			url.append("sessionOk=1&faces-redirect=true");
			
			try {
				// Redirect to obtain user-session
				jsf.getExternalContext().getSession(true);
				log.info("Redirect to:  " + url.toString());
				wfData.getStorage().initialURL = url.toString();
				extcontext.redirect(url.toString());
				return;
			} 
			catch (IOException exception) {
				exception.printStackTrace();
				this.panic("Unable to perform redirection in order to obtain user session");
			}
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
		
		EmulationEnvironment env = emuHelper.getEmulationEnvironment();
		if(emuHelper.requiresUserPrefs())
		{
			if(!this.isDidUserSetPrefs()) {
				System.out.println("requires userprefs");
				try {
					extcontext.redirect("/faces/pages/workflow-embed/WF_EM_prefs.xhtml");
					return;
				} catch (IOException e) {
					this.panic("failed redirecting to user pref site", e);
				}
			}
			setUserPreferences(env);
		}
		
		super.initialize();
		try
		{
			this.resourceManager.register(WorkflowResources.WF_RES.EMU_COMP, this.emuHelper);
			this.resourceManager.disableTimeout();
			this.emuHelper.initialize();
		}
		finally
		{
			this.resourceManager.restartTimeout();
		}
		
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
