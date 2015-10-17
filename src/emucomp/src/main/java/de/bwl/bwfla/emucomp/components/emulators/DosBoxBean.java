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
import de.bwl.bwfla.common.datatypes.EmulationEnvironment;
import de.bwl.bwfla.common.datatypes.Nic;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emucomp.conf.EmucompSingleton;



/**
 * 
 * @author Johann Latocha <johann.latocha@rz.uni-freiburg.de>
 * 
 */
@Stateful
public class DosBoxBean extends EmulatorBean {

	private char letter = 'c';
	private final char[] letters = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
			'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

	@Override
	protected void setRuntimeConfiguration(EmulationEnvironment environment)
			throws BWFLAException {
		super.setRuntimeConfiguration(environment);
		runner.addArguments("-c", "if exist c:\\autoexec.bat c:\\autoexec.bat");
	}

	@Override
	public void prepareEmulatorRunner()
	{
		String config = this.getNativeConfig();
		
		// Initialize the process-runner
		runner.setCommand(EmucompSingleton.CONF.dosBoxBean);
		if (config != null) {
			String[] tokens = config.trim().split(",");
			for (String token : tokens)
				runner.addArgument(token.trim());
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

		String value = this.lookupResource(drive.getData());
/*
		if (value.contains("zip")) {

			Path destFolder = null;
			try { destFolder = Files.createTempDirectory(this.tempDir.toPath(), "dosbox-"); }
			catch(Exception e) {e.printStackTrace();}
			LOG.info("creating temp folder " + destFolder.toString());  
			ZipUtils.unzipTo(new File(value), destFolder.toFile());
			value = destFolder.toString();
		}
*/
		switch (drive.getType()) {
		case FLOPPY:
			runner.addArguments("-c", String.format("imgmount %s %s %s", this.getDriveLetter(drive), value, "-t floppy"));
			break;

		case DISK:
			runner.addArguments("-c", String.format("imgmount %s %s %s", this.getDriveLetter(drive), value, "-t hdd"));
			break;

		case CDROM:
			runner.addArguments("-c", String.format("imgmount %s %s %s", this.getDriveLetter(drive), value, "-t iso"));
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
	

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Utilities
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private String getDriveLetter(Drive drive) {
		char result = '\0';

		switch (drive.getType()) {
		case FLOPPY:
			result = letters[Integer.parseInt(drive.getUnit())];
			break;

		case CDROM:
		case DISK:
			result = letter++; // TODO: check ide, scsi...
			break;

		default:
			LOG.severe("Device type '" + drive.getType() + "' not supported yet.");
		}

		return Character.toString(result);
	}

	@Override
	protected boolean addNic(Nic nic) {
		LOG.warning("Network connection is currently not implemented.");
		return false;
	}

}
