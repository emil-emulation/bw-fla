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

package de.bwl.bwfla.workflows.servlets;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.net.URLDecoder;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import de.bwl.bwfla.workflows.conf.WorkflowSingleton;

/**
 * The Image servlet for serving from absolute path.
 * @author BalusC
 * @link http://balusc.blogspot.com/2007/04/imageservlet.html
 */
public class ImageServlet extends HttpServlet {
	
	// Maybe Another Path should be chosen here
	public static String imgFilePath = WorkflowSingleton.CONF.picDir.replaceAll("\\$userdir", System.getProperty("user.home"));
	
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) {

        // Define base path somehow. You can define it as init-param of the servlet.
        String imageFilePath = imgFilePath;
       System.out.println("--------------------image file path details---------------------" + imageFilePath);
        
        // In a Windows environment with the Applicationserver running on the
        // c: volume, the above path is exactly the same as "c:\images".
        // In UNIX, it is just straightforward "/images".
        // If you have stored images in the WebContent of a WAR, for example in the
        // "/WEB-INF/images" folder, then you can retrieve the absolute path by:
        // String imageFilePath = getServletContext().getRealPath("/WEB-INF/images");
        
        // Get file name from request.
        String imageFileName = request.getParameter("file");
        
        // Try to get requested image also by path info
        if (imageFileName == null) {
        	imageFileName = request.getPathInfo();
        }
        
        // Check if file name is supplied to the request.
        if (imageFileName != null) {
            // Strip "../" and "..\" (avoid directory sniffing by hackers!).
            imageFileName = imageFileName.replaceAll("\\.+(\\\\|/)", "");
        } else {
            // Do your thing if the file name is not supplied to the request.
            // Throw an exception, or show default/warning image, or just ignore it.
            return;
        }
        
        // Decode the file name and prepare file object.
        try
		{
			imageFileName = URLDecoder.decode(imageFileName, "UTF-8");
		} catch (UnsupportedEncodingException e1)
		{
			
			e1.printStackTrace();
		}
        File imageFile = new File(imageFilePath, imageFileName);

        // Check if file actually exists in filesystem.
        if (!imageFile.exists()) {
            // Do your thing if the file appears to be non-existing.
            // Throw an exception, or show default/warning image, or just ignore it.
            return;
        }
        
        // Get content type by filename.
        String contentType = URLConnection.guessContentTypeFromName(imageFileName);

        // Check if file is actually an image (avoid download of other files by hackers!).
        // For all content types, see: http://www.w3schools.com/media/media_mimeref.asp
        if (contentType == null || !contentType.startsWith("image")) {
            // Do your thing if the file appears not being a real image.
            // Throw an exception, or show default/warning image, or just ignore it.
            return;
        }
        
        // Prepare streams.
        BufferedInputStream input = null;
        BufferedOutputStream output = null;
        
        try {
            // Open image file.
            input = new BufferedInputStream(new FileInputStream(imageFile));
            int contentLength = input.available();
            
            // Init servlet response.
            response.reset();
            response.setContentLength(contentLength);
            response.setContentType(contentType);
            response.setHeader(
                "Content-disposition", "inline; filename=\"" + imageFileName + "\"");
            output = new BufferedOutputStream(response.getOutputStream());

            // Write file contents to response.
            while (contentLength-- > 0) {
                output.write(input.read());
            }
            
            // Finalize task.
            output.flush();
        } catch (IOException e) {
            // Something went wrong?
            e.printStackTrace();
        } finally {
            // Gently close streams.
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    // Do your thing with the exception. Print it, log it or mail it.
                    e.printStackTrace();
                }
            }
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    // Do your thing with the exception. Print it, log it or mail it.
                    e.printStackTrace();
                }
            }
        }
    }

}
