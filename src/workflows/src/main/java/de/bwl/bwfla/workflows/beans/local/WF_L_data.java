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

package de.bwl.bwfla.workflows.beans.local;

import java.io.Serializable;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.WindowScoped;
import de.bwl.bwfla.workflows.beans.common.RemoteEmulatorHelper;
import de.bwl.bwfla.workflows.catalogdata.SystemEnvironmentDescription;

@Named
@WindowScoped
public class WF_L_data implements Serializable
{
	private static final long serialVersionUID = -4375128332504825355L;

	private final FacesContext fcontext = FacesContext.getCurrentInstance();
	private RemoteEmulatorHelper emuhelper = null;
	private SystemEnvironmentDescription description = null;

	public void reset()
	{
		emuhelper = null;
		setSystemEnvironmentDescription(null);
	}

	public FacesContext getFacesContext() 
	{
		return fcontext;
	}
	
	public RemoteEmulatorHelper getRemoteEmulatorHelper()
	{
		return emuhelper;
	}
	
	public void setRemoteEmulatorHelper(RemoteEmulatorHelper helper)
	{
		this.emuhelper = helper;
	}

	public SystemEnvironmentDescription getSystemEnvironmentDescription()
	{
		return description;
	}

	public void setSystemEnvironmentDescription(SystemEnvironmentDescription description)
	{
		this.description = description;
	}
}
