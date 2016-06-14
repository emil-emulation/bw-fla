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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.SocketException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.GuacamoleResourceNotFoundException;
import org.glyptodon.guacamole.GuacamoleServerException;
import org.glyptodon.guacamole.io.GuacamoleWriter;
import org.glyptodon.guacamole.net.GuacamoleTunnel;
import org.glyptodon.guacamole.servlet.GuacamoleHTTPTunnelServlet;
import org.glyptodon.guacamole.servlet.GuacamoleSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.bwl.bwfla.api.emucomp.ConnectionType;
import de.bwl.bwfla.common.services.guacplay.net.GuacSession;
import de.bwl.bwfla.common.services.guacplay.net.IGuacReader;
import de.bwl.bwfla.emucomp.components.emulators.EmulatorBeanWrapper;
import de.bwl.bwfla.emucomp.components.emulators.HtmlConnector;
import de.bwl.bwfla.emucomp.conf.EmucompSingleton;


public class BWFLAGuacamoleTunnelServlet extends GuacamoleHTTPTunnelServlet 
{
	private static final long serialVersionUID = 1L;

	/** Charset to use for reading requests and writing responses. */
	private static final String CHARSET = "UTF-8";
	
	/** Logger instance. */
	private final Logger log = LoggerFactory.getLogger(BWFLAGuacamoleTunnelServlet.class);
	
	@Override
	protected GuacamoleTunnel doConnect(HttpServletRequest request) throws GuacamoleException
	{
		String cookie;
		
		// Parse the request's body, that should contain the session's ID
		try {
			BufferedReader reader = request.getReader();
			cookie = reader.readLine();
			if (cookie == null) {
				log.error("Cookie was not specified!");
				return null;
			}
		}
		catch (IOException exception) {
			log.error("Cookie could not be parsed!");
			exception.printStackTrace();
			return null;
		}

		GuacSession.register(request.getSession(true));

		// Lookup the corresponding tunnel
		EmulatorBeanWrapper emubean = EmucompSingleton.getComponent(cookie, EmulatorBeanWrapper.class);
		HtmlConnector connector = (HtmlConnector) emubean.getViewConnectors().get(ConnectionType.HTTP.toString());
		GuacamoleTunnel tunnel = connector.getTunnel(); 
		if (tunnel != null)
			log.info("Predefined tunnel was found! ID: {}", tunnel.getUUID().toString().toUpperCase());
		else log.error("Tunnel for the following session ID was not found! ID: {}", cookie);

		return tunnel; 
	}
	
	@Override
	protected void doWrite(HttpServletRequest request, HttpServletResponse response, String tunnelId) throws GuacamoleException
	{
		// NOTE: This code is mostly copied from the GuacamoleHTTPTunnelServlet.doWrite(...) method!
		//       Important changes are marked apropriately.
		
		GuacamoleSession session = GuacSession.get(request.getSession(false));

		// Get tunnel and ensure, that it exists
		final GuacamoleTunnel tunnel = session.getTunnel(tunnelId);
		if (tunnel == null)
			throw new GuacamoleResourceNotFoundException("No such tunnel.");

		// We still need to set the content type to avoid the default of text/html, as
		// such a content type would cause some browsers to attempt to parse the result,
		// even though the JavaScript client does not explicitly request such parsing.
		response.setContentType("application/octet-stream");
		response.setHeader("Cache-Control", "no-cache");
		response.setContentLength(0);

		// Transfer data from request to tunnel-output, ensuring stream is always closed
		try {
			// Get input-reader for HTTP-stream and writer for the tunnel
			final GuacamoleWriter writer = tunnel.acquireWriter();
			final Reader input = new InputStreamReader(request.getInputStream(), CHARSET);
			try {
				// IMPORTANT CHANGES:
				//    - The data's length is known in the request, no need to allocate the 8kB buffers.
				//    - Furthermore the data can be written in one shot, no need for a loop.

				int numReadCalls = 0;

				// Allocate a new buffer and fill it with data.
				final int expLength = request.getContentLength();
				final char[] buffer = new char[expLength];
				int curLength = 0;
				do {
					final int remaining = expLength - curLength;
					final int numBytesRead = input.read(buffer, curLength, remaining);
					if (numBytesRead < 0)
						break;
					
					curLength += numBytesRead;
					++numReadCalls;
					
				} while (curLength < expLength);
				
				// Transfer data using the buffer
				if (tunnel.isOpen() && (curLength > 0))
					writer.write(buffer, 0, curLength);
				
				if (numReadCalls > 1) {
					String msg = "Client's HTTP-WriteRequest for tunnel {} required {} reads/writes.";
					log.info(msg, tunnel.getUUID(), numReadCalls);
				}
			}

			// Close input-stream in all cases
			finally {
				input.close();
			}
		}
		catch (Exception exception) {
			// Detach tunnel, but don't close it!
			session.detachTunnel(tunnel);

			// Rethrow the exception
			if (exception instanceof GuacamoleException)
				throw (GuacamoleException) exception;
			else {
				String message = "I/O-Error sending data to server: " + exception.getMessage();
				throw new GuacamoleServerException(message, exception);
			}
		}
		
		// Release the tunnel-writer for other threads
		finally {
			tunnel.releaseWriter();
		}
	}
	
	@Override
	protected void doRead(HttpServletRequest request, HttpServletResponse response, String tunnelId) throws GuacamoleException
	{
		// NOTE: This code is mostly copied from the GuacamoleHTTPTunnelServlet.doRead(...) method!
		//       Important changes are marked apropriately.
		
		GuacamoleSession session = GuacSession.get(request.getSession(false));
		
		// Get tunnel and ensure, that it exists
		final GuacamoleTunnel tunnel = session.getTunnel(tunnelId);
		if (tunnel == null)
			throw new GuacamoleResourceNotFoundException("No such tunnel.");

		// Ensure tunnel is open
		if (!tunnel.isOpen())
			throw new GuacamoleResourceNotFoundException("Tunnel is closed.");

		// Note that although we are sending text, Webkit browsers will buffer 1024 bytes
		// before starting a normal stream if we use anything but application/octet-stream.
		response.setContentType("application/octet-stream");
		response.setHeader("Cache-Control", "no-cache");
		
		// IMPORTANT CHANGES:
		//    - BufferedWriter is not needed for writing into the OutputStream in this case!
		//    - Reading through the interceptor implemented in a more efficient way.
		//    - Correct handling of buffer flushing.
		
		// Transfer data from tunnel-input to response, ensuring stream is always closed
		try {
			// Obtain exclusive read access to the tunnel and get writer for response
			final IGuacReader reader = (IGuacReader) tunnel.acquireReader();
			final Writer output = new OutputStreamWriter(response.getOutputStream(), CHARSET);
			try {
				// Detach tunnel and throw error if EOF (and we haven't sent any data yet)
				if (!reader.readInto(output))
					throw new GuacamoleResourceNotFoundException("Tunnel reached end of stream.");
				
				// Send all pending messages, until there are other threads waiting
				while (tunnel.isOpen()) {
					// Stop, if we expect to wait
					if (!reader.available())
						break;

					// Stop, if other requests are waiting
					if (tunnel.hasQueuedReaderThreads())
						break;
					
					// Read and send next message
					if (!reader.readInto(output)) {
						// End-of-stream reached!
						session.detachTunnel(tunnel);
					}
				}

				// End-of-instructions marker
				output.write("0.;");
				output.flush();
			}

			// Always close output-stream
			finally {
				output.close();
			}
		}
		catch (Exception exception) {
			// Detach tunnel, but don't close it
			session.detachTunnel(tunnel);

			Throwable cause = exception.getCause();
			
			// HACK: Catch all possible SocketExceptions and show only
			//       their messages without the lengthy stacktraces.
			//       Inspect here the full chain of exceptions!
			while (cause != null) {
				if (cause instanceof SocketException) {
					log.error("A socket-error occured! Cause: {}", cause.getMessage());
					return;
				}
				
				cause = cause.getCause();
			}
			
			// Rethrow the exception
			if (exception instanceof GuacamoleException)
				throw (GuacamoleException) exception;
			else {
				String message = "I/O-Error reading data from server: " + exception.getMessage();
				throw new GuacamoleServerException(message, exception);
			}
		}
		
		// Release the tunnel-reader for other threads
		finally {
			tunnel.releaseReader();
		}
	}
}
