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

import java.nio.file.Path;
import javax.ejb.Stateful;
import de.bwl.bwfla.common.datatypes.Drive;
import de.bwl.bwfla.common.datatypes.Nic;
import de.bwl.bwfla.common.utils.EmulatorUtils;
import de.bwl.bwfla.emucomp.conf.EmucompSingleton;

/**
 * @author Johann Latocha <johann.latocha@rz.uni-freiburg.de>
 * 
 */
@Stateful
public class MameBean extends EmulatorBean {

	@Override
	public void prepareEmulatorRunner()
	{
		// Initialize the process-runner
		runner.setCommand(EmucompSingleton.CONF.mameBean);
		runner.addArguments("-video", "soft", "-window");
		try {
			// FIXME: read from metadata: String handle = properties.getProperty("rompath");
			String handle = null;
			Path rompath = EmulatorUtils.prepareSoftwareCollection(handle, this.tempDir.toPath());
			runner.addArgument("-rompath");
			runner.addArgument(rompath.toString());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		String config = this.getNativeConfig();
		if (config != null) {
			String[] tokens = config.trim().split("\\s+");
			for (String token : tokens)
				runner.addArgument(token.trim());
		}
		
		runner.addEnvVariable("SDLMAME_DESKTOPDIM", "800x600");
		
		if (this.isLocalModeEnabled())
			this.setupEmulatorForY11();
		else this.setupEmulatorForSDLONP();
	}

	@Override
	public boolean addDrive(Drive drive) {
		return true;
	}

	@Override
	public boolean connectDrive(Drive drive, boolean attach) {
		if (!runner.isProcessRunning()) {
			LOG.warning("Hotplug is unavailable because emulator is not running.");
			return false;
		}

		LOG.severe("Hotplug is not implemented yet.");
		return false;

	}

	@Override
	protected boolean addNic(Nic nic) {
		// TODO Auto-generated method stub
		return false;
	}
}
