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

package de.bwl.bwfla.workflows.component;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.FacesComponent;
import javax.faces.component.UINamingContainer;
import javax.xml.bind.JAXBException;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.workflows.beans.common.WFPanicException;
import de.bwl.bwfla.workflows.conf.WorkflowSingleton;
import de.bwl.bwfla.workflows.softwarearchive.SoftwareArchive;
import de.bwl.bwfla.workflows.softwarearchive.SoftwareDescription;

@ResourceDependencies({
	@ResourceDependency(library = "primefaces", name = "jquery/jquery.js"),
	@ResourceDependency(library = "primefaces", name = "primefaces.js")
})
@FacesComponent(value = SwArchiveSelector.COMPONENT_TYPE)
public class SwArchiveSelector extends UINamingContainer {
	public static final String COMPONENT_TYPE = "de.bwl.bwfla.workflows.component.SwArchiveSelector";
	
	protected static final Logger log = Logger.getLogger(SwArchiveSelector.class.getName());
	
	public static List<SoftwareDescription> getSoftwareListStatic() throws BWFLAException {
		String swArchiveIndex = WorkflowSingleton.CONF.swArchive;
		
		try 
		{
			File swIndex = new File(swArchiveIndex);
			if(!swIndex.exists())
				return new ArrayList<SoftwareDescription>();
			
			SoftwareArchive softwareArchive = SoftwareArchive.fromFile(new File(swArchiveIndex));
			return softwareArchive.getSoftwareList();
		} 
		catch (FileNotFoundException | JAXBException e) 
		{
			// e.printStackTrace();
			log.severe("Software Archive configuration seems to be broken: " + e.getMessage());
			return new ArrayList<>();
		}
	}
	
	public List<SoftwareDescription> getSoftwareList() throws BWFLAException 
	{
		return SwArchiveSelector.getSoftwareListStatic();
	}
}