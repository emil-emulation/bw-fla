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

package de.bwl.bwfla.workflows.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import de.bwl.bwfla.common.datatypes.AccessSession;
import de.bwl.bwfla.common.datatypes.IngestSession;



@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class GlobalBean
{	
	private Map<String, IngestSession> ingestSessions = Collections.synchronizedMap(new HashMap<String, IngestSession>());
	private Map<String, AccessSession> accessSessions = Collections.synchronizedMap(new HashMap<String, AccessSession>());
	private AtomicLong INGEST_SESSION_ID = new AtomicLong(0);
	private AtomicLong ACCESS_SESSION_ID = new AtomicLong(0);
	
	
	public String registerIngest(IngestSession ingestSess)
	{	
		String sessId = (new Long(INGEST_SESSION_ID.getAndIncrement())).toString();
		return ingestSessions.put(sessId, ingestSess) == null ? sessId : null;
	}
	
	public String registerAccess(AccessSession accessSess)
	{
		String sessId = (new Long(ACCESS_SESSION_ID.getAndIncrement())).toString();
		return accessSessions.put(sessId, accessSess) == null ? sessId : null;
	}

	public IngestSession getIngestSession(String sessId)
	{
		return ingestSessions.get(sessId);
	}
	
	public AccessSession getAccessSession(String sessId)
	{
		return accessSessions.get(sessId);
	}
	
	public boolean unregisterIngest(String sessId)
	{			
		return ingestSessions.remove(sessId) != null;
	}
	
	public boolean unregisterAccess(String sessId)
	{
		return accessSessions.remove(sessId) != null;
	}
}
