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

import java.io.File;
import java.io.Serializable;
import javax.inject.Named;
import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.WindowScoped;
import de.bwl.bwfla.common.utils.SystemEnvironmentHelper;
import de.bwl.bwfla.workflows.beans.common.RemoteEmulatorHelper;
import de.bwl.bwfla.workflows.catalogdata.Description;
import de.bwl.bwfla.workflows.catalogdata.MetaDataFacade;


@Named
@WindowScoped
public class WF_A_data implements Serializable 
{
	private static final long serialVersionUID = -7270679547236491305L;
	
	private Storage storage = new Storage();
	public class Storage
	{	
		public File iso = null;
		
		public RemoteEmulatorHelper emuHelper = null;
		public SystemEnvironmentHelper envHelper = null;
		public Description chosenDescriptor = null;
		public MetaDataFacade mdArchive = null;
		public String accessSessId = null;
	}

	public Storage getStorage()
	{
		return storage;
	}
	
	public void resetStorage()
	{
		storage = new Storage();
	}
}
