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

package de.bwl.bwfla.common.interfaces;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.activation.DataHandler;
import de.bwl.bwfla.common.datatypes.AbstractCredentials;
import de.bwl.bwfla.common.datatypes.ConnectionType;
import de.bwl.bwfla.common.datatypes.ProcessMonitorVID;
import de.bwl.bwfla.common.exceptions.BWFLAException;


public interface EmulatorComponentWS
{
	/* =============== Emulator API =============== */
	public int changeMedium(String sessionId, int containerId, String objReference) throws BWFLAException;
	public int attachMedium(String sessionId, DataHandler data, String mediaType) throws BWFLAException;
	public DataHandler detachMedium(String sessionId, int handle) throws BWFLAException;
	public String getControlURL(String sessionId, ConnectionType type, AbstractCredentials credentials) throws BWFLAException;
	public void start(String sessionId);
	public void stop(String sessionId);
    public String getRuntimeConfiguration(String sessionId) throws BWFLAException;
    public Set<String> getColdplugableDrives(String sessionId);
	public Set<String> getHotplugableDrives(String sessionId);
	public String saveEnvironment(String sessionId, String wsHost, String name, String type) throws BWFLAException;
	public String getEmulatorState(String sessionId) throws BWFLAException;
	public void connectNic(String sessionId, String hwaddress, String endpoint) throws BWFLAException;
	
	
	/* =============== Session recording API =============== */
	public boolean prepareSessionRecorder(String sessionId) throws BWFLAException;
	public void startSessionRecording(String sessionId) throws BWFLAException;
	public void stopSessionRecording(String sessionId) throws BWFLAException;
	public boolean isRecordModeEnabled(String sessionId) throws BWFLAException;
	public void addActionFinishedMark(String sessionId) throws BWFLAException;
	public void defineTraceMetadataChunk(String sessionId, String tag, String comment) throws BWFLAException;
	public void addTraceMetadataEntry(String sessionId, String ctag, String key, String value) throws BWFLAException;
	public String getSessionTrace(String sessionId) throws BWFLAException;
	
	
	/* =============== Session replay API =============== */
	public boolean prepareSessionPlayer(String sessionId, String trace, boolean headless) throws BWFLAException;
	public int getSessionPlayerProgress(String sessionId) throws BWFLAException;
	public boolean isReplayModeEnabled(String sessionId) throws BWFLAException;
	
	
	/* ==================== Monitoring API ==================== */
	public boolean updateMonitorValues(String sessionId) throws BWFLAException;
	public String getMonitorValue(String sessionId, ProcessMonitorVID id) throws BWFLAException;
	public List<String> getMonitorValues(String sessionId, Collection<ProcessMonitorVID> ids) throws BWFLAException;
	public List<String> getAllMonitorValues(String sessionId) throws BWFLAException;
	
	
	/* ==================== Screenshot API ==================== */
	public void takeScreenshot(String sessionId) throws BWFLAException;
	public DataHandler getNextScreenshot(String sessionId) throws BWFLAException;
}