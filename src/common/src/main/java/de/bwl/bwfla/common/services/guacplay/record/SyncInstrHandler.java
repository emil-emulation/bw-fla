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

import de.bwl.bwfla.common.services.guacplay.GuacDefs.OpCode;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.SourceType;
import de.bwl.bwfla.common.services.guacplay.protocol.Instruction;
import de.bwl.bwfla.common.services.guacplay.protocol.InstructionDescription;
import de.bwl.bwfla.common.services.guacplay.protocol.InstructionHandler;

// Internal class (package-private)


/**
 * Handler for Guacamole's client-side and server-side <i>sync-</i> instructions.
 * 
 * @see <a href="http://guac-dev.org/doc/gug/protocol-reference.html#client-sync-instruction">
 *          Client-side sync-instruction
 *      </a>,
 *      <a href="http://guac-dev.org/doc/gug/protocol-reference.html#server-sync-instruction">
 *          Server-side sync-instruction
 *      </a>
 */
final class SyncInstrHandler extends InstructionHandler
{
	private final BufferedTraceWriter writer;
	
	
	/** Constructor */
	public SyncInstrHandler(BufferedTraceWriter writer)
	{
		super(OpCode.SYNC);
		
		this.writer = writer;
	}

	@Override
	public void execute(InstructionDescription desc, Instruction instruction)
	{
		// The sync-instructions with the same
		// timestamp can be sent multiple times!
		
		// Flush trace-buffer only from the server thread!
		if (desc.getSourceType() == SourceType.SERVER)
			writer.flush(desc.getTimestamp());
	}
}
