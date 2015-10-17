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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;

import com.google.common.base.Strings;

import de.bwl.bwfla.classification.FileTypeHistogram;
import de.bwl.bwfla.classification.ImageClassificationHelper;
import de.bwl.bwfla.classification.ImageClassificationHelper.EmuEnvType;
import de.bwl.bwfla.common.datatypes.Environment;
import de.bwl.bwfla.common.exceptions.BWFLAException;
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
import de.bwl.bwfla.workflows.objectarchive.DigitalObjectArchive;
import de.bwl.bwfla.workflows.objectarchive.DigitalObjectArchiveFactory;


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
	private DigitalObjectArchive chosenArchive = null;

	private SystemEnvironmentHelper envHelper = null;
	private WF_I_data.Storage storage;

	private List<String> beanList;
	private String selectedBean;

	List<Environment> beanEnvironments = new ArrayList<Environment>();
	List<Environment> derivates = new ArrayList<Environment>();
	List<Environment> systems = new ArrayList<Environment>();
	private String selectedEnv; // by uuid

	// TODO: this method almost should be moved to the WF_I_API bean
	private boolean configureBean()
	{
		String archiveDir = WorkflowSingleton.CONF.objDir;
		String metaDataDir = WorkflowSingleton.CONF.metaDir;
		String imageArchiveHost = WorkflowSingleton.CONF.archiveGw;
		if(archiveDir == null || metaDataDir == null || imageArchiveHost == null)
		{
			log.info("properties are invalid: missing isodir and metadata dir");
			panic("please fix bwfla_workflows.xml");
			return false;
		}

		envHelper = WorkflowSingleton.envHelper;
		wfData.getStorage().objectArchives = DigitalObjectArchiveFactory.createFromJson(new File(archiveDir));

		archivesList.add("---");
		for (DigitalObjectArchive a : wfData.getStorage().objectArchives) {
			archivesList.add(a.getName());
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
			isos = chosenArchive.getObjectList();
			log.info("found " + isos.size() + " isos");
			List<String> uniqueList = new ArrayList<String>(
					new HashSet<String>(isos));
			java.util.Collections.sort(uniqueList);
			isos = uniqueList;
		}
		reloadConfigFromJson();
		storage.oldThumbnail = wfData.getStorage().mdArchive
				.thumbURL(chosenObject);
	}

	public void characterize() {
		if (chosenArchive == null)
			return;

		if (chosenObject != null) {
			Path path = chosenArchive.getFileReference(chosenObject).toPath();
			List<EmuEnvType> result = ImageClassificationHelper.classify(path);

			String resStr = "";
			for (EmuEnvType t : result)
				resStr += t + " ";

			log.info(chosenObject + " : " + resStr);

		} else {

			String csv = "";
			Map<String, Integer> envs = new HashMap<String, Integer>();
			FileTypeHistogram h = new FileTypeHistogram(1024);
			for (String isoid : isos) {
				Path path = chosenArchive.getFileReference(isoid).toPath();
				long startTime = System.currentTimeMillis();
				List<EmuEnvType> result = new ArrayList<EmuEnvType>();// ImageClassificationHelper.classify(path);
				long duration = System.currentTimeMillis() - startTime;
				String resStr = "";
				for (EmuEnvType t : result) {
					resStr += t + " ";
					if (envs.containsKey(t.name()))
						envs.put(t.name(), new Integer(envs.get(t.name())
								.intValue() + 1));
					else
						envs.put(t.name(), new Integer(1));
				}
				csv += isoid + ";" + resStr + ";" + duration / 1000.0 + "\n";
				// log.info(chosenObject + " : " + resStr);

				h.append(ImageClassificationHelper.classifyHist(path));
			}

			for (Map.Entry<String, Integer> entry : envs.entrySet())
				log.info(entry.getKey() + " " + entry.getValue());

			log.info("");

			List<Pair<String, Integer>> puidl = h.getOrderedPUIDList();
			for (Pair<String, Integer> p : puidl)
				log.info(p.getA() + " " + p.getB());

			log.info("");

			Path outpath = new File("/tmp/" + chosenArchive + ".csv").toPath();
			try {
				BufferedWriter writer = Files.newBufferedWriter(outpath,
						StandardCharsets.UTF_8);
				writer.write(csv);
				writer.flush();
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			log.info(csv);
		}

	}

	public String navigate() throws MalformedURLException, URISyntaxException {
		ObjectEvaluationDescription d = null;
		
		if (!Strings.isNullOrEmpty(this.storage.externalImageUrl)) {
			if (Strings.isNullOrEmpty(selectedEnv)) {
				UINotify.warn("Cannot characterize remote images. Please choose a render environment yourself.");
			} else {
				d = new ObjectEvaluationDescription(envHelper, selectedEnv,
						"iso", this.storage.externalImageUrl);
			}
		} else {
			if (Strings.isNullOrEmpty(selectedEnv)) {
				UINotify.info("No Environment selected! Trying autodetect.");

				Path path = chosenArchive.getFileReference(chosenObject)
						.toPath();
				List<EmuEnvType> result = ImageClassificationHelper
						.classify(path);

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
					+ chosenObject);
			
			if(chosenArchive == null)
			{
				UINotify.warn("please choose an archive");
				return "";
			}
			
			d = new ObjectEvaluationDescription(
					envHelper, selectedEnv, chosenObject,
					chosenArchive.getObjectReference(chosenObject));
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
		storage.emuHelper.initialize();
		if (wfData.getStorage().emuHelper.isOutOfResources())
			panic("please try later, no free resources found");

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
			return chosenArchive.getName();
		return null;
	}

	public void setChosenArchive(String a) {
		this.chosenObject = null;
		this.chosenArchive = null;
		this.isos.clear();
		for (DigitalObjectArchive archive : wfData.getStorage().objectArchives) {
			if (!archive.getName().equals(a))
				continue;

			this.chosenArchive = archive;
			reloadData();
		}
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

}
