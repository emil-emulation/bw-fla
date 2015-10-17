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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class NetworkGroup {
	private final Set<String> components = new HashSet<>();
	private int clientCount = 0;

	synchronized public void addComponent(String clientId) {
		this.components.add(clientId);
	}

	synchronized public void addClient(String clientId) {
		this.components.add(clientId);
		++clientCount;
	}

	synchronized public boolean removeClient(String sessId) {
		components.remove(sessId);
		--clientCount;
		
		return (clientCount == 0);
	}
	
	synchronized public void removeComponents() {
		components.clear();
	}
	
	synchronized public Iterable<String> getComponents() {
		return new Iterable<String>() {
			public Iterator<String> iterator() {
				return components.iterator();
			}
		};
	}
	
	public int size() {
		return components.size();
	}
}