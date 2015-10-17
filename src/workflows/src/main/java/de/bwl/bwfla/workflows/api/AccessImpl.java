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

package de.bwl.bwfla.workflows.api;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import javax.activation.DataHandler;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.ws.soap.MTOM;
import org.apache.commons.io.FileUtils;
import org.primefaces.json.JSONObject;
import com.google.common.io.Files;
import de.bwl.bwfla.common.datatypes.AccessSession;
import de.bwl.bwfla.common.services.container.helpers.CdromIsoHelper;
import de.bwl.bwfla.common.services.container.types.Container;
import de.bwl.bwfla.common.utils.Zip32Utils;



@MTOM
@WebService
@Stateless
public class AccessImpl
{
	@EJB
	private GlobalBean globalBean;	
	private File tmpDir;

	
	private File extractFileAndSaveToDisk(DataHandler data)
	{	
		File receivedile = null;
		try
		{
			receivedile = File.createTempFile("cdrom.zip", "", tmpDir).getAbsoluteFile();
		}
		catch(Throwable t)
		{
			t.printStackTrace();
			return null;
		}
		
		try(OutputStream os = new FileOutputStream(receivedile))
		{	
			InputStream is = data.getInputStream();
			
			byte[] buffer = new byte[1024];
		    int bytesRead;
		    while ((bytesRead = is.read(buffer)) != -1)
		        os.write(buffer, 0, bytesRead);
		    
		    os.flush();
			return receivedile;
		}
		catch(Throwable t)
		{
			t.printStackTrace();
			return null;
		}
	}
	
	private File saveMetadataToDisk(String metadata, File isoDir)
	{
		File metadataFile = null;
		
		try
		{
			// FIXME: replacing "aID", a hack to allow compatibility with the "ObjectEnvironmentDescription.java"
			JSONObject json = new JSONObject(metadata);
			json.put("doArchive", isoDir.toString());
			json.put("aID", "");
			
			File metadataDir = new File(this.tmpDir + File.separator + "metadata" +  File.separator + "json");
			metadataDir.mkdirs();
			metadataFile = File.createTempFile("metadata", ".json", metadataDir);
			FileUtils.writeStringToFile(metadataFile, json.toString());
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
			
		return metadataFile;
	}
	
	@WebMethod
	public AccessResult access(@XmlMimeType("application/octet-stream") DataHandler data, String metadata)
	{		
		String accessSessId = null;
		File receivedFile = null;
		File metadataFile = null;
		File unzippedFile = null;
		Container cdrom = null;

		try
		{
			tmpDir = Files.createTempDir(); 
			
			// get file from the data handler and save to file on disk
			receivedFile = this.extractFileAndSaveToDisk(data);
			
			// unzip the packed file
			unzippedFile = Zip32Utils.unzip(receivedFile, this.tmpDir);
            if(unzippedFile == null)
            	throw new Exception("Unable to unzip the received digital object.");
            
            // create empty CD disk
            File isoDir = new File(tmpDir, "isos" + File.separator + "access_iso");
            isoDir.mkdirs();
            CdromIsoHelper cdromHelper = new CdromIsoHelper();
            cdrom = cdromHelper.createEmptyContainer();
            
            // insert file into CD
            cdromHelper.insertIntoContainer(cdrom, new ArrayList<File>(Arrays.asList(unzippedFile)));
            if(cdrom == null) throw new Exception("Unable to create a CDROM for the unzipped data."); 
            
            // write metadata to file on disk
    		metadataFile = this.saveMetadataToDisk(metadata, isoDir);
            
            // register access session
    		cdrom.getFile().renameTo(new File(cdrom.getFile().getParent() + File.separator + "access_iso.iso"));
            accessSessId = globalBean.registerAccess(new AccessSession(isoDir.getParentFile(), metadataFile));
            if(accessSessId == null) throw new Exception("Unable to register access session in the global bean.");
            
            // get server address
            // String serverURL = WorkflowSingleton.CONF.serverUrl;
            String serverURL = null;
    		if(serverURL == null)
    			return null;

    		// form return data
    		String pageLocation = serverURL + "/faces/pages/workflow-access/WF_A_API.xhtml";
    		return new AccessResult(accessSessId, pageLocation + "?" + "accessSessId=" + accessSessId);
        } 
		catch(Throwable t) 
		{
			// FIXME: decide when we to get rid of these files after user finishes the workflow
            FileUtils.deleteQuietly(metadataFile);
            FileUtils.deleteQuietly(cdrom.getFile());
            t.printStackTrace();
            return null;
        }
		finally
		{
			FileUtils.deleteQuietly(receivedFile);
			FileUtils.deleteQuietly(unzippedFile);
		}
	}
}
