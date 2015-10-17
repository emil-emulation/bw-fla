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

package de.bwl.bwfla.workflows.beans.ingest_ext;


import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.commons.io.FileUtils;

import de.bwl.bwfla.common.datatypes.Environment;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.EmulatorUtils;
import de.bwl.bwfla.common.utils.Pair;
import de.bwl.bwfla.common.utils.ProcessRunner;
import de.bwl.bwfla.common.utils.SystemEnvironmentHelper;
import de.bwl.bwfla.workflows.beans.common.BwflaFormBean;
import de.bwl.bwfla.workflows.beans.common.WorkflowResources;
import de.bwl.bwfla.workflows.beans.ingest.WF_I_data.ExternalDataModel;
import de.bwl.bwfla.classification.FileTypeHistogram;
import de.bwl.bwfla.classification.FitsClassifier;
import de.bwl.bwfla.classification.ImageClassificationHelper;
import de.bwl.bwfla.classification.ImageClassificationHelper.EmuEnvType;
import de.bwl.bwfla.workflows.catalogdata.Description;
import de.bwl.bwfla.workflows.catalogdata.DescriptionTypes;
import de.bwl.bwfla.workflows.catalogdata.MetaDataFacade;
import de.bwl.bwfla.workflows.catalogdata.ObjectEnvironmentDescription;
import de.bwl.bwfla.workflows.catalogdata.ObjectEvaluationDescription;
import de.bwl.bwfla.workflows.conf.WorkflowSingleton;
import de.bwl.bwfla.workflows.objectarchive.DigitalObjectArchive;
import de.bwl.bwfla.workflows.objectarchive.DigitalObjectArchiveFactory;
import de.bwl.bwfla.workflows.objectarchive.DigitalObjectFileArchive;


@ManagedBean
@ViewScoped
public class WF_IE_0 extends BwflaFormBean implements Serializable
{
	private static final long			serialVersionUID	= -1233913120593361682L;

	@Inject
	private WF_IE_data					wfData;
	private List<Pair<String, String>>	platforms			= new ArrayList<Pair<String, String>>();

	private SystemEnvironmentHelper 	envHelper = null;
	private WF_IE_data.Storage 			storage; 
	private String 						isolink = null;

	private List<String> beanList;
	private String selectedBean;

	List<Environment> beanEnvironments = new ArrayList<Environment>();
	List<Environment> derivates = new ArrayList<Environment>();
	List<Environment> systems = new ArrayList<Environment>();
	private String selectedEnv; // by uuid

	// TODO: this method almost should be moved to the WF_I_API bean
	private boolean configureBean()
	{
		
		String imageArchiveHost = WorkflowSingleton.CONF.archiveGw;
		if(imageArchiveHost == null)
		{
			log.info("properties are invalid: missing isodir and metadata dir");
			panic("please fix bwfla_workflows.xml");
			return false;
		}

		envHelper = new SystemEnvironmentHelper(imageArchiveHost);

		return true;
	}


	@Override
	public void initialize()
	{
		super.initialize();
		storage = wfData.getStorage();

		if(!configureBean())
			return;

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
	public void loadEnvList() {
		try {
			beanEnvironments = envHelper.getBaseImagesByBean(selectedBean);
			derivates = envHelper.getDerivateImagesByBean(selectedBean);
			systems = envHelper.getSystemImagesByBean(selectedBean);
		} catch (BWFLAException e) {
			panic(e.getMessage());
		}
		this.setEnvironmentList(beanEnvironments);
	}

	public String navigate() throws MalformedURLException, URISyntaxException
	{
		ObjectEnvironmentDescription d = new ObjectEnvironmentDescription(envHelper, selectedEnv, "iso", isolink);

		storage.description = d;
	
		try {
			storage.emuHelper = d.getEmulatorHelper();
		} catch (BWFLAException e) {
			panic(e.getMessage());
		}
		if(storage.emuHelper == null)
		{
			log.severe("could not create emulator helper");
			return "";
		}
		storage.emuHelper.initialize();
		if(wfData.getStorage().emuHelper.isOutOfResources())
			panic("please try later, no free resources found");

		this.resourceManager.register(WorkflowResources.WF_RES.EMU_COMP, storage.emuHelper);

		return "/pages/workflow-ingest-ext/WF_IE_2.xhtml?faces-redirect=true";
	}

	public List<Pair<String, String>> getPlatforms()
	{
		return platforms;
	}


	@Override
	public String forward()
	{
		return "/pages/start.xhtml";
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
	
	public void setSystemsList(List<Environment> systems) {
		this.derivates = systems;
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
	
	public String getIsolink() {
		return isolink;
	}
	
	public void setIsolink(String isolink) {
		this.isolink = isolink;
	}
	
}
