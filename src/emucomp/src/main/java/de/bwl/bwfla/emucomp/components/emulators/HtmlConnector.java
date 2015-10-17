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

import de.bwl.bwfla.common.services.guacplay.net.GuacTunnel;



public class HtmlConnector implements IConnector
{
	private final GuacTunnel tunnel;
	private final String     cookie;
	private final boolean    pointerLock;
	
	public HtmlConnector(String cookie, GuacTunnel tunnel, boolean pointerLock)
	{
		this.tunnel = tunnel;
		this.cookie = cookie;
		this.pointerLock = pointerLock;
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
		return "/faces/pages/client-iframe.xhtml?cookie=" + this.cookie;
	}
}