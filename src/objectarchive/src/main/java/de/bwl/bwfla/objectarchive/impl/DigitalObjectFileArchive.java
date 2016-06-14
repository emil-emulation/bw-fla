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

package de.bwl.bwfla.objectarchive.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import de.bwl.bwfla.common.datatypes.Drive;
import de.bwl.bwfla.common.datatypes.Drive.DriveType;
import de.bwl.bwfla.common.datatypes.FileCollection;
import de.bwl.bwfla.common.datatypes.FileCollectionEntry;
import de.bwl.bwfla.objectarchive.DigitalObjectArchive;
import de.bwl.bwfla.objectarchive.conf.ObjectArchiveSingleton;


// FIXME: this class should be implemented in a style of a "Builder" pattern
public class DigitalObjectFileArchive implements Serializable, DigitalObjectArchive
{
	private static final long	serialVersionUID	= -3958997016973537612L;
	protected final Logger log	= Logger.getLogger(this.getClass().getName());

	private String name;
	private String localPath;

	private static final FileFilter ISO_FILE_FILTER = new IsoFileFilter();
	private static final FileFilter FLOPPY_FILE_FILTER = new FloppyFileFilter();
	
	/**
	 * Simple ObjectArchive example. Files are organized as follows
	 * localPath/
	 *          ID/
	 *            floppy/
	 *            iso/
	 *               disk1.iso
	 *               disk2.iso
	 *               
	 * Allowed extensions:
	 *      iso : {.iso}
	 *      floppy : {.img, .ima, .adf, .D64, .x64, .dsk, .st }
	 * 
	 * @param name
	 * @param localPath
	 */
	
	public DigitalObjectFileArchive(String name, String localPath)
	{
		this.name = name;
		this.localPath = localPath;
	}
	
	protected List<FileCollectionEntry> loadEntries(File dir, String id, Drive.DriveType type)
	{
		List<FileCollectionEntry> l = new ArrayList<>();
		FileFilter ff;
		String prefix;
		switch(type)
		{
		case CDROM:
			ff = ISO_FILE_FILTER;
			prefix = "iso";
			break;
		case FLOPPY:
			ff = FLOPPY_FILE_FILTER;
			prefix = "floppy";
			break;
		default:
			return l;
		}
		
		File[] flist = dir.listFiles(ff);
		if(flist == null)
			return null;
		
		for(File f : flist)
		{
			String reference = "/" + id + "/" + prefix + "/" + f.getName();
			try {
				String url = ObjectArchiveSingleton.CONF.httpExport + URLEncoder.encode(name, "UTF-8") + reference;
				FileCollectionEntry fe = new FileCollectionEntry(url, type, f.getName());
				fe.setFileSize(f.length());
				fe.setLocalAlias(f.getName());
				l.add(fe);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return l;
	}
	
	private FileCollection getFileCollection(File objectDir)
	{
		if(objectDir == null || !objectDir.isDirectory())
				return null;
	
		FileCollection o = new FileCollection(objectDir.getName());
		
		File isoDir = new File(objectDir, "iso");
		if(isoDir.exists() && isoDir.isDirectory())
			o.files.addAll(loadEntries(isoDir, objectDir.getName(), DriveType.CDROM));
		
		File floppyDir = new File(objectDir, "floppy");
		if(floppyDir.exists() && floppyDir.isDirectory())
			o.files.addAll(loadEntries(floppyDir, objectDir.getName(), DriveType.FLOPPY));
		
		return o;
	}
	
	public List<String> getObjectList()
	{	
		List<String> objects = new ArrayList<String>();
		
		File objectDir = new File(localPath);
		if(!objectDir.exists() && !objectDir.isDirectory())
		{
			log.severe("objectDir " + localPath + " does not exist");
			return objects;
		}

		for(File dir: objectDir.listFiles())
		{
			if(dir != null && !dir.isDirectory())
				continue;
			
			FileCollection o = getFileCollection(dir);
			
			if(o.files.size() > 0)
				objects.add(o.label);
		}
		return objects;
	}
	
	public FileCollection getObjectReference(String objectId)
	{
		File topDir = new File(localPath);
		if(!topDir.exists() && !topDir.isDirectory())
		{
			log.severe("objectDir " + localPath + " does not exist");
			return null;
		}		
		File objectDir = new File(topDir, objectId);
		return getFileCollection(objectDir);
	}

	public String getLocalPath()
	{
		return localPath;
	}
	
	@Override
	public String getName() {
		return name;
	}

	
	private static class IsoFileFilter implements FileFilter
	{
		public boolean accept(File file)
		{
			if (file.isDirectory())
				return false;
			
			// Check file's extension...
			final String name = file.getName();
			final int length = name.length();
			return name.regionMatches(true, length - 4, ".iso", 0, 4);
		}
	};
	
	private static class FloppyFileFilter implements FileFilter
	{
		private final Set<String> formats = new HashSet<String>();
		
		public FloppyFileFilter()
		{
			// Add all valid formats
			formats.add(".img");
			formats.add(".ima");
			formats.add(".adf");
			formats.add(".d64");
			formats.add(".x64");
			formats.add(".dsk");
			formats.add(".st");
		}
		
		public boolean accept(File file)
		{
			if (file.isDirectory())
				return false;
			
			// Check the file's extension...
			final String name = file.getName();
			final int extpos = name.lastIndexOf('.');
			if (extpos < 0)
				return false;  // No file extension found!
			
			final String ext = name.substring(extpos);
			return formats.contains(ext.toLowerCase());
		}
	};
}
