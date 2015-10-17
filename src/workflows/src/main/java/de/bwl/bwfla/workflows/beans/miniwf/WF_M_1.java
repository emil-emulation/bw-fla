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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.net.URL;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import de.bwl.bwfla.common.datatypes.Drive;
import de.bwl.bwfla.workflows.beans.common.BwflaFileAttachBean;
import de.bwl.bwfla.workflows.beans.common.UINotify;
import de.bwl.bwfla.workflows.beans.common.WFPanicException;
import de.bwl.bwfla.workflows.beans.miniwf.WF_M_data.Storage;
import de.bwl.bwfla.workflows.conf.WorkflowSingleton;
import de.bwl.bwfla.workflows.softwarearchive.SoftwareArchive;
import de.bwl.bwfla.workflows.softwarearchive.SoftwareDescription;
import de.bwl.bwfla.workflows.softwarearchive.datatypes.BundledFile;


@ManagedBean(name = "WF_M_1")
@ViewScoped
public class WF_M_1 extends BwflaFileAttachBean implements Serializable {
	private static final long serialVersionUID = -2323912800593361682L;

	@Inject
	private WF_M_data wfData;
	protected Storage storage;

	private SoftwareDescription selectedSoftware;

	@Override
	public void initialize() {
		super.initialize();
		storage = wfData.getStorage();
	}

	public List<String> getDevices() 
	{
		if(storage == null || storage.emuHelper == null)
			return null;

		if(imageDevices == null)
			imageDevices = storage.emuHelper.getImageDevices();
		
		if(helperDevices == null)
			helperDevices = storage.emuHelper.getHelperDevices();
			
		return super.getDevices();
	}

	public String forward()
	{
		if (this.selectedSoftware != null) {
			log.info("using selected software");
			try {
				log.info(selectedSoftware.value());
			} catch (JAXBException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			String swArchiveIndex = WorkflowSingleton.CONF.swArchive;
			if(swArchiveIndex == null)
			{
				log.info("properties are invalid: missing swArchive config file path");
				throw new WFPanicException("please fix bwfla_workflows.xml");
			}

			try {
				SoftwareArchive softwareArchive = SoftwareArchive.fromFile(new File(swArchiveIndex));
				URL bundleUrl = softwareArchive.getSoftwareBundleUrl(this.selectedSoftware);
				
				BundledFile file = this.selectedSoftware.getFiles().get(0);
				this.storage.emuHelper.addBundledFile(bundleUrl, file.getPath(), Drive.DriveType.valueOf(file.getType().name().toUpperCase()));
				
			} catch (FileNotFoundException | JAXBException e) {
				UINotify.error("Software archive not available");
				log.severe("Software archive not available");
			//	e.printStackTrace();
				return "";
			} catch (IndexOutOfBoundsException e) {
				UINotify.error("Software contains no bundled media files.");
				return "";
			}

		} else {
			storage.emuHelper.setFilesToInject(uploadedFiles);
		}
		return "/pages/workflow-miniwf/WF_M_2.xhtml?faces-redirect=true";
	}

	public String navigateBack() {
		return "/pages/workflow-miniwf/WF_M_0.xhtml?faces-redirect=true";
	}

	public SoftwareDescription getSelectedSoftware() {
		return selectedSoftware;
	}

	public void setSelectedSoftware(SoftwareDescription selectedSoftware) {
		this.selectedSoftware = selectedSoftware;
	}
}
