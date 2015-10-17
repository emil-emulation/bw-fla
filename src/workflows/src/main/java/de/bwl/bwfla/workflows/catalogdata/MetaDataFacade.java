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

package de.bwl.bwfla.workflows.catalogdata;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;



public class MetaDataFacade  implements Serializable 
{
	private static final long	serialVersionUID	= -5194391890270505633L;

	private String searchDir = null;
	protected final Logger log	= Logger.getLogger(this.getClass().getName());
	private List<Description> descriptors;
	private List<Description> filtered_descriptors;
	
	
	public MetaDataFacade(String searchDir)
	{
		this.searchDir = searchDir;
		descriptors = null;
	}

	public Description getDescriptor(String aId)
	{		
		if(descriptors == null)
			loadDescriptors();

		if(descriptors == null)
				return null;

		for(Description d : descriptors)
		{	
			if(d.getId() != null && d.getId().equalsIgnoreCase(aId))
				return d;
		}
		
		return null;	
	}

	public List<Description> getDescriptors()
	{
		if(descriptors == null)
			loadDescriptors();

		// make sure site does not crash if metadata is not available
		if(descriptors == null)
			return new ArrayList<Description>();

		return filtered_descriptors;
	}

	public void sortDescriptors(int sortSelection) 
	{
		if(descriptors == null)
			loadDescriptors();

		if(descriptors == null)
			return;

		try
		{	
			Collections.sort(this.filtered_descriptors);
		}			
		catch(Throwable e)
		{
			/* NOCODE */
		}
	}

	public void filterDescriptors(String filterString)
	{
		if(descriptors == null)
			loadDescriptors();

		if(descriptors == null)
			return;

		filtered_descriptors = this.descriptors;

		//		if (filterString.isEmpty()) 
		//			filtered_descriptors = this.descriptors;
		//		else 
		//		{
		//			List<DescriptionSerializer> result = new ArrayList<DescriptionSerializer>(); 
		//			for (DescriptionSerializer desc : this.descriptors) 
		//			{
		//				DescriptionSerializer  emulDesc = desc;
		//				String needle = filterString.toLowerCase();
		//
		//				boolean contains = false;
		//				contains |= emulDesc.getArchiveid().toLowerCase().contains(needle);
		//				contains |= emulDesc.getTitle().toLowerCase().contains(needle);
		//				contains |= emulDesc.getAuthor().toLowerCase().contains(needle);
		//				contains |= emulDesc.getYear().toLowerCase().contains(needle);
		//
		//				if (contains)
		//					result.add(emulDesc);
		//			}
		//
		//			this.filtered_descriptors = result;
		//		}
	}

	public void filterDescriptors(String filterString, int sortSelection) 
	{
		if(descriptors == null)
			loadDescriptors();

		if(descriptors == null)
			return;

		filterDescriptors(filterString);
		sortDescriptors(sortSelection);
	}

	private String[] getDirectories(File dir)
	{
		if(!dir.exists() && !dir.isDirectory())
		{
			log.severe("Directory " + dir.toString() + " does not exist!");
			return null;
		}
		FileFilter fileFilter = new FileFilter() 
		{
			public boolean accept(File file) {
				return (file.isDirectory() && !(file.getName()).startsWith("."));
			}
		};

		File[] files = dir.listFiles(fileFilter);
		if(files == null)
			return null;

		String[] dirnames = new String[files.length];
		for (int i = 0; i < files.length; i++)
			dirnames[i] = files[i].getName();

		return dirnames;
	}
	
	private void loadDescriptors()
	{
		File base = new File(searchDir);
		List<Description> result = new ArrayList<Description>();
		String[] metaData = getDirectories(base);
		if(metaData == null)
			return;
		
		log.info("search dir: " + searchDir + "with subdirs #" + metaData.length);
		
		for (String aId : metaData) 
		{
			File dir = new File(base, aId);
			
			if(!dir.isDirectory() || !dir.exists())
				continue;
			
			
			log.info("looking for json in " + dir.getAbsolutePath());
			
			FileFilter fileFilter = new FileFilter()
			{
				public boolean accept(File file)
				{
					return (!file.isDirectory() && file.getName().endsWith(".json"));
				}
			};

			File[] flist = dir.listFiles(fileFilter);
			if(flist == null)
				continue;
		
			for(File jsonFile : flist)
			{
				String json;
				try {
					json = FileUtils.readFileToString(jsonFile);
				} catch (IOException e) {
					
					e.printStackTrace();
					continue;
				}

				Description d = null;
				d = DescriptionSerializer.fromString(json, DescriptionJsonSimple.class);
				switch(d.getDescriptionType())
				{
				case SYSTEM:
					d = SystemEnvironmentDescription.fromString(json);
					break;
				case OBJECT:
					d = ObjectEnvironmentDescription.fromString(json);
					break;
				case EVALUATION:
					d = ObjectEvaluationDescription.fromString(json);
					break;
				default:
				}
				if(d != null) result.add(d);
			}	
		}

		this.descriptors = result;
		log.info("loadDescriptors: " + descriptors.size() + " objects found");	
		filterDescriptors("");
	}

	public String thumbURL(String aId)
	{
		if(aId == null)
			return null;

		File destDir = new File(searchDir, aId);
		String filepath = destDir.toString() + "/" + aId + ".jpg";
		File fl = new File(filepath);
		return fl.exists() ? fl.getAbsolutePath() : null;
	}

	public boolean savePicture(String aId, File pic)
	{
		try
		{
			String newImage = searchDir
					+ File.separator
					+ aId
					+ File.separator
					+ aId
					+ ".jpg";
			File imageFile = new File(newImage);

			if (imageFile.exists())
				FileUtils.deleteQuietly(imageFile);

			log.info("trying to copy : " + pic + " -- " + imageFile);
			FileUtils.copyFile(pic, imageFile);
			return true;
		} catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}
	}


	public boolean saveDescription(Description d)
	{
		log.info("saving metadata in " + searchDir + "for: " + d.getId()); 
		
		File destDir = new File(searchDir, d.getId());
		if(!destDir.exists())
			try {
				Files.createDirectory(destDir.toPath());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return false;
			}
		
		File destFile = new File(destDir, d.getId() + ".json");
		
		BufferedWriter out = null;
		try
		{
			out = new BufferedWriter(new FileWriter(destFile));
			out.write(d.toString());
			out.flush();
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
				if(out != null)
					out.close();
			}
			catch(Throwable e)
			{
				e.printStackTrace();
			}
		}
		
	}
}
