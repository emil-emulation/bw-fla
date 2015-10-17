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

package de.bwl.bwfla.workflows.beans.record;

import java.io.Serializable;
import javax.inject.Named;
import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.WindowScoped;
import de.bwl.bwfla.workflows.beans.common.RemoteSessionRecorder;


/* package-private class */

@Named
@WindowScoped
class WF_REC_Data extends WorkflowData implements Serializable
{
	private static final long serialVersionUID = 2353320752828826394L;
	
	/* Member fields */
	private RemoteSessionRecorder recorder;

	
	/** Constructor */
	public WF_REC_Data()
	{
		super();
	}
	
	public void setRemoteSessionRecorder(RemoteSessionRecorder recorder)
	{
		this.recorder = recorder;
	}

	public RemoteSessionRecorder getRemoteSessionRecorder()
	{
		return recorder;
	}
	
	public static String getPageUrl(int page, boolean redirect)
	{
		StringBuilder builder = new StringBuilder(128);
		builder.append("/pages/workflow-record/WF_REC_");
		builder.append(page);
		builder.append(".xhtml");
		if (redirect)
			builder.append("?faces-redirect=true");
		
		return builder.toString();
	}
}
