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

package de.bwl.bwfla.workflows.beans.miniwf;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.workflows.beans.common.BwflaEmulatorViewBean;
import de.bwl.bwfla.workflows.beans.common.WorkflowResources;
import de.bwl.bwfla.workflows.beans.miniwf.WF_M_data.Storage;
import de.bwl.bwfla.workflows.conf.WorkflowSingleton;

@ManagedBean
@ViewScoped
public class WF_M_2 extends BwflaEmulatorViewBean implements Serializable
{
	private static final long	serialVersionUID	= -2323912800593361682L;
	
	@Inject private WF_M_data	wfData;
	protected Storage			storage;
	private String 				derivateName;
	private String 				imageArchiveHost;
	
	public String forward()
	{
		storage.emuHelper.stop();
		return "/pages/workflow-miniwf/WF_M_3.xhtml?faces-redirect=true";
	}
	
	@Override
	public void initialize()
	{
		super.initialize();
		if(resourceManager.hasResource(WorkflowResources.WF_RES.EMU_COMP))
			return;
		
		storage = wfData.getStorage();
		
		this.storage.emuHelper.initialize();
		resourceManager.register(WorkflowResources.WF_RES.EMU_COMP, storage.emuHelper);

		emuHelper = storage.emuHelper;
		description = wfData.getStorage().description;
		imageArchiveHost = WorkflowSingleton.CONF.archiveGw;
	}
	
	public String save()
	{
		if(derivateName.trim().isEmpty())
			return "";
		
		try {
			storage.emuHelper.saveEnvironment(imageArchiveHost, derivateName, "derivate");
		} catch (BWFLAException e) {
			panic("Failed saving derivate: " + e.getMessage(), e);
		}
		return "/pages/workflow-miniwf/WF_M_3.xhtml?faces-redirect=true";
	}
	
	public String getDerivateName() {
		return derivateName;
	}

	public void setDerivateName(String derivateName) {
		this.derivateName = derivateName;
	}
}
