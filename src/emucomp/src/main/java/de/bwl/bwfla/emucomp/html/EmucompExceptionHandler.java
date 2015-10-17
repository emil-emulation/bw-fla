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

import java.util.Iterator;
import javax.faces.FacesException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;



public class EmucompExceptionHandler extends ExceptionHandlerWrapper
{
	private ExceptionHandler	wrapped		= null;
	private final static String	DEFAULT_ERROR_PAGE		= "error.xhtml";
	private final static String	DISCONNECT_ERROR_PAGE	= "disconnect.xhtml";
	
	public EmucompExceptionHandler(ExceptionHandler wrapped)
	{
		this.wrapped = wrapped;
	}

	@Override
	public ExceptionHandler getWrapped()
	{
		return this.wrapped;
	}

	@Override
	public Throwable getRootCause(Throwable e)
	{
		Throwable rootCause = e;

		if(rootCause != null) 
			for(int i = 0, MAX_DEPTH = 20; i < MAX_DEPTH; ++i)
			{
				Throwable cause = rootCause.getCause();
				if(cause == null) 
					break;
				
				rootCause = rootCause.getCause();
			}

		return rootCause;
	}
	
	synchronized public void handle() throws FacesException
	{
		Iterator<ExceptionQueuedEvent> iterator = getUnhandledExceptionQueuedEvents().iterator();
		if(!iterator.hasNext())
			return;
		
		Throwable rootCause = null;
		try
		{
			ExceptionQueuedEventContext exceptionCtx = (ExceptionQueuedEventContext) iterator.next().getSource();
			rootCause = this.getRootCause(exceptionCtx.getException());
		}
		catch(Throwable t)
		{
			t.printStackTrace();
			this.wrapped.handle();
		}

		if(rootCause instanceof IllegalStateException)
		{
			this.wrapped.handle();
			return;
		}
		
		iterator = getUnhandledExceptionQueuedEvents().iterator();
		while(iterator.hasNext())
		{
			iterator.next();
			iterator.remove();
		}

		try
		{	
			FacesContext jsf = FacesContext.getCurrentInstance();
			
			if(rootCause instanceof EmucompDisconnectException)
				jsf.getExternalContext().redirect(DISCONNECT_ERROR_PAGE + "?faces-redirect=true");
			else
				jsf.getExternalContext().redirect(DEFAULT_ERROR_PAGE  + "?faces-redirect=true");
		}
		catch(Throwable t)
		{
			t.printStackTrace();
			this.wrapped.handle();
		}
	}
}