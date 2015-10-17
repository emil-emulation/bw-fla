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

package de.bwl.bwfla.workflows.beans.common;

import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.inject.Inject;
import de.bwl.bwfla.common.services.guacplay.net.TunnelConfigRegistry;
import de.bwl.bwfla.common.services.guacplay.net.TunnelRegistry;


@ManagedBean
@SessionScoped
public class UserSession implements Serializable
{
	private static final long serialVersionUID = -6806026353515528833L;
	
	// Member fields
	private final TunnelConfigRegistry configs;
	private final TunnelRegistry tunnels;
	
	
	/** Constructor for Injection. */
	@Inject public UserSession()
	{
		this.configs = new TunnelConfigRegistry();
		this.tunnels = new TunnelRegistry();
	}
	
	/** Returns the {@link TunnelConfigRegistry} of this session. */
	public TunnelConfigRegistry getTunnelConfigRegistry()
	{
		return configs;
	}
	
	/** Returns the {@link TunnelRegistry} of this session. */
	public TunnelRegistry getTunnelRegistry()
	{
		return tunnels;
	}
}
