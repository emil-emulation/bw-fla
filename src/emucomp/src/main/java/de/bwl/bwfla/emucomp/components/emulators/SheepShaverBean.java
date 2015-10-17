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

import javax.ejb.Stateful;
import de.bwl.bwfla.common.datatypes.Drive;
import de.bwl.bwfla.common.datatypes.Nic;
import de.bwl.bwfla.emucomp.conf.EmucompSingleton;

/**
 * @author iv1004
 * 
 */
@Stateful
public class SheepShaverBean extends EmulatorBean
{
	@Override
	public void prepareEmulatorRunner()
	{
		String config = this.getNativeConfig();
		
		// Initialize the process-runner
		runner.setCommand(EmucompSingleton.CONF.sheepShaverBean);
		if (config != null) {
			String[] tokens = config.trim().split("\n");
			for (String token : tokens) {	
				String[] args = token.trim().split("\\s+");
				if (args.length < 1 || args.length > 2) {
					LOG.warning("check your native config file, some 'param-value' pairs are malformed");
					continue;
				}
				
				runner.addArgument("--" + args[0].trim());
				if (args.length == 2 && !args[1].isEmpty())
					runner.addArgument(args[1].trim());
			}
		}

		if (this.isLocalModeEnabled())
			this.setupEmulatorForY11();
		else this.setupEmulatorForSDLONP();
	}

	@Override
	public boolean addDrive(Drive drive) {
		if (drive == null || this.lookupResource(drive.getData()) == null) {
			LOG.warning("Drive doesn't contain an image, attach canceled.");
			return false;
		}

		switch (drive.getType()) {
		case FLOPPY:
			runner.addArgument("--floppy");
			runner.addArgument(this.lookupResource(drive.getData()));
			break;

		case DISK:
			runner.addArgument("--disk");
			runner.addArgument(this.lookupResource(drive.getData()));
			break;

		case CDROM:
			runner.addArgument("--cdrom");
			runner.addArgument(this.lookupResource(drive.getData()));
			break;

		default:
			LOG.severe("Device type '" + drive.getType() + "' not supported yet.");
			return false;
		}

		return true;
	}

	@Override
	public boolean connectDrive(Drive drive, boolean attach) {
		LOG.warning("Hotplug is not supported by this emulator.");
		return false;
	}

//    @Override
//    protected VolatileDrive allocateDrive(DriveType type, Drive proto)
//    {
//        // Note: BasiliskII supports 7 drives and does not really care about
//        //       the type, order or bus/unit number
//        
//        if (this.emuEnvironment.getDrive().size() > 7) {
//            return null;
//        }
//
//        VolatileDrive result = new VolatileDrive();
//        result.setType(type);
//        result.setBoot(false);
//        result.setPlugged(true);
//// FIXME
////        result.setTransport(Resource.TransportType.FILE);
//
//        result.setIface("scsi"); // this is rather for documenting purposes
//        return result;
//    }

	@Override
	protected boolean addNic(Nic nic) {
		LOG.warning("Network connection is currently not implemented.");
		return false;
	}
}
