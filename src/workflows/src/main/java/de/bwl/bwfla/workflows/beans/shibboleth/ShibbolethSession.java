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
//import java.io.IOException;
//import java.io.Serializable;
//import java.util.List;
//import javax.annotation.PostConstruct;
//import javax.enterprise.context.SessionScoped;
//import javax.faces.context.ExternalContext;
//import javax.faces.context.FacesContext;
//import javax.inject.Inject;
//import javax.inject.Named;
//import org.apache.log4j.Logger;
//import de.bwl.bwfla.workflows.beans.common.UINotify;
//import de.bwl.bwfla.workflows.shibboleth.util.SessionManager;
//import edu.kit.scc.webreg.entity.FederationEntity;
//import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
//import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
//import edu.kit.scc.webreg.entity.UserEntity;
//import edu.kit.scc.webreg.service.FederationService;
//import edu.kit.scc.webreg.service.SamlIdpMetadataService;
//import edu.kit.scc.webreg.service.SamlSpConfigurationService;
//import edu.kit.scc.webreg.service.UserService;
//
///**
// * 
// * This class manages the shibboleth login and provides access to the user that
// * is logged in.
// * 
// * @author Leander Sabel
// * 
// */
//@SessionScoped
//@Named("ShibbolethSession")
//public class ShibbolethSession implements Serializable {
//
//	// Injected fields
//	@Inject
//	private SamlSpConfigurationService spService;
//
//	@Inject
//	private FederationService service;
//
//	@Inject
//	private SamlIdpMetadataService idpService;
//
//	@Inject
//	private SessionManager sessionManager;
//
//	@Inject
//	private UserService userService;
//
//	// Private fields
//	private static final long serialVersionUID = 3691780836441851596L;
//	private final Logger log = Logger.getLogger(getClass());
//	private List<FederationEntity> federationList;
//	private FederationEntity selectedFederation;
//	private List<SamlIdpMetadataEntity> idpList;
//	private SamlIdpMetadataEntity selectedIdP;
//	private boolean localSpEnabled;
//	
//	@PostConstruct
//	public void init() {
//		if (spService.findAll().isEmpty())
//			localSpEnabled = false;
//		else {
//			SamlSpConfigurationEntity localSp = spService.findByHostname("localhost");
//			localSpEnabled  = localSp.isEnabled();
//		}
//	}
//
//	public void initializeSession() {
//		federationList = service.findAll();
//
//		if (federationList == null || federationList.size() == 0) {
//			error("The list of federations is empty");
//		} else {
//			selectedFederation = federationList.get(0);
//			updateIdpList();
//
//			if (idpList == null || idpList.size() == 0) {
//				error("The IdP list for " + selectedFederation.getName() + " is empty");
//			} else {
//				selectedIdP = idpList.get(0);
//			}
//		}
//	}
//
//	/**
//	 * @return the federationList
//	 */
//	public List<FederationEntity> getFederationList() {
//		return federationList;
//	}
//
//	/**
//	 * @return the idpList
//	 */
//	public List<SamlIdpMetadataEntity> getIdpList() {
//		return idpList;
//	}
//
//	/**
//	 * @param selectedFederation
//	 *          the selectedFederation to set
//	 */
//	public void setSelectedFederation(FederationEntity selectedFederation) {
//		this.selectedFederation = selectedFederation;
//	}
//
//	/**
//	 * @return the selectedFederation
//	 */
//	public FederationEntity getSelectedFederation() {
//		return selectedFederation;
//	}
//
//	/**
//	 * @param selectedIdP
//	 *          the selectedIdP to set
//	 */
//	public void setSelectedIdP(SamlIdpMetadataEntity selectedIdP) {
//		this.selectedIdP = selectedIdP;
//		log.info("Selected " + selectedIdP.getEntityId());
//	}
//
//	/**
//	 * @return the selectedIdP
//	 */
//	public SamlIdpMetadataEntity getSelectedIdP() {
//		return selectedIdP;
//	}
//
//	/**
//	 * Get the user that is currently logged in
//	 */
//	public UserEntity getUser() {
//		if (sessionManager.isLoggedIn())
//			return userService.findById(sessionManager.getUserId());
//		else
//			return null;
//	}
//	
//	public String getUsername() {
//		UserEntity user = userService.findById(sessionManager.getUserId());
//		return user != null ? user.getGivenName() + " " + user.getSurName() : null;
//	}
//	
//	/**
//	 * @return the localSpEnabled
//	 */
//	public boolean isLocalSpEnabled() {
//		return localSpEnabled;
//	}
//
//	public boolean isLoggedIn() {
//		return sessionManager.isLoggedIn();
//	}
//
//	/**
//	 * Setup the Shibboleth module
//	 * 
//	 * @return
//	 */
//
//	public void login() {
//		
//		if (!validState()) {
//			error("The Shibboleth system state is invalid. The login will not continue.");
//			return;
//		}
//
//		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
//		String hostname = externalContext.getRequestServerName();
//		SamlSpConfigurationEntity spConfig = spService.findByHostname(hostname);
//
//		sessionManager.setSpId(spConfig.getId());
//		sessionManager.setIdpId(selectedIdP.getId());
//
//		try {
//			externalContext.redirect("/Shibboleth.sso/Login");
//		} catch (IOException e) {
//			error(e.getMessage());
//			e.printStackTrace();
//		}
//	}
//
//	/**
//	 * Logout the current user session.
//	 */
//	public String logout() {
//		// Logout is not yet implemented
//		sessionManager.logout();
//		return "/pages/start.xhtml?faces-redirect=true";
//	}
//
//	/**
//	 * Refresh the list of available IdP's based on the selected federation
//	 */
//	public void updateIdpList() {
//		idpList = idpService.findAllByFederationOrderByOrgname(selectedFederation);
//	}
//
//	/**
//	 * Validate the presence of all local fields required for the shibboleth
//	 * login.
//	 * 
//	 * @return
//	 */
//	private boolean validState() {		
//		return federationList != null && federationList.size() > 0 && selectedFederation != null && idpList != null && idpList.size() > 0
//				&& selectedIdP != null;
//	}
//
//	/**
//	 * Show error to the user and print it to the error log.
//	 * 
//	 * @param error
//	 */
//	private void error(String error) {
//		UINotify.error(error);
//		log.error(error);
//	}
//
//}
