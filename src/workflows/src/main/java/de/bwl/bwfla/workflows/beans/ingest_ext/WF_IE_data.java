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

package de.bwl.bwfla.workflows.beans.ingest_ext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.WindowScoped;

import de.bwl.bwfla.workflows.beans.common.RemoteEmulatorHelper;
import de.bwl.bwfla.workflows.catalogdata.Description;
import de.bwl.bwfla.workflows.catalogdata.MetaDataFacade;
import de.bwl.bwfla.workflows.catalogdata.ObjectEvaluationDescription;
import de.bwl.bwfla.workflows.objectarchive.DigitalObjectArchive;

@Named
@WindowScoped
public class WF_IE_data implements Serializable 
{
	private static final long serialVersionUID = -555179542826491305L;
	
	public class Storage 
	{
		public Description description = null;
		public RemoteEmulatorHelper emuHelper = null;
	}
	

	private Storage storage = new Storage();

	public Storage getStorage() 
	{
		return storage;
	}
	
	public void resetStorage()
	{
		storage = new Storage();
	}
}
