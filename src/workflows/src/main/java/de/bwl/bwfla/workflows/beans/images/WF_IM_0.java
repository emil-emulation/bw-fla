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
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;

import de.bwl.bwfla.common.datatypes.EmulationEnvironment;
import de.bwl.bwfla.common.datatypes.Html5Options;
import de.bwl.bwfla.common.datatypes.UiOptions;
import de.bwl.bwfla.common.datatypes.utils.EmulationEnvironmentHelper;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.EmulatorUtils;
import de.bwl.bwfla.common.utils.SystemEnvironmentHelper;
import de.bwl.bwfla.workflows.beans.common.BwflaFormBean;
import de.bwl.bwfla.workflows.beans.common.RemoteEmulatorHelper;
import de.bwl.bwfla.workflows.beans.common.WorkflowResources;
import de.bwl.bwfla.workflows.beans.images.WF_IM_data.Storage;
import de.bwl.bwfla.workflows.catalogdata.SystemEnvironmentDescription;
import de.bwl.bwfla.workflows.conf.WorkflowSingleton;

@ManagedBean
@ViewScoped
public class WF_IM_0 extends BwflaFormBean implements Serializable {
	private static final long serialVersionUID = -2323912800593361682L;
	
	 
	private List<String> imagesList;
	private String selectedImage;
	private String config;
	private SystemEnvironmentHelper envHelper = null;
	private String selectedEnv;
	private boolean crt;
	private boolean network;
	private boolean relMouse;
	private String name;
	private List<EmulationEnvironment> templates = null;
	private boolean requirePrefs = false;
	
	@Inject
	private WF_IM_data wfData;
	protected Storage storage;

	private boolean configureBean()
	{	
		envHelper = WorkflowSingleton.envHelper;
		return true;
	}
	
	@Override
	
	public void initialize()
	{
		super.initialize();
		storage = wfData.getStorage();
		storage.externalImageCOW = true;
		if(!configureBean())
			return;
	}

	private EmulationEnvironment getTemplate(String id)
	{
		if(templates == null)
			return null;
		
		for(EmulationEnvironment e : templates)
			if(e.getId().equals(id)) 
				return e.copy();
		
		return null;
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
		EmulationEnvironment emuEnv = getTemplate(selectedEnv);
		
		if(emuEnv == null)
		{
			log.severe("selected template not found... bug");
			return "";
		}
		
		emuEnv.getDescription().setTitle(name);
		EmulationEnvironment.NativeConfig nativeConf = new EmulationEnvironment.NativeConfig();
		nativeConf.setValue(config);
		nativeConf.setLinebreak(emuEnv.getNativeConfig().getLinebreak());
		emuEnv.setNativeConfig(nativeConf);
	
		UiOptions ui = emuEnv.getUiOptions();
		if(ui == null) 
		{
			ui = new UiOptions();
			emuEnv.setUiOptions(ui);
		}
		
		Html5Options ops = ui.getHtml5();
		if(ops == null) 
		{
			ops = new Html5Options();
			ui.setHtml5(ops);
		}
		
		if(crt) ops.setCrt("yes");
		if(relMouse) ops.setPointerLock(relMouse);
		
		String envConf = emuEnv.toString();
		if(envConf == null)
		{
			log.info("failed to marshall env");
			return "";
		}
		
		if (selectedImage != null && !selectedImage.equals("")) {
			try {
				wfData.getStorage().env =  envHelper.registerImageTemp(envConf, selectedImage);
			} catch (BWFLAException e) {
				panic("Creating runtime config failed: " + e.getMessage(), e);
			}
			wfData.getStorage().image = selectedImage;
		} else {
			try {
				wfData.getStorage().env = EmulationEnvironmentHelper.setMainHdRef(
						envConf, this.getExternalImageUrl(),
						this.getExternalImageCOW());
			} catch (BWFLAException e) {
				panic("Creating runtime config failed: " + e.getMessage(), e);
			}
				wfData.getStorage().image = null;
		}
		if (wfData.getStorage().env == null)
			panic("invalid environment");
		
		
		wfData.getStorage().emuHelper = new RemoteEmulatorHelper(wfData.getStorage().env);
		if(wfData.getStorage().emuHelper == null)
		{
			panic("could init emulator");
			return "";
		}
		wfData.getStorage().config = wfData.getStorage().env.toString();
		if(wfData.getStorage().emuHelper.requiresUserPrefs())
		{
			System.out.println("requires userprefs");
			requirePrefs = true;
			if(!this.isDidUserSetPrefs())
			{
				return "";
			}
			
			setUserPreferences(wfData.getStorage().emuHelper.getEmulationEnvironment());
		}
		
		wfData.getStorage().emuHelper.initialize();
		this.resourceManager.register(WorkflowResources.WF_RES.EMU_COMP, wfData.getStorage().emuHelper);
		wfData.getStorage().description = new SystemEnvironmentDescription(wfData.getStorage().env.toString());
		wfData.getStorage().envHelper = envHelper;
		
		return "/pages/workflow-images/WF_IM_2.xhtml?faces-redirect=true";
	}

	public List<String> getImagesList() {
		if(imagesList == null)
		{
			try {
				imagesList = envHelper.getIncomingImagesList();
			} catch (BWFLAException e) {
				panic("failed fetching incoming images: " + e.getMessage(), e);
			}
			// System.out.println("images " + imagesList.size());
		}
		return imagesList;
	}

	public void setImagesList(List<String> imagesList) {
		this.imagesList = imagesList;
	}

	public String getSelectedImage() {
		return selectedImage;
	}

	/**
	 * @param selectedEmulatorId
	 *            the selectedEmulatorId to set
	 */
	public void setSelectedImage(String sb) {
		this.selectedImage = sb;
	}

	@Override
	public String forward()
	{
		return "/pages/start.xhtml";
	}

	public String getConfig() {
		EmulationEnvironment env = getTemplate(selectedEnv);
		if(env == null || env.getNativeConfig() == null)
			return null;
		return env.getNativeConfig().getValue();
	}

	public void setConfig(String config) {
		this.config = config;
	}
	
	public void setSelectedTemplate(String env)
	{
		this.selectedEnv = env;
	}
	
	public String getSelectedTemplate()
	{
		return this.selectedEnv;
	}
	
	public List<EmulationEnvironment> getTemplates()
	{
		if(templates == null)
		{
			try {
				templates = envHelper.getTemplates();
			} catch (BWFLAException e) {
				panic(e.getMessage());
			}
			if(templates.size() > 0)
				selectedEnv = templates.get(0).getId();
		}
		return templates;
	}

	public boolean isCrt() {
		return crt;
	}

	public void setCrt(boolean crt) {
		this.crt = crt;
	}

	public boolean isNetwork() {
		return network;
	}

	public void setNetwork(boolean network) {
		this.network = network;
	}

	public boolean isRelMouse() {
		return relMouse;
	}

	public void setRelMouse(boolean relMouse) {
		this.relMouse = relMouse;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
