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

package de.bwl.bwfla.emucomp.components.network;

import java.io.IOException;
import javax.annotation.PostConstruct;
import javax.ejb.Stateful;
import javax.xml.bind.JAXBException;
import org.apache.commons.io.FileUtils;
import de.bwl.bwfla.common.datatypes.VdeNetworkEndpoint;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.NetworkUtils;
import de.bwl.bwfla.common.utils.ProcessRunner;
import de.bwl.bwfla.emucomp.conf.EmucompSingleton;


// TODO: currently the default of 32 ports is used on the switch,
//       evaluate penalty of higher number and set to e.g. 1024 or use dynamic
//       port allocation
@Stateful
public class VdeSwitchBean extends NetworkSwitchBean {

	// vde_switch process maintenance members
	protected ProcessRunner runner = new ProcessRunner();

	public void initialize(String configXML) throws BWFLAException {		
		// create a new vde switch instance in tmpdir/sockets
		runner.setCommand(EmucompSingleton.CONF.vdeSwitchBean);
		runner.addArgument("-s");
		runner.addArgument(this.tempDir.toPath().resolve("sockets").toString());
		if (!runner.start())
			throw new BWFLAException("Could not create a vde-switch instance!");
	}
	
	@Override
	public String getNetworkEndpoint() {
		VdeNetworkEndpoint localEndpoint = new VdeNetworkEndpoint(
				NetworkUtils.getHostAddress().getHostAddress(), 
				this.tempDir.toPath().resolve("sockets").toString(),
				null);
		try {
			return localEndpoint.value();
		} catch (JAXBException e) {
			LOG.severe("Could not marshal network endpoint information.");
			e.printStackTrace();

			// TODO throw BWFLAException
			return null;
		}
	}

	@Override
	public void destroy() {

		runner.stop();
		runner.cleanup();

		// remove temporary dir
		if (tempDir != null && tempDir.exists()) {
			try {
				FileUtils.deleteDirectory(tempDir);
				LOG.info("Temporary directory removed: "
						+ tempDir.getAbsolutePath());
				tempDir = null;
			} catch (IOException e) {
				LOG.severe(e.getMessage());
			}
		}
	}
}
