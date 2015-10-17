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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import de.bwl.bwfla.workflows.api.GlobalBean;
import de.bwl.bwfla.workflows.beans.common.BwflaFormBean;
import de.bwl.bwfla.workflows.beans.common.ImageUtils;
import de.bwl.bwfla.workflows.catalogdata.ObjectEvaluationDescription;

@ManagedBean
@ViewScoped
public class WF_I_6 extends BwflaFormBean implements Serializable
{
	private static final long serialVersionUID = -2223912800593361682L;

	@Inject
	private WF_I_data wfData;
	private WF_I_data.Storage storage;
	// @Inject Conversation conversation;

	@EJB
	private GlobalBean globalBean;

	@Override
	public void initialize()
	{
		super.initialize();
		storage = wfData.getStorage();
	}
	
	public String save() throws MalformedURLException, URISyntaxException
	{
		File chosenPicture = null;
		if (storage.uploadedPicture != null)
			chosenPicture = new File(storage.uploadedPicture);
		else if (storage.screenshotFile != null)
			chosenPicture = storage.screenshotFile;
		else if (storage.oldThumbnail != null)
			chosenPicture = new File(storage.oldThumbnail);

		if (chosenPicture != null)
		{
			
			storage.mdArchive.savePicture(storage.description.getArchiveid(), chosenPicture);
		}
		
		storage.mdArchive.saveDescription(storage.description);
		
		return "/pages/start.xhtml?faces-redirect=true";
	}

	@Override
	public void cleanup()
	{
		super.cleanup();
		this.globalBean.unregisterIngest(storage.ingestSessId);
 
		/*
		if (storage.screenshot != null)
			if(storage.screenshot.exists())
					FileUtils.deleteQuietly(new File(storage.screenshot));
		*/
		if (storage.uploadedPicture != null)
			if(new File(storage.uploadedPicture).exists())
					FileUtils.deleteQuietly(new File(storage.uploadedPicture));
	}

	public void handleFileUpload(FileUploadEvent event)
	{
		try
		{
			if (storage.uploadedPicture == null)
				storage.uploadedPicture = File.createTempFile(
						"uploaded_picture", ".jpg").getAbsolutePath();

			InputStream data = event.getFile().getInputstream();
			FileOutputStream out = new FileOutputStream(
					storage.uploadedPicture);
			IOUtils.copy(data, out);
			out.flush();
			out.close();
		} catch (IOException e)
		{
			e.printStackTrace();
			storage.uploadedPicture = null;
			return;
		}
	}

	public String getPictureURL()
	{
		try
		{
			if (storage.uploadedPicture != null)
				return ImageUtils.publishImage(new File(
						storage.uploadedPicture), true);

			if (storage.screenshot != null)
				return storage.screenshot;

			if (storage.oldThumbnail != null)
				return ImageUtils.publishImage(new File(
						storage.oldThumbnail), true);
			
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		return "/faces/javax.faces.resource/images/dummy-cdrom.jpg";
	}

	public ObjectEvaluationDescription getDescription()
	{
		return storage.description;
	}

	public StreamedContent getFile()
	{
		String jsonString = storage.description.toString();
		return new DefaultStreamedContent(new ByteArrayInputStream(jsonString.getBytes()), 
				"application/json", 
				"ObjectEvaluationDescription.json");
	}

	@Override
	public String forward()
	{
		return "/pages/start.xhtml?faces-redirect=true";
	}
}
