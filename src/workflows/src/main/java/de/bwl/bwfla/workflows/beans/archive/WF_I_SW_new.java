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

package de.bwl.bwfla.workflows.beans.archive;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import org.primefaces.event.TransferEvent;
import org.primefaces.model.DualListModel;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.Pair;
import de.bwl.bwfla.workflows.beans.common.BwflaFormBean;
import de.bwl.bwfla.workflows.beans.common.UINotify;
import de.bwl.bwfla.workflows.beans.common.WFPanicException;
import de.bwl.bwfla.workflows.conf.WorkflowSingleton;
import de.bwl.bwfla.workflows.softwarearchive.SoftwareArchive;
import de.bwl.bwfla.workflows.softwarearchive.SoftwareDescription;
import de.bwl.bwfla.workflows.softwarearchive.datatypes.BundledFile;
import de.bwl.bwfla.workflows.softwarearchive.datatypes.MediumType;
import de.bwl.bwfla.workflows.softwarearchive.datatypes.SoftwareBundle;

@ManagedBean(name = "WF_I_SW_new")
@ViewScoped
public class WF_I_SW_new extends BwflaFormBean implements Serializable {
	private static final long serialVersionUID = 580894899792866817L;

	@Inject
	private WF_I_SW_data wfData;

	private SoftwareDescription software;
	private String selectedPlatform = null;
	private DualListModel<BundledFile> files;

	// protected final Path SOURCE_PATH = Paths.get(SoftwareArchive.);
	
	private boolean configureBean() {
		String swArchiveIndex = WorkflowSingleton.CONF.swArchive;
		if (swArchiveIndex == null) {
			log.info("properties are invalid: missing swArchive config file path");
			panic("please fix bwfla_workflows.xml");
			return false;
		}

		try {
			wfData.getStorage().softwareArchive = SoftwareArchive
					.fromFile(new File(swArchiveIndex));
		} catch (FileNotFoundException | JAXBException e) {
			log.severe("file: " + swArchiveIndex + " not found");
			return false;
		}

		return true;
	}

	@Override
	public void initialize() {
		super.initialize();
		configureBean();

		software = wfData.getStorage().selectedSoftware;
		if (software == null) {
			software = new SoftwareDescription();
		}

		List<BundledFile> filesSource = new ArrayList<BundledFile>();

		// TODO This is empty now, but later should be pre filled with existing
		// data
		// List<File> filesTarget = software.getFiles();
		if (this.software.getSoftwareBundle() == null) {
			this.software.setSoftwareBundle(new SoftwareBundle());
		}

		
		files = new DualListModel<BundledFile>(filesSource,
				this.software.getSoftwareBundle().files);

		// Add an empty record, so the users knows where to add them
//		if (this.software.getMetadata_fmt().isEmpty()) {
//			this.software.getMetadata_fmt().add(
//					new Pair<String, String>("native", ""));
//		}
	}

	public void addMetadata_fmt() {
//		this.software.getMetadata_fmt().add(
//				new Pair<String, String>("native", ""));
	}

	public void removeMetadata_fmt(Pair<String, String> fmt) {
//		this.software.getMetadata_fmt().remove(fmt);
	}

	@Override
	public String forward() {
		this.software.getSoftwareBundle().files = this.files.getTarget();
			
		// TODO save gathered data to the db
		try {
		    this.wfData.getStorage().softwareArchive.saveSoftware(this.software);
		} catch (BWFLAException e) {
		    throw new WFPanicException("An error occurred saving the software bundle.", e);
		}
		log.info("software-debug: " + software.toString());

		// this preserves our messages, so they show up after the redirect
		this.jsf.getExternalContext().getFlash().setKeepMessages(true);

		UINotify.success("Archive was saved successfully!");

		// redirect back to archive overview
		return "/pages/workflow-archive/WF_I_SW_start.xhtml?faces-redirect=true";
	}

	public SoftwareDescription getArchive() {
		return software;
	}

	public void setArchive(SoftwareDescription archive) {
		this.software = archive;
	}

	public DualListModel<BundledFile> getFiles() {
		return files;
	}

	public void setFiles(DualListModel<BundledFile> files) {
		this.files = files;
	}

	public List<String> getNatives() {
		return this.software.getNatives();
	}

	public void setNatives(List<String> natives) {
		this.software.setNatives(natives);
	}

	public List<String> getImports() {
		return this.software.getImports();
	}

	public void setImports(List<String> imports) {
		this.software.setImports(imports);
		log.info("==============================");
		log.info("new imports:");
		for (String s: imports) {
			log.info(s);
		}
		log.info("==============================");
	}

	public List<String> getExports() {
		return this.software.getExports();
	}

	public void setExports(List<String> exports) {
		this.software.setExports(exports);
	}

	public void onTransfer(TransferEvent event) {
		// TODO optionally sort the list of files (again)
	}

	public void rcSetFileMediumType() {
		Map<String, String> params = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap();

		String filename = params.get("filename");
		String type = params.get("type");

		this.getFiles()
				.getTarget()
				.add(new BundledFile(Paths.get(filename), selectedPlatform, MediumType
						.fromValue(type)));
		log.info(type);
		log.info(filename);
		log.info("" + this.getFiles().getTarget().size());
		// this.fileMediumTypes.put(filename, type);

		UINotify.success("File " + filename
				+ " was added with the medium type " + type + ".");
	}
	
	public void removeFromList(List<String> l, int rowid) {
		log.info(l.toString());
		l.remove(rowid);
		log.info(l == this.getImports() ? "true" : "false");
		log.info(l.toString());
	}
	
	public void addToList(List<String> l) {
		l.add("");
	}
	
	public List<String> getSupportedPlatforms()
	{
		try {
			return wfData.getStorage().softwareArchive.getSupportedPlatforms();
		} catch (BWFLAException e) {
			panic(e.getMessage());
		}
		return null;
	}
	
	public String getSelectedPlatform()
	{
		return selectedPlatform;
	}
	
	public void setSelectedPlatform(String platform)
	{
		this.selectedPlatform = platform;
		List<BundledFile> filesSource = wfData.getStorage().softwareArchive.getAvailableFiles(selectedPlatform);
		files = new DualListModel<BundledFile>(filesSource,
				this.software.getSoftwareBundle().files);
	}
}
