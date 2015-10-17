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

package de.bwl.bwfla.eaas.allocation;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.jws.WebMethod;
import javax.jws.WebService;
import de.bwl.bwfla.common.exceptions.OutOfResourcesException;
import de.bwl.bwfla.eaas.conf.EaasConf;
import de.bwl.bwfla.eaas.conf.EaasSingleton;



@Singleton
@Local(IAllocator.class)
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@WebService(targetNamespace = "http://bwfla.bwl.de/api/cluster/")
public class ResourceAllocatorWS implements IAllocator
{
	private static ResourceAllocator	allocator;

	static
	{  
		EaasConf config = EaasSingleton.CONF;
		
		if(config != null && config.plugins != null)
			allocator = new ResourceAllocator(config.plugins);
	}
	
    @Override
    @WebMethod
    public String allocate(String resourceType, String configXml) throws OutOfResourcesException
    {	
		return allocator.allocate(resourceType, configXml);
    }

    @Override
    @WebMethod
    public EmuSession getInternalSession(String clientSessionId)
    {
        return allocator.getInternalSession(clientSessionId);
    }

    @Override
    @WebMethod
    public void release(String clientSessionId)
    {
    	allocator.release(clientSessionId);
    }
}