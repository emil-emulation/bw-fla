package de.bwl.bwfla.emucomp.components.emulators;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import javax.ejb.Stateful;

import de.bwl.bwfla.common.datatypes.Drive;
import de.bwl.bwfla.common.datatypes.Nic;
import de.bwl.bwfla.common.datatypes.Drive.DriveType;
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
		
		if(qemu_bin == null)
			throw new BWFLAException("Emulator's executable not found! Make sure you have specified " + "a valid path to your executable in the corresponding 'properties' file");
		
		if(!qemu_bin.contains("i386"))
			qemu_bin += emuEnvironment.getArch(); 

		File exec = new File(qemu_bin);
		if (exec == null || !exec.exists())
			throw new BWFLAException("Emulator's executable not found! Make sure you have specified " + "a valid path to your executable in the corresponding 'properties' file");
		
		String config = this.getNativeConfig();
		// Initialize the process-runner
		runner.setCommand(exec.getAbsolutePath());
		runner.addArguments("-monitor", "stdio");
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
			runner.addArguments("-k", "en-us");
			this.setupEmulatorForSDLONP();
		}
	}
	
	@Override
	public Set<String> getHotplugableDrives()
	{
		HashSet<String> set = new HashSet<String>();
		set.add(DriveType.CDROM.name());
		set.add(DriveType.DISK.name());
		set.add(DriveType.FLOPPY.name());
		return set;
	}

	@Override
	public boolean addDrive(Drive drive) {
        if (drive == null || (drive.getData() == null)) {
            LOG.warning("Drive doesn't contain an image, attach canceled.");
            return false;
        }

        Path imagePath = null;
        try {
            imagePath = Paths.get(this.lookupResource(drive.getData(), this.getImageFormatForDriveType(drive.getType())));
        } catch (Exception e) {
            LOG.warning("Drive doesn't reference a valid binding, attach canceled.");
            return false;
        }
        
		switch (drive.getType()) {
		case FLOPPY:
			runner.addArgument("-drive");
			runner.addArgument("file=", imagePath.toString(), ",index=", drive.getUnit(), ",if=floppy");
			if (drive.isBoot())
				runner.addArguments("-boot", "order=a");

			break;

		case DISK:
			runner.addArgument("-drive");
			runner.addArgument("file=", imagePath.toString(),
			                   ",if=", drive.getIface(),
			                   ",bus=", drive.getBus(),
			                   ",unit=", drive.getUnit(),
			                   ",media=disk");
			
			if (drive.isBoot())
				runner.addArguments("-boot", "order=c");

			break;

		case CDROM:
			runner.addArgument("-drive");
			runner.addArgument("file=", imagePath.toString(),
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

		StringBuilder command = new StringBuilder();

        if (!connect) {
            // detach/eject
            command.append("eject -f ");

            switch (drive.getType()) {
            case FLOPPY:
                command.append("floppy" + drive.getUnit());
                break;
            case CDROM:
                command.append(drive.getIface().toLowerCase());
                command.append(drive.getBus());
                command.append("-");
                command.append("cd");
                command.append(drive.getUnit());
                command.append(" ");
                break;
            default:
                LOG.severe("Device type '" + drive.getType()
                        + "' is not hot-pluggable.");
                return false;
            }
        } else {
            if (drive == null) {
                LOG.warning("Drive doesn't contain an image, attach canceled.");
                return false;
            }
            
            Path imagePath = null;
            try {
                imagePath = Paths.get(this.lookupResource(drive.getData(), this.getImageFormatForDriveType(drive.getType())));
            } catch (Exception e) {
                LOG.warning("Drive doesn't reference a valid binding, attach cancelled.");
                return false;
            }
            
            command.append("change ");

            switch (drive.getType()) {
            case FLOPPY:
                command.append("floppy" + drive.getUnit());
                command.append(" ");
                break;
            case CDROM:
                command.append(drive.getIface().toLowerCase());
                command.append(drive.getBus());
                command.append("-");
                command.append("cd");
                command.append(drive.getUnit());
                command.append(" ");
                break;
            default:
                LOG.severe("Device type '" + drive.getType()
                        + "' is not hot-pluggable.");
                return false;
            }
            
            command.append(imagePath.toString());
            
        }
        this.sendMonitorCommand(command.toString());
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
    
    protected void sendMonitorCommand(String command) {
        if (command != null && !command.isEmpty()) {
            try {
                this.runner.writeToStdIn(command + "\n");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
