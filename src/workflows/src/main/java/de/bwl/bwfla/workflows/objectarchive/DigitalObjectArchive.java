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

package de.bwl.bwfla.workflows.objectarchive;

import java.io.File;
import java.util.List;

public interface DigitalObjectArchive 
{	
	/**
	 * 
	 * @return an archive's name presented to the user to choose from
	 */
	public String getName();
	
	/**
	 * 
	 * @return list of object IDs
	 */
	public List<String> getObjectList();
	
	/**
	 * 
	 * @param id object-id
	 * @return object reference as PID / PURL
	 */
	public String getObjectReference(String id);
	
	public File getFileReference(String id);
	
}
