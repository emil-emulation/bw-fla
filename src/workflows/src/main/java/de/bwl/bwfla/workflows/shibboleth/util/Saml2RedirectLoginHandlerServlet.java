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
//import org.opensaml.ws.message.encoder.MessageEncodingException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
//import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
//import edu.kit.scc.webreg.service.SamlIdpMetadataService;
//import edu.kit.scc.webreg.service.SamlSpConfigurationService;
//import edu.kit.scc.webreg.service.saml.Saml2RedirectService;
//
//@Named
//@WebServlet(urlPatterns = { "/Shibboleth.sso/Login" })
//public class Saml2RedirectLoginHandlerServlet implements Servlet {
//
//	Logger logger = LoggerFactory.getLogger(Saml2RedirectLoginHandlerServlet.class);
//
//	@Inject
//	private SessionManager session;
//
//	@Inject
//	private SamlIdpMetadataService idpService;
//
//	@Inject
//	private SamlSpConfigurationService spService;
//
//	@Inject
//	private Saml2RedirectService saml2RedirectService;
//
//	@Override
//	public void init(ServletConfig config) throws ServletException {
//
//	}
//
//	@Override
//	public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
//
//		HttpServletRequest request = (HttpServletRequest) servletRequest;
//		HttpServletResponse response = (HttpServletResponse) servletResponse;
//
//		if (session == null || session.getIdpId() == null || session.getSpId() == null) {
//			logger.debug("Client session from {} not established. Sending client back to welcome page", request.getRemoteAddr());
//			response.sendRedirect("/pages/start.xhtml");
//			return;
//		}
//
//		try {
//			SamlIdpMetadataEntity idpEntity = idpService.findById(session.getIdpId());
//			SamlSpConfigurationEntity spEntity = spService.findById(session.getSpId());
//
//			saml2RedirectService.redirectClient(idpEntity, spEntity, response);
//
//		} catch (MessageEncodingException e) {
//			throw new ServletException("Error encoding outgoing message", e);
//		}
//
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
