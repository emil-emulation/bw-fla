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

package de.bwl.bwfla.conf;

import java.io.File;
import java.nio.file.Files;
import java.util.logging.Logger;

import de.bwl.bwfla.common.utils.config.ConfigurationManager;

public class CommonSingleton
{
	protected static final Logger		LOG	= Logger.getLogger(CommonSingleton.class.getName());
	public static volatile boolean 		confValid = false;
	public static volatile CommonConf	CONF;
	public static HelpersConf helpersConf;
	public static RunnerConf runnerConf;

	static
	{
		File tempBaseDir = new File(System.getProperty("java.io.tmpdir"));
		if(!tempBaseDir.exists())
		{
			if(!tempBaseDir.mkdirs())
				System.setProperty("java.io.tmpdir", "/tmp");
		}
		else if(tempBaseDir.canWrite())
		{
			System.setProperty("java.io.tmpdir", "/tmp");
		}
		loadConf();
	}

	public static boolean validate(CommonConf conf)
	{
		// TODO: implement validation
		return true;
	}

	synchronized public static void loadConf()
	{
		final ClassLoader loader = CommonSingleton.class.getClassLoader();
		
		CONF = ConfigurationManager.load(CommonConf.class);
		helpersConf = ConfigurationManager.load(HelpersConf.class, loader.getResourceAsStream("helpers.xml"));
		runnerConf = ConfigurationManager.load(RunnerConf.class, loader.getResourceAsStream("runners.xml"));
		confValid = validate(CONF); 
	}
}