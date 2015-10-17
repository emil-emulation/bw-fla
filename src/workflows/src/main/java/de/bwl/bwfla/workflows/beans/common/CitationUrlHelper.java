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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.faces.context.FacesContext;
import org.apache.commons.codec.binary.Base64;
import de.bwl.bwfla.workflows.catalogdata.Description;
import de.bwl.bwfla.workflows.catalogdata.DescriptionTypes;
import de.bwl.bwfla.workflows.catalogdata.ObjectEnvironmentDescription;
import de.bwl.bwfla.workflows.catalogdata.ObjectEvaluationDescription;
import de.bwl.bwfla.workflows.catalogdata.SystemEnvironmentDescription;

public class CitationUrlHelper 
{
	protected static final Logger	log	= Logger.getLogger("CitationUrlHelper");
	private static final String object = "objectDescription";
	private static final String type = "objectType";
	
	public static Description getDescription(FacesContext fx)
	{
		Map<String, String> requestParams = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		
		String oType = requestParams.get(type);
		if(oType == null || oType.isEmpty())
		{
			log.severe("objectType is empty or missing");
			return null;
		}
		
		String oDescr = requestParams.get(object);
		if(oDescr == null || oDescr.isEmpty())
		{
			log.severe("objectDescription is empty or missing");
			return null;
		}
		
		// decode BASE64 in which "str" is encoded
		byte[] compressedBytes = (new Base64(true)).decode(oDescr);

		// decompressing ZIP'pped data to JSON string
		String jsonString = decompress(compressedBytes);
		
		if(oType.equals(DescriptionTypes.TYPE.OBJECT.toString()))
		{ 
			return ObjectEnvironmentDescription.fromString(jsonString);
		} 
		else if(oType.equals(DescriptionTypes.TYPE.SYSTEM.toString()))
		{
			return SystemEnvironmentDescription.fromString(jsonString);
		}
		else if(oType.equals(DescriptionTypes.TYPE.EVALUATION.toString()))
		{
			return ObjectEvaluationDescription.fromString(jsonString);
		}
		else
		{
			log.severe("unknown object type " + oType);
			return null;
		}
	}
	
	public static String urlString(Description d)
	{
		byte[] compressedBytes = compress(d.toString());

		// BASE64 encoding the ZIP'ped data
		byte[] b64CompressedBytes = (new Base64(-1, null, true)).encode(compressedBytes);

		// forming resulting string object
		String result = new String(b64CompressedBytes);
		
		return "?" + object + "=" + result + "&" + type +"=" +  d.getDescriptionType();
	}
	
	private static byte[] compress(String string)
	{
		try
		(
				ByteArrayOutputStream os = new ByteArrayOutputStream(string.length());
				GZIPOutputStream gos = new GZIPOutputStream(os);
				)
				{
			gos.write(string.getBytes());
			gos.close();
			byte[] compressed = os.toByteArray();
			os.close();

			return compressed;
				}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}


	private static String decompress(byte[] compressed)
	{
		final int BUFFER_SIZE = 32;
		try
		(
				ByteArrayInputStream is = new ByteArrayInputStream(compressed);
				GZIPInputStream gis = new GZIPInputStream(is, BUFFER_SIZE);
				)
				{
			StringBuilder string = new StringBuilder();
			byte[] data = new byte[BUFFER_SIZE];
			int bytesRead;
			while((bytesRead = gis.read(data)) != -1)
				string.append(new String(data, 0, bytesRead));
			gis.close();
			is.close();

			return string.toString();
				}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}
}
