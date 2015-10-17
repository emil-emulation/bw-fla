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
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Set;
//import javax.annotation.PostConstruct;
//import javax.enterprise.context.ApplicationScoped;
//import javax.inject.Named;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//
//@Named("accessChecker")
//@ApplicationScoped
//public class AccessChecker {
//
//	private Logger logger = LoggerFactory.getLogger(AccessChecker.class);
//	private AccessNode root;
//	
//	@PostConstruct
//	public void init() {
//		logger.info("Initializing accessChecker");
//		root = new AccessNode();
//		root.addAllowRole("ROLE_User");
//
//		addAccessNode(root, "user", true);
//		addAccessNode(root, "service", true);
//		addAccessNode(root, "service-admin", true);
//		addAccessNode(root, "service-approver", true);
//
//		addDenyNode(root, "register", false, "ROLE_User");
//		
//		AccessNode adminNode = addAccessNode(root, "admin", false, "ROLE_MasterAdmin");
//		addAccessNode(adminNode, "role", true, "ROLE_RoleAdmin");
//		addAccessNode(adminNode, "user", true, "ROLE_UserAdmin");
//		addAccessNode(adminNode, "service", true, "ROLE_ServiceAdmin");
//		addAccessNode(adminNode, "saml", true, "ROLE_SamlAdmin");
//		addAccessNode(adminNode, "business-rule", true, "ROLE_BusinessRuleAdmin");
//		addAccessNode(adminNode, "bulk", true, "ROLE_BulkAdmin");
//
//		AccessNode restNode = addAccessNode(root, "rest", false, "ROLE_MasterAdmin", "ROLE_RestAdmin");
//
//		AccessNode droolsNode = addAccessNode(restNode, "drools", true);
//		addAccessNode(droolsNode, "test", true);
//
//		AccessNode attrqNode = addAccessNode(restNode, "attrq", true);
//		addAccessNode(attrqNode, "eppn", true);
//
//		AccessNode ecpNode = addAccessNode(restNode, "ecp", true);
//		addAccessNode(ecpNode, "eppn", true);
//
//		AccessNode imageNode = addAccessNode(restNode, "image", true);
//		addAccessNode(imageNode, "original", true, "ROLE_User");
//		addAccessNode(imageNode, "small", true, "ROLE_User");
//		addAccessNode(imageNode, "icon", true, "ROLE_User");
//	}
//	
//	public Boolean check(String path, Set<String> roles) {
//		if (path.startsWith("/"))
//			path = path.substring(1);
//		
//		String[] splitPath = path.split("/");
//		List<String> splitList = new ArrayList<String>(Arrays.asList(splitPath));
//		
//		if (! path.endsWith("/"))
//			splitList.remove(splitList.size() - 1);
//		
//		return evaluate(root, splitList, roles);
//	}
//	
//	private Boolean evaluate(AccessNode an, List<String> splitList, Set<String> roles) {
//		if (splitList.size() == 0) {
//			return evaluateNode(an, roles);
//		}
//		else {
//			String path = splitList.remove(0);
//			AccessNode subAn = an.getChild(path);
//			
//			if (subAn == null)
//				return evaluateNode(an, roles);
//			
//			for (String role : an.getDenyRoles()) {
//				if (roles.contains(role))
//					return false;
//			}
//
//			return evaluate(subAn, splitList, roles);
//		}
//	}
//	
//	private Boolean evaluateNode(AccessNode an, Set<String> roles) {
//		for (String role : an.getDenyRoles()) {
//			if (roles.contains(role))
//				return false;
//		}
//		
//		for (String role : an.getAllowRoles()) {
//			if (roles.contains(role))
//				return true;
//		}
//		
//		return false;		
//	}
//	
//	private AccessNode addAccessNode(AccessNode parent, String path, Boolean inherit, String... roles) {
//		AccessNode an = new AccessNode(parent, path, inherit);
//		for (String role : roles)
//			an.addAllowRole(role);
//		
//		return an;
//	}
//
//	private AccessNode addDenyNode(AccessNode parent, String path, Boolean inherit, String... roles) {
//		AccessNode an = new AccessNode(parent, path, inherit);
//		for (String role : roles)
//			an.addDenyRole(role);
//		
//		return an;
//	}
//}
