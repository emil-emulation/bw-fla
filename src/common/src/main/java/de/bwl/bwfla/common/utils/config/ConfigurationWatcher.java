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

package de.bwl.bwfla.common.utils.config;

import java.util.logging.Logger;
import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileMonitor;
import de.bwl.bwfla.common.utils.ConfigChangeListener;



public class ConfigurationWatcher<C extends Configuration> implements FileListener
{
	private final Logger			LOG	= Logger.getLogger(this.getClass().getCanonicalName());
	private DefaultFileMonitor		fileMonitor;
	private ConfigChangeListener<C>	configListener;
	private final Class<C>			confClass;
	private final String			confFilePath;
	
	public ConfigurationWatcher(ConfigChangeListener<C> configListener, Class<C> confClass)
	{
		this.configListener = configListener; 
		this.confClass = confClass;
		this.confFilePath = ConfigurationManager.getConfFilePath(confClass);
		
		try
		{
			FileSystemManager fsManager = VFS.getManager();
			FileObject file = fsManager.resolveFile(this.confFilePath);
			fileMonitor = new DefaultFileMonitor(this);
			fileMonitor.setRecursive(false);
			fileMonitor.addFile(file);
			fileMonitor.start();
			
			this.notifyListener();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	synchronized private void notifyListener()
	{	
		LOG.warning("configuration file was changed (created/altered/deleted) under '" + this.confFilePath + "', passing the new configuration value to the config-file-listener: " + configListener.getClass().getCanonicalName());
		C conf = ConfigurationManager.load(confClass);
		configListener.handleConfigChange(conf);
	}
	
	@Override
	public void fileChanged(FileChangeEvent arg0) throws Exception
	{
		this.notifyListener();
	}

	@Override
	public void fileCreated(FileChangeEvent arg0) throws Exception
	{
		
		this.notifyListener();
	}

	@Override
	public void fileDeleted(FileChangeEvent arg0) throws Exception
	{
		this.notifyListener();
	}
}