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

import de.bwl.bwfla.common.services.guacplay.util.Barrier;
import de.bwl.bwfla.common.services.guacplay.util.ConditionVariable;


/** A worker for processing posted server-messages. */
public class ServerMessageProcessor extends VSyncedMessageProcessor
{
	private final ConditionVariable syncCondition;
	private volatile long syncTimestamp;
	
	/** Timeout to wait for synchronization with client-messages */
	private static final long TIMEOUT_ON_SYNCHRONIZE  = 500L;  // in ms 
	
	
	/** Constructor */
	public ServerMessageProcessor(String name, int msgBufferCapacity, Barrier vsyncBarrier)
	{
		super(name, msgBufferCapacity, vsyncBarrier);
		
		this.syncCondition = new ConditionVariable();
		this.syncTimestamp = Long.MIN_VALUE;
	}
	
	/** Set new timestamp for synchronization. */
	public void setSyncTimestamp(long timestamp)
	{
		syncTimestamp = timestamp;
		syncCondition.signal();
	}
	
	
	/* ========== BufferedMessageProcessor Implementation ========== */
	
	@Override
	protected void synchronize(long timestamp)
	{
		// Delay current message, until client-message processor catches up
		while (timestamp > syncTimestamp)
			syncCondition.await(TIMEOUT_ON_SYNCHRONIZE);
	}
}
