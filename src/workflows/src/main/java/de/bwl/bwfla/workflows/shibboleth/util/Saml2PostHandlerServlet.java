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
//import java.io.IOException;
//import java.util.Date;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Map.Entry;
//import java.util.Set;
//import javax.inject.Inject;
//import javax.inject.Named;
//import javax.servlet.Servlet;
//import javax.servlet.ServletConfig;
//import javax.servlet.ServletException;
//import javax.servlet.ServletRequest;
//import javax.servlet.ServletResponse;
//import javax.servlet.annotation.WebServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import org.opensaml.saml2.core.Assertion;
//import org.opensaml.saml2.core.EncryptedAssertion;
//import org.opensaml.saml2.core.NameID;
//import org.opensaml.saml2.core.Response;
//import org.opensaml.saml2.metadata.EntityDescriptor;
//import org.opensaml.ws.message.decoder.MessageDecodingException;
//import org.opensaml.xml.encryption.DecryptionException;
//import org.opensaml.xml.security.SecurityException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
//import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
//import edu.kit.scc.webreg.entity.UserEntity;
//import edu.kit.scc.webreg.exc.RegisterException;
//import edu.kit.scc.webreg.exc.SamlAuthenticationException;
//import edu.kit.scc.webreg.service.GroupService;
//import edu.kit.scc.webreg.service.SamlIdpMetadataService;
//import edu.kit.scc.webreg.service.SamlSpConfigurationService;
//import edu.kit.scc.webreg.service.UserService;
//import edu.kit.scc.webreg.service.saml.Saml2AssertionService;
//import edu.kit.scc.webreg.service.saml.Saml2DecoderService;
//import edu.kit.scc.webreg.service.saml.Saml2ResponseValidationService;
//import edu.kit.scc.webreg.service.saml.SamlHelper;
//
//@Named
//@WebServlet(urlPatterns = {"/Shibboleth.sso/SAML2/POST"})
//public class Saml2PostHandlerServlet implements Servlet {
//
//	Logger logger = LoggerFactory.getLogger(Saml2PostHandlerServlet.class);
//
//	@Inject
//	private SessionManager session;
//
//	@Inject
//	private UserService userService;
//	
//	@Inject
//	private GroupService groupService;
//	
//	@Inject
//	private Saml2DecoderService saml2DecoderService;
//	
//	@Inject
//	private Saml2ResponseValidationService saml2ValidationService;
//	
//	@Inject
//	private Saml2AssertionService saml2AssertionService;
//	
//	@Inject
//	private SamlHelper samlHelper;
//		
//	@Inject 
//	private SamlIdpMetadataService idpService;
//	
//	@Inject 
//	private SamlSpConfigurationService spService;
//
//	@Override
//	public void init(ServletConfig config) throws ServletException {
//		
//	}
//
//	@Override
//	public void service(ServletRequest servletRequest, ServletResponse servletResponse)
//			throws ServletException, IOException {
//
//		HttpServletRequest request = (HttpServletRequest) servletRequest;
//		HttpServletResponse response = (HttpServletResponse) servletResponse;
//
//		if (session == null || session.getIdpId() == null || session.getSpId() == null) {
//			logger.debug("Client session from {} not established. Sending client back to welcome page",
//					request.getRemoteAddr());
//			response.sendRedirect("/faces/pages/start.xhtml");
//			return;
//		}
//		
//		logger.debug("attempAuthentication, Consuming SAML Assertion");
//		
//		try {
//			SamlSpConfigurationEntity spEntity = spService.findById(session.getSpId());
//			SamlIdpMetadataEntity idpEntity = idpService.findById(session.getIdpId());
//			EntityDescriptor idpEntityDescriptor = samlHelper.unmarshal(
//					idpEntity.getEntityDescriptor(), EntityDescriptor.class);
//		
//			Response samlResponse = saml2DecoderService.decodePostMessage(request);
//
//			saml2ValidationService.verifyStatus(samlResponse);
//			saml2ValidationService.verifyIssuer(idpEntity, samlResponse);
//			saml2ValidationService.verifyExpiration(samlResponse, 1000L * 60L * 10L);
//
//			Boolean responseSignatureValid = false;
//			try {
//				logger.debug("Validating SamlResponse Signature for " + samlResponse.getID());					
//				saml2ValidationService.validateSignature(samlResponse, samlResponse.getIssuer(), idpEntityDescriptor);
//				logger.debug("Validating SamlResponse Signature success for " + samlResponse.getID());					
//				responseSignatureValid = true;
//			} catch (SamlAuthenticationException e) {
//				logger.debug("SamlResponse doesn't contain a signature");
//			}
//
//			List<Assertion> assertionList = samlResponse.getAssertions();
//			List<EncryptedAssertion> encryptedAssertionList = samlResponse.getEncryptedAssertions();
//			logger.debug("Got {} assertion and {} encrypted assertion", assertionList.size(), encryptedAssertionList.size());
//
//			Assertion assertion;
//			
//			/**
//			 * take first encrypted assertion, then first assertion, ignore all other
//			 */
//			if (encryptedAssertionList.size() > 0) {
//				assertion = saml2AssertionService.decryptAssertion(
//						encryptedAssertionList.get(0), spEntity.getPrivateKey());
//			}
//			else if (assertionList.size() > 0) {
//				assertion = assertionList.get(0);
//			}
//			else {
//				throw new SamlAuthenticationException("SAML2 Response contained no Assertion");
//			}
//
//			if (! responseSignatureValid) {
//				logger.debug("Validating Assertion Signature for " + assertion.getID());					
//				saml2ValidationService.validateSignature(assertion, assertion.getIssuer(), idpEntityDescriptor);
//				logger.debug("Validating Assertion Signature success for " + assertion.getID());
//			}
//			else {
//				logger.debug("Skipping assertion signature validation. SamlResponse was signed");
//			}
//			
//			logger.debug("Fetching name Id from assertion");
//			String persistentId;
//			if (assertion.getSubject() == null)
//				throw new SamlAuthenticationException("No Subject in assertion!");
//			else if (assertion.getSubject().getNameID() == null)
//				throw new SamlAuthenticationException("SAML2 NameID is missing.");
//			else {
//				NameID nid = assertion.getSubject().getNameID();
//				logger.debug("NameId format {} value {}", nid.getFormat(), nid.getValue());
//				if (nid.getFormat().equals(NameID.TRANSIENT)) {
//					throw new SamlAuthenticationException("NameID is Transient but must be Persistent");
//				}
//				else if (nid.getFormat().equals(NameID.PERSISTENT)) {
//					persistentId = nid.getValue();
//				}
//				else
//					throw new SamlAuthenticationException("Unsupported SAML2 NameID Type");
//			}
//			
//			logger.debug("Storing relevant SAML data in session");
//			session.setAssertion(samlHelper.marshal(assertion));
//			session.setPersistentId(persistentId);
//			Map<String, String> attributeMap = saml2AssertionService.extractAttributes(assertion);
//			session.setAttributeMap(attributeMap);
//			
//			UserEntity user = userService.findByPersistentWithRoles(spEntity.getEntityId(), 
//						idpEntity.getEntityId(), persistentId);
//			
//			if (user == null) {
//				logger.info("New User detected, sending to register Page");
//				Set<String> newRoles = new HashSet<String>();
//				newRoles.add("ROLE_New");
//				session.setRoles(newRoles);
//				response.sendRedirect("/faces/pages/workflow-shibboleth/RegisterUser.xhtml");
//				return;
//			} 
//
//			logger.debug("Updating user {}", persistentId);
//			
//			try {
//				userService.updateUserFromAttribute(user, attributeMap);
//				groupService.updateUserPrimaryGroupFromAttribute(user, attributeMap);
//				groupService.updateUserSecondaryGroupsFromAttribute(user, attributeMap);
//			} catch (RegisterException e) {
//				logger.warn("Could not update user {}: {}", e.getMessage(), user.getEppn());
//				throw new SamlAuthenticationException(e.getMessage());
//			}
//			
//			Map<String, String> attributeStore = user.getAttributeStore();
//			attributeStore.clear();
//			for (Entry<String, String> entry : attributeMap.entrySet()) {
//				attributeStore.put(entry.getKey(), entry.getValue());
//			}
//			
//			user.setLastUpdate(new Date());
//			
//			userService.mergeAndSave(user);
//			session.setUserId(user.getId());
//			session.setTheme(user.getTheme());
//
//			if (session.getOrinignalRequestPath() != null)
//				response.sendRedirect(session.getOrinignalRequestPath());
//			else
//				response.sendRedirect("/faces/pages/start.xhtml");
//
//			return;
//
//		} catch (MessageDecodingException e) {
//			throw new SamlAuthenticationException("Authentication problem", e);
//		} catch (SecurityException e) {
//			throw new SamlAuthenticationException("Authentication problem", e);
//		} catch (DecryptionException e) {
//			throw new SamlAuthenticationException("Authentication problem", e);
//		}
//	}
//	
//	@Override
//	public ServletConfig getServletConfig() {
//		return null;
//	}
//
//	@Override
//	public String getServletInfo() {
//		return null;
//	}
//
//	@Override
//	public void destroy() {
//	}	
//}
