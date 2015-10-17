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
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import de.bwl.bwfla.common.utils.ProcessRunner;
import de.bwl.bwfla.workflows.servlets.ImageServlet;

public class ImageUtils {
	/**
	 * Makes the Image available to the Image Servlet
	 * 
	 * @param image Image file
	 * @param symlink if false the Image will be copied to the Servlet Image Folder, if false only a symlink wil be made
	 * @return URL of the Image
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static String publishImage(File image, boolean symlink) throws IOException {
		File newpath = publishImageToFile(image, symlink);
		return getURLFromFilename(newpath);
	}
	
	public static String getURLFromFilename(File image) {		
		// Process the uploaded file
		String previewUrl = Utils.getBaseURL() + "/image/" + image.getName();
		
		return previewUrl;
	}
	
	public static File publishImageToFile(File image, boolean symlink) throws IOException {
		String md5Checksum = MD5Checksum.getMD5(image.getAbsolutePath());
		
		if(!new File(ImageServlet.imgFilePath).exists()) {
			new File(ImageServlet.imgFilePath).mkdirs();
		}
			
		File newpath = new File(ImageServlet.imgFilePath + File.separator + "image_" + md5Checksum + "." +  FilenameUtils.getExtension(image.getName()));
	
		if (newpath.exists()) {
			return newpath;
		}
		
		if (!symlink) {
			FileUtils.moveFile(image, newpath);
		}
		else {
			ProcessRunner process = new ProcessRunner("ln");
			process.addArgument("-sf");
			process.addArgument(image.getAbsolutePath());
			process.addArgument(newpath.getAbsolutePath());
			if (!process.execute())
				Utils.log("handleFileUpload", "New Album art upload failed!");
			else Utils.log("handleFileUpload", "New Album art uploaded: " + newpath.getAbsolutePath());				
		}
		
		return newpath;
	}
}