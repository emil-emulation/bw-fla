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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.application.ViewExpiredException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.WindowContext;
import de.bwl.bwfla.workflows.conf.WorkflowSingleton;



public class BWFLAExceptionHandler extends ExceptionHandlerWrapper
{
	protected static final Logger	LOG			= Logger.getLogger(BWFLAExceptionHandler.class.getName());
	private final static String		ERROR_PAGE	= "/faces/pages/error.xhtml";
	private final static String		START_PAGE	= "/faces/pages/bwfla.xhtml";
	private ExceptionHandler		wrapped		= null;
	
	
	public BWFLAExceptionHandler(ExceptionHandler wrapped)
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

	private String getWindowId()
	{
		BeanManager beanManager = null;
		
		try
		{
			InitialContext initialContext = new InitialContext();
			beanManager = (BeanManager) initialContext.lookup("java:comp/BeanManager");
		}
		catch(NamingException e)
		{
			e.printStackTrace();
			return null;
		}

		@SuppressWarnings("unchecked")
		Bean<WindowContext> bean = (Bean<WindowContext>) beanManager.getBeans(WindowContext.class).iterator().next();
		CreationalContext<WindowContext> ctx = beanManager.createCreationalContext(bean);
		WindowContext winCtx = (WindowContext) beanManager.getReference(bean, WindowContext.class, ctx);
		
		return winCtx.getId();
	}
	
	static void sendErrorEmail(String sender, String recepient, String message)
	{
		Properties properties = System.getProperties();
		properties.setProperty("mail.smtp.host", WorkflowSingleton.CONF.emailNotifier.smtpHost);
		Session session = Session.getDefaultInstance(properties);
		
		String hostname = null;
		try
		{
			hostname = InetAddress.getLocalHost().getHostName();
			
			if(hostname == null || hostname.isEmpty())
				hostname = "(unkown)";
		}
		catch(UnknownHostException e)
		{
			e.printStackTrace();
		}
		
		try
		{
			MimeMessage mimeMessage = new MimeMessage(session);
			mimeMessage.setFrom(new InternetAddress(sender));
			mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(recepient));
			mimeMessage.setSubject("bwFLA Error Notification. Time: " + "'" + (new Date()).toString() + "'" + "; " + "Host: " + "'" + hostname + "'");
			mimeMessage.setText(message);
			Transport.send(mimeMessage);
		}
		catch(MessagingException mex)
		{
			mex.printStackTrace();
		}
	}
	
	synchronized public void handle() throws FacesException
	{
		Iterator<ExceptionQueuedEvent> iterator = getUnhandledExceptionQueuedEvents().iterator();
		if(!iterator.hasNext())
			return;

		FacesContext jsf = FacesContext.getCurrentInstance();
		Throwable cause = null, rootCause = null;
		
		try
		{	
			ExceptionQueuedEventContext exceptionCtx = (ExceptionQueuedEventContext) iterator.next().getSource();
			cause = exceptionCtx.getException();
			rootCause = this.getRootCause(exceptionCtx.getException());
			
			if(rootCause instanceof ViewExpiredException)
			{	
				String currentPage = "/faces" + jsf.getViewRoot().getViewId();
				
				if(!currentPage.equalsIgnoreCase(START_PAGE))
					jsf.addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_WARN, "Previous Link: Expired", "You've been automatically\nredirected to the start page."));
				
				jsf.getExternalContext().redirect(START_PAGE);
				return;
			}
			
			if(rootCause instanceof IllegalStateException)
			{
				this.wrapped.handle();
				return;
			}
			
		}
		catch(Throwable t)
		{
			t.printStackTrace();
			this.wrapped.handle();
		}

		iterator = getUnhandledExceptionQueuedEvents().iterator();
		while(iterator.hasNext())
		{
			iterator.next();
			iterator.remove();
		}

		try
		{
			Map<String, Object> sessionParams = jsf.getExternalContext().getSessionMap();
			sessionParams.put("rootCause" + this.getWindowId(), rootCause);
			jsf.getExternalContext().redirect(ERROR_PAGE + "?faces-redirect=true");
			
			if(WorkflowSingleton.CONF.emailNotifier != null && WorkflowSingleton.CONF.emailNotifier.sender != null && WorkflowSingleton.CONF.emailNotifier != null && WorkflowSingleton.CONF.emailNotifier.smtpHost != null)
			{
				StringWriter errorMessage = new StringWriter();
				PrintWriter pw = new PrintWriter(errorMessage);
				pw.println("SEE THE STACKTRACE BELOW: :)"); 
				pw.println();
				cause.printStackTrace(pw);
				
				try
				{
					sendErrorEmail(WorkflowSingleton.CONF.emailNotifier.sender, WorkflowSingleton.CONF.emailNotifier.recepient, errorMessage.toString());
				}
				catch(Throwable t)
				{
					t.printStackTrace();
				}
			}
			else
				LOG.warning("error notification will not be sent by email, since email notifier properties are not set in the configuration file");
		}
		catch(Throwable t)
		{
			t.printStackTrace();
			this.wrapped.handle();
		}
	}
}