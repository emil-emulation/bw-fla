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

public class Session 
{
	private final String sessionId;
	private final String sessionName;
	private final NetworkGroup components;
	private final long creationTime;
	private long updateTime;
	private String clientId;
	public String state;
	
	public Session(String sessionId, String sessionName, NetworkGroup components) {
		this.sessionId = sessionId;
		this.sessionName = sessionName;
		this.components = components;
		this.creationTime = System.currentTimeMillis();
		this.updateTime = creationTime;
	}
	
	public String getSessionId() {
		return sessionId;
	}
	
	public String getSessionName() {
		return sessionName;
	}
	
	public void addComponent(String id) {
		this.components.addComponent(id);
	}
	
	public int getNumComponents() {
		return this.components.size();
	}

	public String getClientId() {
		return clientId;
	}

	public void addClient(String clientId) {
		this.components.addClient(clientId);
		this.clientId = clientId;
	}
	
	public boolean removeClient() {
		return this.components.removeClient(this.clientId);
	}
	
	public Iterable<String> getComponents() {
		return components.getComponents();
	}
	
	public void removeComponents() {
		this.components.removeComponents();
	}
	
	public long getDurationInSeconds()
	{
		long time = System.currentTimeMillis() - creationTime;
		return (time / 1000);
	}

	public long getUpdateTimestamp()
	{
		return updateTime;
	}

	public void setUpdateTime()
	{
		this.updateTime = System.currentTimeMillis();
	}
}
