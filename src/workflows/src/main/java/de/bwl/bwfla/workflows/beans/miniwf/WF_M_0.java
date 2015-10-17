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
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;

import de.bwl.bwfla.common.datatypes.Environment;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.SystemEnvironmentHelper;
import de.bwl.bwfla.workflows.beans.common.BwflaFormBean;
import de.bwl.bwfla.workflows.beans.miniwf.WF_M_data.Storage;
import de.bwl.bwfla.workflows.catalogdata.SystemEnvironmentDescription;
import de.bwl.bwfla.workflows.conf.WorkflowSingleton;

@ManagedBean
@ViewScoped
public class WF_M_0 extends BwflaFormBean implements Serializable {
	private static final long serialVersionUID = -2323912800593361682L;
	private List<String> beanList;
	private String selectedBean;

	List<Environment> beanEnvironments = new ArrayList<Environment>();
	List<Environment> derivates = new ArrayList<Environment>();
	List<Environment> systems = new ArrayList<Environment>();
	private String selectedEnv; // by uuid
	private SystemEnvironmentHelper envHelper = null;

	@Inject
	private WF_M_data wfData;
	protected Storage storage;

	@Override
	public void initialize()
	{			
		super.initialize();
		storage = wfData.getStorage();
		envHelper = WorkflowSingleton.envHelper;
		if(envHelper == null)
		{
			panic("Workflow module is not configured properly");
		}

		try {
			this.setBeanList(envHelper.getBeanList());
		} catch (BWFLAException e) {
			panic(e.getMessage());
		}
		this.selectedBean = null;
	}

	/**
	 * To load os names for selected emulator.
	 */
	public void loadEnvList() 
	{
		try {
			beanEnvironments = envHelper.getBaseImagesByBean(selectedBean);
			derivates = envHelper.getDerivateImagesByBean(selectedBean);
			systems = envHelper.getSystemImagesByBean(selectedBean);
		} catch (BWFLAException e) {
			panic(e.getMessage());
		}
		this.setEnvironmentList(beanEnvironments);
	}

	/**
	 * To navigate to next page. before going to next page instantiate remote
	 * emulator Class with emulator bean name and configuration file.
	 * 
	 * @return
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	public String navigate() throws MalformedURLException, URISyntaxException 
	{	
		SystemEnvironmentDescription _d = new SystemEnvironmentDescription(envHelper, selectedEnv);
		try {
			storage.emuHelper = _d.getEmulatorHelper();
		} catch (BWFLAException e) {
			panic(e.getMessage());
		}
		storage.description = _d;
		if(storage.emuHelper == null)
		{
			panic("could not create emulator helper");
		}
		
		return "/pages/workflow-miniwf/WF_M_1.xhtml?faces-redirect=true";
	}
	
	/**
	 * @return the emulatorList
	 */
	public List<String> getBeanList() {
		return beanList;
	}

	/**
	 * @param emulatorList
	 *            the emulatorList to set
	 */
	public void setBeanList(List<String> beanList) {
		this.beanList = beanList;
	}

	/**
	 * @return the selectedEmulatorId
	 */
	public String getSelectedBean() {
		return selectedBean;
	}

	/**
	 * @param selectedEmulatorId
	 *            the selectedEmulatorId to set
	 */
	public void setSelectedBean(String sb) {
		this.selectedBean = sb;
	}

	/**
	 * @return the environmentList
	 */
	public List<Environment> getEnvironmentList() {
		return beanEnvironments;
	}
	
	public List<Environment> getDerivatesList() {
		return derivates;
	}
	
	public List<Environment> getSystemsList() {
		return systems;
	}

	/**
	 * @param environmentList
	 *            the environmentList to set
	 */
	public void setEnvironmentList(List<Environment> environmentList) {
		this.beanEnvironments = environmentList;
	}
	
	public void setDerivatesList(List<Environment> environmentList) {
		this.derivates = environmentList;
	}
	
	public void setSystemsList(List<Environment> environmentList) {
		this.systems = environmentList;
	}

	/**
	 * @return the selectedOs
	 */
	public String getSelectedEnv() {
		return selectedEnv;
	}

	/**
	 * @param selectedOs
	 *            the selectedOs to set
	 */
	public void setSelectedEnv(String uuid) {
		this.selectedEnv = uuid;
	}

	@Override
	public String forward()
	{
		return "/pages/start.xhtml";
	}
}
