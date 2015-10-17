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

import java.util.Map;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;



@ManagedBean
@ViewScoped
public class ErrorBean extends BwflaFormBean
{
	private static final long		serialVersionUID	= 4153295994835362590L;
	private static String			DEFAULT_ERROR_INFO	= "(no error info specified)";
	
	public String getApologize()
	{
		return "Error Details:";
	}
	
	public String getErrorInfo()
	{
		String errorInfo = DEFAULT_ERROR_INFO;
		
		Map<String, Object> sessionMap = jsf.getExternalContext().getSessionMap();
		Throwable rootCause = (Throwable) sessionMap.get("rootCause" + this.winCtx.getId());

		if(rootCause instanceof WFPanicException)
		{
			errorInfo = rootCause.getMessage();
			if(errorInfo == null || errorInfo.isEmpty())
				errorInfo = DEFAULT_ERROR_INFO;
		}
		else if(rootCause != null)
			rootCause.printStackTrace();
			
		
		return errorInfo;
	}
	
	public String forward()
	{		
		return "/pages/start.xhtml?faces-redirect=true";
	}
}