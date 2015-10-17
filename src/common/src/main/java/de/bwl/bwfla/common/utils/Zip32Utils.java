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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.IOUtils;



public abstract class Zip32Utils
{
	private static final Logger	LOG	= Logger.getLogger(Zip32Utils.class.getName());
	
	public static void zip(File zipFile, File src)
	{
		if(zipFile.exists())
		{
			LOG.warning("will not overwrite an existing file, exiting: " + zipFile.getAbsolutePath());
			return;
		}

		try(ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(zipFile, false)))
		{
			if(src.isDirectory())
			{
				for(File fl: src.listFiles())
					Zip32Utils.addToZip("", fl, zip);
			}
			else
			{
				Zip32Utils.addToZip("", src, zip);
			}
			
			zip.flush();
			zip.close();
		}
		catch(IOException e)
		{			
			if(zipFile.isFile() && !zipFile.delete())
				LOG.warning("could not remove a malformed/unfinished ZIP-file: " + zipFile.getAbsolutePath());
			
			e.printStackTrace();
		}
	}

	private static void addToZip(String path, File src, ZipOutputStream zipOs) throws IOException
	{
		if(src.isFile())
		{
			try(InputStream fileIs = new FileInputStream(src))
			{
				zipOs.putNextEntry(new ZipEntry(path + "/" + src.getName()));
				IOUtils.copy(fileIs, zipOs);
				zipOs.closeEntry();
			}
		}
		else if(src.isDirectory())
		{
			zipOs.putNextEntry(new ZipEntry(path + "/" + src.getName() + "/"));
			
			for(File fl: src.listFiles())
				addToZip(path + "/" + src.getName(), fl, zipOs);
			
			zipOs.closeEntry();
		}
		else
		{
			LOG.warning("the following file is not of type 'regular' or 'directory', skipping': " + src.getAbsolutePath());
		}
	}
	
	private static void extractFile(ZipInputStream in, File outdir, String name) throws IOException
	{
		final int BUFFER_SIZE = 1024 * 1000;
		byte[] buffer = new byte[BUFFER_SIZE];
		
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(outdir, name)));
		
		int count = -1;
		while((count = in.read(buffer)) != -1)
			out.write(buffer, 0, count);
		
		out.close();
	}

	private static void mkdirs(File outdir, String path)
	{
		File d = new File(outdir, path);
		
		if(!d.exists())
			d.mkdirs();
	}

	private static String dirpart(String name)
	{
		int s = name.lastIndexOf(File.separatorChar);
		return s == -1 ? null : name.substring(0, s);
	}

	public static File unzip(File in, File out)
	{
		String name = null;
		
		try
		{
			ZipInputStream zin = new ZipInputStream(new FileInputStream(in));
			ZipEntry entry;
			String dir;
			while((entry = zin.getNextEntry()) != null)
			{
				name = entry.getName();
				if(entry.isDirectory())
				{
					mkdirs(out, name);
					continue;
				}

				dir = dirpart(name);
				if(dir != null)
					mkdirs(out, dir);

				extractFile(zin, out, name);
			}

			zin.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return null;
		}

		return new File(out, name);
	}
	
	public static void main(String[] args)
	{
		zip(new File("/home/iv1004/test.zip"), new File("/home/iv1004/test"));
	}
}