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

package de.bwl.bwfla.objectarchive;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebService;



@WebService
@Stateless
@LocalBean
public class DigitalObjectArchive
{
	protected static final Logger LOG = Logger.getLogger(DigitalObjectArchive.class.getName());
	
	/**
	 * @return an archive's name presented to the user
	 */
	@WebMethod
	public String getName()
	{
		LOG.info("'String getName()' method called");
		return new String("test result");
	}

	/**
	 * @return list of object IDs
	 */
	@WebMethod
	public List<String> getObjectList()
	{
		LOG.info("'List<String> getObjectList()' method called");
		return new ArrayList<>();
	}

	/**
	 * @param id object-id
	 * @return object reference as PID / PURL
	 */
	@WebMethod
	public String getObjectReference(String id)
	{
		LOG.info("'String getObjectReference(String id)' method called");
		return new String();
	}
}