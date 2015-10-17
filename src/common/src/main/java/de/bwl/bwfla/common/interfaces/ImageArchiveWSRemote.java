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

package de.bwl.bwfla.common.interfaces;

import java.util.List;

import javax.activation.DataHandler;
import javax.ejb.Remote;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlMimeType;

import de.bwl.bwfla.common.datatypes.Drive.DriveType;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.services.guacplay.replay.IWDMetaData;



/**
 * @author Isgandar Valizada, bwFLA project, 
 * University of Freiburg, Germany
 * 
 */
@Remote
@WebService
public interface ImageArchiveWSRemote
{
	@WebMethod
	public String registerImage(String conf,  @XmlMimeType("application/octet-stream") DataHandler image, String type);
	
	@WebMethod(operationName="registerImageUsingFile")
	public String registerImage(String conf, String image, boolean delete, String type);

	@WebMethod
	public boolean releaseImage(String url);

	@WebMethod
	public List<String> getIncomingImageList();
	
	@WebMethod
	public String getRecording(String envId, String traceId);
	
	@WebMethod 
	public List<IWDMetaData> getRecordings(String envId);
	
	@WebMethod
	public boolean addRecordingFile(String envId, String traceId, String data);

	@WebMethod
	public List<String> getImages(String type);

	@WebMethod
	public String publishImage(String image, String exportId);

	@WebMethod
	public List<String> getTemplates();
	
	@WebMethod
	public String getImageById(String id);
}
