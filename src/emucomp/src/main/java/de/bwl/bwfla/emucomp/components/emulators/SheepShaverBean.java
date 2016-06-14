package de.bwl.bwfla.emucomp.components.emulators;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
            try {
                Path link = this.tempDir.toPath().resolve(Paths.get("floppy-" + drive.getBus() + "-" + drive.getUnit() + ".img"));
                Files.deleteIfExists(link);
                Files.createSymbolicLink(link, imagePath);
                imagePath = link;
            } catch (IOException e) {
                LOG.warning("Cannot create generic symlink for floppy image, attach cancelled.");
                return false;
            }
			runner.addArgument("--disk");
			runner.addArgument(imagePath.toString());
			break;

		case DISK:
			runner.addArgument("--disk");
			runner.addArgument(imagePath.toString());
			break;

		case CDROM:
            try {
                Path link = this.tempDir.toPath().resolve(Paths.get("cdrom-" + drive.getBus() + "-" + drive.getUnit() + ".iso"));
                Files.deleteIfExists(link);
                Files.createSymbolicLink(link, imagePath);
                imagePath = link;
            } catch (IOException e) {
                LOG.warning("Cannot create generic symlink for cdrom image, attach cancelled.");
                return false;
            }
			runner.addArgument("--cdrom");
			runner.addArgument(imagePath.toString());
			break;

		default:
			LOG.severe("Device type '" + drive.getType() + "' not supported yet.");
			return false;
		}

		return true;
	}

    @Override
    public int changeMedium(int containerId, String objReference)
            throws BWFLAException {
        throw new BWFLAException("Hotplug is not supported by this emulator");
    }

    @Override
    public boolean connectDrive(Drive drive, boolean attach) {
        // This method should never be called.
        LOG.severe("Hotplug is not supported by this emulator");
        LOG.info("The previous message cannot appear. Please verify that changeMedium is correctly overridden in BasiliskIIBean.");
        return false;

        // This code WOULD implement hotswapping media IF BasiliskII would allow
        // it
        /*
        if (drive == null) {
            LOG.warning("Drive is null, (de-)attach cancelled.");
            return false;
        }

        Path imagePath = Paths.get(this.lookupResource(drive.getData()));
        if (attach) {
            if (imagePath == null || !Files.exists(imagePath)) {
                LOG.warning("Drive doesn't reference a valid binding, attach cancelled.");
                return false;
            }
        } else {
            imagePath = Paths.get("/dev/null");
        }

        switch (drive.getType()) {
        case FLOPPY:
            try {
                Path link = this.tempDir.toPath().resolve(
                        Paths.get("floppy-" + drive.getBus() + "-"
                                + drive.getUnit() + ".img"));
                Files.deleteIfExists(link);
                Files.createSymbolicLink(link, imagePath);
            } catch (IOException e) {
                LOG.severe("Could not remove symbolic link to floppy. Detach cancelled.");
                return false;
            }
            break;

        case DISK:
            LOG.warning("Hotplug for disk drives is not supported by this emulator.");
            return false;

        case CDROM:
            try {
                Path link = this.tempDir.toPath().resolve(
                        Paths.get("cdrom-" + drive.getBus() + "-"
                                + drive.getUnit() + ".iso"));
                Files.deleteIfExists(link);
                Files.createSymbolicLink(link, imagePath);
            } catch (IOException e) {
                LOG.severe("Could not remove symbolic link to cdrom. Detach cancelled.");
                return false;
            }
            break;

        default:
            LOG.severe("Device type '" + drive.getType()
                    + "' not supported yet.");
            return false;
        }
        return true;
        */
    }

	@Override
	protected boolean addNic(Nic nic) {
		LOG.warning("Network connection is currently not implemented.");
		return false;
	}
}
