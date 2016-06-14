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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;

import org.apache.commons.io.FilenameUtils;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.Pair;
import de.bwl.bwfla.workflows.beans.common.BwflaFormBean;
import de.bwl.bwfla.workflows.beans.common.WorkflowResources;
import de.bwl.bwfla.workflows.beans.common.WorkflowsFile;
import de.bwl.bwfla.workflows.beans.miniwf.WF_M_data.Storage;



@ManagedBean
@ViewScoped
public class WF_M_3 extends BwflaFormBean implements Serializable
{
	private static final long	serialVersionUID	= -1255237582477237647L;

	@Inject
	private WF_M_data					wfData;
	protected Storage					storage;
	
	private List<Pair<WorkflowsFile, String>>	files;

	@Override
	public void initialize()
	{
		super.initialize();
		
		this.storage = wfData.getStorage();
		
		resourceManager.disableTimeout();
		try {
			this.files = storage.emuHelper.getMediaManager().detachAndDownloadContainers();
		} catch (BWFLAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		resourceManager.restartTimeout();
		
		for(Pair<WorkflowsFile, String> file: files)
			resourceManager.register(WorkflowResources.WF_RES.FILE, file.getA());
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
		return "/pages/start.xhtml";
	}
}