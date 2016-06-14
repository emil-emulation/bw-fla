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

import javax.xml.bind.annotation.XmlRootElement;

import de.bwl.bwfla.common.utils.config.Configuration;


@XmlRootElement
public class EmucompConf extends Configuration
{	 
	private static final long	serialVersionUID	= -2357225601116924014L;

	public int       inactivityTimeout = 0;  // in seconds (0 == disabled)
	public boolean   localmode = false;
    public String    controlUrlAddressHttp;
    public String    controlUrlAddressHttps;
	public String	 qemuBean;
	public String 	 basiliskBean;
	public String	 sheepShaverBean;
	public String	 dosBoxBean;
	public String	 mameBean;
	public String	 messBean;
	public VboxExecs vboxBean;
	public ViceExecs viceBean;
	public PceExecs  pceBean;
	public String	 amigaBean;
	public String	 hatariBean;
	public String	 vdeSwitchBean;
	public String beebemBean;
	public String kegsBean;
}