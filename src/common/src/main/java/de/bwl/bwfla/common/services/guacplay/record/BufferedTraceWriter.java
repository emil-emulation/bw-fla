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

import java.io.IOException;

import de.bwl.bwfla.common.services.guacplay.GuacDefs.SourceType;
import de.bwl.bwfla.common.services.guacplay.io.TraceBlockWriter;
import de.bwl.bwfla.common.services.guacplay.protocol.Message;
import de.bwl.bwfla.common.services.guacplay.protocol.Instruction;
import de.bwl.bwfla.common.services.guacplay.protocol.InstructionDescription;
import de.bwl.bwfla.common.services.guacplay.util.FlagSet;
import de.bwl.bwfla.common.services.guacplay.util.RingBufferSPSC;

// Internal class (package-private)


final class BufferedTraceWriter
{
	// Member fields
	private final RingBufferSPSC<Message> smessages;
	private final RingBufferSPSC<Message> cmessages;
	private final TraceBlockWriter tblock;
	private long prevts;
	
	
	/** Constructor */
	public BufferedTraceWriter(TraceBlockWriter tblock, int capacity)
	{
		this.smessages = BufferedTraceWriter.newMessageBuffer(capacity);
		this.cmessages = BufferedTraceWriter.newMessageBuffer(capacity);
		this.tblock = tblock;
		this.prevts = Long.MIN_VALUE;
	}
	
	/** Add the specified instruction for later writing. */
	public void post(InstructionDescription desc, Instruction instr)
	{
		final SourceType source = desc.getSourceType();
		final long timestamp = desc.getTimestamp();
		final FlagSet flags = instr.flags();
		
		// Get the correct list for this instruction
		final RingBufferSPSC<Message> messages = (source == SourceType.SERVER) ? smessages : cmessages;
		final Message message = messages.beginBlockingPutOp();
		
		// Add instruction's data into the destination-list
		if (flags.enabled(Instruction.FLAG_SHARED_ARRAYDATA)) {
			// Allocate a new array and copy the data
			final char[] data = instr.toCharArray();
			message.set(source, timestamp, data, 0, data.length);
		}
		else {
			// The data-array is not shared and can be safely passed as-is
			message.set(source, timestamp, instr.array(), instr.offset(), instr.length());
		}
		
		messages.finishPutOp();
	}
	
	/** Flush all buffered messages (not reentrant!) */
	public void flush()
	{
		this.flush(Long.MAX_VALUE);
	}
	
	/** Flush all buffered messages below syncts (not reentrant!) */
	public void flush(final long syncts)
	{
		try {
			
			boolean repeat = true;
			long maxts;

			// Write all buffered messages with a timestamp less than
			// specified syncts, sorted according to their timestamps
			do {
				
				// Flush client-messages
				maxts = BufferedTraceWriter.maxts(smessages, syncts);
				this.write(cmessages, maxts);
				repeat &= (maxts < syncts);

				// Flush server-messages
				maxts = BufferedTraceWriter.maxts(cmessages, syncts);
				this.write(smessages, maxts);
				repeat &= (maxts < syncts);

			} while (repeat);

		}
		catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}
	
	private void write(RingBufferSPSC<Message> messages, long maxts) throws IOException
	{
		long timestamp = 0;
		Message msg = null;
		
		// Write all messages, until maxts
		while ((msg = messages.beginTakeOp()) != null) {

			timestamp = msg.getTimestamp();
			if (timestamp > maxts)
				break;
			
			try {
				// Write the message to the trace-block
				tblock.write(timestamp, msg.getDataArray(), msg.getOffset(), msg.getLength());
			}
			finally {
				messages.finishTakeOp();
			}
			
			// Safety check.
			{
				// This should never happen!
				if (timestamp < prevts) {
					String string = "Previous: " + prevts + ", Current: " + timestamp;
					throw new IllegalStateException("Invalid timestamp order! " + string);
				}
				
				prevts = timestamp;
			}
		}
	}
	
	private static long maxts(RingBufferSPSC<Message> messages, long syncts)
	{
		Message message = messages.beginTakeOp();
		if (message != null)
			return Math.min(syncts, message.getTimestamp());
		
		return syncts;
	}
	
	private static RingBufferSPSC<Message> newMessageBuffer(int size)
	{
		Message[] array = new Message[size];
		for (int i = 0; i < size; ++i)
			array[i] = new Message();
		
		return new RingBufferSPSC<Message>(array);
	}
}
