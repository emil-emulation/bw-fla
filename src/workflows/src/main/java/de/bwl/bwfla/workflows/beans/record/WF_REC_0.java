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

package de.bwl.bwfla.workflows.beans.record;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;

import de.bwl.bwfla.common.datatypes.Environment;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.SystemEnvironmentHelper;
import de.bwl.bwfla.workflows.beans.common.BwflaFormBean;
import de.bwl.bwfla.workflows.beans.common.RemoteEmulatorHelper;
import de.bwl.bwfla.workflows.beans.common.WorkflowResources;
import de.bwl.bwfla.workflows.conf.WorkflowSingleton;


@ManagedBean
@ViewScoped
public class WF_REC_0 extends BwflaFormBean implements Serializable
{
	// Injected members
	@Inject private WF_REC_Data wfdata;
	
	// Member fields
	private List<String> emulators;
//	private List<Environment> environments;
	List<Environment> beanEnvironments = new ArrayList<Environment>();
	List<Environment> derivates = new ArrayList<Environment>();
	List<Environment> systems = new ArrayList<Environment>();
	
	private String selectedBean;
	private String selectedEnv;
	
	private SystemEnvironmentHelper envHelper;

	private boolean configureBean()
	{	
		envHelper = WorkflowSingleton.envHelper;
		return true;
	}
	
	@Override
	public void initialize()
	{
		super.initialize();
		if(!configureBean())
			return;
	}

	/** Load available image names for selected emulator. */
	public void loadAvailableImages() {
			try {
				beanEnvironments = envHelper.getBaseImagesByBean(selectedBean);
				derivates = envHelper.getDerivateImagesByBean(selectedBean);
				systems = envHelper.getSystemImagesByBean(selectedBean);
				
			} catch (BWFLAException e) {
				panic(e.getMessage());
			}
		//	this.setEnvironmentList(this.beanEnvironments);
	}

	/** Returns the list of all available emulators. */
	public List<String> getEmulatorList() {
		if(emulators == null)
			try {
				this.emulators = envHelper.getBeanList();
			} catch (BWFLAException e) {
				panic(e.getMessage());
			}
		return emulators;
	}

	/** Set the list of available emulators. */
	public void setEmulatorList(List<String> emulist) {
		emulators = emulist;
	}

	
	public String getSelectedBean() {
		return selectedBean;
	}

	public void setSelectedBean(String selectedBean) {
		this.selectedBean = selectedBean;
	}

	/** Returns the list of environments. */
	public List<Environment> getEnvironmentList() {
		return beanEnvironments;
	}
	
	public List<Environment> getDerivatesList() {
		return derivates;
	}
	
	public List<Environment> getSystemsList() {
		return systems;
	}

	/** Set the environment list. */
	public void setEnvironmentList(List<Environment> envlist) {
		beanEnvironments = envlist;
	}
	
	public void setDerivatesList(List<Environment> environmentList) {
		this.derivates = environmentList;
	}
	
	public void setSystemsList(List<Environment> environmentList) {
		this.systems = environmentList;
	}

	public String getSelectedEnv() {
		return selectedEnv;
	}

	public void setSelectedEnv(String image) {
		selectedEnv = image;
	}

	@Override
	public String forward()
	{
		Environment env = null;
		try {
			env = envHelper.getPlatformById(selectedEnv);
		} catch (BWFLAException e) {
			panic(e.getMessage());
		}
		if (env == null)
			panic("invalid environment");
		
		RemoteEmulatorHelper emuHelper = new RemoteEmulatorHelper(env);
		emuHelper.initialize();
		this.resourceManager.register(WorkflowResources.WF_RES.EMU_COMP, emuHelper);
		
		wfdata.setSystemEnvironmentHelper(envHelper);
		wfdata.setRemoteEmulatorHelper(emuHelper);
		wfdata.setEmulatorEnvId(selectedEnv);
		wfdata.setRemoteSessionRecorder(null);
		
		// Construct the next page's URL
		return WF_REC_Data.getPageUrl(1, true);
	}

	
	/* =============== Internal Stuff =============== */
	
	private static final long serialVersionUID = -3426491753333380378L;
}
