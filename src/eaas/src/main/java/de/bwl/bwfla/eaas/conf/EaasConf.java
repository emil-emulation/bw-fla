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

package de.bwl.bwfla.eaas.conf;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import de.bwl.bwfla.common.utils.config.Configuration;
import de.bwl.bwfla.eaas.allocation.IPlugin;
import de.bwl.bwfla.eaas.allocation.spi.plugins.bladecluster.BladeClusterPlugin;
import de.bwl.bwfla.eaas.allocation.spi.plugins.googlecloud.GoogleCloudPlugin;


@XmlRootElement
public class EaasConf extends Configuration
{
	private static final long	serialVersionUID	= -4176097416561837971L;
	
	/** Timeout for unused/expired sessions (in seconds) */
	public long sessionIdleTimeout = 5 * 60;
	
	public String softwareArchive;
	
	@XmlElements({@XmlElement(name="bladeClusterPlugin", type=BladeClusterPlugin.class), @XmlElement(name="googleCloudPlugin", type=GoogleCloudPlugin.class)})
	public List<IPlugin> plugins;
}