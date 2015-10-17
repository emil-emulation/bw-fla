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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.io.GuacamoleWriter;

import de.bwl.bwfla.common.services.guacplay.GuacDefs.EventType;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.ExtOpCode;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.MetadataTag;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.OpCode;
import de.bwl.bwfla.common.services.guacplay.events.EventSink;
import de.bwl.bwfla.common.services.guacplay.events.GuacEvent;
import de.bwl.bwfla.common.services.guacplay.graphics.OffscreenCanvas;
import de.bwl.bwfla.common.services.guacplay.graphics.ScreenRegionList;
import de.bwl.bwfla.common.services.guacplay.io.BlockReader;
import de.bwl.bwfla.common.services.guacplay.io.Metadata;
import de.bwl.bwfla.common.services.guacplay.io.MetadataChunk;
import de.bwl.bwfla.common.services.guacplay.io.TraceBlockReader;
import de.bwl.bwfla.common.services.guacplay.io.TraceFile;
import de.bwl.bwfla.common.services.guacplay.io.TraceFileReader;
import de.bwl.bwfla.common.services.guacplay.net.GuacTunnel;
import de.bwl.bwfla.common.services.guacplay.net.PlayerSocket;
import de.bwl.bwfla.common.services.guacplay.net.PlayerTunnel;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.ActfinInstrHandler;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.ArcInstrHandler;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.CFillInstrHandler;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.CStrokeInstrHandler;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.CloseInstrHandler;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.CurveInstrHandler;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.DisposeInstrHandler;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.InstructionSkipper;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.KeyInstrHandlerPLAY;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.LineInstrHandler;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.PngInstrHandlerPLAY;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.RectInstrHandler;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.SizeInstrHandler;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.StartInstrHandler;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.SupdInstrHandler;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.InstructionTrap;
import de.bwl.bwfla.common.services.guacplay.protocol.handler.VSyncInstrHandler;
import de.bwl.bwfla.common.services.guacplay.record.SessionRecorder;
import de.bwl.bwfla.common.services.guacplay.replay.ServerMessageProcessor;
import de.bwl.bwfla.common.services.guacplay.util.ICharArrayConsumer;
import de.bwl.bwfla.common.utils.ProcessMonitor;


public class SessionPlayer implements ICharArrayConsumer
{
	/** Logger instance. */
	private final Logger log = LoggerFactory.getLogger(SessionPlayer.class);
	
	// Member fields
	private final GuacTunnel emutunnel;
	private final PlayerTunnel tunnel;
	private final EventSink esink;
	private final OffscreenCanvas canvas;
	private final TraceBlockReader tblock;
	private final TraceFileProcessor traceFileProcessor;
	private final ServerMessageProcessor serverMsgProcessor;
	private TraceFile tfile;
	private TraceFileReader treader;
	private IReplayProgress progress;
	private volatile State state;
	
	/** Possible States */
	private static enum State
	{
		READY,
		PREPARED,
		FINISHED
	}
	
	
	/** Constructor */
	public SessionPlayer(String id, GuacTunnel emutunnel, ProcessMonitor monitor)
	{
		this(id, emutunnel, null, monitor);
	}
	
	/** Constructor */
	public SessionPlayer(String id, GuacTunnel emutunnel, PlayerTunnel tunnel, ProcessMonitor monitor)
	{
		this.emutunnel = emutunnel;
		this.tunnel = tunnel;
		this.esink = new EventSink(2);
		this.canvas = new OffscreenCanvas();
		this.tblock = new TraceBlockReader();
		this.tfile = null;
		this.treader = null;
		this.progress = null;
		
		PlayerSocket socket = null;
		if (tunnel != null) {
			socket = (PlayerSocket) tunnel.getSocket();
			socket.setWriterOutput(this);
		}
		
		id = id.toUpperCase();
		
		this.serverMsgProcessor = new ServerMessageProcessor("SMP-" + id, emutunnel.getGuacReader(), socket);
		this.traceFileProcessor = new TraceFileProcessor("TFP-" + id, tblock);
		
		final InstructionForwarder forwarder = new InstructionForwarder(this);
		final ScreenRegionList updates = new ScreenRegionList(128);
		
		// Construct the handlers for client messages
		{
			VSyncInstrHandler vsyncInstrHandler = new VSyncInstrHandler(canvas, socket, esink);
			SupdInstrHandler supdInstrHandler = new SupdInstrHandler(updates, socket);
			esink.addConsumer(vsyncInstrHandler);
			esink.addConsumer(supdInstrHandler);
			
			traceFileProcessor.addInstructionHandler(OpCode.KEY, new KeyInstrHandlerPLAY(this, esink));
			traceFileProcessor.addInstructionHandler(OpCode.MOUSE, forwarder);
			traceFileProcessor.addInstructionHandler(ExtOpCode.VSYNC, vsyncInstrHandler);
			traceFileProcessor.addInstructionHandler(ExtOpCode.SCREEN_UPDATE, supdInstrHandler);
			traceFileProcessor.addInstructionHandler(ExtOpCode.ACTION_FINISHED, new ActfinInstrHandler(monitor));
		}
		
		// Construct the handlers for server messages
		{
			InstructionSkipper iskipper = new InstructionSkipper();
			InstructionTrap itrap = new InstructionTrap();
			
			PngInstrHandlerPLAY pngInstrHandler = new PngInstrHandlerPLAY(canvas, updates, null);
			esink.addConsumer(pngInstrHandler);
			
			SizeInstrHandler sizeInstrHandler = new SizeInstrHandler(2);
			sizeInstrHandler.addListener(pngInstrHandler);
			sizeInstrHandler.addListener(canvas);
			
			// Add implemented handlers
			serverMsgProcessor.addInstructionHandler(OpCode.ARC, new ArcInstrHandler(canvas));
			serverMsgProcessor.addInstructionHandler(OpCode.CFILL, new CFillInstrHandler(canvas));
			serverMsgProcessor.addInstructionHandler(OpCode.CLOSE, new CloseInstrHandler(canvas));
			serverMsgProcessor.addInstructionHandler(OpCode.CSTROKE, new CStrokeInstrHandler(canvas));
			serverMsgProcessor.addInstructionHandler(OpCode.CURVE, new CurveInstrHandler(canvas));
			serverMsgProcessor.addInstructionHandler(OpCode.DISPOSE, new DisposeInstrHandler(canvas));
			serverMsgProcessor.addInstructionHandler(OpCode.LINE, new LineInstrHandler(canvas));
			serverMsgProcessor.addInstructionHandler(OpCode.PNG, pngInstrHandler);
			serverMsgProcessor.addInstructionHandler(OpCode.RECT, new RectInstrHandler(canvas));
			serverMsgProcessor.addInstructionHandler(OpCode.SIZE, sizeInstrHandler);
			serverMsgProcessor.addInstructionHandler(OpCode.START, new StartInstrHandler(canvas));
			serverMsgProcessor.addInstructionHandler(OpCode.SYNC, forwarder);
			
			// Mark instructions to ignore
			serverMsgProcessor.addInstructionHandler(OpCode.ARGS, iskipper);
			serverMsgProcessor.addInstructionHandler(OpCode.CURSOR, iskipper);
			serverMsgProcessor.addInstructionHandler(OpCode.NAME, iskipper);
			serverMsgProcessor.addInstructionHandler(OpCode.AUDIO, iskipper);
			serverMsgProcessor.addInstructionHandler(OpCode.VIDEO, iskipper);
			serverMsgProcessor.addInstructionHandler(OpCode.ACK, iskipper);
			serverMsgProcessor.addInstructionHandler(OpCode.BLOB, iskipper);
			serverMsgProcessor.addInstructionHandler(OpCode.END, iskipper);
			serverMsgProcessor.addInstructionHandler(OpCode.FILE, iskipper);
			serverMsgProcessor.addInstructionHandler(OpCode.NEST, iskipper);
			serverMsgProcessor.addInstructionHandler(OpCode.PIPE, iskipper);
			serverMsgProcessor.addInstructionHandler(OpCode.NEST, iskipper);
			serverMsgProcessor.addInstructionHandler(OpCode.READY, iskipper);
			
			// Mark important, but not implemented, handlers
			serverMsgProcessor.addInstructionHandler(OpCode.CLIP, itrap);
			serverMsgProcessor.addInstructionHandler(OpCode.COPY, itrap);
			serverMsgProcessor.addInstructionHandler(OpCode.DISTORT, itrap);
			serverMsgProcessor.addInstructionHandler(OpCode.IDENTITY, itrap);
			serverMsgProcessor.addInstructionHandler(OpCode.LFILL, itrap);
			serverMsgProcessor.addInstructionHandler(OpCode.LSTROKE, itrap);
			serverMsgProcessor.addInstructionHandler(OpCode.MOVE, itrap);
			serverMsgProcessor.addInstructionHandler(OpCode.POP, itrap);
			serverMsgProcessor.addInstructionHandler(OpCode.PUSH, itrap);
			serverMsgProcessor.addInstructionHandler(OpCode.RESET, itrap);
			serverMsgProcessor.addInstructionHandler(OpCode.SET, itrap);
			serverMsgProcessor.addInstructionHandler(OpCode.SHADE, itrap);
			serverMsgProcessor.addInstructionHandler(OpCode.TRANSFER, itrap);
			serverMsgProcessor.addInstructionHandler(OpCode.TRANSFORM, itrap);
		}
		
		this.state = State.READY;
	}
	
	/** Returns the path of the replayed trace-file. */
	public Path getTraceFilePath()
	{
		if (tfile == null)
			throw new IllegalStateException("The trace-file was not set!");
		
		return tfile.getPath();
	}
	
	/** Returns the current progress in percent. */
	public int getProgress() throws IOException
	{
		return (progress != null) ? progress.getCurrentValue() : 0;
	}
	
	/** Returns the trace-file's metadata entries. */
	public Metadata getTraceMetadata()
	{
		if (tfile == null)
			throw new IllegalStateException("The trace-file was not set!");
		
		return tfile.getMetadata();
	}
	
	/** Returns true, when this player is running, else false. */
	public boolean isPlaying()
	{
		return traceFileProcessor.isRunning();
	}
	
	/** Returns true, when this player finished, else false. */
	public boolean isFinished()
	{
		return (state == State.FINISHED);
	}
	
	/** Prepare the trace-file for reading. */
	public void prepare(Path path) throws IOException
	{
		if (state != State.READY)
			throw new IllegalStateException("Attempt to call SessionPlayer.prepare() multiple times!");
		
		// Create a reader using the specified path
		tfile = new TraceFile(path, StandardCharsets.UTF_8);
		treader = tfile.newBufferedReader();
		treader.prepare();
		treader.begin(tblock);

		log.info("Replaying from trace-file:  {}", path.toString());
		
		// Initialize the progress handler
		MetadataChunk chunk = tfile.getMetadata().getChunk(MetadataTag.INTERNAL);
		if (chunk.containsKey(SessionRecorder.MDKEY_NUM_TRACE_ENTRIES)) {
			int numEntriesMax = chunk.getAsInt(SessionRecorder.MDKEY_NUM_TRACE_ENTRIES);
			progress = new EntryBasedProgress(traceFileProcessor, numEntriesMax);
		}
		else progress = new SizeBasedProgress(tblock); 
		
		// Start the processors now
		serverMsgProcessor.start();
		traceFileProcessor.start();
		
		state = State.PREPARED;
	}
	
	/** Finish the replaying-process and terminate all message-processors. */
	public void finish() throws IOException, GuacamoleException
	{
		if (state != State.PREPARED) {
			log.warn("Skip finishing an unprepared player!");
			return;
		}
		
		// Notify about the termination of the processor
		esink.consume(new GuacEvent(EventType.TERMINATION, this));
		
		// Request the termination of server-processor, then
		// trace-processor and wait for both to finish!
		serverMsgProcessor.terminate(false);
		traceFileProcessor.terminate(true);
		serverMsgProcessor.terminate(true);
		
		// Finish reading and close!
		if (tfile != null) {
			treader.finish();
			treader.close();
		}
		
		// Close also the tunnels!
		if (tunnel != null && tunnel.isOpen())
			tunnel.close();
		
		state = State.FINISHED;
	}
	
	@Override
	public void consume(char[] data, int offset, int length) throws Exception
	{
		if (log.isDebugEnabled())
			log.debug("Instruction consumed: {}", new String(data, offset, length));
		
		try {
			GuacamoleWriter writer = emutunnel.acquireWriter();
			writer.write(data, offset, length);
		}
		finally {
			emutunnel.releaseWriter();
		}
	}
}


interface IReplayProgress
{
	/** Returns the current progress in percent. */
	int getCurrentValue() throws IOException;
}


final class SizeBasedProgress implements IReplayProgress
{
	private final BlockReader block;
	private final float blockSize;
	
	public SizeBasedProgress(BlockReader block) throws IOException
	{
		this.block = block;
		this.blockSize = (float) block.remaining();
	}
	
	@Override
	public int getCurrentValue() throws IOException
	{
		float numBytesRead = blockSize - (float) block.remaining();
		float fraction = numBytesRead / blockSize;
		return (int) (100.0F * fraction);
	}
}


final class EntryBasedProgress implements IReplayProgress
{
	private final TraceFileProcessor processor;
	private final int numEntriesMax;
	
	public EntryBasedProgress(TraceFileProcessor processor, int numEntriesMax)
	{
		this.processor = processor;
		this.numEntriesMax = numEntriesMax;
	}
	
	@Override
	public int getCurrentValue() throws IOException
	{
		int curnum = processor.getNumEntriesRead();
		return (curnum * 100) / numEntriesMax;
	}
}
