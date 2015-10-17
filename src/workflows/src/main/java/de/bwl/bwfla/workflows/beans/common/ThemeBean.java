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

package de.bwl.bwfla.workflows.beans.common;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import de.bwl.bwfla.workflows.conf.WorkflowSingleton;

@ManagedBean
@ViewScoped
public class ThemeBean implements Serializable
{
	private String title;
	private String panel_header;
	private List<String> logos;
	private String welcome_text;
	private String footer;
	private String extracss;

	public void isConfigSet() 
	{
		FacesContext fc = FacesContext.getCurrentInstance();
		
		if(WorkflowSingleton.CONF == null) 
		{
			ConfigurableNavigationHandler nav = (ConfigurableNavigationHandler) fc.getApplication().getNavigationHandler();
			nav.performNavigation("/pages/workflow-conf/WF_CONF_0.xhtml?faces-redirect=true");
		}
	}
	
	@PostConstruct
	private void init() {
		loadTheme();
	}

	public void loadTheme() 
	{	
		isConfigSet();
		
		try 
		{
			InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("theme.xml");
			if(resourceAsStream == null)
            {
					FacesContext.getCurrentInstance().getExternalContext().redirect("bwfla.xhtml");
					return;
            }
			
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(resourceAsStream);
			
			try {
				// Read title
				this.title = doc.getElementsByTagName("title").item(0).getFirstChild().getNodeValue();
			} catch (Exception e) {
				Utils.log("ThemeBean#loadTheme", "Hint: 'title' Theme (workflows/res/theme.xml) value was left empty. This may be desired.");
			}
			
			try {
				// Read title
				this.panel_header = doc.getElementsByTagName("panel-header").item(0).getFirstChild().getNodeValue();
			} catch (Exception e) {
				Utils.log("ThemeBean#loadTheme", "Hint: 'title' Theme (workflows/res/theme.xml) value was left empty. This may be desired.");
			}
			
			try {
				// Read welcome text
				this.welcome_text = doc.getElementsByTagName("welcome- ").item(0).getFirstChild().getNodeValue();
			} catch (Exception e) {
				Utils.log("ThemeBean#loadTheme", "Hint: 'welcome_text' Theme (workflows/res/theme.xml) value was left empty. This may be desired.");
			}
			
			try {
				// Read welcome text
				this.extracss = doc.getElementsByTagName("extracss").item(0).getFirstChild().getNodeValue();
			} catch (Exception e) {
				Utils.log("ThemeBean#loadTheme", "Hint: 'extracss' Theme (workflows/res/theme.xml) value was left empty. This may be desired.");
			}			
			
			try {
				// Read logos
				this.logos = new ArrayList<>();			
				NodeList logos = doc.getElementsByTagName("logo");
				for (int i = 0; i < logos.getLength(); i++) {			
					this.logos.add(logos.item(i).getFirstChild().getNodeValue());
				}
			} catch (Exception e) {
				Utils.log("ThemeBean#loadTheme", "Hint: 'logos' Theme (workflows/res/theme.xml) value was left empty. This may be desired.");
			}
			
			try {
				// Read footer
				this.footer = doc.getElementsByTagName("footer").item(0).getFirstChild().getNodeValue();
			} catch (Exception e) {
				Utils.log("ThemeBean#loadTheme", "Hint: 'footer' Theme (workflows/res/theme.xml) value was left empty. This may be desired.");
			}
		} catch (Exception e) {
			welcome_text = "<h1>Either an error occured while loading the theme or you forgot to create a theme.xml in workflow resources!</h1>";
			Utils.log("ThemeBean#loadTheme", "An error occured while loading the theme!");
			e.printStackTrace();
		}		
	}
	
	/* Getters and Setters */	
	public String getTitle() {
		return title;
	}

	public List<String> getLogos() {
		return logos;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setLogos(List<String> logos) {
		this.logos = logos;
	}

	public String getWelcome_text() {
		return welcome_text;
	}

	public void setWelcome_text(String welcome_text) {
		this.welcome_text = welcome_text;
	}

	public String getFooter() {
		return footer;
	}

	public void setFooter(String footer) {
		this.footer = footer;
	}

	public String getPanel_header() {
		return panel_header;
	}

	public void setPanel_header(String panel_header) {
		this.panel_header = panel_header;
	}

	public String getExtracss() {
		return extracss;
	}

	public void setExtracss(String extracss) {
		this.extracss = extracss;
	}
}
