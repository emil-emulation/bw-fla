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

package de.bwl.bwfla.emucomp.components.emulators;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import de.bwl.bwfla.common.datatypes.Drive;
import de.bwl.bwfla.common.datatypes.Nic;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.Zip32Utils;
import de.bwl.bwfla.emucomp.conf.EmucompSingleton;



public class HatariBean extends EmulatorBean
{	
	@Override
	public void prepareEmulatorRunner() throws BWFLAException
	{
		runner.setCommand(EmucompSingleton.CONF.hatariBean);
		if (this.isLocalModeEnabled()) {
			runner.addArgument("--fullscreen");
			this.setupEmulatorForY11();
		}
		else this.setupEmulatorForSDLONP();
	}

	@Override
	public boolean addDrive(Drive drive)
	{
		String resultHddFile;
		if(drive == null || (resultHddFile = this.lookupResource(drive.getData())) == null) 
		{
			LOG.warning("drive doesn't contain an image, attach cancelled");
			return false;
		}

		if(!drive.isBoot())
		{
			switch(drive.getType()) 
			{
				case DISK:
					try
					{
						Path unzippedHddDir = Files.createTempDirectory(tempDir.toPath(), "unzipped_container", new FileAttribute<?>[0]);
						Zip32Utils.unzip(new File(resultHddFile), unzippedHddDir.toFile());
						resultHddFile = unzippedHddDir.toString();
						runner.addArgument("--harddrive");
					}
					catch(IOException e)
					{
						e.printStackTrace();
					}
					
					break;
		
				default:
					LOG.severe("device type '" + drive.getType() + "' not supported yet, attach cancelled");
					return false;
			}
		}
		else
			runner.addArgument("--tos");
		
		runner.addArgument(resultHddFile);
		return true;
	}

	@Override
	public boolean connectDrive(Drive drive, boolean connect)
	{
		LOG.severe("operation unsupported yet: " + this.getClass().getEnclosingMethod().getName());
		return false;
	}

//	@Override
//	synchronized protected VolatileDrive allocateDrive(DriveType type, Drive proto)
//	{
//        VolatileDrive result = new VolatileDrive();
//        result.setType(type);
//        result.setBoot(false);
//        result.setPlugged(true);
//
//        switch(type)
//        {
//	        case DISK:
//	        	for(Drive d: this.emuEnvironment.getDrive()) 
//	                if(d.getType().equals(Drive.DriveType.DISK) && (d.getData() == null || d.getData().isEmpty())) 
//			            return result;
//	            
//	        default:
//	        	LOG.severe("device type '" + type + "' not supported yet, allocation cancelled");
//	        	break;
//        }
//        
//        return null;
//	}

	protected boolean addNic(Nic nic)
	{
		LOG.severe("operation unsupported yet: " + this.getClass().getEnclosingMethod().getName());
		return false;
	}
}