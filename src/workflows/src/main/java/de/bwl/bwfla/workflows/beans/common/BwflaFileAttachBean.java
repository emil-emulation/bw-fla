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

package de.bwl.bwfla.workflows.beans.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import org.apache.commons.io.IOUtils;
import org.primefaces.event.FileUploadEvent;
import com.google.common.io.Files;
import de.bwl.bwfla.common.datatypes.Drive;
import de.bwl.bwfla.common.utils.Pair;
import de.bwl.bwfla.workflows.beans.common.WorkflowResources.WF_RES;



public abstract class BwflaFileAttachBean extends BwflaFormBean
{
	private static final long			serialVersionUID	= 541351153368693987L;
	protected List<String>				imageDevices;
	protected List<Pair<String,String>>	helperDevices;
	
	protected Pair<String, File>		selectedFile;
	protected Map<String, List<File>>	uploadedFiles		= new HashMap<>();
	protected WorkflowsFile				tempDir;
	
	public List<String> getDevices()
	{		
		ArrayList<String> drives = new ArrayList<String>();
		
		for (String dev: imageDevices)
			drives.add(dev);
		
		for (Pair<String,String> dev: helperDevices)
			drives.add(dev.getA());
		
		return drives;
	}

	public Pair<String, File> getSelectedFile()
	{
		return selectedFile;
	}

	public void setSelectedFile(Pair<String, File> selectedFile)
	{
		this.selectedFile = selectedFile;
	}

	public void stopTimer(ActionEvent e)
	{
		resourceManager.disableTimeout();
	}

	public void restartTimer(ActionEvent e)
	{
		resourceManager.restartTimeout();
	}

	public List<Pair<String, File>> getUploadedFiles()
	{
		List<Pair<String, File>> result = new ArrayList<>();
		
		for(Map.Entry<String, List<File>> files : uploadedFiles.entrySet())
			for(File file : files.getValue())
				result.add(new Pair<String, File>(files.getKey(), file));
		
		return result;
	}

	public void handleFileUpload(FileUploadEvent event)
	{
		resourceManager.disableTimeout();

		String dev = null;
		Map<String, String> requestParameterMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();

		for(String key : requestParameterMap.keySet())
			if(key.endsWith("hiddenUploadDeviceParam"))
			{
				dev = requestParameterMap.get(key);
				break;
			}

		if(dev == null)
		{
			log.severe("device value was not uploaded via hidden JSF param, check web-page code");
			return;
		}

		List<File> devFiles = uploadedFiles.get(dev);
		if(devFiles == null)
		{
			devFiles = new ArrayList<>();
			uploadedFiles.put(dev, devFiles);
		}

		
		for(File fl: devFiles)
			if(fl.getName().equalsIgnoreCase(event.getFile().getFileName()))
			{
				log.warning("file uploaded twice for the same device, skipping");
				return;
			}

		if(tempDir == null)
		{
			tempDir = new WorkflowsFile(Files.createTempDir().getAbsolutePath());
			resourceManager.register(WF_RES.FILE, tempDir);
		}

		File fl = new File(tempDir, event.getFile().getFileName());
		resourceManager.register(WF_RES.FILE, new WorkflowsFile(fl.getAbsolutePath()));
		
		try(OutputStream out = new FileOutputStream(fl))
		{
			InputStream data = event.getFile().getInputstream();
			IOUtils.copy(data, out);
			out.flush();

			if(dev.equalsIgnoreCase(Drive.DriveType.CDROM_IMG.value()) || dev.equalsIgnoreCase(Drive.DriveType.DISK_IMG.value()) || dev.equalsIgnoreCase(Drive.DriveType.FLOPPY_IMG.value()))
				devFiles.clear();
			
			devFiles.add(fl);
		}
		catch(IOException e)
		{
			devFiles.remove(fl);

			if(fl.isFile())
				fl.delete();

			e.printStackTrace();
		}

		resourceManager.restartTimeout();
	}

	public void removeUploadedFile()
	{
	    if (this.selectedFile == null) {
	        UINotify.info("You have to select an item to remove it.");
	        return;
	    }
		String dev = this.selectedFile.getA();
		List<File> files = uploadedFiles.get(dev);

		if(files == null)
		{
			log.severe("no uploaded files found for this device type: " + dev);
			return;
		}

		for(Iterator<File> iterator = files.iterator(); iterator.hasNext();)
		{
			File file = iterator.next();

			if(this.selectedFile.getB().equals(file))
			{
				iterator.remove();

				if(!file.delete())
					log.severe("unable to delete the following file during cleanup: " + file.getAbsolutePath());
		
				if(files.isEmpty())
					uploadedFiles.remove(dev);
				
				return;
			}
		}

		log.severe("uploaded file not found, unable to delete: " + this.selectedFile.getB().getAbsolutePath());
	}
}