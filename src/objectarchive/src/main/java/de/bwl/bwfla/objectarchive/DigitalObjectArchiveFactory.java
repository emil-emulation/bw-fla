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

package de.bwl.bwfla.objectarchive;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import de.bwl.bwfla.objectarchive.impl.DigitalObjectArchiveDescriptor;
import de.bwl.bwfla.objectarchive.impl.DigitalObjectFileArchive;
import de.bwl.bwfla.objectarchive.impl.DigitalObjectFileArchiveDescriptor;
import de.bwl.bwfla.objectarchive.impl.DigitalObjectArchiveDescriptor.ArchiveType;
import de.bwl.bwfla.objectarchive.impl.DigitalObjectRosettaArchive;
import de.bwl.bwfla.objectarchive.impl.DigitalObjectRosettaArchiveDescriptor;


public class DigitalObjectArchiveFactory {

	protected final static Logger log	= Logger.getLogger(DigitalObjectArchiveFactory.class.getName());

	
	private static DigitalObjectArchive fromString(String jsonString)
	{
		GsonBuilder gson = new GsonBuilder();
		
		// form resulting Description object
		try {
			DigitalObjectArchiveDescriptor result = gson.create().fromJson(jsonString, DigitalObjectArchiveDescriptor.class); 
			if(result == null)
				return null;
			DigitalObjectArchiveDescriptor.ArchiveType type = result.getType();
			switch(type)
			{
			case FILE:
				DigitalObjectFileArchiveDescriptor d = gson.create().fromJson(jsonString, DigitalObjectFileArchiveDescriptor.class); 
				if(d == null)
					return null;
				
				return new DigitalObjectFileArchive(d.getName(), d.getLocalPath());
			case EMIL_ROSETTA:
				DigitalObjectRosettaArchiveDescriptor r = gson.create().fromJson(jsonString, DigitalObjectRosettaArchiveDescriptor.class);
				if(r == null)
					return null;
				return new DigitalObjectRosettaArchive(r.getUrl());
			default:
				return null;
			}
		}
		catch (JsonSyntaxException e)
		{
			System.out.println(jsonString);
			e.printStackTrace();
			return null;
		}
	}
	
	public static List<DigitalObjectArchive> createFromJson(File dir)
	{
		List<DigitalObjectArchive> archives = new ArrayList<DigitalObjectArchive>();
		if(!dir.exists() || !dir.isDirectory())
		{
			log.severe("loading path: " + dir.getAbsolutePath() + " failed");
			return archives;
		}
		
		FileFilter fileFilter = new FileFilter()
		{
			public boolean accept(File file)
			{
				return (!file.isDirectory() && file.getName().endsWith(".json"));
			}
		};

		File[] flist = dir.listFiles(fileFilter);
		if(flist == null)
		{
			log.severe("loading json files in " + dir.getAbsolutePath() + " failed");
			return archives;
		}
	
		for(File jsonFile : flist)
		{
			String json;
			try {
				json = FileUtils.readFileToString(jsonFile);
			} catch (IOException e) {
				
				e.printStackTrace();
				continue;
			}

			DigitalObjectArchive a = fromString(json);
			if (a == null)
				continue;
		
			archives.add(a);
		}
		return archives;
	}
}
