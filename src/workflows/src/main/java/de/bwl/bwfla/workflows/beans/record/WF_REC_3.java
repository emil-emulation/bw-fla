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

import java.io.Serializable;
import java.util.UUID;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;

import de.bwl.bwfla.common.services.guacplay.GuacDefs.MetadataTag;
import de.bwl.bwfla.common.services.handle.HandleService;
import de.bwl.bwfla.common.utils.SystemEnvironmentHelper;
import de.bwl.bwfla.workflows.beans.common.BwflaFormBean;
import de.bwl.bwfla.workflows.beans.common.CitationUrlHelper;
import de.bwl.bwfla.workflows.beans.common.RemoteSessionRecorder;
import de.bwl.bwfla.workflows.beans.common.UINotify;
import de.bwl.bwfla.workflows.beans.common.WorkflowResources;
import de.bwl.bwfla.workflows.catalogdata.SystemEnvironmentDescription;
import de.bwl.bwfla.workflows.conf.WorkflowSingleton;


@ManagedBean
@ViewScoped
public class WF_REC_3 extends BwflaFormBean implements Serializable
{
	// Injected members
	@Inject private WF_REC_Data wfdata;
	
	// Member fields
	private RemoteSessionRecorder recorder;
	private String title;
	private String description;
	private boolean citationEnabled;

	
	@Override
	public void initialize()
	{
		super.initialize();
		this.recorder = wfdata.getRemoteSessionRecorder();
		this.title = null;
		this.description = null;
		this.citationEnabled = false;
		
		wfdata.setCitationLink(null);
	}

	@Override
	public String forward()
	{
		String id = UUID.randomUUID().toString() + String.valueOf(System.currentTimeMillis()).substring(0, 2);
		
		recorder.defineMetadataChunk(MetadataTag.INTERNAL, "Internal information");
		recorder.defineMetadataChunk(MetadataTag.PUBLIC, "Public information");
		
		// Add some information as metadata
		recorder.addMetadataEntry(MetadataTag.INTERNAL, "id", id);
		recorder.addMetadataEntry(MetadataTag.INTERNAL, "emuenvid", wfdata.getEmulatorEnvId());
		recorder.addMetadataEntry(MetadataTag.PUBLIC, "title", title);
		if (!description.isEmpty())
			recorder.addMetadataEntry(MetadataTag.PUBLIC, "description", description);
		
		// Request and wait for the recorded trace
		final String trace = recorder.getSessionTrace();
		if (trace == null)
			this.panic("The trace could not be saved! Please check the logs for further details.");
		
		log.info("Save trace-file to image-archive");
		wfdata.getSystemEnvironmentHelper().addRecording(wfdata.getEmulatorEnvId(), id, trace);
		
		if (this.isCitationEnabled()) {
			String link = this.createHandleLink(id, "traceid=" + id);
			wfdata.setCitationLink(link);
		}
		
		// Construct the next page's URL
		return WF_REC_Data.getPageUrl(4, true);
	}
	
	public String cancel()
	{
		log.info("Session-recording was cancelled!");

		// Construct the next page's URL
		return WF_REC_Data.getPageUrl(4, true);
	}
	
//	public void validateFilename(FacesContext context, UIComponent component, Object value) throws ValidatorException
//	{
//		final String name = (String) value;
//		
//		// Contains invalid characters?
//		if (!name.matches("[\\w-.()]+")) {
//			String detail = "Specified filename contains invalid characters! Allowed characters: a-z, A-Z, '_', '-', '.', '(', ')'";
//			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid filename!", detail);
//			throw new ValidatorException(message);
//		}
//		
//		// Destination file exists?
//		if (Files.exists(outdir.resolve(name + GuacDefs.TRACE_FILE_EXT))) {
//			String detail = "A file with the specified name already exists! Please choose a different name.";
//			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "File exists!", detail);
//			throw new ValidatorException(message);
//		}
//	}

	public String getTitle()
	{
		return title;
	}
	
	public void setTitle(String name)
	{
		title = name;
	}
	
	public String getDescription()
	{
		return description;
	}
	
	public void setDescription(String desc)
	{
		description = desc;
	}
	
	public boolean isCitationEnabled()
	{
		return citationEnabled;
	}
	
	public void setCitationEnabled(boolean enable)
	{
		this.citationEnabled = enable;
	}
	
	@Override
	public void cleanup()
	{
		this.resourceManager.cleanupResources(WorkflowResources.WF_RES.EMU_COMP);
		super.cleanup();
	}

	
	private String createHandleLink(String id, String params)
	{
		if (id == null || id.isEmpty())
			throw new IllegalArgumentException("Handle's ID not specified correctly!");

		final String embedgw = WorkflowSingleton.CONF.embedGw;
		if (embedgw == null) {
			String message = "Gateway for embedded URLs not configured";
			UINotify.error(message);
			log.info(message);
			return null;
		}

		final SystemEnvironmentHelper envhelper = wfdata.getSystemEnvironmentHelper();
		final String envid = wfdata.getEmulatorEnvId();
		final SystemEnvironmentDescription envdesc = new SystemEnvironmentDescription(envhelper, envid);
		String url = embedgw + CitationUrlHelper.urlString(envdesc);
		if (params != null && !params.isEmpty())
			url +=  "&" + params;
		
		if (!HandleService.createUrlHandle("11270/" + id, url)) {
			String message = "Registration of a new Handle-Link failed!";
			log.severe(message);
			return message;
		}
			
		String handle = "http://hdl.handle.net/11270/" + id;
		log.info("Created handle: " + handle);
		return handle;
	}
	
	private static final long serialVersionUID = 1026749915518843895L;
}
