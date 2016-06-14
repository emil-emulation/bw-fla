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

package de.bwl.bwfla.imagearchive;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import javax.activation.DataHandler;

public class DataUtil {
	
	static protected boolean writeData(DataHandler image, File destImgFile)
	{
		InputStream imageIs = null;
		OutputStream flOut = null;
		try
		{    
			imageIs = image.getInputStream();
			flOut = new FileOutputStream(destImgFile);

			int bytesRead = -1;
			final int BUFFER_SIZE = 1024;
			byte[] buffer = new byte[BUFFER_SIZE];

			while((bytesRead = imageIs.read(buffer)) != -1)
				flOut.write(buffer, 0, bytesRead);
			
			flOut.flush();
			return true;
		}
		catch(Throwable e)
		{
			e.printStackTrace();
			return false;
		}
		finally
		{
			try
			{
				if(imageIs != null) imageIs.close();
				if(flOut != null) flOut.close();
			}
			catch(Throwable e) {e.printStackTrace(); return false;}
		}
	}
	
	protected static boolean writeString(String conf, File destConfFile)
	{
		BufferedWriter confOut = null;
		try
		{
			confOut = new BufferedWriter(new FileWriter(destConfFile));
			confOut.write(conf);
			confOut.flush();
			return true;
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(confOut != null)
					confOut.close();
			}
			catch(Throwable e)
			{
				e.printStackTrace();
			}
		}
		return false;
	}
	
}
