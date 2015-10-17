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

package de.bwl.bwfla.workflows.beans.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.bwl.bwfla.api.eaas.EaasWS;


public class RemoteSessionRecorder
{
	/** Logger instance. */
	private final Logger log = LoggerFactory.getLogger(RemoteSessionRecorder.class);

	// Member fields
	private final EaasWS eaas;
	private final String sid;
	
	
	public RemoteSessionRecorder(EaasWS eaas, String sid)
	{
		if (eaas == null)
			throw new IllegalArgumentException("Invalid EaasWS service specified!");
		
		if (sid == null || sid.isEmpty())
			throw new IllegalArgumentException("Invalid SessionID specified!");
		
		this.eaas = eaas;
		this.sid = sid;
	}

	public boolean prepare()
	{
		log.info("Initializing remote session-recording service");
		try {
			return eaas.prepareSessionRecorder(sid);
		}
		catch (Exception exception) {
			exception.printStackTrace();
			return false;
		}
	}
	
	public boolean start()
	{
		log.info("Starting remote session-recording");
		try {
			eaas.startSessionRecording(sid);
			return true;
		}
		catch (Exception exception) {
			exception.printStackTrace();
			return false;
		}
	}
	
	public boolean stop()
	{
		log.info("Stopping remote session-recording");
		try {
			eaas.stopSessionRecording(sid);
			return true;
		}
		catch (Exception exception) {
			exception.printStackTrace();
			return false;
		}
	}
	
	public boolean isRecordModeEnabled()
	{
		try {
			return eaas.isRecordModeEnabled(sid);
		}
		catch (Exception exception) {
			exception.printStackTrace();
			return false;
		}
	}
	
	public void addActionFinishedMark()
	{
		try {
			eaas.addActionFinishedMark(sid);
		}
		catch (Exception exception) {
			exception.printStackTrace();
		}
	}
	
	public boolean defineMetadataChunk(String tag, String comment)
	{
		try {
			eaas.defineTraceMetadataChunk(sid, tag, comment);
			return true;
		}
		catch (Exception exception) {
			exception.printStackTrace();
			return false;
		}
	}
	
	public boolean addMetadataEntry(String ctag, String key, String value)
	{
		try {
			eaas.addTraceMetadataEntry(sid, ctag, key, value);
			return true;
		}
		catch (Exception exception) {
			exception.printStackTrace();
			return false;
		}
	}
	
	public String getSessionTrace()
	{
		try {
			return eaas.getSessionTrace(sid);
		}
		catch (Exception exception) {
			exception.printStackTrace();
			return null;
		}
	}
}
