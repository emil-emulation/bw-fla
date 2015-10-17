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

package de.bwl.bwfla.workflows.beans.replay;

import java.io.Serializable;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import de.bwl.bwfla.workflows.beans.common.BwflaFileAttachBean;
import de.bwl.bwfla.workflows.beans.common.RemoteEmulatorHelper;


@ManagedBean
@ViewScoped
public class WF_PLAY_1 extends BwflaFileAttachBean implements Serializable
{
	private static final long serialVersionUID = 1798241899209865115L;
	@Inject private WF_PLAY_Data wfdata;
	
	
	@Override
	public List<String> getDevices() 
	{
		RemoteEmulatorHelper emuHelper = wfdata.getRemoteEmulatorHelper();

		if(imageDevices == null)
			imageDevices = emuHelper.getImageDevices();

		if(helperDevices == null)
			helperDevices = emuHelper.getHelperDevices();

		return super.getDevices();
	}
	
	@Override
	public String forward()
	{
		RemoteEmulatorHelper emuhelper = wfdata.getRemoteEmulatorHelper();
		emuhelper.setFilesToInject(uploadedFiles);
		return WF_PLAY_Data.getPageUrl(2, true);
	}
	
	public String back()
	{
		return WF_PLAY_Data.getPageUrl(0, true);
	}
}
