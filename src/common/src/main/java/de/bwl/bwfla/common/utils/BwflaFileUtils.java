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

package de.bwl.bwfla.common.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;



public class BwflaFileUtils
{
	// FIXME: implement appending and not rewriting
	public static int appendStreamBytesToFile(InputStream in, File fl, int count) throws IOException
	{	
		File parentDir = fl.getParentFile();
		
		final byte[] buffer = new byte[10 * 1024];
		final long SPACE_THRESHOLD = (30) * 1024 * 1024;
		
		try(OutputStream out = new FileOutputStream(fl.getAbsolutePath(), true))
		{
			int bytesRead = 0;
			int totalBytesRead = 0;
			
			while(totalBytesRead <= count && (bytesRead = in.read(buffer)) != -1)
		    {
				totalBytesRead += bytesRead;

				long freeSpace = (parentDir.getUsableSpace() - bytesRead) - SPACE_THRESHOLD; 
					
				if(freeSpace < 0)
					throw new IOException("no free space left (wrt. to SPACE_THRESHOLD) on partition where the following file is located: "  + fl.getAbsolutePath());
					
				out.write(buffer, 0, bytesRead);
			}
			
			out.flush();
			
			return totalBytesRead;
		}
	}
	
	/*
	private static class FileTask implements Callable<Void>
	{
		@Override
		public Void call()
		{
			File fl = new File("/mnt/data/home/iv1004/Workspace/bw-fla/test.img");
			
			File myFile = null;
			try(InputStream is = new FileInputStream(fl))
			{
				myFile = File.createTempFile("file", "");
				writeInputStreamToFile(is, myFile);
			}
			catch(IOException e)
			{
				if(myFile != null) 
					myFile.delete();
				
				e.printStackTrace();
			}
			
			return null;
		}
	}
	
	public static void main(String [] args)
	{
		System.setProperty("java.io.tmpdir", "/mnt/data/home/iv1004/Workspace/bw-fla/tmp");
		
		List<FileTask> flTasks = new ArrayList<>();
		flTasks.add(new FileTask());
		flTasks.add(new FileTask());
		flTasks.add(new FileTask());
		flTasks.add(new FileTask());
		flTasks.add(new FileTask());
		flTasks.add(new FileTask());

		try
		{
			ExecutorService executor = Executors.newCachedThreadPool();
			executor.invokeAll(flTasks);
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	*/
}