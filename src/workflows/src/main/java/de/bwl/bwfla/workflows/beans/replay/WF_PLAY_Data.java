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
import javax.inject.Named;
import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.WindowScoped;
import de.bwl.bwfla.common.jaxwsstubs.imagearchive.IwdMetaData;
import de.bwl.bwfla.workflows.beans.common.RemoteSessionPlayer;
import de.bwl.bwfla.workflows.beans.record.WorkflowData;


/* package-private class */

@Named
@WindowScoped
class WF_PLAY_Data extends WorkflowData implements Serializable
{
	private static final long serialVersionUID = -5643559161043665781L;
	
	// Member fields
	private RemoteSessionPlayer player;
	private IwdMetaData trace;
	

	/** Constructor */
	public WF_PLAY_Data()
	{
		super();
	}
	
	public void setRemoteSessionPlayer(RemoteSessionPlayer player)
	{
		this.player = player;
	}
	
	
	public RemoteSessionPlayer getRemoteSessionPlayer()
	{
		return player;
	}
	
	public static String getPageUrl(int page, boolean redirect)
	{
		StringBuilder builder = new StringBuilder(128);
		builder.append("/pages/workflow-replay/WF_PLAY_");
		builder.append(page);
		builder.append(".xhtml");
		if (redirect)
			builder.append("?faces-redirect=true");
		
		return builder.toString();
	}

	public IwdMetaData getTrace() {
		return trace;
	}

	public void setTrace(IwdMetaData selectedTrace) {
		this.trace = selectedTrace;
	}
}
