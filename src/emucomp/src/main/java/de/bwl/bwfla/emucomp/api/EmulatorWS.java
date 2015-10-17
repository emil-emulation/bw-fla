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

package de.bwl.bwfla.emucomp.api;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.activation.DataHandler;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.ws.soap.MTOM;
import de.bwl.bwfla.common.datatypes.AbstractCredentials;
import de.bwl.bwfla.common.datatypes.ConnectionType;
import de.bwl.bwfla.common.datatypes.ProcessMonitorVID;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.interfaces.EmulatorComponent;
import de.bwl.bwfla.common.interfaces.EmulatorComponentWS;
import de.bwl.bwfla.emucomp.conf.EmucompSingleton;



@MTOM
@Stateless
@WebService(targetNamespace="http://bwfla.bwl.de/api/emucomp")
public class EmulatorWS implements EmulatorComponentWS
{	
	@Override
	public void start(String sessionId)
	{
		final EmulatorComponent emul = EmucompSingleton.getComponent(sessionId, EmulatorComponent.class);
		
		Runnable task = new Runnable()
		{
			@Override
			public void run()
			{
				emul.start();
			}
		};
		
		EmucompSingleton.executor.submit(task);
	}
	
	@Override
	public void stop(String sessionId)
	{
		final EmulatorComponent emul = EmucompSingleton.getComponent(sessionId, EmulatorComponent.class);
		
		Runnable task = new Runnable()
		{
			@Override
			public void run()
			{
				emul.stop();
			}
		};
		
		EmucompSingleton.executor.submit(task);
	}
	
	@Override
	public int changeMedium(String sessionId, int containerId, String objReference) throws BWFLAException
	{
		final EmulatorComponent emul = EmucompSingleton.getComponent(sessionId, EmulatorComponent.class);
		return emul.changeMedium(containerId, objReference);
	}
	
	public int attachMedium(String sessionId, @XmlMimeType("application/octet-stream") DataHandler data, String mediaType) throws BWFLAException
	{
		final EmulatorComponent emul = EmucompSingleton.getComponent(sessionId, EmulatorComponent.class);
		return emul.attachMedium(data, mediaType);
	}
	
	@Override
	public @XmlMimeType("application/octet-stream") DataHandler detachMedium(String sessionId, int handle) throws BWFLAException
	{
		EmulatorComponent emul = EmucompSingleton.getComponent(sessionId, EmulatorComponent.class);		
		return emul.detachMedium(handle);
	}
	
	@Override
	public String getRuntimeConfiguration(String sessionId) throws BWFLAException
	{
		final EmulatorComponent emul = EmucompSingleton.getComponent(sessionId, EmulatorComponent.class);		
		return emul.getRuntimeConfiguration();
	}
	
	@Override
	public Set<String> getColdplugableDrives(String sessionId)
	{
		final EmulatorComponent emul = EmucompSingleton.getComponent(sessionId, EmulatorComponent.class);
		return emul.getColdplugableDrives();
	}
	
	@Override
	public Set<String> getHotplugableDrives(String sessionId)
	{
		final EmulatorComponent emul = EmucompSingleton.getComponent(sessionId, EmulatorComponent.class);
		return emul.getHotplugableDrives();
	}

	@Override
	public String saveEnvironment(String sessionId, String wsHost, String name, String type) throws BWFLAException
	{
		final EmulatorComponent emul = EmucompSingleton.getComponent(sessionId, EmulatorComponent.class);
		return emul.saveEnvironment(wsHost, name, type);
	}
	
	@Override
	public String getEmulatorState(String sessionId)
	{
		final EmulatorComponent emul = EmucompSingleton.getComponent(sessionId, EmulatorComponent.class);
		return emul.getEmulatorState();
	}

	@Override
	public void connectNic(String sessionId, String hwaddress, String endpoint) throws BWFLAException 
	{
		final EmulatorComponent emul = EmucompSingleton.getComponent(sessionId, EmulatorComponent.class);
		emul.connectNic(hwaddress, endpoint);
	}

	@Override
	public String getControlURL(String sessionId, ConnectionType type, AbstractCredentials credentials) throws BWFLAException
	{
		final EmulatorComponent emul = EmucompSingleton.getComponent(sessionId, EmulatorComponent.class);
		return emul.getControlURL(type, credentials);
	}
	
	
	/* ==================== Session recording API ==================== */
	
	@Override
	public boolean prepareSessionRecorder(String sessionId) throws BWFLAException
	{
		final EmulatorComponent emul = EmucompSingleton.getComponent(sessionId, EmulatorComponent.class);
		return emul.prepareSessionRecorder();
	}
	
	@Override
	public void startSessionRecording(String sessionId) throws BWFLAException
	{
		final EmulatorComponent emul = EmucompSingleton.getComponent(sessionId, EmulatorComponent.class);
		emul.startSessionRecording();
	}
	
	@Override
	public void stopSessionRecording(String sessionId) throws BWFLAException
	{
		final EmulatorComponent emul = EmucompSingleton.getComponent(sessionId, EmulatorComponent.class);
		emul.stopSessionRecording();
	}
	
	@Override
	public boolean isRecordModeEnabled(String sessionId) throws BWFLAException
	{
		final EmulatorComponent emul = EmucompSingleton.getComponent(sessionId, EmulatorComponent.class);
		return emul.isRecordModeEnabled();
	}
	
	@Override
	public void addActionFinishedMark(String sessionId) throws BWFLAException
	{
		final EmulatorComponent emul = EmucompSingleton.getComponent(sessionId, EmulatorComponent.class);
		emul.addActionFinishedMark();
	}
	
	@Override
	public void defineTraceMetadataChunk(String sessionId, String tag, String comment) throws BWFLAException
	{
		final EmulatorComponent emul = EmucompSingleton.getComponent(sessionId, EmulatorComponent.class);
		emul.defineTraceMetadataChunk(tag, comment);
	}
	
	@Override
	public void addTraceMetadataEntry(String sessionId, String ctag, String key, String value) throws BWFLAException
	{
		final EmulatorComponent emul = EmucompSingleton.getComponent(sessionId, EmulatorComponent.class);
		emul.addTraceMetadataEntry(ctag, key, value);
	}
	
	@Override
	public String getSessionTrace(String sessionId) throws BWFLAException
	{
		final EmulatorComponent emul = EmucompSingleton.getComponent(sessionId, EmulatorComponent.class);
		return emul.getSessionTrace();
	}
	
	
	/* ==================== Session replay API ==================== */
	
	@Override
	public boolean prepareSessionPlayer(String sessionId, String trace, boolean headless) throws BWFLAException
	{
		final EmulatorComponent emul = EmucompSingleton.getComponent(sessionId, EmulatorComponent.class);
		return emul.prepareSessionPlayer(trace, headless);
	}
	
	@Override
	public int getSessionPlayerProgress(String sessionId) throws BWFLAException
	{
		final EmulatorComponent emul = EmucompSingleton.getComponent(sessionId, EmulatorComponent.class);
		return emul.getSessionPlayerProgress();
	}
	
	@Override
	public boolean isReplayModeEnabled(String sessionId) throws BWFLAException
	{
		final EmulatorComponent emul = EmucompSingleton.getComponent(sessionId, EmulatorComponent.class);
		return emul.isReplayModeEnabled();
	}
	
	
	/* ==================== Monitoring API ==================== */
	
	@Override
	public boolean updateMonitorValues(String sessionId) throws BWFLAException
	{
		final EmulatorComponent emul = EmucompSingleton.getComponent(sessionId, EmulatorComponent.class);
		return emul.updateMonitorValues();
	}
	
	@Override
	public String getMonitorValue(String sessionId, ProcessMonitorVID id) throws BWFLAException
	{
		final EmulatorComponent emul = EmucompSingleton.getComponent(sessionId, EmulatorComponent.class);
		return emul.getMonitorValue(id);
	}
	
	@Override
	public List<String> getMonitorValues(String sessionId, Collection<ProcessMonitorVID> ids) throws BWFLAException
	{
		final EmulatorComponent emul = EmucompSingleton.getComponent(sessionId, EmulatorComponent.class);
		return emul.getMonitorValues(ids);
	}

	@Override
	public List<String> getAllMonitorValues(String sessionId) throws BWFLAException
	{
		final EmulatorComponent emul = EmucompSingleton.getComponent(sessionId, EmulatorComponent.class);
		return emul.getAllMonitorValues();
	}
	
	
	/* ==================== Screenshot API ==================== */
	
	@Override
	public void takeScreenshot(String sessionId) throws BWFLAException
	{
		final EmulatorComponent emul = EmucompSingleton.getComponent(sessionId, EmulatorComponent.class);
		emul.takeScreenshot();
	}
	
	@Override
	public @XmlMimeType("application/octet-stream") DataHandler getNextScreenshot(String sessionId) throws BWFLAException
	{
		final EmulatorComponent emul = EmucompSingleton.getComponent(sessionId, EmulatorComponent.class);
		return emul.getNextScreenshot();
	}
}
