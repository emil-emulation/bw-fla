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

package de.bwl.bwfla.workflows.beans.access;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import de.bwl.bwfla.workflows.api.GlobalBean;
import de.bwl.bwfla.workflows.beans.common.BwflaEmulatorViewBean;



@ManagedBean
@ViewScoped
public class WF_A_API extends BwflaEmulatorViewBean
{	
	private static final long	serialVersionUID	= 6659922656559351223L;
	
	@EJB
	private GlobalBean	globalBean;
	
	@Inject
	private WF_A_data	wfData;
	
	// XXX: compile fix... needs to be refactored
	/*
	public void prepareFacadeAndRedirect(ComponentSystemEvent event)
	{
		try
		{
			String sessId = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("accessSessId");
			if(sessId == null)
				panic("workflow triggered programmatically but the passed session identifier is null");
			
			AccessSession accessSession = globalBean.getAccessSession(sessId);
			if(accessSession == null)
				panic("no session was found corresponding to the passed session identifier");
			
			File isoFile = globalBean.getAccessSession(sessId).getCdLocation();
			if(isoFile == null)
				panic("a directory with the uploaded object was not found on the server");
			
			File metadata = globalBean.getAccessSession(sessId).getMetadata();
			if(metadata == null)
				panic("a directory with the uploaded object was not found on the server");
			
			wfData.getStorage().doArchive = null;
			wfData.getStorage().mdArchive = new MetaDataFacade(metadata.getParentFile().getParent(), DescriptionTypes.TYPE.EVALUATION);
			wfData.getStorage().chosenDescriptor = wfData.getStorage().mdArchive.getDescriptors().get(0);
			
			wfData.getStorage().emuHelper = wfData.getStorage().chosenDescriptor.getEmulatorHelper(); 
			wfData.getStorage().emuHelper.initialize();
		}
		catch(Throwable t)
		{
			t.printStackTrace();
			panic("internal error occured, session, detailed log is on the server side");
		}
		
		String urlString = "/pages/workflow-access/WF_A_2.xhtml" + "?" + WorkflowTypes.WF_TYPE.class.getSimpleName() + "=" + WorkflowTypes.WF_TYPE.ACCESS_LA + "&faces-redirect=true";
		ConfigurableNavigationHandler nav = (ConfigurableNavigationHandler) FacesContext.getCurrentInstance().getApplication().getNavigationHandler();
		nav.performNavigation(urlString);	
	}
	*/
	

	@Override
	public String forward()
	{
		return "";
	}	
}
