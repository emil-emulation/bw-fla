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

package de.bwl.bwfla.common.services.guacplay.record;

import de.bwl.bwfla.common.services.guacplay.GuacDefs.EventType;
import de.bwl.bwfla.common.services.guacplay.events.GuacEvent;
import de.bwl.bwfla.common.services.guacplay.events.IGuacEventListener;
import de.bwl.bwfla.common.services.guacplay.events.VisualSyncBeginEvent;
import de.bwl.bwfla.common.services.guacplay.protocol.BufferedMessageProcessor;
import de.bwl.bwfla.common.services.guacplay.protocol.Message;
import de.bwl.bwfla.common.services.guacplay.util.Barrier;

/* Internal class (package-private) */


/** A worker for processing posted messages. */
abstract class VSyncedMessageProcessor extends BufferedMessageProcessor implements IGuacEventListener
{
	// Member fields
	private final Barrier vsyncBarrier;
	private volatile long vsyncBarrierTimestamp;
	
	/** Constant, representing an invalid timestamp */
	private static final long INVALID_TIMESTAMP = Long.MAX_VALUE;
	
	
	/** Constructor */
	public VSyncedMessageProcessor(String name, int msgBufferCapacity, Barrier vsyncBarrier)
	{
		super(name, msgBufferCapacity);
		
		this.vsyncBarrier = vsyncBarrier;
		this.vsyncBarrierTimestamp = INVALID_TIMESTAMP;
	}
	
	/** Synchronize processing of current message with specified timestamp. */
	protected abstract void synchronize(long timestamp);
	
	
	/* ========== IActionEventListener Implementation ========== */
	
	@Override
	public final void onGuacEvent(GuacEvent event)
	{
		switch (event.getType())
		{
			case EventType.VSYNC_BEGIN: {
				log.info("Begin vsync-phase for {}.", msgProcessorName);
				VisualSyncBeginEvent vsevent = (VisualSyncBeginEvent) event;
				vsyncBarrierTimestamp = vsevent.getTimestamp();
				condition.signal();
				break;
			}
		
			case EventType.VSYNC_END: {
				log.info("End vsync-phase for {}.", msgProcessorName);
				vsyncBarrierTimestamp = INVALID_TIMESTAMP;
				break;
			}
		}
	}
	
	
	/* ========== AsyncMessageProcessor Implementation ========== */
	
	@Override
	protected final void execute() throws Exception
	{
//		final int msgNumProcessedPrev = super.getNumProcessedMessages();
//		final int msgNumSkippedPrev = super.getNumSkippedMessages();
		
		Message message = null;
		
		// Processing of recieved messages, until the buffer is empty
		while ((message = messages.beginTakeOp()) != null) {
			// Handle the visual-synchronization, when needed
			final long timestamp = message.getTimestamp();
			if (timestamp > vsyncBarrierTimestamp)
				vsyncBarrier.await();
			
			try {
				// Handle the recieved message
				this.synchronize(timestamp);
				this.process(message);
			}
			finally {
				messages.finishTakeOp();
			}
		}
		
//		// Any new messages processed?
//		final int msgNumProcessed = super.getNumProcessedMessages() - msgNumProcessedPrev;
//		final int msgNumSkipped = super.getNumSkippedMessages() - msgNumSkippedPrev;
//		final int msgNumTotal = msgNumProcessed + msgNumSkipped;
//		if (msgNumTotal > 0) {
//			// Print an info message
//			StringBuffer strbuf = new StringBuffer(128);
//			strbuf.append(msgNumProcessed);
//			strbuf.append(" message(s) processed, ");
//			strbuf.append(msgNumSkipped);
//			strbuf.append(" skipped.");
//			log.info(strbuf.toString());
//		}
		
		// At this point there are currently no messages to process, block and wait.
		condition.await(TIMEOUT_ON_EMPTY_BUFFER);
	}

	@Override
	protected void finish() throws Exception
	{
		// Process all pending messages, before the final termination
		if (!Thread.currentThread().isInterrupted())
			this.execute();
		
		vsyncBarrier.leave(true);  // Exit vsync-mode
	}
}
