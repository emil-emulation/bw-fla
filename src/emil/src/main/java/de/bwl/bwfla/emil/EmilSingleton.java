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

package de.bwl.bwfla.emil;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import javax.ejb.Singleton;
import javax.ejb.Startup;

import de.bwl.bwfla.api.eaas.EaasWS;
import de.bwl.bwfla.common.utils.EmulatorUtils;
import de.bwl.bwfla.common.utils.ObjectArchiveHelper;
import de.bwl.bwfla.common.utils.SoftwareArchiveHelper;
import de.bwl.bwfla.common.utils.SystemEnvironmentHelper;
import de.bwl.bwfla.common.utils.config.ConfigurationManager;


@Singleton
@Startup
public class EmilSingleton
{
    protected static final Logger           LOG = Logger.getLogger(EmilSingleton.class.getName());
    public static volatile boolean          confValid = false;
    public static volatile EmilConf         CONF;
    
    public static EaasWS                    eaas;
    public static SystemEnvironmentHelper   envHelper;
    public static ObjectArchiveHelper       objHelper;
    public static SoftwareArchiveHelper     swHelper;
    
    static
    {
        loadConf();
    }

    public static boolean validate(EmilConf conf)
    {
        if(conf == null)
            return false;
        
        Field[] declaredFields  = EmilConf.class.getDeclaredFields();
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
        CONF = ConfigurationManager.load(EmilConf.class);
        confValid = validate(CONF);     
        
        if(!confValid)
        {
            LOG.severe("invalid workflow configuration");
            return;
        }
        
        envHelper = new SystemEnvironmentHelper(CONF.imageArchive);
        objHelper = new ObjectArchiveHelper(CONF.objectArchive);
        swHelper  = new SoftwareArchiveHelper(CONF.softwareArchive);
    }
    
    synchronized public static void saveConf(EmilConf conf)
    {
        CONF = conf;
        confValid = validate(CONF);     
        ConfigurationManager.save(conf);
        
        envHelper = confValid ? new SystemEnvironmentHelper(CONF.imageArchive) : null;
        objHelper = confValid ? new ObjectArchiveHelper(CONF.objectArchive) : null;
        swHelper  = confValid ? new SoftwareArchiveHelper(CONF.softwareArchive) : null;
    }
    
    public static EaasWS getEaasWS()
    {
    	if (eaas == null) {
    		try {
    			eaas = EmulatorUtils.getEaas(new URL(CONF.eaasGw));
    		}
            catch (MalformedURLException exception) {
            	LOG.severe("Connecting to EaasWS failed!");
    			exception.printStackTrace();
    		}
    	}
    	
    	return eaas;
    }
}