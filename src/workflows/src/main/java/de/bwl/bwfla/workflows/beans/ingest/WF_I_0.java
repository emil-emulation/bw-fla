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

package de.bwl.bwfla.workflows.beans.ingest;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import com.google.common.base.Strings;

import de.bwl.bwfla.classification.FileTypeHistogram;
import de.bwl.bwfla.classification.ImageClassificationHelper;
import de.bwl.bwfla.classification.ImageClassificationHelper.EmuEnvType;
import de.bwl.bwfla.common.datatypes.Drive.DriveType;
import de.bwl.bwfla.common.datatypes.Environment;
import de.bwl.bwfla.common.datatypes.FileCollection;
import de.bwl.bwfla.common.datatypes.FileCollectionEntry;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.EmulatorUtils;
import de.bwl.bwfla.common.utils.EmulatorUtils.XmountOutputFormat;
import de.bwl.bwfla.common.utils.ObjectArchiveHelper;
import de.bwl.bwfla.common.utils.Pair;
import de.bwl.bwfla.common.utils.SystemEnvironmentHelper;
import de.bwl.bwfla.workflows.beans.common.BwflaFormBean;
import de.bwl.bwfla.workflows.beans.common.UINotify;
import de.bwl.bwfla.workflows.beans.common.WorkflowResources;
import de.bwl.bwfla.workflows.beans.ingest.WF_I_data.ExternalDataModel;
import de.bwl.bwfla.workflows.catalogdata.Description;
import de.bwl.bwfla.workflows.catalogdata.DescriptionTypes;
import de.bwl.bwfla.workflows.catalogdata.MetaDataFacade;
import de.bwl.bwfla.workflows.catalogdata.ObjectEnvironmentDescription;
import de.bwl.bwfla.workflows.catalogdata.ObjectEvaluationDescription;
import de.bwl.bwfla.workflows.conf.WorkflowSingleton;

@ManagedBean
@ViewScoped
public class WF_I_0 extends BwflaFormBean implements Serializable {
	private static final long serialVersionUID = -1233913120593361682L;

	@Inject
	private WF_I_data wfData;
	private List<Pair<String, String>> platforms = new ArrayList<Pair<String, String>>();

	private List<String> isos = new ArrayList<String>();
	private List<String> archivesList = new ArrayList<String>();

	private String chosenObject = null;
	private String chosenArchive = null;

	private SystemEnvironmentHelper envHelper = null;
	private ObjectArchiveHelper objHelper = null;
	private WF_I_data.Storage storage;

	private List<String> beanList;
	private String selectedBean;

	List<Environment> beanEnvironments = new ArrayList<Environment>();
	List<Environment> derivates = new ArrayList<Environment>();
	List<Environment> systems = new ArrayList<Environment>();
	private String selectedEnv; // by uuid
	private boolean requirePrefs = false;

	// TODO: this method almost should be moved to the WF_I_API bean
	private boolean configureBean()
	{
		String metaDataDir = WorkflowSingleton.CONF.metaDir;
		if(metaDataDir == null)
		{
			log.info("properties are invalid: missing isodir and metadata dir");
			panic("Workflow is not configured");
			return false;
		}

		envHelper = WorkflowSingleton.envHelper;
		objHelper = WorkflowSingleton.objHelper;

		archivesList.add("---");
		try {
			if(objHelper == null)
			{
				panic("obj Archive not available");
			}
			List<String> a = objHelper.getArchives();
			if(a == null)
			{
				panic("no archives found");
			}
			archivesList.addAll(a);
		} catch (BWFLAException e) {
			panic("Workflow configuration issue: ", e);
		}


		wfData.getStorage().mdArchive = new MetaDataFacade(metaDataDir);
		if (wfData.getStorage().mdArchive == null)
			panic("mdArchive is null : " + metaDataDir);

		return true;
	}

	@Override
	public void initialize() {
		super.initialize();
		storage = wfData.getStorage();

		if (!configureBean())
			return;

		try {
			this.setBeanList(envHelper.getBeanList());
		} catch (BWFLAException e) {
			panic(e.getMessage());
		}
		this.selectedBean = null;
		this.storage.externalImageCOW = true;
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

	private boolean reloadConfigFromJson() {
		if (chosenObject == null || chosenObject.isEmpty())
			return false;
		if (wfData == null)
			return false;

		if (wfData.getStorage().mdArchive == null)
			return false;

		// FIXME: we should not make hard connections to file name patterns
		Description d = wfData.getStorage().mdArchive
				.getDescriptor(chosenObject);
		if (d == null)
			return false;

		DescriptionTypes.TYPE t = d.getDescriptionType();
		if (t == DescriptionTypes.TYPE.SYSTEM)
			return false;

		ObjectEnvironmentDescription o = (ObjectEnvironmentDescription) d;
		ExternalDataModel res = storage.extData;

		res.setAuthor(o.getAuthor());
		res.setTitle(o.getTitle());
		res.setYear(o.getYear());

		return true;
	}

	private void reloadData() {
		if (wfData == null)
			return;

		if (wfData.getStorage().mdArchive == null)
			return;

		isos.clear();
		if (chosenArchive != null) {
			try {
				isos = objHelper.getObjectList(chosenArchive);
			} catch (BWFLAException e) {
				panic("objArchive not working: ", e);
			}
		}
		reloadConfigFromJson();
		storage.oldThumbnail = wfData.getStorage().mdArchive
				.thumbURL(chosenObject);
	}

	public String navigate() throws MalformedURLException, URISyntaxException {
		ObjectEvaluationDescription d = null;

		if (!Strings.isNullOrEmpty(this.storage.externalImageUrl)) {
			if (Strings.isNullOrEmpty(selectedEnv)) {
				List<EmuEnvType> result = ImageClassificationHelper
						.classify(this.storage.externalImageUrl);

				String resStr = "";
				for (EmuEnvType t : result)
					resStr += t + " ";

				log.info(chosenObject + " : " + resStr);

				if(result.size() > 1)
				{
					UINotify.info("found multiple matching environments: " + resStr);
				}

				String autodetectedBaseimageId = ImageClassificationHelper
						.getEnvironmentForEmuEnvType(result);

				if (autodetectedBaseimageId != null) {
					selectedEnv = autodetectedBaseimageId;
				}
			}

			if(Strings.isNullOrEmpty(selectedEnv))
			{ 
				UINotify.error("Could not autodetect a suitable environment for object. Please choose one yourself.");
				return "";
			}
			d = new ObjectEvaluationDescription(envHelper, selectedEnv, 
					null, null, this.storage.externalImageUrl, DriveType.CDROM);
		} else {
			DriveType type = null;
			if (chosenArchive == null || Strings.isNullOrEmpty(chosenObject)) {
				UINotify.warn("You have to select an archive and object or provide an object URL!");
				return "";
			}

			FileCollection fc = null;
			try {
				fc = objHelper.getObjectReference(chosenArchive, chosenObject);
			} catch (BWFLAException e) {
				UINotify.warn("failed loading object information: " + e.getMessage());
				return "";
			}

			FileCollectionEntry fce = fc.files.get(0);
			type = fce.getType();

			if (Strings.isNullOrEmpty(selectedEnv)) {
				UINotify.info("No Environment selected! Trying autodetect.");

				List<EmuEnvType> result = ImageClassificationHelper
						.classify(fc);

				String resStr = "";
				for (EmuEnvType t : result)
					resStr += t + " ";

				log.info(chosenObject + " : " + resStr);

				if(result.size() > 1)
				{
					UINotify.info("found multiple matching environments: " + resStr);
				}

				String autodetectedBaseimageId = ImageClassificationHelper
						.getEnvironmentForEmuEnvType(result);

				if (autodetectedBaseimageId != null) {
					selectedEnv = autodetectedBaseimageId;
				} else {
					UINotify.error("Could not autodetect a suitable environment for object. Please choose one yourself.");
					return "";
				}
			}

			log.info("using environment " + selectedEnv + " for object "
					+ chosenObject + " type " + type );

			d = new ObjectEvaluationDescription(
					envHelper, selectedEnv, WorkflowSingleton.CONF.objectArchive, chosenArchive, chosenObject, type);
			// chosenArchive.getObjectReference(chosenObject));
		}


		storage.description = d;
		d.setAuthor(storage.extData.author);
		d.setTitle(storage.extData.title);
		d.setYear(storage.extData.year);
		try {
			storage.emuHelper = d.getEmulatorHelper();
		} catch (BWFLAException e) {
			panic(e.getMessage());
		}
		if (storage.emuHelper == null) {
			log.severe("could not create emulator helper");
			return "";
		}

		if(storage.emuHelper.requiresUserPrefs())
		{
			System.out.println("requires userprefs");
			requirePrefs = true;
			if(!this.isDidUserSetPrefs())
			{
				return "";
			}
			
			setUserPreferences(storage.emuHelper.getEmulationEnvironment());
		}
		
		storage.emuHelper.initialize();
		this.resourceManager.register(WorkflowResources.WF_RES.EMU_COMP,
				storage.emuHelper);

		return "/pages/workflow-ingest/WF_I_2.xhtml?faces-redirect=true";
	}

	public List<String> getIsos() {
		if (isos == null || isos.isEmpty()) {
			reloadData();
		}
		return isos;
	}

	public ExternalDataModel getConfigbean() {
		return storage.extData;
	}

	public List<Pair<String, String>> getPlatforms() {
		return platforms;
	}

	public String getChosenArchive() {
		if (chosenArchive != null)
			return chosenArchive;
		return null;
	}

	public void setChosenArchive(String a) {
		this.chosenObject = null;
		this.isos.clear();
		this.chosenArchive = a;
		reloadData();
	}

	public String getChosenObject() {
		return chosenObject;
	}

	public void setChosenObject(String chosenObject) {
		this.chosenObject = chosenObject;
	}

	@Override
	public String forward() {
		return "/pages/start.xhtml";
	}

	public List<String> getArchives() {
		return archivesList;
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

	public String getExternalImageUrl() {
		return this.wfData.getStorage().externalImageUrl;
	}

	public void setExternalImageUrl(String imageUrl) {
		this.wfData.getStorage().externalImageUrl = imageUrl;
	}

	public boolean getExternalImageCOW() {
		return this.wfData.getStorage().externalImageCOW;
	}

	public void setExternalImageCOW(boolean cow) {
		this.wfData.getStorage().externalImageCOW = cow;
	}

	public boolean isRequirePrefs()
	{
		return requirePrefs;
	}
	
}
