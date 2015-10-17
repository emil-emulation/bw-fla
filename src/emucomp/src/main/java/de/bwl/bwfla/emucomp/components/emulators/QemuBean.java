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
import javax.ejb.Stateful;
import de.bwl.bwfla.common.datatypes.Drive;
import de.bwl.bwfla.common.datatypes.Nic;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emucomp.conf.EmucompSingleton;

/**
 * @author iv1004
 * 
 */
@Stateful
public class QemuBean extends EmulatorBean
{
	@Override
	public void prepareEmulatorRunner() throws BWFLAException
	{
		String qemu_bin = EmucompSingleton.CONF.qemuBean;
		if(qemu_bin != null && !qemu_bin.contains("i386"))
			qemu_bin += emuEnvironment.getArch(); 

		File exec = new File(qemu_bin);
		if (exec == null || !exec.exists())
			throw new BWFLAException("Emulator's executable not found! Make sure you have specified " + "a valid path to your executable in the corresponding 'properties' file");
		
		String config = this.getNativeConfig();
		// Initialize the process-runner
		runner.setCommand(exec.getAbsolutePath());
		// runner.addArguments("-monitor", "stdio", "-k", "de");
		if (config != null && !config.isEmpty()) {
			String[] tokens = config.trim().split("\\s+");
			for (String token : tokens)
			{
				if(token.isEmpty())
						continue;
				runner.addArgument(token.trim());
			}
		}
		
		if (this.isLocalModeEnabled()) {
			this.setupEmulatorForY11();
			runner.addArgument("-full-screen");
		}
		else 
		{
			runner.addEnvVariable("QEMU_AUDIO_DRV", "sdl");
			this.setupEmulatorForSDLONP();
		}
	}

	@Override
	public boolean addDrive(Drive drive) {
		if (drive == null) {
			LOG.warning("Drive is null, attach cancelled.");
			return false;
		}
		String driveData = this.lookupResource(drive.getData());
		if (driveData == null) {
			LOG.warning("Drive doesn't reference a valid binding, attach cancelled.");
			return false;
		}
		switch (drive.getType()) {
		case FLOPPY:
			runner.addArgument("-drive");
			runner.addArgument("file=", driveData, ",index=", drive.getUnit(), ",if=", drive.getIface());
			if (drive.isBoot())
				runner.addArguments("-boot", "order=a");

			break;

		case DISK:
			runner.addArgument("-drive");
			runner.addArgument("file=", driveData,
			                   ",if=", drive.getIface(),
			                   ",bus=", drive.getBus(),
			                   ",unit=", drive.getUnit(),
			                   ",media=disk");
			
			if (drive.isBoot())
				runner.addArguments("-boot", "order=c");

			break;

		case CDROM:
			runner.addArgument("-drive");
			runner.addArgument("file=", driveData,
			                   ",if=", drive.getIface(),
			                   ",bus=", drive.getBus(),
			                   ",unit=", drive.getUnit(),
			                   ",media=cdrom");
			
			if (drive.isBoot())
				runner.addArguments("-boot", "order=d");

			break;

		default:
			LOG.severe("Device type '" + drive.getType() + "' not supported yet.");
			return false;
		}

		return true;
	}

	@Override
	public boolean connectDrive(Drive drive, boolean connect) {
		if (!runner.isProcessRunning()) {
			LOG.warning("Hotplug is unavailable because emulator is not running.");
			return false;
		}

		String hotplug = new String();
		switch (drive.getType()) {
		
		case FLOPPY:
			hotplug = (drive.getUnit().equals("0")) ? "floppy0" : "floppy1";
			break;

		case CDROM:
			hotplug = "ide1-cd0";
			break;

		default:
			LOG.severe("Device type '" + drive.getType() + "' is not hot-pluggable.");
			return false;
		}

		String monitorCommand = null;
		if (connect) {
			monitorCommand = String.format("change %s %s%n", hotplug,
			        drive.getData().substring("file://".length()));
		}
		else monitorCommand = String.format("eject %s%n", hotplug);
		
		LOG.info("connect cmd: " + monitorCommand);
		try {
			runner.writeToStdIn(monitorCommand);
		}
		catch (IOException e) {
			LOG.warning("Writing to emulator's stdin failed!");
			e.printStackTrace();
			return false;
		}

		return true;
	}

//    @Override
//    protected VolatileDrive allocateDrive(DriveType type, Drive proto)
//    {
//        // Note: Qemu only supports 2 floppy drives (on same bus)
//        //       and 2 ide controllers (with 2 units each).
//
//        VolatileDrive result = new VolatileDrive();
//        result.setType(type);
//        result.setBoot(false);
//        result.setPlugged(true);
//// FIXME
////        result.setTransport(Resource.TransportType.FILE);
//
//        switch (type) {
//        case FLOPPY:
//            result.setIface("floppy");
//            // find first available floppy connector
//
//            // Logic: 2 - (0+1) = 1 => 0 taken, use 1
//            //        2 - (1+1) = 0 => 1 taken, use 0
//            //        2 - (0+1) - (1+1) = -1 => both taken, no drive available
//            int possibleUnit = 2;
//            for (Drive d : this.emuEnvironment.getDrive()) {
//                if (d.getType().equals(Drive.DriveType.FLOPPY)) {
//                    possibleUnit -= Integer.parseInt(d.getUnit()) + 1;
//                }
//            }
//            if (possibleUnit >= 2) {
//                // all connectors available, use first
//                possibleUnit = 0;
//            }
//            if (possibleUnit < 0) {
//                // no drive available
//                return null;
//            }
//
//            result.setBus("0");
//            result.setUnit(Integer.toString(possibleUnit));
//
//            return result;
//        case DISK:
//        case CDROM:
//            // HDDs and CD drives both go to the ide bus
//            result.setIface("ide");
//
//            // same logic as for floppy drives, only for two busses now
//            int possibleBus0 = 2;
//            int possibleBus1 = 2;
//            for (Drive d : this.emuEnvironment.getDrive())
//            {
//                if (d.getIface().equals("ide"))
//                {
//                    if (d.getBus().equals("0")) {
//                        possibleBus0 -= Integer.parseInt(d.getUnit()) + 1;
//                    }
//                    if (d.getBus().equals("1")) {
//                        possibleBus1 -= Integer.parseInt(d.getUnit()) + 1;
//                    }
//                }
//            }
//            if (possibleBus0 >= 2) {
//                possibleBus0 = 0;
//            }
//            if (possibleBus1 >= 2) {
//                possibleBus1 = 0;
//            }
//            if (possibleBus0 == 0 || possibleBus0 == 1) {
//                // connector on bus 0 available
//                result.setBus("0");
//                result.setUnit(Integer.toString(possibleBus0));
//            } else if (possibleBus1 == 0 || possibleBus1 == 1) {
//                // connector on bus 1 available
//                result.setBus("1");
//                result.setUnit(Integer.toString(possibleBus1));
//            } else {
//                // no ide drive available
//                return null;
//            }
//            
//            return result;
//        }
//        return null;
//    }
    
    protected boolean addNic(Nic nic) {
		if (nic == null) {
			LOG.warning("NIC is null, attach canceled.");
			return false;
		}

		runner.addArgument("-net");
		runner.addArgument("nic,macaddr=", nic.getHwaddress());
		runner.addArgument("-net");
		runner.addArgument("vde,sock=", this.tempDir.toPath().resolve("nic_" + nic.getHwaddress()).toString());
		return true;
    }
}
