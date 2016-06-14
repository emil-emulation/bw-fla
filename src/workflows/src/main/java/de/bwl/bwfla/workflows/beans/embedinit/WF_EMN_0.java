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

package de.bwl.bwfla.workflows.beans.embedinit;

import java.io.IOException;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import de.bwl.bwfla.workflows.beans.common.BwflaEmulatorViewBean;
import de.bwl.bwfla.workflows.beans.common.WorkflowResources;

@ManagedBean
@ViewScoped
public class WF_EMN_0 extends BwflaEmulatorViewBean 
{
	private static final long serialVersionUID = 825673104605199469L;

	@Inject
	private WF_EMN_data wfData;
	
	
	@Override
	public void initialize()
	{	
		if (jsf.isPostback())
			return;
		
		final ExternalContext extcontext = FacesContext.getCurrentInstance().getExternalContext();
		final Map<String, String> parameters = extcontext.getRequestParameterMap();
		
		final String sessid = parameters.get("sessionId");
		if (sessid == null || sessid.isEmpty())
			this.panic("No user-session ID was found!");
		
		final String sessok = parameters.get("sessionOk");
		if (sessok == null || !sessok.contentEquals("1")) {
			// Reconstruct the original URL
			final StringBuilder url = new StringBuilder(512);
			url.append("/faces/pages/workflow-embedinit/WF_EMN_0.xhtml?");
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
				wfData.getStorage().initialURL = url.toString();
				extcontext.redirect(url.toString());
				return;
			} 
			catch (IOException exception) {
				exception.printStackTrace();
				this.panic("Unable to perform redirection in order to obtain user session");
			}
		}
		
		this.emuHelper = new SimpleRemoteEmulatorHelper(sessid);
		if(emuHelper.requiresUserPrefs())
		{
			if(!this.isDidUserSetPrefs()) {
				System.out.println("requires userprefs");
				try {
					extcontext.redirect("/faces/pages/workflow-embedinit/WF_EMN_prefs.xhtml");
					return;
				} catch (IOException e) {
					this.panic("failed redirecting to user pref site", e);
				}
			}
			setUserPreferences(emuHelper.getEmulationEnvironment());
		}
		super.initialize();
		emuHelper.initialize();
		
		resourceManager.register(WorkflowResources.WF_RES.EMU_COMP, this.emuHelper);
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
