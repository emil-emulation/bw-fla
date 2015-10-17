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

package de.bwl.bwfla.common.services.guacplay.net;

import java.io.Writer;

import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.GuacamoleServerException;
import org.glyptodon.guacamole.io.GuacamoleReader;
import org.glyptodon.guacamole.io.GuacamoleWriter;
import org.glyptodon.guacamole.net.GuacamoleSocket;
import org.glyptodon.guacamole.protocol.GuacamoleInstruction;
import de.bwl.bwfla.common.services.guacplay.util.CharArrayWrapper;
import de.bwl.bwfla.common.services.guacplay.util.ConditionVariable;
import de.bwl.bwfla.common.services.guacplay.util.ICharArrayConsumer;
import de.bwl.bwfla.common.services.guacplay.util.RingBufferSPSC;


/** A custom socket for the {@link PlayerTunnel}. */
public final class PlayerSocket implements GuacamoleSocket
{
	// Member fields
	private final MessageReader reader;
	private final Forwarder forwarder;
	private GuacamoleWriter writer;
	private boolean opened;
	
	/** A dummy-writer instance.  */
	private static final DisabledWriter DISABLED_WRITER = new DisabledWriter();
	
	
	/** Constructor */
	public PlayerSocket(IGuacInterceptor interceptor, int msgBufferCapacity)
	{
		this.reader = new MessageReader(interceptor, msgBufferCapacity);
		this.forwarder = new Forwarder();
		this.writer = DISABLED_WRITER;
		this.opened = true;
	}
	
	/** Post a new message for the socket's reader. */
	public void post(char[] data, int offset, int length)
	{
		reader.put(data, offset, length);
	}
	
	/** Disable the writing through this socket. */
	public void disableWriting()
	{
		writer = DISABLED_WRITER;
	}
	
	/** Enable the writing through this socket. */
	public void enableWriting()
	{
		writer = forwarder;
	}
	
	/** Set the output-destination for the socket's writer. */
	public void setWriterOutput(ICharArrayConsumer output)
	{
		forwarder.setOutput(output);
	}
	
	
	/* =============== GuacamoleSocket Implementation =============== */
	
	@Override
	public GuacamoleWriter getWriter()
	{
		return writer;
	}
	
	@Override
	public GuacamoleReader getReader()
	{
		return reader;
	}

	@Override
	public boolean isOpen()
	{
		return opened;
	}

	@Override
	public void close() throws GuacamoleException
	{
		reader.close();
		opened = false;
	}
}


/** Internal class for reading from a message queue. */
final class MessageReader implements IGuacReader
{
	// Member fields
	private final RingBufferSPSC<CharArrayWrapper> messages;
	private final IGuacInterceptor interceptor;
	private final ConditionVariable condition;
	private volatile boolean closed;
	
	/** A timeout for waiting on empty/full queue. */
	private static final long RETRY_TIMEOUT = 500L;
	
	
	/** Constructor */
	public MessageReader(IGuacInterceptor interceptor, int msgBufferCapacity)
	{
		final CharArrayWrapper[] entries = new CharArrayWrapper[msgBufferCapacity];
		for (int i = 0; i < msgBufferCapacity; ++i)
			entries[i] = new CharArrayWrapper();
		
		this.messages = new RingBufferSPSC<CharArrayWrapper>(entries);
		this.interceptor = interceptor;
		this.condition = new ConditionVariable();
		this.closed = false;
	}
	
	/** Add a new message to the queue. */
	public void put(char[] data, int offset, int length)
	{
		CharArrayWrapper message = null;
		
		// Retry until the message can be added!
		while ((message = messages.beginPutOp()) == null)
			condition.await(RETRY_TIMEOUT);
		
		message.set(data, offset, length);
		final int count = messages.finishPutOp();
		if (count == 1)
			condition.signal();  // Was empty before!
	}
	
	/** Mark this reader as closed. */
	public void close()
	{
		closed = true;
		condition.signalAll();
	}
	
	
	/* =============== IGuacReader Implementation =============== */
	
	@Override
	public IGuacInterceptor getInterceptor()
	{
		return interceptor;
	}
	
	@Override
	public boolean available() throws GuacamoleException
	{
		return !messages.isEmpty();
	}

	@Override
	public boolean readInto(Writer output) throws GuacamoleException
	{
		CharArrayWrapper message = null;
		
		// Read the next message
		while ((message = messages.beginTakeOp()) == null) {
			condition.await(RETRY_TIMEOUT);
			if (closed)
				return false;  // Nothing read!
		}
		
		// Handle the data...
		try {
			// Write the message directly into the output, when not dropped.
			if ((interceptor == null) || interceptor.onServerMessage(message))
				output.write(message.array(), message.offset(), message.length());
		}
		catch (Exception exception) {
			// Something is broken, rethrow
			if (exception instanceof GuacamoleException)
				throw (GuacamoleException) exception;
			else throw new GuacamoleServerException(exception);
		}
		finally {
			// Signal, that a new entry can be added!
			messages.finishTakeOp();
			condition.signal();
		}

		return true;
	}
	
	@Override
	public GuacamoleInstruction readInstruction() throws GuacamoleException
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public char[] read() throws GuacamoleException
	{
		throw new UnsupportedOperationException();
	}
}


/** Special writer, that forwards everything to the registered {@link ICharArrayConsumer}. */
final class Forwarder implements GuacamoleWriter
{
	private ICharArrayConsumer output;
	
	/** Set the output-consumer. */
	public void setOutput(ICharArrayConsumer output)
	{
		this.output = output;
	}
	
	
	/* =============== GuacamoleWriter Implementation =============== */
	
	@Override
	public void write(char[] data) throws GuacamoleException
	{
		try {
			output.consume(data, 0, data.length);
		}
		catch (Exception exception) {
			throw new GuacamoleServerException(exception);
		}
	}

	@Override
	public void write(char[] data, int offset, int length) throws GuacamoleException
	{
		try {
			output.consume(data, offset, length);
		}
		catch (Exception exception) {
			throw new GuacamoleServerException(exception);
		}
	}

	@Override
	public void writeInstruction(GuacamoleInstruction instruction) throws GuacamoleException
	{
		this.write(instruction.toString().toCharArray());
	}
}


/** Special writer, that does nothing. */
final class DisabledWriter implements GuacamoleWriter
{
	@Override
	public void write(char[] data) throws GuacamoleException
	{
		// Do nothing!
	}

	@Override
	public void write(char[] data, int offset, int length) throws GuacamoleException
	{
		// Do nothing!
	}

	@Override
	public void writeInstruction(GuacamoleInstruction instruction) throws GuacamoleException
	{
		// Do nothing!
	}
}
