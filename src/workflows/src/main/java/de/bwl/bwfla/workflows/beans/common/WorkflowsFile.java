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

package de.bwl.bwfla.workflows.beans.common;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.apache.commons.io.FileUtils;
import de.bwl.bwfla.workflows.api.WorkflowResource;

public class WorkflowsFile extends File implements WorkflowResource
{
	private static final long	serialVersionUID	= 4069873725317227379L;

	public WorkflowsFile(File parent, String child)
	{
		super(parent, child);
	}

	public WorkflowsFile(String pathname)
	{
		super(pathname);
	}
	
	@Override
	public void cleanup()
	{
		try
		{
			if(this.isFile())
				Files.deleteIfExists(this.toPath());
			else if (this.isDirectory())
			{
				FileUtils.cleanDirectory(this);
				FileUtils.deleteDirectory(this);
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}
