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
import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import de.bwl.bwfla.workflows.conf.WorkflowSingleton;


@ManagedBean
@ViewScoped
public class StartBean extends BwflaFormBean implements Serializable {
	private static final long serialVersionUID = 1064181213466955483L;

	/**
	 * To check if all configurations are set in Workflows.xml. If any value is
	 * empty then user will be redirected to Configurations page.
	 * 
	 * @param event
	 */
	public void isConfigSet() 
	{		
		FacesContext fc = FacesContext.getCurrentInstance();	
		if(!WorkflowSingleton.confValid)
		{
			ConfigurableNavigationHandler nav = (ConfigurableNavigationHandler) fc.getApplication().getNavigationHandler();
			nav.performNavigation("/pages/workflow-conf/WF_CONF_0.xhtml?faces-redirect=true");
		}
	}
	
	@Override
	protected void initialize()
	{
		super.initialize();
		resources.getResMngr().cleanupAll();
		isConfigSet();
	}
	
	public String gotoIngestDO() {
		return "/pages/workflow-ingest/WF_I_0.xhtml?WF_TYPE=INGEST_BWFLA_OBJECTS&faces-redirect=true";
	}

	public String gotoIngestDigArt() {
		return "/pages/workflow-ingest/WF_I_0.xhtml?WF_TYPE=INGEST_HFG&faces-redirect=true";
	}

	public String gotoAccessDO() {
		return "/pages/workflow-access/WF_A_0.xhtml?WF_TYPE=ACCESS_BWFLA_OBJECTS&faces-redirect=true";
	}

	public String gotoAccessDigArt() {
		return "/pages/workflow-access/WF_A_0.xhtml?WF_TYPE=ACCESS_HFG&faces-redirect=true";
	}

	public String gotoAccessSysenv() {
		return "/pages/workflow-access/WF_A_0.xhtml?WF_TYPE=ACCESS_BWFLA_SYSTEMS&faces-redirect=true";
	}

	public String gotoBaseImages() {
		return "/pages/workflow-miniwf/WF_M_0.xhtml?faces-redirect=true";
	}

	public String gotoMameMess() {
		return "/pages/workflow-mamemess/WF_MESS_0.xhtml?faces-redirect=true";
	}

	public String gotoMigration() {
		return "/pages/workflow-migration/WF_MG_0.xhtml?faces-redirect=true";
	}

	public String gotoConfiguration() {
		return "/pages/workflow-conf/WF_CONF_0.xhtml?faces-redirect=true";
	}

	public String gotoShibboleth() {
		return "/pages/workflow-shibboleth/DiscoveryService.xhtml?faces-redirect=true";
	}

	public String gotoNewSWPackage() {
		return "/pages/workflow-newswpackage/WF_SWI_0.xhtml?faces-redirect=true";
	}
	
	public String gotoSessionRecording()
	{
		return "/pages/workflow-record/WF_REC_0.xhtml?faces-redirect=true";
	}
	
	public String gotoSessionReplay()
	{
		return "/pages/workflow-replay/WF_PLAY_0.xhtml?faces-redirect=true";
	}
	public String gotoNewImage() {
		return "/pages/workflow-images/WF_IM_0.xhtml?faces-redirect=true";
	}

	public String gotoSWArchive() {
		return "/pages/workflow-archive/WF_I_SW_start.xhtml?faces-redirect=true";
	}
	
	public String gotoIngestByo() {
		return "/pages/workflow-ingest-ext/WF_IE_0.xhtml?faces-redirect=true";
	}
	
	@Override
	public String forward()
	{
		return null;
	}
}