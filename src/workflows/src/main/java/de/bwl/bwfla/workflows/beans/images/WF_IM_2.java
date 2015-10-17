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

package de.bwl.bwfla.workflows.beans.images;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.workflows.beans.common.BwflaEmulatorViewBean;
import de.bwl.bwfla.workflows.beans.common.WorkflowResources;

@ManagedBean
@ViewScoped
public class WF_IM_2 extends BwflaEmulatorViewBean implements Serializable
{

	@Inject private WF_IM_data				wfData;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public String navigate()
	{
		if( wfData.getStorage().image != null)
			wfData.getStorage().envHelper.releaseTempImage(wfData.getStorage().description.getId(), wfData.getStorage().image);
		
		try {
			wfData.getStorage().envHelper.registerImage(wfData.getStorage().config, wfData.getStorage().image, false, "system");
		} catch (BWFLAException e) {
			panic("failed register image: " + e.getMessage(), e);
		}
		
		return "/pages/start.xhtml?faces-redirect=true";
	}
	
	
	@Override
	public void initialize()
	{
		super.initialize();
		emuHelper = wfData.getStorage().emuHelper;
		description = wfData.getStorage().description;
	}
	
	@Override
	public void cleanup()
	{
		resourceManager.cleanupResources(WorkflowResources.WF_RES.EMU_COMP);
		super.cleanup();
	}

	@Override
	public String forward() {
		// TODO Auto-generated method stub
		return null;
	}
}
