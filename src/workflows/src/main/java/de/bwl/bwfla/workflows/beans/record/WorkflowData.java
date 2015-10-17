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

package de.bwl.bwfla.workflows.beans.record;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import de.bwl.bwfla.common.services.guacplay.GuacDefs;
import de.bwl.bwfla.common.utils.SystemEnvironmentHelper;
import de.bwl.bwfla.workflows.beans.common.RemoteEmulatorHelper;


public class WorkflowData
{
	/** Extension for temporary trace-files. */
	public static final String TEMPFILE_EXT = GuacDefs.TRACE_FILE_EXT + ".temp";
	
	/** Logger instance */
	protected final Logger log = Logger.getLogger(this.getClass().getName());

	// Member fields
	private final FacesContext facesContext;
	
	private RemoteEmulatorHelper emuHelper;
	private SystemEnvironmentHelper envHelper;
	private String emulatorEnvId;
	private File tempDir = null; 
	
	/** Constructor */
	protected WorkflowData()
	{
		this.facesContext = FacesContext.getCurrentInstance();
		this.emuHelper = null;
		
	}

	public void setRemoteEmulatorHelper(RemoteEmulatorHelper helper)
	{
		emuHelper = helper;
	}
	
	public RemoteEmulatorHelper getRemoteEmulatorHelper()
	{
		return emuHelper;
	}
	
	public FacesContext getFacesContext()
	{
		return facesContext;
	}
	
	public static String getStartPageUrl()
	{
		return "/pages/start.xhtml";
	}
	
	public Path getTracePath(String emuenvid)
	{
		if(tempDir == null)
		{
			try {
				tempDir = Files.createTempDirectory(emuenvid).toFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
			log.info("Temporary directory created: "
					+ tempDir.getAbsolutePath());
		}
		return tempDir.toPath();
	}

	public SystemEnvironmentHelper getSystemEnvironmentHelper() {
		return envHelper;
	}

	public void setSystemEnvironmentHelper(SystemEnvironmentHelper envHelper) {
		this.envHelper = envHelper;
	}

	public String getEmulatorEnvId() {
		return emulatorEnvId;
	}

	public void setEmulatorEnvId(String emulatorEnvId) {
		this.emulatorEnvId = emulatorEnvId;
	}
}
