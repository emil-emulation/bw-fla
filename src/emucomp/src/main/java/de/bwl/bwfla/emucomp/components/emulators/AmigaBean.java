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

import java.nio.file.Paths;
import javax.ejb.Stateful;
import de.bwl.bwfla.common.datatypes.Drive;
import de.bwl.bwfla.common.datatypes.Nic;
import de.bwl.bwfla.emucomp.conf.EmucompSingleton;

@Stateful
public class AmigaBean extends EmulatorBean {

	String configdir;
	
	@Override
	public void prepareEmulatorRunner()
	{
		String configdir = this.getNativeConfig();
		
		// Initialize the process-runner
		runner.setCommand(EmucompSingleton.CONF.amigaBean);
		runner.addArgument("-f");
		runner.addArgument(configdir + "config");
		runner.setWorkingDirectory(Paths.get(configdir));
		
		if (this.isLocalModeEnabled())
			this.setupEmulatorForY11();
		else this.setupEmulatorForSDLONP();
	}

	@Override
	public boolean addDrive(Drive drive) {
		return false;
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
		LOG.warning("Network connection is currently not implemented.");
		return false;
	}
}
