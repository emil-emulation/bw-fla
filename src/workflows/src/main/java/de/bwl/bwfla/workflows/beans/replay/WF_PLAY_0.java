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

package de.bwl.bwfla.workflows.beans.replay;

import java.io.Serializable;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.bwl.bwfla.common.datatypes.Environment;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.jaxwsstubs.imagearchive.IwdMetaData;
import de.bwl.bwfla.common.utils.SystemEnvironmentHelper;
import de.bwl.bwfla.workflows.beans.common.BwflaFormBean;
import de.bwl.bwfla.workflows.beans.common.RemoteEmulatorHelper;
import de.bwl.bwfla.workflows.beans.common.WorkflowResources;
import de.bwl.bwfla.workflows.conf.WorkflowSingleton;


@ManagedBean
@ViewScoped
public class WF_PLAY_0 extends BwflaFormBean implements Serializable
{
	/** Logger instance. */
	private final Logger log = LoggerFactory.getLogger(WF_PLAY_0.class);
	
	// Injected members
	@Inject private WF_PLAY_Data wfdata;
	
	private List<String> emulators;
	private List<Environment> environments;
	private List<IwdMetaData> traces;
	
	private String selectedBean;
	private String selectedEnv;
	private String selectedTrace;
	
	private SystemEnvironmentHelper envHelper;

	private boolean configureBean()
	{	
		String imageArchiveHost = WorkflowSingleton.CONF.archiveGw;
		if(imageArchiveHost == null)
		{
			log.info("imageArchiveHost property not set");
			return false;
		}
		envHelper = WorkflowSingleton.envHelper;
		return true;
	}
	
	private IwdMetaData getTraceById(String id)
	{
		if(selectedTrace == null || traces == null)
			return null;
		log.info("looking for trace " + id);
		for (IwdMetaData trace : traces)
		{
			if(trace.getUuid().equals(id))
				return trace;
		}
		return null;
	}
	
	@Override
	public void initialize()
	{
		super.initialize();
		
		if(!configureBean())
			return;
		
		wfdata.setSystemEnvironmentHelper(envHelper);
	}

	/** Load available image names for selected emulator. */
	public void loadAvailableImages()
	{
		try {
			this.setEnvironmentList(envHelper.getBaseImagesByBean(selectedBean));
		} catch (BWFLAException e) {
			panic(e.getMessage());
		}
	}

	/** Load available trace-names for selected emulator and os-image. */
	public void loadAvailableTraces()
	{
		try {
			traces = envHelper.getRecordings(selectedEnv);
		} catch (BWFLAException e) {
			panic("failed loading recordings: " + e.getMessage(), e);
		}
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
		this.selectedTrace = null;
		this.selectedEnv = null;
	}

	/** Returns the list of environments. */
	public List<Environment> getEnvironmentList() {
		return environments;
	}

	/** Set the environment list. */
	public void setEnvironmentList(List<Environment> envlist) {
		environments = envlist;
	}

	public String getSelectedEnv() {
		return selectedEnv;
	}

	public void setSelectedEnv(String image) {
		selectedTrace = null;
		selectedEnv = image;
	}

	/** Returns the list of traces. */
	public List<IwdMetaData> getTraceList() {
		return traces;
	}
	
	/** Returns the ID of currently selected trace. */
	public String getSelectedTrace() {
		return selectedTrace;
	}

	/** Set the ID of currently selected trace. */
	public void setSelectedTrace(String trace) {
		selectedTrace = trace;
	}
	
	public String getSelectedTraceDescription()
	{
		IwdMetaData trace;
		if((trace = getTraceById(selectedTrace)) != null)
			return trace.getDescription();
		log.info("no description available for " + selectedTrace);
		return "";
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
		
		wfdata.setRemoteEmulatorHelper(new RemoteEmulatorHelper(env));
		if(wfdata.getRemoteEmulatorHelper() == null)
			return "";
		
		RemoteEmulatorHelper emuhelper = wfdata.getRemoteEmulatorHelper();
		emuhelper.initialize();
		
		if(emuhelper.isOutOfResources())
			panic("please try later, no free resources found");
		
		resourceManager.register(WorkflowResources.WF_RES.EMU_COMP, emuhelper);
		
		wfdata.setEmulatorEnvId(selectedEnv);
		IwdMetaData trace = getTraceById(selectedTrace);
		if(trace == null)
			return "";
		wfdata.setTrace(trace);
		
		wfdata.setRemoteSessionPlayer(null);
		
		// Construct the next page's URL
		return WF_PLAY_Data.getPageUrl(1, true);
	}
	
	private static final long serialVersionUID = 395956308923258955L;
}
