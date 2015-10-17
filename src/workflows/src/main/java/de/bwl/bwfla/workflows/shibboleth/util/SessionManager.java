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
//import java.io.Serializable;
//import java.util.Map;
//import java.util.Set;
//import javax.enterprise.context.SessionScoped;
//import javax.inject.Named;
//
//@Named("sessionManager")
//@SessionScoped
//public class SessionManager implements Serializable {
//
//	private static final long serialVersionUID = 1L;
//
//	private Long userId;
//	
//	private Long idpId;
//	
//	private Long spId;
//
//	private Map<String, String> attributeMap;
//	
//	private String persistentId;
//	
//	private String orinignalRequestPath;
//	
//	private String assertion;
//	
//	private Set<String> roles;
//	
//	private String theme;
//	
//	public String getAssertion() {
//		return assertion;
//	}
//
//	public void setAssertion(String assertion) {
//		this.assertion = assertion;
//	}
//
//	public String getOrinignalRequestPath() {
//		return orinignalRequestPath;
//	}
//
//	public void setOrinignalRequestPath(String orinignalRequestPath) {
//		this.orinignalRequestPath = orinignalRequestPath;
//	}
//
//	public Long getUserId() {
//		return userId;
//	}
//	
//	public String getUserIdString() {
//		return "You are logged in as user " + userId.toString();
//	}
//
//	public boolean isLoggedIn() {
//		return (userId != null ? true : false);		
//	}
//
//	public void logout() {
//		
//	}
//
//	public Long getIdpId() {
//		return idpId;
//	}
//
//	public void setIdpId(Long idpId) {
//		this.idpId = idpId;
//	}
//
//	public Long getSpId() {
//		return spId;
//	}
//
//	public void setSpId(Long spId) {
//		this.spId = spId;
//	}
//
//	public void setUserId(Long userId) {
//		this.userId = userId;
//	}
//
//	public Map<String, String> getAttributeMap() {
//		return attributeMap;
//	}
//
//	public void setAttributeMap(Map<String, String> attributeMap) {
//		this.attributeMap = attributeMap;
//	}
//
//	public String getPersistentId() {
//		return persistentId;
//	}
//
//	public void setPersistentId(String persistentId) {
//		this.persistentId = persistentId;
//	}
//	
//    public void setRoles(Set<String> roles) {
//		this.roles = roles;
//	}
//
//	public boolean isUserInRole(String role) {
//        return roles.contains(role);
//    }
//
//	public void setTheme(String theme) {
//		this.theme = theme;
//	}
//	
//	public String getTheme() {
//		if (theme == null)
//			theme = "aristo";
//		return theme;
//	}	
//}
