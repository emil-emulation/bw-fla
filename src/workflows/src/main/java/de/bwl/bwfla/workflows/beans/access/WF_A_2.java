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

package de.bwl.bwfla.workflows.beans.access;

import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import de.bwl.bwfla.workflows.beans.common.BwflaEmulatorViewBean;


@ManagedBean
@ViewScoped
public class WF_A_2 extends BwflaEmulatorViewBean implements Serializable
{
	private static final long				serialVersionUID	= -2323912800593361682L;
	
	@Inject private WF_A_data				wfData;

	public String forward()
	{
		return "/pages/start.xhtml?faces-redirect=true";
	}
	
	@Override
	public void initialize()
	{
		super.initialize();
		description = wfData.getStorage().chosenDescriptor;
		emuHelper = wfData.getStorage().emuHelper;
	}
	
	@Override
	public void cleanup()
	{
		super.cleanup();
	}
}
