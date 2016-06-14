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

package de.bwl.bwfla.emucomp.components.emulators;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.services.guacplay.net.GuacTunnel;
import de.bwl.bwfla.common.utils.NetworkUtils;
import de.bwl.bwfla.emucomp.conf.EmucompSingleton;



public class HtmlConnector implements IConnector
{
	private final GuacTunnel tunnel;
	private final String     cookie;
	private final boolean    https;
	private final boolean    pointerLock;
	
	public HtmlConnector(String cookie, GuacTunnel tunnel, boolean pointerLock, boolean https)
	{
		this.tunnel = tunnel;
		this.cookie = cookie;
		this.pointerLock = pointerLock;
		this.https  = https;
	}
	
	public boolean isPointerLock()
    {
        return pointerLock;
    }
	
	public String getCookie()
	{
		return cookie;
	}

	public GuacTunnel getTunnel()
	{
		return tunnel;
	}
	
	@Override
	public String toString()
	{
	    String prefix = "http://" + EmucompSingleton.CONF.controlUrlAddressHttp;
	    if (this.https) {
	        prefix = "https://" + EmucompSingleton.CONF.controlUrlAddressHttps;
	    }
        return prefix + "/emucomp/faces/pages/client-iframe.xhtml?cookie=" + this.cookie;
	}
}