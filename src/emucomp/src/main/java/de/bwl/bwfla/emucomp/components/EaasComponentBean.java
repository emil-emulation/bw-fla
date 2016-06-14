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

package de.bwl.bwfla.emucomp.components;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;


public abstract class EaasComponentBean extends AbstractEaasComponent 
{
	protected final Logger LOG;
	protected File tempDir;
	
	protected EaasComponentBean()
	{
		LOG = Logger.getLogger(this.getClass().getName());
		
		try 
		{
			Set<PosixFilePermission> permissions = new HashSet<>();
			permissions.add(PosixFilePermission.OWNER_READ);
			permissions.add(PosixFilePermission.OWNER_WRITE);
			permissions.add(PosixFilePermission.OWNER_EXECUTE);
			permissions.add(PosixFilePermission.GROUP_READ);
			permissions.add(PosixFilePermission.GROUP_WRITE);
			permissions.add(PosixFilePermission.GROUP_EXECUTE);

			tempDir = Files.createTempDirectory("", PosixFilePermissions.asFileAttribute(permissions)).toFile();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		BwflaShutdown shutdownHook = new BwflaShutdown(this);
		Runtime.getRuntime().addShutdownHook(shutdownHook);
	}

	public void destroy()
	{
		// XXX: no implementation
	}
}