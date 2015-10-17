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

package de.bwl.bwfla.common.datatypes;

import java.util.ArrayList;
import java.util.List;
import de.bwl.bwfla.api.eaas.EaasWS;
import de.bwl.bwfla.common.exceptions.BWFLAException;


/**
 * A helper class for accessing the monitor-values of remote
 * processes through the {@link EaasWS} web-service APIs.
 */
public class RemoteProcessMonitor
{
	private final EaasWS eaas;
	private final String sid;
	
	
	/**
	 * Constructor.
	 * @param sid The session ID.
	 * @param eaas The webservice API.
	 */
	public RemoteProcessMonitor(String sid, EaasWS eaas)
	{
		this.eaas = eaas;
		this.sid = sid;
	}

	/**
	 * Updates the remote monitor-values.
	 * @return true when update was successful, else false.
	 */
	public boolean update()
	{
		try {
			return eaas.updateMonitorValues(sid);
		}
		catch (BWFLAException exception) {
			exception.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Returns a single monitored value.
	 * @param vid The ID of the value to return.
	 * @return The value as string or null, if it is not valid.
	 */
	public String getValue(ProcessMonitorVID vid)
	{
		try {
			return eaas.getMonitorValue(sid, vid.value());
		}
		catch (BWFLAException exception) {
			exception.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Returns a collection of specific monitored values.
	 * @param vids The IDs of the values to return.
	 * @return The values as collection of strings.
	 * @see #getValuesByRawVID(List)
	 */
	public List<String> getValuesByVID(List<ProcessMonitorVID> vids)
	{
		ArrayList<Integer> ids = new ArrayList<Integer>(vids.size());
		for (ProcessMonitorVID vid : vids)
			ids.add(vid.value());
		
		return this.getValuesByRawVID(ids);
	}
	
	/**
	 * Returns a collection of specific monitored values.
	 * @param vids The raw IDs of the values to return.
	 * @return The values as collection of strings.
	 */
	public List<String> getValuesByRawVID(List<Integer> vids)
	{
		try {
			return eaas.getMonitorValues(sid, vids);
		}
		catch (BWFLAException exception) {
			exception.printStackTrace();
			return null;
		}
	}
	
	/** Returns all monitored values. */
	public List<String> getAllValues()
	{
		try {
			return eaas.getAllMonitorValues(sid);
		}
		catch (BWFLAException exception) {
			exception.printStackTrace();
			return null;
		}
	}
	
	
	/* ==================== Helper Methods ==================== */
	
	/** Returns true when the value is not an empty string, else false. */
	public static boolean isValidValue(String value)
	{
		return !value.contentEquals(MonitorValueMap.INVALID_VALUE);
	}
	
	/** Parses and returns the specified value as time amount. */
	public long parseTimeAmountValue(String value)
	{
		return Long.parseLong(value);
	}
	
	/** Parses and returns the specified value as memory size in bytes. */
	public static long parseMemoryValue(String value)
	{
		return Long.parseLong(value);
	}
	
	/** Parses and returns the specified value as memory pointer. */
	public static long parseInstrPointerValue(String value)
	{
		return Long.parseLong(value);
	}
}
