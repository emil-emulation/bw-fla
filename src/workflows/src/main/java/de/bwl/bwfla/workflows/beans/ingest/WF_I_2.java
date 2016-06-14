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

import java.io.File;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.Pair;
import de.bwl.bwfla.common.utils.SystemEnvironmentHelper;
import de.bwl.bwfla.workflows.beans.common.BwflaEmulatorViewBean;
import de.bwl.bwfla.workflows.beans.common.ImageUtils;
import de.bwl.bwfla.workflows.beans.common.WorkflowResources;
import de.bwl.bwfla.workflows.catalogdata.ObjectEvaluationDescription;
import de.bwl.bwfla.workflows.conf.WorkflowSingleton;

@ManagedBean
@ViewScoped
public class WF_I_2 extends BwflaEmulatorViewBean 
{
	@Inject private WF_I_data wfData;
	private WF_I_data.Storage storage;

	SystemEnvironmentHelper env = WorkflowSingleton.envHelper;	
	private boolean keepEnvironment = false;
	String imageArchiveHost;
	
	public boolean isKeepEnvironment() {
		return keepEnvironment;
	}

	public void setKeepEnvironment(boolean keepEnvironment) 
	{
		this.keepEnvironment = keepEnvironment;
	}
	
	@Override
	public String forward()
	{	
		if(keepEnvironment)
		{
			SystemEnvironmentHelper dstArchive = WorkflowSingleton.envHelper;
			String uuid = null;
			try {
				uuid = storage.emuHelper.saveEnvironment(imageArchiveHost, "WF_I_2 generated env", "user");
			} catch (BWFLAException e) {
				panic("Failed saving custom environment: " + e.getMessage(), e);
			}
			log.info("got new id " + uuid);
			storage.description.updateEmulationEnvironmentId(dstArchive, uuid);
		}
		storage.screenshotFile = this.screenshot;
		return "/pages/workflow-ingest/WF_I_6.xhtml?faces-redirect=true";
	}
	
	@Override
	public void initialize() 
	{
		storage = wfData.getStorage();
		emuHelper = storage.emuHelper;
		imageArchiveHost = WorkflowSingleton.CONF.imageArchive;
		super.initialize();
	}
	
	public String getPictureURL()
	{		
		try
		{
			if(this.screenshot != null)
			{
				storage.screenshot = ImageUtils.publishImage(this.screenshot, true);
				return storage.screenshot;
			}
				
			if(storage.oldThumbnail != null)
				return ImageUtils.publishImage(new File(storage.oldThumbnail), true); 
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return "/faces/javax.faces.resource/images/dummy-cdrom.jpg";
	}
	
	public ObjectEvaluationDescription getDescription() {
		return storage.description;
	}
	
	public List<Pair<Integer, String>> getResolutions() {
		return SystemEnvironmentHelper.getAvailableResolutions();
	}

	public List<Pair<Integer, String>> getColordepths() {
		return SystemEnvironmentHelper.getAvailableColorDepths();
	}
	
	@Override
	public void cleanup()
	{
		resourceManager.cleanupResources(WorkflowResources.WF_RES.EMU_COMP);
		super.cleanup();
	}
}
