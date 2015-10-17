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

package de.bwl.bwfla.emucomp.api;

import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.xml.ws.soap.MTOM;
import de.bwl.bwfla.common.interfaces.ClusterComponent;
import de.bwl.bwfla.common.interfaces.ClusterComponentWS;
import de.bwl.bwfla.emucomp.components.AbstractEaasComponent;
import de.bwl.bwfla.emucomp.components.emulators.EmulatorBean;
import de.bwl.bwfla.emucomp.components.emulators.EmulatorBeanWrapper;
import de.bwl.bwfla.emucomp.components.network.NetworkSwitchBean;
import de.bwl.bwfla.emucomp.conf.EmucompSingleton;



@MTOM
@Stateless
@WebService(targetNamespace = "http://bwfla.bwl.de/api/emucomp")
public class EaasComponentWS implements ClusterComponentWS
{	
	@Override
	public String initialize(String beanType, final String config)
	{
		final AbstractEaasComponent comp = beanType.equalsIgnoreCase("emulator") ?
				new EmulatorBeanWrapper(EmulatorBean.createEmulatorBean(config))
				: NetworkSwitchBean.createNetworkSwitch(config);

		final String sessionId = EmucompSingleton.registerComponent(comp);
					
		Runnable task = new Runnable()
		{
			@Override
			public void run()
			{				
				try
				{
					comp.initialize(config);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		};
		
		EmucompSingleton.executor.submit(task);
		
		return sessionId;
	}

	@Override
	public void destroy(String sessionId)
	{
		final ClusterComponent comp = EmucompSingleton.getComponent(sessionId, ClusterComponent.class);
				
		Runnable task = new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					comp.destroy();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		};
		
		EmucompSingleton.unregisterComponent(sessionId);
		EmucompSingleton.executor.submit(task);
	}
}