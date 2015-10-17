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

package de.bwl.bwfla.common.services.guacplay.protocol.handler;

import java.util.concurrent.TimeUnit;

import de.bwl.bwfla.common.services.guacplay.GuacDefs.EventType;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.MouseButton;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.OpCode;
import de.bwl.bwfla.common.services.guacplay.events.EventSink;
import de.bwl.bwfla.common.services.guacplay.events.GuacEvent;
import de.bwl.bwfla.common.services.guacplay.events.IGuacEventListener;
import de.bwl.bwfla.common.services.guacplay.events.SessionBeginEvent;
import de.bwl.bwfla.common.services.guacplay.events.VisualSyncBeginEvent;
import de.bwl.bwfla.common.services.guacplay.protocol.Instruction;
import de.bwl.bwfla.common.services.guacplay.protocol.InstructionDescription;
import de.bwl.bwfla.common.services.guacplay.protocol.InstructionHandler;
import de.bwl.bwfla.common.services.guacplay.protocol.InstructionSink;
import de.bwl.bwfla.common.services.guacplay.util.TimeUtils;


/**
 * Handler for Guacamole's <i>mouse-</i> instruction (Record-Version).
 * 
 * @see <a href="http://guac-dev.org/doc/gug/protocol-reference.html#mouse-instruction">
 *          Guacamole's protocol reference
 *      </a>
 */
public class MouseInstrHandlerREC extends InstructionHandler implements IGuacEventListener
{
	// Member fields
	private final InstructionSink isink;
	private final VisualSyncBeginEvent vsevent;
	private final EventSink esink;
	private long lastPressedTimestamp;
	private int lastPressedButtons;
	private int lastButtons;

	/** Mask for mouse buttons only! (excluding scroll-up and scroll-down buttons) */
	private static final int BUTTONS_MASK = MouseButton.LEFT | MouseButton.MIDDLE | MouseButton.RIGHT;
	
	/** Maximal time between two mouse clicks (in ns) */
	private static final long DOUBLE_CLICK_TIMEOUT
			= TimeUtils.convert(250L, TimeUnit.MILLISECONDS, TimeUnit.NANOSECONDS);
	
	
	/** Constructor */
	public MouseInstrHandlerREC(InstructionSink isink, EventSink esink)
	{
		super(OpCode.MOUSE);
		this.isink = isink;
		this.esink = esink;
		this.vsevent = new VisualSyncBeginEvent(this);
		this.lastPressedTimestamp = Long.MIN_VALUE;
		this.lastButtons = 0;
		this.lastPressedButtons = 0;
	}
	
	@Override
	public void execute(InstructionDescription desc, Instruction instruction) throws Exception
	{
		// Handles the following cases:
		//    1) Single-click
		//    2) Double-click
		//    3) Dragging (mouse down and move)
		
		// Get the mouse-button mask
		final int buttons = instruction.argAsInt(2) & BUTTONS_MASK;

		// Any button pressed?
		if ((buttons != 0) && (buttons != lastButtons)) {
			// Yes! Is it a double click?
			final long timestamp = desc.getTimestamp();
			if ((buttons != lastPressedButtons) || (timestamp - lastPressedTimestamp) > DOUBLE_CLICK_TIMEOUT) {
				// No, begin the synchronization
				vsevent.setIntruction(instruction.clone());
				vsevent.setTimestamp(timestamp);
				esink.consume(vsevent);

				// Update variables for next run
				lastPressedTimestamp = timestamp;
				lastPressedButtons = buttons;
				lastButtons = buttons;
				
				return;  // Defer writing of this mouse-click!
			}
			else {
				// We have a double-click!
				// Reset buttons for next run.
				lastPressedButtons = 0;
			}
		}
		
		lastButtons = buttons;  // Update
		
		// Write the instruction to sink
		isink.consume(desc, instruction);
	}

	@Override
	public void onGuacEvent(GuacEvent event)
	{
		if (event.getType() != EventType.SESSION_BEGIN)
			return;
		
		// Initialize the timestamp here!
		SessionBeginEvent sbevent = (SessionBeginEvent) event;
		lastPressedTimestamp = sbevent.getTimestamp();
	}
}
