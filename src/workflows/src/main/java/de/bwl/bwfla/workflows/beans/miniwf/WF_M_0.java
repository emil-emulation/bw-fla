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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PreDestroy;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import de.bwl.bwfla.common.datatypes.EmulationEnvironment;
import de.bwl.bwfla.common.datatypes.Environment;
import de.bwl.bwfla.common.datatypes.Html5Options;
import de.bwl.bwfla.common.datatypes.InputOptions;
import de.bwl.bwfla.common.datatypes.UiOptions;
import de.bwl.bwfla.common.datatypes.utils.EmulationEnvironmentHelper;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.EmulatorUtils;
import de.bwl.bwfla.common.utils.ProcessRunner;
import de.bwl.bwfla.common.utils.SystemEnvironmentHelper;
import de.bwl.bwfla.common.utils.EmulatorUtils.XmountOutputFormat;
import de.bwl.bwfla.workflows.beans.common.BwflaFormBean;
import de.bwl.bwfla.workflows.beans.common.UINotify;
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
	private boolean requirePrefs = false; 
	
	List<File> downloadDirs = new ArrayList<File>();

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
	
		EmulationEnvironment env = storage.emuHelper.getEmulationEnvironment();
		if(storage.emuHelper.requiresUserPrefs())
		{
			System.out.println("requires userprefs");
			requirePrefs = true;
			if(!this.isDidUserSetPrefs())
			{
				return "";
			}
			
			setUserPreferences(env);
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
	 * the environmentList to set
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
	
	public boolean isRequirePrefs()
	{
		return requirePrefs;
	}
	
	public StreamedContent getImageFile() 
	{
		if(selectedEnv == null)
		{
			UINotify.error("Select an environment first!");
			return null;
		}
		
		Environment env;
		try {
			env = envHelper.getPlatformById(selectedEnv);
		} catch (BWFLAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			UINotify.error("Failed loading environment!");
			return null;
		}
		
		if(env == null)
		{
			UINotify.error("Failed loading environment!");
			return null;
		}
		
		if(!(env instanceof EmulationEnvironment))
			return null;
		
		String ref = EmulationEnvironmentHelper.getMainHddRef((EmulationEnvironment) env);
		log.info("download ref: " + ref);
		
		Set<PosixFilePermission> permissions = new HashSet<>();
		permissions.add(PosixFilePermission.OWNER_READ);
		permissions.add(PosixFilePermission.OWNER_WRITE);
		permissions.add(PosixFilePermission.OWNER_EXECUTE);
		permissions.add(PosixFilePermission.GROUP_READ);
		permissions.add(PosixFilePermission.GROUP_WRITE);
		permissions.add(PosixFilePermission.GROUP_EXECUTE);
		File tempDir;
		try {
			tempDir = Files.createTempDirectory("", PosixFilePermissions.asFileAttribute(permissions)).toFile();
		} catch (IOException e1) {
			e1.printStackTrace();
			return null;
		}
		Path cowPath = tempDir.toPath().resolve("download.cow");
		try {
			EmulatorUtils.createCowFile(ref, cowPath);
		} catch (BWFLAException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
		Path fuseMountpoint = cowPath
                .resolveSibling(cowPath.getFileName() + ".fuse");
		
		File exportFile = null;
		try {
            exportFile = EmulatorUtils.mountCowFile(cowPath, fuseMountpoint).toFile();
        } catch (IOException | IllegalArgumentException | BWFLAException e) {
            e.printStackTrace();
            return null;
        }
		
		downloadDirs.add(tempDir);
		InputStream input = null;
		try {
			input = new FileInputStream(exportFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return new DefaultStreamedContent(input, "octet-stream", env.getId());
    }
	
	public StreamedContent getImageMetaData() 
	{
		if(selectedEnv == null)
		{
			UINotify.error("Select an environment first!");
			return null;
		}
		
		Environment env;
		try {
			env = envHelper.getPlatformById(selectedEnv);
		} catch (BWFLAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			UINotify.error("Failed loading environment!");
			return null;
		}
		
		if(env == null)
		{
			UINotify.error("Failed loading environment!");
			return null;
		}
		
		InputStream stream = new ByteArrayInputStream(env.toString().getBytes(StandardCharsets.UTF_8));
		return new DefaultStreamedContent(stream, "application/xml", env.getId() + ".xml");
	}
	
	@Override
	public void cleanup() 
	{ 	
		for (File dir : downloadDirs)
		{
			Path cowPath = dir.toPath().resolve("download.cow");	
			Path fuseMountpoint = cowPath
	                .resolveSibling(cowPath.getFileName() + ".fuse");
			try {
				EmulatorUtils.unmountFuse(fuseMountpoint.toFile());
				FileUtils.deleteDirectory(dir);
			} catch (BWFLAException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
}
