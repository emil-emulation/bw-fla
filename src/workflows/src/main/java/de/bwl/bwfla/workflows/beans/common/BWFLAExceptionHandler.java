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
import javax.servlet.http.HttpServletRequest;
import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.WindowContext;
import de.bwl.bwfla.workflows.conf.WorkflowSingleton;



public class BWFLAExceptionHandler extends ExceptionHandlerWrapper
{
	protected static final Logger	LOG			= Logger.getLogger(BWFLAExceptionHandler.class.getName());
	private final static String		ERROR_PAGE	= "/faces/pages/error.xhtml";
	private final static String		START_PAGE	= "/faces/pages/bwfla.xhtml";
	private ExceptionHandler		wrapped		= null;
	
	private final static String	LOCAL_START_PAGE = "/faces/pages/workflow-local/WF_L_0.xhtml";
	private final static String	LOCAL_ERROR_PAGE = "/faces/pages/workflow-local/WF_L_error.xhtml";
	
	private final static String LOCAL_SPECIAL_START_PAGE = "/faces/pages/workflow-local-special/WF_LS_0.xhtml";
	private final static String LOCAL_SPECIAL_ERROR_PAGE = "/faces/pages/workflow-local-special/WF_LS_error.xhtml";
	
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
		
		FacesContext jsf = FacesContext.getCurrentInstance();
		HttpServletRequest request = (HttpServletRequest) jsf.getExternalContext().getRequest();
		String clientIp = request.getRemoteHost();
		if(clientIp == null || clientIp.isEmpty())
			clientIp= "(unkown)";
		
		try
		{
			MimeMessage mimeMessage = new MimeMessage(session);
			mimeMessage.setFrom(new InternetAddress(sender));
			mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(recepient));
			mimeMessage.setSubject("bwFLA Error Notification. Time: " + "'" + (new Date()).toString() + "'" + "; " + "Host: " + "'" + hostname + "'" + "; " + "Client IP: " + "'" + clientIp + "'");
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

		final FacesContext jsf = FacesContext.getCurrentInstance();
		final String currentPage = "/faces" + jsf.getViewRoot().getViewId();
		final boolean isInLocalSpecialMode = currentPage.contains("workflow-local-special");
		final boolean isInLocalMode = !isInLocalSpecialMode && currentPage.contains("workflow-local");
		Throwable cause = null, rootCause = null;
		
		try
		{	
			ExceptionQueuedEventContext exceptionCtx = (ExceptionQueuedEventContext) iterator.next().getSource();
			cause = exceptionCtx.getException();
			rootCause = this.getRootCause(exceptionCtx.getException());
			
			if(rootCause instanceof ViewExpiredException)
			{	
				if(!currentPage.equalsIgnoreCase(START_PAGE))
					jsf.addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_WARN, "Previous Link: Expired", "You've been automatically\nredirected to the start page."));
				
				
				String startPage = START_PAGE;
				if (isInLocalMode)
					startPage = LOCAL_START_PAGE;
				else if (isInLocalSpecialMode)
					startPage = LOCAL_SPECIAL_START_PAGE;
				
				jsf.getExternalContext().redirect(startPage);
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
			
			String errorPage = ERROR_PAGE;
			if (isInLocalMode)
				errorPage = LOCAL_ERROR_PAGE;
			else if (isInLocalSpecialMode)
				errorPage = LOCAL_SPECIAL_ERROR_PAGE;
			
			jsf.getExternalContext().redirect(errorPage + "?faces-redirect=true");
			
			if(WorkflowSingleton.CONF.emailNotifier != null && WorkflowSingleton.CONF.emailNotifier.sender != null && WorkflowSingleton.CONF.emailNotifier.smtpHost != null)
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
