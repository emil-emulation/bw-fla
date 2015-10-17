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

import java.util.Date;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import de.bwl.bwfla.api.cluster.ResourceState;



class NodeSession
{
    public final Date          startTime       = new Date();
    public final ReadWriteLock lock            = new ReentrantReadWriteLock(true);
    public ResourceState        state           = ResourceState.RESOURCE_ALLOCATING;
    public Future<?>           thread          = null;
    
    public IPlugin             plugin          = null;
    public ComputeInstance     instance        = null;
    public String              internSessionId = null;
    public Date                endTime         = null; 
}