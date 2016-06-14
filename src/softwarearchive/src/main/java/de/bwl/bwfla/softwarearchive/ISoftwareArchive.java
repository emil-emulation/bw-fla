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

package de.bwl.bwfla.softwarearchive;

import java.util.List;

import de.bwl.bwfla.common.datatypes.SoftwarePackage;
import de.bwl.bwfla.common.datatypes.SoftwareDescription;


/** The internal view on software archive implementation. */
public interface ISoftwareArchive
{
	public int getNumSoftwareSeatsById(String id);
	public SoftwarePackage getSoftwarePackageById(String id);
	public List<String> getSoftwarePackages();
	public SoftwareDescription getSoftwareDescriptionById(String id);
	public List<SoftwareDescription> getSoftwareDescriptions();
	public String getName();
}
