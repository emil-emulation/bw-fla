package de.bwl.bwfla.workflows.beans.local;

import de.bwl.bwfla.workflows.beans.common.ErrorBean;


public class LocalErrorBean extends ErrorBean
{
	private static final long serialVersionUID = 8547453424852911589L;

	@Override
	public String forward()
	{		
		return "/pages/workflow-local/WF_L_0.xhtml?faces-redirect=true";
	}
}
