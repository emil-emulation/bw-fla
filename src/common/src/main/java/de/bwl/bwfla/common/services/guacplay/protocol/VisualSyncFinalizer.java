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

package de.bwl.bwfla.common.services.guacplay.protocol;

import de.bwl.bwfla.common.services.guacplay.GuacDefs.EventType;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.SourceType;
import de.bwl.bwfla.common.services.guacplay.events.EventSink;
import de.bwl.bwfla.common.services.guacplay.events.GuacEvent;
import de.bwl.bwfla.common.services.guacplay.events.IGuacEventListener;
import de.bwl.bwfla.common.services.guacplay.events.VisualSyncBeginEvent;
import de.bwl.bwfla.common.services.guacplay.graphics.OffscreenCanvas;
import de.bwl.bwfla.common.services.guacplay.util.FlagSet;


/**
 * A generator for the custom visual-sync instruction. It handles the
 * end of the visual-sync phase and performs the corresponding actions.
 */
public class VisualSyncFinalizer implements Runnable, IGuacEventListener
{
	// Member fields
	private final VSyncInstrGenerator vsyncgen;
	private final InstructionDescription idesc;
	private final Instruction vsync;
	private final GuacEvent endevent;
	private final InstructionSink isink;
	private final EventSink esink;
	
	// Members read/written by different threads
	private Instruction mouse;
	private long timestamp;
	
	
	/** Constructor */
	public VisualSyncFinalizer(InstructionSink isink, EventSink esink, OffscreenCanvas canvas, int typeid)
	{
		this.vsyncgen = VSyncInstrGenerator.construct(canvas, typeid, true);
		this.idesc = new InstructionDescription(SourceType.INTERNAL);
		this.vsync = new Instruction(5);
		this.endevent = new GuacEvent(EventType.VSYNC_END, this);
		this.isink = isink;
		this.esink = esink;
		this.mouse = null;
		this.timestamp = 0L;
		
		// Mark this instruction-instance as shared!
		final FlagSet flags = vsync.flags();
		flags.set(Instruction.FLAG_SHARED_INSTANCE);
		flags.set(Instruction.FLAG_SHARED_ARRAYDATA);
	}
	
	/** Set the mouse-instruction, that caused the visual-sync. */
	public void setMouseInstruction(Instruction mouse)
	{
		this.mouse = mouse;
	}

	/** Set the timestamp of the visual-sync. */
	public void setSyncTimestamp(long timestamp)
	{
		this.timestamp = timestamp;
	}
	
	@Override
	public synchronized void run()
	{
		
		int ypos = -1;
		int xpos = -1;
		try
		{
			ypos = mouse.argAsInt(1);
			xpos = mouse.argAsInt(0);
		}
		catch(InstructionParserException e)
		{
			e.printStackTrace();
			return;
		}
		
		// Generate and send the result to the sink
		vsyncgen.generate(xpos, ypos, vsync);
		idesc.setTimestamp(timestamp);
		try {
			isink.consume(idesc, vsync);   // Write vsync-instruction first
			isink.consume(idesc, mouse);   // Write deferred mouse-instruction
		}
		catch (Exception exception) {
			// Something is wrong, rethrow 
			throw new RuntimeException(exception);
		}
		
		// Signal the end of the synchronization
		esink.consume(endevent);
	}

	@Override
	public void onGuacEvent(GuacEvent event)
	{
		if (event.getType() != EventType.VSYNC_BEGIN)
			return;
		
		synchronized (this) {
			// Update the visual-sync parameters
			final VisualSyncBeginEvent vsevent = (VisualSyncBeginEvent) event;
			mouse = vsevent.getInstruction();
			timestamp = vsevent.getTimestamp();
		}
	}
}
