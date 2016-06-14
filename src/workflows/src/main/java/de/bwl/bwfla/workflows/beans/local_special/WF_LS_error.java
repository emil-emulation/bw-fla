package de.bwl.bwfla.workflows.beans.local_special;

import javax.inject.Inject;

import de.bwl.bwfla.workflows.beans.common.ErrorBean;


public class WF_LS_error extends ErrorBean
{
	private static final long serialVersionUID = -8306195780131155791L;

	@Inject private WF_LS_data wfData;
	
	@Override
	public String forward()
	{
		final String params = "faces-redirect=true&envid=" + wfData.getEnvironmentID();
		return "/pages/workflow-local-special/WF_LS_0.xhtml?" + params;
	}
}
