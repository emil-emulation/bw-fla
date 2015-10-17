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

package de.bwl.bwfla.workflows.softwarearchive;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.Iso9660Utils;
import de.bwl.bwfla.workflows.softwarearchive.datatypes.BundledFile;
import de.bwl.bwfla.workflows.softwarearchive.datatypes.MediumType;

@XmlRootElement(name = "fileSoftwareArchive")
@XmlAccessorType(XmlAccessType.FIELD)
public class FileSoftwareArchive extends SoftwareArchive {
	protected final static Logger log = Logger
			.getLogger(FileSoftwareArchive.class.getName());

	@XmlElement(required = true)
	protected String archiveDirectory;
	
	@XmlElement(required = true)
	protected String incomingDirectory;
	
	@XmlElement(required = true)
	protected String exportUrlBase;

	public String getArchiveDirectory() {
		return archiveDirectory;
	}

	@Override
	public List<SoftwareDescription> getSoftwareList() {
		ArrayList<SoftwareDescription> swList = new ArrayList<>();

		if (archiveDirectory == null) {
			log.warning("No software archive directory given, please fix your configuration metadata");
			return swList;
		}

		Path archivePath = Paths.get(archiveDirectory);
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(
				archivePath, "*.xml")) {
			for (Path path : stream) {
				SoftwareDescription desc = SoftwareDescription.fromFile(path
						.toFile());
				swList.add(desc);
			}

		} catch (Exception e) {
			log.info("failure");
			e.printStackTrace();
		}

		return swList;
	}

	@Override
	public void saveSoftware(SoftwareDescription desc) throws BWFLAException {
		Path p = Paths.get(archiveDirectory).resolve(desc.getId() + ".xml");
		try {
			Files.write(p, desc.value().getBytes());

			Path container = Paths.get(archiveDirectory).resolve(
					desc.getId() + ".iso");
			ArrayList<Path> paths = new ArrayList<Path>(
					desc.getSoftwareBundle().files.size());

			for (BundledFile f : desc.getSoftwareBundle().files) {
				paths.add(Paths.get(incomingDirectory).resolve(f.getPlatform()).resolve(f.getPath()));
			}
			Iso9660Utils.createIso9660(container, paths);
		} catch (IOException e) {
			throw new BWFLAException("Cannot create software bundle.", e);
		} catch (JAXBException e) {
		    throw new BWFLAException("Cannot unmarshal Software Description.", e);
		}
	}

	@Override
	public URL getSoftwareBundleUrl(SoftwareDescription desc) {
		try {
			return new URL(this.exportUrlBase + desc.getId() + ".iso");
		} catch (MalformedURLException e) {
			log.severe("Could not create URL for software bundle.");
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public List<BundledFile> getAvailableFiles(String platform)
	{
		File in = new File(incomingDirectory, platform);
		if(!in.exists())
		{
			log.severe("dir : " + in + " does not exist");
			return null;
		}
		
		List<BundledFile> filesSource = new ArrayList<>();
		try (DirectoryStream<Path> directoryStream = Files
				.newDirectoryStream(in.toPath())) {
			for (Path path : directoryStream) {
				filesSource.add(new BundledFile(path.getFileName(), platform, MediumType.CDROM));
			}
		} catch (IOException ex) {
		}

		Collections.sort(filesSource, new Comparator<BundledFile>() {
			@Override
			public int compare(BundledFile o1, BundledFile o2) {
				if (Files.isDirectory(o1.getPath()) == Files.isDirectory(o2
						.getPath())) {
					return o1
							.getPath()
							.getFileName()
							.toString()
							.compareToIgnoreCase(
									o2.getPath().getFileName().toString());
				}

				return Files.isDirectory(o1.getPath()) ? -1 : 1;
			}
		});
		return filesSource;
	}
	
	@Override
	public List<String> getSupportedPlatforms() throws BWFLAException {
		List<String> platforms = new ArrayList<String>();
		
		File dir = new File(incomingDirectory);
		if(!dir.exists())
		{
			throw new BWFLAException("sw archive is not configured poperly: " + incomingDirectory + " does not exist");
		}
		FileFilter fileFilter = new FileFilter()
		{
			public boolean accept(File file)
			{
				return (file.isDirectory() && !file.getName().startsWith("."));
			}
		};

		File[] flist = dir.listFiles(fileFilter);
		if(flist == null)
			return null;
		
		platforms.add("---");
		for(File f : flist)
			platforms.add(f.getName());
		
		return platforms;
	}
	
	

}
