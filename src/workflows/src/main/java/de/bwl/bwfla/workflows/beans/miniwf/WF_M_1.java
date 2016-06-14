package de.bwl.bwfla.workflows.beans.miniwf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
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

	@Inject private WF_M_data wfData;
	protected Storage storage;
	private SoftwareDescription selectedSoftware = null;
	

	@Override
	public void initialize() {
		super.initialize();
		storage = wfData.getStorage();
	}

	public List<String> getDevices() 
	{
		if(storage == null || storage.emuHelper == null)
			return null;

		if(imageDevices == null) {
			imageDevices = storage.emuHelper.getMediaManager().getImageDevices();
		}
		
		if(helperDevices == null)
			helperDevices = storage.emuHelper.getMediaManager().getHelperDevices();
			
		return super.getDevices();
	}

	public String forward()
	{
		if (selectedSoftware != null) {
			String swArchiveIndex = WorkflowSingleton.CONF.swArchive;
			if(swArchiveIndex == null)
				throw new WFPanicException("Properties are invalid: Missing swArchive config file path. Please fix bwfla_workflows.xml");

			try {
				SoftwareArchive softwareArchive = SoftwareArchive.fromFile(new File(swArchiveIndex));
				URL bundleUrl = softwareArchive.getSoftwareBundleUrl(selectedSoftware);
				storage.emuHelper.getMediaManager().attachSoftwarePackage(selectedSoftware, bundleUrl);
			} catch (FileNotFoundException | JAXBException e) {
				UINotify.error("Software archive not available");
				log.severe("Software archive not available");
				return "";
			} 
		} else {
			storage.emuHelper.getMediaManager().setFilesToInject(uploadedFiles);
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
