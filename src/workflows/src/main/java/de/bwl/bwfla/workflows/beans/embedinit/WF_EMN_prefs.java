package de.bwl.bwfla.workflows.beans.embedinit;

import java.io.IOException;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import de.bwl.bwfla.workflows.beans.common.BwflaFormBean;

@ManagedBean
@ViewScoped
public class WF_EMN_prefs extends BwflaFormBean
{
	@Inject
	private WF_EMN_data wfData;
	
	@Override
	public String forward() {
		System.out.println("forward: " + wfData.getStorage().initialURL);
		final ExternalContext extcontext = FacesContext.getCurrentInstance().getExternalContext();
		try {
			extcontext.redirect(wfData.getStorage().initialURL);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
}
