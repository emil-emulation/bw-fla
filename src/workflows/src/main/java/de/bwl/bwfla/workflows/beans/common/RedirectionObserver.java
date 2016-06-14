package de.bwl.bwfla.workflows.beans.common;

import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.bwl.bwfla.common.datatypes.EaasState;


public class RedirectionObserver implements EaasStateObserver
{
	/** Logger instance. */
	private final Logger log = LoggerFactory.getLogger(RedirectionObserver.class);
	
	private final Map<EaasState, Entry> entries;
	private final FacesContext context;


	public RedirectionObserver(FacesContext context)
	{
		this.entries = new HashMap<EaasState, Entry>();
		this.context = context;
	}

	public void addEntry(EaasState state, String url, String message)
	{
		if (url == null || url.isEmpty())
			throw new IllegalArgumentException("Invalid URL passed!");
		
		if (message != null) {
			message = message.trim();
			switch (message.charAt(message.length() - 1))
			{
				case '.':
				case '!':
				case '?':
					// Do nothing.
					break;
					
				default:
					message += '.';
			}
		}
		
		entries.put(state, new Entry(url, message));
	}

	public void removeEntry(EaasState state)
	{
		entries.remove(state);
	}

	public void clear()
	{
		entries.clear();
	}

	@Override
	public void onStateChanged(EaasState oldstate, EaasState newstate) throws Exception
	{
		Entry entry = entries.get(newstate);
		if (entry == null)
			return;  // No redirect for the new state needed!
		
		if (entry.message != null)
			log.info("{} Redirecting to '{}'", entry.message, entry.url);
		else log.info("Redirecting to '{}'", entry.url);
		
		context.getExternalContext().redirect(entry.url);
	}

	/** Creates and initializes a default instance. */
	public static RedirectionObserver create(FacesContext context, String url)
	{
		RedirectionObserver observer = new RedirectionObserver(context);
		observer.addEntry(EaasState.SESSION_INACTIVE, url, "Session inactivity detected.");
		observer.addEntry(EaasState.SESSION_STOPPED , url, "Session stopped unexpectedly!");
		return observer;
	}


	/* ========== Internals ========== */

	private static final class Entry
	{
		public final String url;
		public final String message;
		
		public Entry(String url, String message)
		{
			this.url = url;
			this.message = message;
		}
	}
}
