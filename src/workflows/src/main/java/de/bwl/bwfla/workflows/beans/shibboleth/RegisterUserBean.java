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

//package de.bwl.bwfla.workflows.beans.shibboleth;
//
//import java.io.Serializable;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Map.Entry;
//import javax.annotation.PostConstruct;
//import javax.faces.application.FacesMessage;
//import javax.faces.bean.ManagedBean;
//import javax.faces.bean.ViewScoped;
//import javax.faces.context.FacesContext;
//import javax.inject.Inject;
//import de.bwl.bwfla.workflows.shibboleth.util.SessionManager;
//import edu.kit.scc.webreg.entity.RoleEntity;
//import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
//import edu.kit.scc.webreg.entity.UserEntity;
//import edu.kit.scc.webreg.entity.UserStatus;
//import edu.kit.scc.webreg.exc.RegisterException;
//import edu.kit.scc.webreg.service.GroupService;
//import edu.kit.scc.webreg.service.RoleService;
//import edu.kit.scc.webreg.service.SamlIdpMetadataService;
//import edu.kit.scc.webreg.service.SamlSpConfigurationService;
//import edu.kit.scc.webreg.service.UserService;
//
//@ManagedBean
//@ViewScoped
//public class RegisterUserBean implements Serializable {
//
//	private static final long serialVersionUID = 1L;
//
//	@Inject 
//    private SessionManager sessionManager;
//	
//    @Inject
//    private UserService service;
//
//    @Inject
//    private GroupService groupService;
//
//	@Inject 
//	private SamlIdpMetadataService idpService;
//	
//	@Inject 
//	private SamlSpConfigurationService spService;
//
//	@Inject
//	private RoleService roleService;
//	
//	private UserEntity entity;
//	private SamlIdpMetadataEntity idpEntity;
//	
//	private Boolean errorState = false;
//	
//	@PostConstruct
//	public void init() 
//	{
//	idpEntity = idpService.findById(sessionManager.getIdpId());
//  	entity = service.createNew();
//  	entity.setPersistentIdpId(idpEntity.getEntityId());
//  	entity.setPersistentSpId(spService.findById(sessionManager.getSpId()).getEntityId());
//  	entity.setPersistentId(sessionManager.getPersistentId());
//  	entity.setRoles(new HashSet<RoleEntity>());
//  	entity.setAttributeStore(new HashMap<String, String>());
//
//  	try {
//		service.updateUserFromAttribute(entity, sessionManager.getAttributeMap());
//		groupService.updateUserPrimaryGroupFromAttribute(entity, sessionManager.getAttributeMap());
//		groupService.updateUserSecondaryGroupsFromAttribute(entity, sessionManager.getAttributeMap());
//  	} catch (RegisterException e) {
//  		errorState = true;
//  		FacesContext.getCurrentInstance().addMessage(null, 
//				new FacesMessage(FacesMessage.SEVERITY_ERROR, "Es fehlen notwendige Attribute", e.getMessage()));
//  	}
//	}
//
//    public String save() {
//    	RoleEntity userRole = roleService.findByName("User");
//    	
//    	entity.getRoles().add(userRole);
//    	entity.setUserStatus(UserStatus.ACTIVE);
//
//		Map<String, String> attributeStore = entity.getAttributeStore();
//		for (Entry<String, String> entry : sessionManager.getAttributeMap().entrySet()) {
//			attributeStore.put(entry.getKey(), entry.getValue());
//		}
//		
//		entity.setLastUpdate(new Date());
//
//		entity = service.save(entity);
//
//    	sessionManager.setUserId(entity.getId());
//    	
//		return "/faces/pages/start.xhtml?faces-redirect=true";
//    }
//    
//	public UserEntity getEntity() {
//		return entity;
//	}
//
//	public void setEntity(UserEntity entity) {
//		this.entity = entity;
//	}
//
//	public SamlIdpMetadataEntity getIdpEntity() {
//		return idpEntity;
//	}
//
//	public Boolean getErrorState() {
//		return errorState;
//	}
//
//	public void setErrorState(Boolean errorState) {
//		this.errorState = errorState;
//	}
//
//	
//}
