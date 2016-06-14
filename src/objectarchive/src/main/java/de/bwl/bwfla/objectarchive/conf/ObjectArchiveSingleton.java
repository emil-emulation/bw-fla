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

package de.bwl.bwfla.objectarchive.conf;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import de.bwl.bwfla.objectarchive.*;
import de.bwl.bwfla.common.datatypes.FileCollection;
import de.bwl.bwfla.common.utils.config.ConfigurationManager;


@Singleton
@Startup
public class ObjectArchiveSingleton
{
	protected static final Logger				LOG	= Logger.getLogger(ObjectArchiveSingleton.class.getName());
	public static volatile boolean 				confValid = false;
	public static volatile ObjectArchiveConf	CONF;
	public static ConcurrentHashMap<String, DigitalObjectArchive> archiveMap = null;
	// public static ConcurrentHashMap<String, List<FileCollection>> archiveContent = null;

	@PostConstruct
	public void init()
	{
		ObjectArchiveSingleton.loadConf();
	}

	public static boolean validate(ObjectArchiveConf conf)
	{
		if(conf == null)
			return false;
		
		if(conf.objDir == null || conf.httpExport == null)
			return false;
		
		File test = new File(conf.objDir);
		if(!test.exists())
			return false;
		
		return true;
	}

	synchronized public static void loadConf()
	{ 
		CONF = ConfigurationManager.load(ObjectArchiveConf.class);
		confValid = ObjectArchiveSingleton.validate(CONF); 
		
		if(!confValid)
			return;
		
		List<DigitalObjectArchive> archives = DigitalObjectArchiveFactory.createFromJson(new File(CONF.objDir));
		
		// archiveContent = new ConcurrentHashMap<>();
		ObjectArchiveSingleton.archiveMap = new ConcurrentHashMap<>();
		for(DigitalObjectArchive a : archives)
		{
			ObjectArchiveSingleton.archiveMap.put(a.getName(), a);
			LOG.info("adding archive: " + a.getName());
			// archiveContent.put(a.getName(), a.getObjectList());
		}
	}
}