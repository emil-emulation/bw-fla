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

package de.bwl.bwfla.common.services.container.helpers;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import com.google.common.io.Files;
import de.bwl.bwfla.common.services.container.types.Container;
import de.bwl.bwfla.common.services.container.types.HddHfsContainer;
import de.bwl.bwfla.common.utils.ProcessRunner;
import de.bwl.bwfla.conf.CommonSingleton;



/**
 * @author iv1004
 *
 */
public class HddHfsHelper extends ContainerHelper
{
	private static final Logger LOG = Logger.getLogger(HddHfsHelper.class.getSimpleName());
	private File createScript = new File(CommonSingleton.CONF.helpersConf.hddHfsCreate);
	private File ioScript = new File(CommonSingleton.CONF.helpersConf.hddHfsIo);
	
	public HddHfsHelper()
	{	
		if(!this.createScript.isFile() || !this.createScript.canExecute())
		{
			LOG.severe("exiting, make sure the block device creation script exists and is executable:" + createScript.getAbsolutePath());
			this.createScript = null;
		}
		
		if(!this.ioScript.isFile() || !this.ioScript.canExecute())
		{
			LOG.severe("exiting, make sure the block device creation script exists and is executable:" + ioScript.getAbsolutePath());
			this.ioScript = null;
		}
	}
	
	@Override
	public Container createEmptyContainer()
	{
		if(this.createScript == null || this.ioScript == null)
		{
			LOG.severe("an error occured on previous step, during object construction, this object will be unusable, returning 'null' value");
			return null;
		}
				
		final int DEFAULT_HDD_SIZE = 150;
		return this.createEmptyContainer(DEFAULT_HDD_SIZE);
	}

	@Override
	public Container createEmptyContainer(int size)
	{
		if(this.createScript == null || this.ioScript == null)
		{
			LOG.severe("an error occured on previous step, during object construction, this object will be unusable, returning 'null' value");
			return null;
		}
		
		File hddFile = null;
		boolean success = false;
		try
		{
			hddFile = File.createTempFile("/hdd_", ".img");
			hddFile.delete();
			ProcessRunner runner = new ProcessRunner(this.createScript.toString());
			runner.addArgument(String.valueOf(size));
			runner.addArgument(hddFile.toString());
			success = runner.execute(true, true);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(!success && hddFile != null && hddFile.isFile())
				hddFile.delete();
		}
		
		Container container = new HddHfsContainer();
		container.setFile(hddFile);
		return container;
	}

	@Override
	public boolean insertIntoContainer(Container container, List<File> files)
	{
		if(this.createScript == null || this.ioScript == null)
		{
			LOG.severe("an error occured on previous step, during object construction, this object will be unusable, returning 'false' value");
			return false;
		}
		
		for(File file: files)
		{
			ProcessRunner runner = new ProcessRunner(this.ioScript.toString());
			runner.addArgument("i");
			runner.addArgument(container.getFile().getAbsolutePath());
			runner.addArgument(file.getAbsolutePath());
			if(!runner.execute(true, true))
				return false;
		}
		
		return true;
	}

	@Override
	public File extractFromContainer(Container container)
	{
		if(this.createScript == null || this.ioScript == null)
		{
			LOG.severe("an error occured on previous step, during object construction, this object will be unusable, returning 'null' value");
			return null;
		}
		
		File result = null;
		boolean success = false;
		try
		{
			result = Files.createTempDir();
 
			ProcessRunner runner = new ProcessRunner(this.ioScript.toString());
			runner.addArgument("e");
			runner.addArgument(container.getFile().getAbsolutePath());
			runner.addArgument(result.getAbsolutePath());
			success = runner.execute(true, true);
		}
		finally
		{
			if(!success && result != null && result.isDirectory())
			{
				try
				{
					FileUtils.deleteDirectory(result);
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}

				return null;
			}
		}

		return result;		
	}
}