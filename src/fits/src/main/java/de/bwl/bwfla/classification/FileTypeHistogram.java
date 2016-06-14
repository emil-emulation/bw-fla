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
package de.bwl.bwfla.classification;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.bwl.bwfla.common.utils.Pair;
import edu.harvard.hul.ois.fits.FitsMetadataElement;
import edu.harvard.hul.ois.fits.FitsOutput;
import edu.harvard.hul.ois.fits.identity.ExternalIdentifier;
import edu.harvard.hul.ois.fits.identity.FitsIdentity;


public class FileTypeHistogram
{
	/** Logger instance. */
	private final Logger log = LoggerFactory.getLogger(FileTypeHistogram.class);
	
	// Member fields
	public final Map<String, List<FitsOutput>> mimetypes;
	public final Map<String, List<FitsOutput>> exttypes;
	public final List<FitsOutput> unclassified; 
	
	/** Constructor */
	public FileTypeHistogram(int capacity)
	{
		this.mimetypes = new HashMap<String, List<FitsOutput>>(capacity);
		this.exttypes = new HashMap<String, List<FitsOutput>>(capacity);
		this.unclassified = new ArrayList<FitsOutput>();
	}
	
	/** Build the histogram from specified outputs. */
	public void build(List<FitsOutput> outputs)
	{
		// Look at every result...
		for (FitsOutput output : outputs) {
			// ... and identity section
			for (FitsIdentity identity : output.getIdentities()) {
				this.add(mimetypes, identity.getMimetype(), output);
				
				// Save also the external identifiers, when available
				for (ExternalIdentifier extid : identity.getExternalIdentifiers())
					this.add(exttypes, extid.getValue(), output);
			}
			
			if(output.getIdentities().isEmpty())
				unclassified.add(output);
			
		}
	}
	
	/** Returns the outputs indexed by mimetypes. */
	public Map<String, List<FitsOutput>> getMimetypeIndex()
	{
		return mimetypes;
	}
	
	/** Returns the outputs indexed by external types. */
	public Map<String, List<FitsOutput>> getExtTypeIndex()
	{
		return exttypes;
	} 
	
	public void append(FileTypeHistogram m)
	{
		if(m == null)
			return;
		
		if(m.mimetypes == null)
		{
			log.info("mimetypes == null");
			return;
		}
		
		for (Map.Entry<String, List<FitsOutput>> entry : m.mimetypes.entrySet())
			this.addList(mimetypes, entry.getKey(), entry.getValue());
		
		for (Map.Entry<String, List<FitsOutput>> entry : m.exttypes.entrySet())
			this.addList(exttypes, entry.getKey(), entry.getValue());
	}
	
	public List<Pair<String, Integer>> getOrderedPUIDList()
	{
		List<Pair<String, Integer>> out = new ArrayList<Pair<String,Integer>>();
		for (Map.Entry<String, List<FitsOutput>> entry : exttypes.entrySet())
		{
			out.add(new Pair<String, Integer>(entry.getKey(), new Integer(entry.getValue().size())));
		}
		
		Collections.sort(out, new Comparator<Pair<String, Integer>>() {
			public int compare(final Pair<String, Integer> a, final Pair<String, Integer> b) {
				return -a.getB().compareTo(b.getB());}
			}); 
		return out;
	}
	
	/** Print the histogram to log. */
	public void print(boolean printExtTypes)
	{
		log.info("Mimetype-Histogram:");
		
		for (Map.Entry<String, List<FitsOutput>> entry : mimetypes.entrySet())
			log.info("{} {}", entry.getKey(), entry.getValue().size());
		
		if (!printExtTypes)
			return;
		
		log.info("ExtType-Histogram:");
		
		for (Map.Entry<String, List<FitsOutput>> entry : exttypes.entrySet())
			log.info("{} {}", entry.getKey(), entry.getValue().size());
		
		log.info("Unclassified " + unclassified.size());
		for(FitsOutput o : unclassified)
		{
			List<FitsMetadataElement> m = o.getFileInfoElements();
			for (FitsMetadataElement e : m)
			{
				log.info(e.getName() + " : " + e.getValue());
				if(e.getName().equals("fslastmodified"))
				{
					long uts = Long.parseLong(e.getValue());
					Date ts = new Date(uts);
					log.info("year " + (ts.getYear() + 1900));
				}
			}
			
			log.info("--");
		}
	}
	
	/** Generate and write the report to specified path. */
	public void dump(Path outpath) throws IOException
	{
		BufferedWriter writer = Files.newBufferedWriter(outpath, StandardCharsets.UTF_8);
		
		FileTypeHistogram.writeHistogram(writer, mimetypes, "Mimetype-Histogram");
		FileTypeHistogram.writeHistogram(writer, exttypes, "ExtType-Histogram");
		
		writer.write("==================== Detailed Report ====================");
		writer.newLine();
		writer.newLine();
		
		FileTypeHistogram.writeFilePaths(writer, mimetypes);
		FileTypeHistogram.writeFilePaths(writer, exttypes);
		
		writer.flush();
		writer.close();
		
		log.info("Fits-Report written to '{}'.", outpath);
	}
	
	
	/* ==================== Internal Helpers ==================== */
	
	private void add(Map<String, List<FitsOutput>> map, String key, FitsOutput value)
	{
		List<FitsOutput> list = map.get(key);
		if (list == null) {
			list = new ArrayList<FitsOutput>();
			map.put(key, list);
		}
		
		list.add(value);
	}
	
	private void addList(Map<String, List<FitsOutput>> map, String key, List<FitsOutput> l)
	{
		List<FitsOutput> list = map.get(key);
		if (list == null) 
			map.put(key, l);
		else
			list.addAll(l);
	}
	
	private static void writeHistogram(BufferedWriter writer, Map<String, List<FitsOutput>> map, String title) throws IOException
	{
		writer.write(title + ":");
		writer.newLine();
		
		for (Map.Entry<String, List<FitsOutput>> entry : map.entrySet()) {
			writer.write("\t" + entry.getKey() + "  " + entry.getValue().size());
			writer.newLine();
		}
		
		writer.newLine();
	}
	
	private static void writeFilePaths(BufferedWriter writer, Map<String, List<FitsOutput>> map) throws IOException
	{
		for (Map.Entry<String, List<FitsOutput>> entry : map.entrySet()) {
			writer.write(entry.getKey() + ":");
			writer.newLine();
			
			for (FitsOutput output : entry.getValue()) {
				writer.write("\t" + output.getMetadataElement("filepath").getValue());
				writer.newLine();
			}
			
			writer.newLine();
		}
	}
}
