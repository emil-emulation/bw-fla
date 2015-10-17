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

//package de.bwl.bwfla.workflows.shibboleth.util;
//
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Set;
//
//public class AccessNode {
//
//	private String path;
//	
//	private AccessNode parent;
//	
//	private Map<String, AccessNode> children;
//	
//	private Set<String> allowRoles;
//	private Set<String> denyRoles;
//	
//	public AccessNode() {
//		this(null, "", false);
//	}
//	
//	public AccessNode(AccessNode parent, String path, Boolean inherit) {
//		this.parent = parent;
//		this.path = path;
//		children = new HashMap<String, AccessNode>();
//		allowRoles = new HashSet<String>();
//		denyRoles = new HashSet<String>();
//		
//		if (inherit) {
//			allowRoles.addAll(parent.getAllowRoles());
//			denyRoles.addAll(parent.getDenyRoles());
//		}
//		
//		if (parent != null)
//			parent.addChild(this);
//	}
//
//	public AccessNode getChild(String path) {
//		return children.get(path);
//	}
//	
//	public void addAllowRole(String role) {
//		allowRoles.add(role);
//	}
//
//	public void addDenyRole(String role) {
//		denyRoles.add(role);
//	}
//	
//	public void addChild(AccessNode an) {
//		if (an.getParent() != this)
//			throw new IllegalArgumentException("Cannot add AccessNode Child. Wrong parent.");
//		children.put(an.getPath(), an);
//	}
//	
//	public Set<String> getAllowRoles() {
//		return allowRoles;
//	}
//
//	public Set<String> getDenyRoles() {
//		return denyRoles;
//	}
//	
//	public AccessNode getParent() {
//		return parent;
//	}	
//
//	public String getPath() {
//		return path;
//	}
//}
