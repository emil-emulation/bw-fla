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

package de.bwl.bwfla.common.services.guacplay.replay;

import de.bwl.bwfla.common.services.guacplay.GuacDefs.SourceType;
import de.bwl.bwfla.common.services.guacplay.net.GuacReader;
import de.bwl.bwfla.common.services.guacplay.net.PlayerSocket;
import de.bwl.bwfla.common.services.guacplay.protocol.AsyncMessageProcessor;
import de.bwl.bwfla.common.services.guacplay.protocol.Message;


public class ServerMessageProcessor extends AsyncMessageProcessor
{
	// Member fields
	private final Message message;
	private final PlayerSocket socket;
	private final GuacReader input;
	
	/** Timeout for waiting, when nothing can be read. */
	private static final long TIMEOUT_ON_UNAVAILABLE = 250L;
	

	/** Constructor */
	public ServerMessageProcessor(String name, GuacReader input, PlayerSocket socket)
	{
		super(name);
		
		this.message = new Message(SourceType.SERVER);
		this.socket = socket;
		this.input = input;
	}
	
	@Override
	protected void execute() throws Exception
	{
		// Can something be read?
		if (!input.available()) {
			condition.await(TIMEOUT_ON_UNAVAILABLE);
			return;  // No, retry later
		}
		
		// Yes, then read the new message
		final char[] data = input.read();
		if (data == null)
			return;  // End-of-stream reached!
		
		// Post this message to the client
		if (socket != null) {
			synchronized (socket) {
				socket.post(data, 0, data.length);
			}
		}
		
		// Process it also on the server-side
		message.set(0L, data, 0, data.length);
		this.process(message);
	}

	@Override
	protected void finish() throws Exception
	{
		// Do nothing!
	}
}
