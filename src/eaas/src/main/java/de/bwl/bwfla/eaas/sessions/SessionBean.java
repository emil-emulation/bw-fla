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

package de.bwl.bwfla.eaas.sessions;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;

import de.bwl.bwfla.api.cluster.ResourceAllocatorWS;


@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class SessionBean
{	
	// session id -> session
	private Map<String, Session> sessions;
	
	// name -> group
	public Map<String, NetworkGroup> componentGroups;
	
	private static final int SESSID_MIN = 0x1111;
	private int sessId = SESSID_MIN;
	
	@PostConstruct
	private void initialize()
	{
		sessions = new HashMap<String, Session>();
		componentGroups = new HashMap<String, NetworkGroup>();
	}
	
	@Lock(LockType.WRITE)
	public Session allocateSession(String name) {
		NetworkGroup components = null;

		// lookup existing group
		if (name != null && !name.equals("")) {
			components = this.componentGroups.get(name);
		}

		// create new empty group if lookup failed or the name was null 
		if (components == null) {
			components = new NetworkGroup();
			if (name != null && !name.equals("")) {
				this.componentGroups.put(name, components);
			}
		}

		String sessionId = String.valueOf(sessId++);
		Session session = new Session(sessionId, name, components);
		this.sessions.put(sessionId, session);
		return session;
	}
	
	@Lock(LockType.WRITE)
	public Session getSession(String sessionId) {
		return this.sessions.get(sessionId);
	}
	
	@Lock(LockType.WRITE)
	public void releaseSession(Session session, ResourceAllocatorWS allocator) {
	    if (session == null)
	    	return;
	    
	    sessions.remove(session.getSessionId());
	    
	    // Deallocate all components
	    for (String clusterId : session.getComponents())
			allocator.release(clusterId);
	    
	    // Cleanup
	    session.removeClient();
	    session.removeComponents();
	    
	    String name = session.getSessionName();
	    if (name != null && !name.isEmpty())
	    	componentGroups.remove(name);
	}
	
//	@Lock(LockType.WRITE)
//	public String registerSession(String clusterId) {
//		String sessionId = String.valueOf(sessId++);
//		clientSessions.put(sessionId, clusterId);
//		
//		return sessionId;
//	}
//	
//	@Lock(LockType.WRITE)
//	public boolean unregisterComponent(String sessionId)
//	{	
//		return sessions.remove(sessionId) != null; 
//	}
}