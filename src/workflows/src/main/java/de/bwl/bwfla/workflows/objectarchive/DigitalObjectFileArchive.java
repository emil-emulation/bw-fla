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

package de.bwl.bwfla.workflows.objectarchive;

import java.io.File;
import java.io.FileFilter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


// FIXME: this class should be implemented in a style of a "Builder" pattern
public class DigitalObjectFileArchive implements Serializable, DigitalObjectArchive
{
	private static final long	serialVersionUID	= -3958997016973537612L;
	protected final Logger log	= Logger.getLogger(this.getClass().getName());

	private String name;
	private String baseUri;
	private String localPath;

	public DigitalObjectFileArchive(String name, String baseUri, String localPath)
	{
		this.name = name;
		this.localPath = localPath;
		this.baseUri = baseUri;
	}

	protected static File[] loadISOs(String baseDir)
	{
		File dir = new File(baseDir);
		if(!dir.exists())
			return null;
		FileFilter fileFilter = new FileFilter()
		{
			public boolean accept(File file)
			{
				return (!file.isDirectory() && file.getName().endsWith(".iso"));
			}
		};

		File[] flist = dir.listFiles(fileFilter);
		if(flist == null)
			return null;
		return flist;
	}

	public List<String> getObjectList()
	{	
		List<String> isos = new ArrayList<String>();

		File isoDir = new File(localPath);
		if(!isoDir.exists() && !isoDir.isDirectory())
		{
			log.severe("isoDir " + localPath + " does not exist");
			return isos;
		}

		for(File dir: isoDir.listFiles())
		{
			if(dir != null && !dir.isDirectory())
				continue;

			File[] dlist = loadISOs(dir.getAbsolutePath());
			for(int i = 0; i < dlist.length; ++i)
			{
				String isoName = dlist[i].getName().substring(0, dlist[i].getName().lastIndexOf('.'));
				isos.add(isoName);
			}
		}

		return isos;
	}

	@Override
	public String getObjectReference(String id) {
		return this.baseUri + id + "/" + id + ".iso";
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public File getFileReference(String id) {
		return new File(this.localPath + "/" + id + "/" + id + ".iso");
	}

}
