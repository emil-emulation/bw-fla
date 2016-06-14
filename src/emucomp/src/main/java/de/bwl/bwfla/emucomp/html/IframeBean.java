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

package de.bwl.bwfla.emucomp.html;

import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import de.bwl.bwfla.common.datatypes.EmuCompState;
import de.bwl.bwfla.emucomp.components.emulators.EmulatorBeanWrapper;
import de.bwl.bwfla.emucomp.components.emulators.HtmlConnector;
import de.bwl.bwfla.emucomp.conf.EmucompSingleton;



@ManagedBean
@ViewScoped
public class IframeBean implements Serializable
{
	private static final long	serialVersionUID	= -8097771932811201770L;
	private String				sessionId;
	private EmulatorBeanWrapper	emulatorBean;
	private HtmlConnector		htmlConnector;
	
	
	@PostConstruct
	public void initialize()
	{
		String cookie = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("cookie");
		
		this.sessionId = ((HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false)).getId();
		this.emulatorBean = EmucompSingleton.getComponent(cookie, EmulatorBeanWrapper.class);
		this.htmlConnector = (HtmlConnector) emulatorBean.getViewConnectors().get("HTTP");
	}
	
	public String getSessionId()
	{
		 return sessionId;
    }
	
	public HtmlConnector getHtmlConnector()
	{
		return htmlConnector;
	}
	
	public void checkRunningState() throws EmucompDisconnectException
	{
		EmuCompState state = EmuCompState.fromValue(emulatorBean.getEmulatorState());
		switch (state) {
			case EMULATOR_RUNNING:
			case EMULATOR_INACTIVE:
			case EMULATOR_BUSY:
				break;
				
			case EMULATOR_STOPPED:
				final Logger log = Logger.getLogger(this.getClass().getName());
				final String url = "stopped.xhtml";
				try {
					log.info("Emulator stopped, redirecting to: " + url);
					ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
					context.redirect(url + "?faces-redirect=true");
				}
				catch (IOException exception) {
					log.warning("Redirection to '" + url + "' failed!");
					exception.printStackTrace();
				}
				
				break;
				
			default:
				throw new EmucompDisconnectException();
		}
	}
}