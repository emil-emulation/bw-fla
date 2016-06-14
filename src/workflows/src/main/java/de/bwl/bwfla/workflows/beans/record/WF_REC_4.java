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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;

import org.apache.commons.io.FilenameUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.Pair;
import de.bwl.bwfla.workflows.beans.common.BwflaFormBean;
import de.bwl.bwfla.workflows.beans.common.WorkflowResources;
import de.bwl.bwfla.workflows.beans.common.WorkflowsFile;


@ManagedBean
@ViewScoped
public class WF_REC_4 extends BwflaFormBean implements Serializable
{
	private static final long serialVersionUID = 6176375630130995190L;
	@Inject private WF_REC_Data	wfData;	
	private List<Pair<WorkflowsFile, String>>	files;

	@Override
	public void initialize()
	{
		super.initialize();
		try {
			this.files = wfData.getRemoteEmulatorHelper().getMediaManager().detachAndDownloadContainers();
		} catch (BWFLAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for(Pair<WorkflowsFile, String> file: files)
			resourceManager.register(WorkflowResources.WF_RES.FILE, file.getA());
		
		final String citationLink = wfData.getCitationLink();
		if (citationLink != null && !citationLink.isEmpty()) {
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Your Citation-Link", citationLink);
			RequestContext.getCurrentInstance().showMessageInDialog(message);
		}
	}

	public List<StreamedContent> getFiles()
	{
		List<StreamedContent> result = new ArrayList<>();
		
		if(files != null)
			for(Pair<WorkflowsFile, String> file: files)
				try
				{
					result.add(new DefaultStreamedContent(new FileInputStream(file.getA()), FilenameUtils.getExtension(file.getB()), file.getB()));
				}
				catch(FileNotFoundException e)
				{
					e.printStackTrace();
				}
				
		return result;
	}
	
	@Override
	public String forward()
	{
		return WF_REC_Data.getStartPageUrl();
	}
}