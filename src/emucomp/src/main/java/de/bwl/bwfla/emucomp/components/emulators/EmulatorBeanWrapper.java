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

package de.bwl.bwfla.emucomp.components.emulators;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.activation.DataHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.bwl.bwfla.common.datatypes.AbstractCredentials;
import de.bwl.bwfla.common.datatypes.ConnectionType;
import de.bwl.bwfla.common.datatypes.ProcessMonitorVID;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.exceptions.ConcurrentAccessException;
import de.bwl.bwfla.common.interfaces.EmulatorComponent;
import de.bwl.bwfla.common.utils.ConcurrentAccessGuard;
import de.bwl.bwfla.emucomp.components.AbstractEaasComponent;


/** Wrapper for thread-unsafe EmulatorBeans, used to prevent concurrent access. */
public class EmulatorBeanWrapper extends AbstractEaasComponent implements EmulatorComponent
{
	private static final int DESTROY_REQUESTED = 2;
	
	private final ConcurrentAccessGuard guard;
	private final EmulatorBean emubean;

	public EmulatorBeanWrapper(EmulatorBean bean)
	{
		this.guard = new ConcurrentAccessGuard();
		this.emubean = bean;
	}
	
	@Override
	public void setSessionId(String id)
	{
		super.setSessionId(id);
		emubean.setSessionId(id);
	}
	
	
	/* =============== Public EmulatorBean API =============== */
	
	public Map<String, IConnector> getViewConnectors()
	{
		try {
			guard.prolog("EmulatorBean.getViewConnectors()");
			try {
				return emubean.getViewConnectors();
			}
			finally {
				this.epilog();
			}
		}
		catch (Exception exception) {
			exception.printStackTrace();
			return null;
		}
	}
	
	
	/* ========== ClusterComponent Implemenatation ========== */
	
	@Override
	public void initialize(String config) throws BWFLAException
	{
		guard.prolog("EmulatorBean.initialize()");

		try {
			emubean.initialize(config);
		}
		finally {
			this.epilog();
		}
	}

	@Override
	public void destroy() throws BWFLAException
	{
		if (!guard.force(0, DESTROY_REQUESTED, "EmulatorBean.destroy()")) {
			Logger log = LoggerFactory.getLogger(EmulatorBeanWrapper.class);
			log.warn("Deferring a call to EmulatorBean.destroy() for session {}.", emubean.getSessionId());
			return;  // Other method is still in-flight!
		}
		
		try {
			emubean.destroy();
		}
		finally {
			guard.epilog();
		}
	}

	
	/* ========== EmulatorComponent Implementation ========== */
	
	@Override
	public void start()
	{
		try {
			guard.prolog("EmulatorBean.start()");
		}
		catch (ConcurrentAccessException exception) {
			exception.printStackTrace();
			return;
		}
		
		try {
			emubean.start();
		}
		finally {
			this.epilog();
		}
	}

	@Override
	public void stop()
	{
		try {
			guard.prolog("EmulatorBean.stop()");
		}
		catch (ConcurrentAccessException exception) {
			exception.printStackTrace();
			return;
		}
		
		try {
			emubean.stop();
		}
		finally {
			this.epilog();
		}
	}

	@Override
	public String saveEnvironment(String host, String name, String type) throws BWFLAException
	{
		guard.prolog("EmulatorBean.saveEnvironment()");

		try {
			return emubean.saveEnvironment(host, name, type);
		}
		finally {
			this.epilog();
		}
	}

	@Override
	public int attachMedium(DataHandler data, String mediumType) throws BWFLAException
	{
		guard.prolog("EmulatorBean.attachMedium()");

		try {
			return emubean.attachMedium(data, mediumType);
		}
		finally {
			this.epilog();
		}
	}

	@Override
	public DataHandler detachMedium(int containerId) throws BWFLAException
	{
		guard.prolog("EmulatorBean.detachMedium()");

		try {
			return emubean.detachMedium(containerId);
		}
		finally {
			this.epilog();
		}
	}

	@Override
	public String getControlURL(ConnectionType type, AbstractCredentials credentials) throws BWFLAException
	{
		guard.prolog("EmulatorBean.getControlURL()");

		try {
			return emubean.getControlURL(type, credentials);
		}
		finally {
			this.epilog();
		}
	}

	@Override
	public String getRuntimeConfiguration() throws BWFLAException
	{
		guard.prolog("EmulatorBean.getRuntimeConfiguration()");

		try {
			return emubean.getRuntimeConfiguration();
		}
		finally {
			this.epilog();
		}
	}

	@Override
	public Set<String> getColdplugableDrives()
	{
		try {
			guard.prolog("EmulatorBean.getColdplugableDrives()");
		}
		catch (ConcurrentAccessException exception) {
			exception.printStackTrace();
			return null;
		}
		
		try {
			return emubean.getColdplugableDrives();
		}
		finally {
			this.epilog();
		}
	}

	@Override
	public Set<String> getHotplugableDrives()
	{
		try {
			guard.prolog("EmulatorBean.getHotplugableDrives()");
		}
		catch (ConcurrentAccessException exception) {
			exception.printStackTrace();
			return null;
		}
		
		try {
			return emubean.getHotplugableDrives();
		}
		finally {
			this.epilog();
		}
	}

	@Override
	public String getEmulatorState()
	{
		// This method can be called concurrently!
		return emubean.getEmulatorState();
	}

	@Override
	public void connectNic(String hwaddress, String endpoint) throws BWFLAException
	{
		guard.prolog("EmulatorBean.connectNic()");

		try {
			emubean.connectNic(hwaddress, endpoint);
		}
		finally {
			this.epilog();
		}
	}

	@Override
	public boolean prepareSessionRecorder() throws BWFLAException
	{
		guard.prolog("EmulatorBean.prepareSessionRecorder()");

		try {
			return emubean.prepareSessionRecorder();
		}
		finally {
			this.epilog();
		}
	}

	@Override
	public void startSessionRecording() throws BWFLAException
	{
		guard.prolog("EmulatorBean.startSessionRecording()");

		try {
			emubean.startSessionRecording();
		}
		finally {
			this.epilog();
		}
	}

	@Override
	public void stopSessionRecording() throws BWFLAException
	{
		guard.prolog("EmulatorBean.stopSessionRecording()");

		try {
			emubean.stopSessionRecording();
		}
		finally {
			this.epilog();
		}
	}

	@Override
	public boolean isRecordModeEnabled() throws BWFLAException
	{
		// This method can be called concurrently!
		return emubean.isRecordModeEnabled();
	}

	@Override
	public void addActionFinishedMark() throws BWFLAException
	{
		guard.prolog("EmulatorBean.addActionFinishedMark()");

		try {
			emubean.addActionFinishedMark();
		}
		finally {
			this.epilog();
		}
	}

	@Override
	public void defineTraceMetadataChunk(String tag, String comment) throws BWFLAException
	{
		guard.prolog("EmulatorBean.defineTraceMetadataChunk()");

		try {
			emubean.defineTraceMetadataChunk(tag, comment);
		}
		finally {
			this.epilog();
		}
	}

	@Override
	public void addTraceMetadataEntry(String ctag, String key, String value) throws BWFLAException
	{
		guard.prolog("EmulatorBean.addTraceMetadataEntry()");

		try {
			emubean.addTraceMetadataEntry(ctag, key, value);
		}
		finally {
			this.epilog();
		}
	}

	@Override
	public String getSessionTrace() throws BWFLAException
	{
		guard.prolog("EmulatorBean.getSessionTrace()");

		try {
			return emubean.getSessionTrace();
		}
		finally {
			this.epilog();
		}
	}

	@Override
	public boolean prepareSessionPlayer(String trace, boolean headless) throws BWFLAException
	{
		guard.prolog("EmulatorBean.prepareSessionPlayer()");

		try {
			return emubean.prepareSessionPlayer(trace, headless);
		}
		finally {
			this.epilog();
		}
	}

	@Override
	public int getSessionPlayerProgress() throws BWFLAException
	{
		guard.prolog("EmulatorBean.getSessionPlayerProgress()");

		try {
			return emubean.getSessionPlayerProgress();
		}
		finally {
			this.epilog();
		}
	}

	@Override
	public boolean isReplayModeEnabled() throws BWFLAException
	{
		// This method can be called concurrently!
		return emubean.isReplayModeEnabled();
	}

	@Override
	public boolean updateMonitorValues() throws BWFLAException
	{
		guard.prolog("EmulatorBean.updateMonitorValues()");

		try {
			return emubean.updateMonitorValues();
		}
		finally {
			this.epilog();
		}
	}

	@Override
	public String getMonitorValue(ProcessMonitorVID id) throws BWFLAException
	{
		guard.prolog("EmulatorBean.getMonitorValue()");

		try {
			return emubean.getMonitorValue(id);
		}
		finally {
			this.epilog();
		}
	}

	@Override
	public List<String> getMonitorValues(Collection<ProcessMonitorVID> ids) throws BWFLAException
	{
		guard.prolog("EmulatorBean.getMonitorValues()");

		try {
			return emubean.getMonitorValues(ids);
		}
		finally {
			this.epilog();
		}
	}

	@Override
	public List<String> getAllMonitorValues() throws BWFLAException
	{
		guard.prolog("EmulatorBean.getAllMonitorValues()");

		try {
			return emubean.getAllMonitorValues();
		}
		finally {
			this.epilog();
		}
	}

	@Override
	public void takeScreenshot() throws BWFLAException
	{
		guard.prolog("EmulatorBean.takeScreenshot()");

		try {
			emubean.takeScreenshot();
		}
		finally {
			this.epilog();
		}
	}

	@Override
	public DataHandler getNextScreenshot() throws BWFLAException
	{
		guard.prolog("EmulatorBean.getNextScreenshot()");

		try {
			return emubean.getNextScreenshot();
		}
		finally {
			this.epilog();
		}
	}
	
	@Override
	public int changeMedium(int containerId, String objReference) throws BWFLAException
	{
		guard.prolog("EmulatorBean.changeMedium()");

		try {
			return emubean.changeMedium(containerId, objReference);
		}
		finally {
			this.epilog();
		}
	}
	
	private void epilog()
	{
		if (guard.epilog() == DESTROY_REQUESTED) {
			try {
				Logger log = LoggerFactory.getLogger(EmulatorBeanWrapper.class);
				log.warn("Calling deferred method EmulatorBean.destroy() for session {}.", emubean.getSessionId());
				this.destroy();
			}
			catch (BWFLAException exception) {
				exception.printStackTrace();
			}
		}
	}
}
