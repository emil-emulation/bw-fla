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

package de.bwl.bwfla.common.utils.config;

import java.io.File;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public abstract class Configuration implements Serializable
{
	private static final long	serialVersionUID	= -6117911538818586872L;
	public static final String	BWFLA_CONF_DIR		= System.getProperty("user.home") + File.separator + ".bwFLA";
	
	public String getConfFilePath()
	{
		return BWFLA_CONF_DIR + File.separator + this.getClass().getSimpleName() + ".xml"; 
	}
}