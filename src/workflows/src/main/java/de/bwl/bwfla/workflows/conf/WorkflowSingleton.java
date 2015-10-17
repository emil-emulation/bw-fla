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

package de.bwl.bwfla.workflows.conf;

import java.lang.reflect.Field;
import java.util.logging.Logger;
import de.bwl.bwfla.common.utils.SystemEnvironmentHelper;
import de.bwl.bwfla.common.utils.config.ConfigurationManager;



public class WorkflowSingleton
{
	protected static final Logger			LOG	= Logger.getLogger(WorkflowSingleton.class.getName());
	public static volatile boolean 			confValid = false;
	public static volatile WorkflowsConf	CONF;
	
	public static SystemEnvironmentHelper 	envHelper;
	
	static
	{
		loadConf();
	}

	public static boolean validate(WorkflowsConf conf)
	{
		if(conf == null)
			return false;
		
		Field[]	declaredFields	= WorkflowsConf.class.getDeclaredFields();
		if(declaredFields != null)
		{
			for (Field field : declaredFields) 
				if(java.lang.reflect.Modifier.isPublic(field.getModifiers()))
					try
					{
						Object property = field.get(conf);
						
						if(property instanceof String)
						{
							String value = (String) field.get(conf);
							if(value == null || value.isEmpty())
							{
								LOG.severe("configuration validation failed: field " + field.getName());
								return false;
							}
						}
					}
					catch(Exception e)
					{
						e.printStackTrace();
						return false;
					}			
		}
		
		return true;
	}

	synchronized public static void loadConf()
	{ 
		CONF = ConfigurationManager.load(WorkflowsConf.class);
		confValid = validate(CONF);		
		
		if(!confValid)
		{
			LOG.severe("invalid workflow configuration");
			return;
		}
		envHelper = new SystemEnvironmentHelper(CONF.archiveGw);
	}
	
	synchronized public static void saveConf(WorkflowsConf conf)
	{
		CONF = conf;
		confValid = validate(CONF);		
		ConfigurationManager.save(conf);
		
		envHelper = confValid ? new SystemEnvironmentHelper(CONF.archiveGw) : null;
	}
}