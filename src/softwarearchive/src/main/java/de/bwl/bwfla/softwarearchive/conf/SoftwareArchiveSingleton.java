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

package de.bwl.bwfla.softwarearchive.conf;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import de.bwl.bwfla.common.utils.config.ConfigurationManager;
import de.bwl.bwfla.softwarearchive.ISoftwareArchive;
import de.bwl.bwfla.softwarearchive.SoftwareArchiveFactory;


@Singleton
@Startup
public class SoftwareArchiveSingleton
{
	protected static final Logger LOG = Logger.getLogger(SoftwareArchiveSingleton.class.getName());
	
	public static volatile SoftwareArchiveConf CONF;
	public static volatile boolean confValid = false;
	
	private static ISoftwareArchive ARCHIVE;

	@PostConstruct
	public void init()
	{
		SoftwareArchiveSingleton.loadConf();
		
		if (!confValid) {
			LOG.warning("SoftwareArchive's configuration is invalid! Skipping initialization.");
			return;
		}
		
		List<ISoftwareArchive> archives = SoftwareArchiveFactory.createAllFromJson(CONF.basedir);
		if (!archives.isEmpty()) {
			// NOTE: currently only one software archive is supported!
			ARCHIVE = archives.get(0);
			LOG.info("Adding software archive: " + ARCHIVE.getName());
		}
		else LOG.warning("No software archives found at: " + CONF.basedir);
	}
	
	public static ISoftwareArchive getArchiveInstance()
	{
		return ARCHIVE;
	}

	private static boolean validate(SoftwareArchiveConf conf)
	{
		if (conf == null)
			return false;
		
		if (conf.basedir == null)
			return false;
		
		File dir = new File(conf.basedir);
		if (!dir.exists())
			return false;
		
		return true;
	}

	synchronized private static void loadConf()
	{ 
		CONF = ConfigurationManager.load(SoftwareArchiveConf.class);
		confValid = SoftwareArchiveSingleton.validate(CONF); 
	}
}
