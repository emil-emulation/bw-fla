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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.bwl.bwfla.common.utils.EmulatorUtils;
import de.bwl.bwfla.common.utils.Pair;
import de.bwl.bwfla.common.utils.ProcessRunner;
import edu.harvard.hul.ois.fits.FitsMetadataElement;
import edu.harvard.hul.ois.fits.FitsOutput;


public class ImageClassificationHelper {

	static final String DOSEXE = "x-fmt/409";
	static final String WIN16EXE = "x-fmt/410";
	static final String WIN32EXE = "x-fmt/411";
	static final String PPCMACPEF = "bwfla/1";
	static final String ELF32LE = "fmt/688";
	static final String ELF32BE = "fmt/689";
	static final String ELF64LE = "fmt/690";
	static final String ELF64BE = "fmt/691";
	static final String OSXMAC32 = "fmt/692";
	static final String OSXMAC64 = "fmt/693";
	static final String MACBINHEX = "x-fmt/416";
 	
	// secondary fmts
	
	static final String AUTORUN = "fmt/212";

	private final static Logger log = LoggerFactory.getLogger(ImageClassificationHelper.class);

	public static enum EmuEnvType {
		EMUENV_UNKNOWN,
		EMUENV_DOS,
		EMUENV_WIN311,
		EMUENV_WIN9X,
		EMUENV_WINXP,
		EMUENV_MACPPC,
		EMUENV_MACMK68,
		EMUENV_UNIX32LE,
		EMUENV_UNIX64LE,
		EMUENV_UNIX32BE,
		EMUENV_UNIX64BE,
		EMUENV_OSXMAC32,
		EMUENV_OSXMAC64,
	}

	private static boolean isHfsHybridIso(Path isopath)
	{
		try { 
			ProcessRunner process = new ProcessRunner();
			process.setCommand("/bin/bash");
			process.addArgument("-c");
			String cmd = "head -c 2048 " + isopath.toString() + " | grep -c Apple_partition_map";
			process.addArgument(cmd);
			process.execute(false, false);
			String res = process.getStdOutString();
			process.cleanup();

			if(Integer.parseInt(res.trim()) == 0)
			{
				log.info(isopath.toString() + " is NOT an hybrid ");
				return false;
			}
			else 
			{
				log.info(isopath.toString() + " is an hybrid ");
				return true;
			}
		}
		catch(Exception e)
		{
			return false;
		}
	}

	private static FileTypeHistogram getHfsHistogram(Path isopath)
	{
		try { 
			ProcessRunner process = new ProcessRunner();
			File tempDir = Files.createTempDirectory("hfs-mount-").toFile();

			process.setCommand("sudo");
			process.addArgument("/bin/mount");
			process.addArgument("-t");
			process.addArgValue("hfs");
			process.addArgument(isopath.toString());
			process.addArgument(tempDir.getAbsolutePath());
			if(!process.execute())
			{
				log.error("mount failed");
				FileUtils.deleteDirectory(tempDir);
				return null;
			}

			FitsClassifier classifier = new FitsClassifier();
			classifier.classify(tempDir.toPath(), false);

			process.setCommand("sudo");
			process.addArgument("/bin/umount");
			process.addArgument(tempDir.getAbsolutePath());
			if(!process.execute())
			{
				log.debug("unmounting " + tempDir.getAbsolutePath() + "failed");
			}

			FileTypeHistogram histogram = new FileTypeHistogram(256);
			histogram.build(classifier.getResults());
			histogram.print(true);
			histogram.dump(Files.createTempFile("fits-report-hfs-", ".txt"));
			
			FileUtils.deleteDirectory(tempDir);

			return histogram;
		}
		catch(Exception e)
		{
			return null;
		}
	}

	private static FileTypeHistogram getIsoHistogram(Path isopath)
	{
		try {
			File tempDir = Files.createTempDirectory("iso-mount-").toFile();

			if(!EmulatorUtils.mountUdfFile(isopath, tempDir.toPath()))
			{
				FileUtils.deleteDirectory(tempDir);
				return null;
			}
			FitsClassifier classifier = new FitsClassifier();
			classifier.classify(tempDir.toPath(), false);

			FileTypeHistogram histogram = new FileTypeHistogram(256);
			histogram.build(classifier.getResults());
			histogram.print(true);
			histogram.dump(Files.createTempFile("fits-report-iso-", ".txt"));
			
			ProcessRunner process = new ProcessRunner();
			process.setCommand("fusermount");
			process.addArguments("-u", "-z");
			process.addArgument(tempDir.toString());
			if(!process.execute())
			{
				log.debug("unmounting " + tempDir.getAbsolutePath() + " failed");
			}
			
			FileUtils.deleteDirectory(tempDir);
			
			return histogram;
		}
		catch(Exception e)
		{
			return null;
		}
	}
	
	private static List<EmuEnvType> classifyHfsForOs(FileTypeHistogram histogram)
	{
		List<EmuEnvType> result = new ArrayList<EmuEnvType>();
		List<FitsOutput> peflist = histogram.exttypes.get(PPCMACPEF);
		List<FitsOutput> binhex = histogram.exttypes.get(MACBINHEX);
		
		if(peflist != null && peflist.size() > 0)
			 result.add(EmuEnvType.EMUENV_MACPPC);
		
		if(binhex != null && binhex.size() > 0)
			result.add(EmuEnvType.EMUENV_MACPPC);
		
		result.add(EmuEnvType.EMUENV_MACMK68);

		return result;
	}

	private static List<EmuEnvType> classifyIsoForOs(FileTypeHistogram histogram)
	{
		List<EmuEnvType> result = new ArrayList<EmuEnvType>();
		List<FitsOutput> doslist = histogram.exttypes.get(DOSEXE);
		List<FitsOutput> win16list = histogram.exttypes.get(WIN16EXE);
		List<FitsOutput> win32list = histogram.exttypes.get(WIN32EXE);
		
		if(histogram.exttypes.get(ELF32LE) != null)
			result.add(EmuEnvType.EMUENV_UNIX32LE);
		
		if(histogram.exttypes.get(ELF32BE) != null)
			result.add(EmuEnvType.EMUENV_UNIX32BE);
		
		if(histogram.exttypes.get(ELF64LE) != null)
			result.add(EmuEnvType.EMUENV_UNIX64LE);
		
		if(histogram.exttypes.get(ELF64BE) != null)
			result.add(EmuEnvType.EMUENV_UNIX64BE);
		
		if(histogram.exttypes.get(OSXMAC32) != null)
			result.add(EmuEnvType.EMUENV_OSXMAC32);
		
		if(histogram.exttypes.get(OSXMAC64) != null)
			result.add(EmuEnvType.EMUENV_OSXMAC64);
		
		if(win32list != null && win32list.size() > 0)
		{
			int oldWin32 = 0;
			int newWin32 = 0;
		
			for(FitsOutput o : win32list)
			{
				List<FitsMetadataElement> m = o.getFileInfoElements();
				for (FitsMetadataElement e : m)
				{
					log.info(e.getName() + " : " + e.getValue());
					if(e.getName().equals("fslastmodified"))
					{
						long uts = Long.parseLong(e.getValue());
						Date ts = new Date(uts);
						int y = ts.getYear() + 1900;
						if(y < 2003)
							oldWin32++;
						else 
							newWin32++;
					}
				}
			}
			
			if(oldWin32 >= newWin32)
				result.add(EmuEnvType.EMUENV_WIN9X);
			else
				result.add(EmuEnvType.EMUENV_WINXP);
		}
			
		if(win16list != null && win16list.size() > 0)
			result.add(EmuEnvType.EMUENV_WIN311);

		if(doslist != null && doslist.size() > 0)
			result.add(EmuEnvType.EMUENV_DOS);

		return result;
	}
	
	private static void classifyForSecondaryProperties(FileTypeHistogram hist, List<EmuEnvType> result)
	{
		if(hist.exttypes.get(AUTORUN) != null)
			result.add(EmuEnvType.EMUENV_WIN9X);
	}
	
	public static FileTypeHistogram classifyHist(Path isopath)
	{
		return getIsoHistogram(isopath);
	}
	
	public static List<EmuEnvType> classify(Path isopath)
	{
		List<EmuEnvType> result = new ArrayList<EmuEnvType>();
		List<EmuEnvType> hfsResult = null, isoResult = null; 
		
		if(isHfsHybridIso(isopath))
		{
			FileTypeHistogram hfsHist = getHfsHistogram(isopath);
			if(hfsHist != null)
				hfsResult = classifyHfsForOs(hfsHist);
		}
	
		FileTypeHistogram isoHist = getIsoHistogram(isopath);
		if(isoHist != null)
		{
			isoResult = classifyIsoForOs(isoHist);
			if(isoResult.size() == 0)
			{
				classifyForSecondaryProperties(isoHist, isoResult);
			}
		}
		
		if(isoResult != null)
			result.addAll(isoResult);
		
		if(hfsResult != null) 
			result.addAll(hfsResult);
		
		return result;
	}

	/**
	 * @param result
	 * @return
	 */
	public static String getEnvironmentForEmuEnvType(List<EmuEnvType> result) {
		String autodetectedBaseimageId = null;
		
		if(result.size() == 0)
			return null;
		
		switch (result.get(0)) {
		case EMUENV_DOS:
			// MS DOS 6.20 with MSCDEX on Qemu
			autodetectedBaseimageId = "2010";
			break;
		case EMUENV_WIN311:
			// Win 3.11 on VirtualBox
			autodetectedBaseimageId = "2012";
			break;
		case EMUENV_WIN9X:
			// Win 98 (SE) on VBox
			autodetectedBaseimageId = "4404";
			break;
		case EMUENV_WINXP:
			// Win XP Pro 32bit english on VBox
			autodetectedBaseimageId = "4003";
			break;
		case EMUENV_MACPPC:
			// Mac OS 9 on Sheepshaver
			autodetectedBaseimageId = "3004";
			break;
		case EMUENV_MACMK68:
			// Mac OS 7.5 on BasiliskII
			autodetectedBaseimageId = "1005";
			break;
		case EMUENV_UNIX32LE:
		case EMUENV_UNIX64LE:
		case EMUENV_UNIX32BE:
		case EMUENV_UNIX64BE:
		case EMUENV_OSXMAC32:
		case EMUENV_OSXMAC64:
			autodetectedBaseimageId = null;
		default:
			break;
		}
		return autodetectedBaseimageId;
	}
}
