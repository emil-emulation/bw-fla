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
import com.google.common.io.Files;
import de.bwl.bwfla.common.datatypes.IngestSession;
import de.bwl.bwfla.common.services.container.helpers.CdromIsoHelper;
import de.bwl.bwfla.common.services.container.types.Container;
import de.bwl.bwfla.common.utils.Zip32Utils;



@MTOM
@WebService
@Stateless
public class IngestImpl
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
	
	@WebMethod
	public IngestResult ingest(@XmlMimeType("application/octet-stream") DataHandler data)
	{
		String ingestSessId = null;
		try
		{
			tmpDir = Files.createTempDir();
			
			// get file from the data handler
			File receivedFile = this.extractFileAndSaveToDisk(data);
			
			// unzip the packed file
			File unzippedFile = Zip32Utils.unzip(receivedFile, this.tmpDir);
            if(unzippedFile == null)
            	throw new Exception("Unable to unzip the received digital object.");
            
            // create empty CD disk
            File isoDir = new File(tmpDir, "isos" + File.separator + "ingest_iso");
            isoDir.mkdirs();
            CdromIsoHelper cdromHelper = new CdromIsoHelper();
            Container cdrom = cdromHelper.createEmptyContainer();
            
            // insert file into CD
            cdromHelper.insertIntoContainer(cdrom, new ArrayList<File>(Arrays.asList(unzippedFile)));
            if(cdrom == null) throw new Exception("Unable to create a CDROM for the unzipped data."); 
            
            // register ingest session
            cdrom.getFile().renameTo(new File(cdrom.getFile().getParent() + File.separator + "ingest_iso.iso"));
            ingestSessId = globalBean.registerIngest(new IngestSession(isoDir.getParentFile()));
            if(ingestSessId == null) throw new Exception("Unable to register ingest session in the global bean.");
            
            // get server address
            // String serverURL = WorkflowSingleton.CONF.serverUrl;
            String serverURL = null;
    		if(serverURL == null)
    			return null;

    		// form return data
    		String pageLocation = serverURL + "/faces/pages/workflow-ingest/WF_I_API.xhtml";
    		return new IngestResult(ingestSessId, pageLocation + "?" + "ingestSessId=" + ingestSessId);
        } 
		catch(Throwable t) 
		{
             t.printStackTrace();
             return null;
        }
	}
}
