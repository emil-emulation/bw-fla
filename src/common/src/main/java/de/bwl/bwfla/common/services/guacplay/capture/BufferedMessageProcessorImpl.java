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

package de.bwl.bwfla.common.services.guacplay.capture;

import de.bwl.bwfla.common.services.guacplay.protocol.BufferedMessageProcessor;
import de.bwl.bwfla.common.services.guacplay.protocol.Message;

/* Internal class (package-private) */


/** A worker for processing posted messages. */
class BufferedMessageProcessorImpl extends BufferedMessageProcessor
{
	/** Constructor */
	public BufferedMessageProcessorImpl(String name, int msgBufferCapacity)
	{
		super(name, msgBufferCapacity);
	}
	
	
	/* ========== AsyncMessageProcessor Implementation ========== */
	
	@Override
	protected final void execute() throws Exception
	{
		Message message = null;
		
		// Processing of recieved messages, until the buffer is empty
		while ((message = messages.beginTakeOp()) != null) {
			try {
				// Handle the recieved message
				this.process(message);
			}
			finally {
				messages.finishTakeOp();
			}
		}
		
		// At this point there are currently no messages to process, block and wait.
		condition.await(TIMEOUT_ON_EMPTY_BUFFER);
	}

	@Override
	protected void finish() throws Exception
	{
		// Process all pending messages, before the final termination
		if (!Thread.currentThread().isInterrupted())
			this.execute();
	}
}
