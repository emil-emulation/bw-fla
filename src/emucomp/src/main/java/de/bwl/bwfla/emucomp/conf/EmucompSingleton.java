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

package de.bwl.bwfla.emucomp.conf;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import de.bwl.bwfla.common.utils.config.ConfigurationManager;
import de.bwl.bwfla.emucomp.components.AbstractEaasComponent;


@ManagedBean
@ApplicationScoped
public class EmucompSingleton
{	
	protected static final Logger							LOG				= Logger.getLogger(EmucompSingleton.class.getName());
	public static final ExecutorService						executor		= Executors.newCachedThreadPool();
	private static final Map<String, AbstractEaasComponent>	clusterSessions	= Collections.synchronizedMap(new HashMap<String, AbstractEaasComponent>());
	
	// TODO: read fields from a property file 
	private static final int					SESSID_MIN		= 0x1111;
	private static AtomicInteger				SESS_ID			= new AtomicInteger(SESSID_MIN);

	public static volatile boolean 				confValid 		= false;
	public static volatile EmucompConf 			CONF;
	
	static
	{
		loadConf();
	}

	public static boolean validate(EmucompConf conf)
	{
	    if (conf.controlUrlAddressHttp == null ||
	            conf.controlUrlAddressHttp.isEmpty())
	        return false;

		// TODO: here perform validation (if any)
		return true;
	}

	synchronized public static void loadConf()
	{ 
		CONF = ConfigurationManager.load(EmucompConf.class);
		
		if(CONF != null)
			LOG.info(CONF.toString());
		
		confValid = validate(CONF); 
	}
	
	static public AbstractEaasComponent getEmulatorBean(String sessionId)
	{
		return EmucompSingleton.getComponent(sessionId, AbstractEaasComponent.class);
	}
	
	static public String registerComponent(AbstractEaasComponent emul)
	{
		String res = String.valueOf(SESS_ID.getAndIncrement());
		emul.setSessionId(res);
		clusterSessions.put(res, emul);
		LOG.info("Session " + res + " registered. " + clusterSessions.size() + " session(s) currently active.");
		return res;
	}

	static public boolean unregisterComponent(String sessionId)
	{
		final boolean success = clusterSessions.remove(sessionId) != null;
		final int sessionCount = clusterSessions.size();
		if (success)
			LOG.info("Session " + sessionId + " unregistered. " + sessionCount + " session(s) remaining.");
		else LOG.warning("Unregistering session " + sessionId + " failed! " + sessionCount + " session(s) remaining.");
		
		return success;
	}

	static public <T> T getComponent(String sessId, Class<T> klass) throws IllegalArgumentException
	{
		AbstractEaasComponent entry = EmucompSingleton.clusterSessions.get(sessId);
		if (entry == null)
			throw new IllegalArgumentException("the passed session id does not correspond to any component session: " + sessId);

		return klass.cast(entry);
	}
}