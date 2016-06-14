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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.protocol.GuacamoleClientInformation;
import org.glyptodon.guacamole.protocol.GuacamoleConfiguration;

import de.bwl.bwfla.common.datatypes.AbstractCredentials;
import de.bwl.bwfla.common.datatypes.AbstractDataResource;
import de.bwl.bwfla.common.datatypes.ArchiveBinding;
import de.bwl.bwfla.common.datatypes.ConnectionType;
import de.bwl.bwfla.common.datatypes.Drive;
import de.bwl.bwfla.common.datatypes.Drive.DriveType;
import de.bwl.bwfla.common.datatypes.EmuCompState;
import de.bwl.bwfla.common.datatypes.EmulationEnvironment;
import de.bwl.bwfla.common.datatypes.FileCollection;
import de.bwl.bwfla.common.datatypes.FileCollectionEntry;
import de.bwl.bwfla.common.datatypes.Html5Options;
import de.bwl.bwfla.common.datatypes.InputOptions;
import de.bwl.bwfla.common.datatypes.NetworkEndpoint;
import de.bwl.bwfla.common.datatypes.Nic;
import de.bwl.bwfla.common.datatypes.ProcessMonitorVID;
import de.bwl.bwfla.common.datatypes.Binding;
import de.bwl.bwfla.common.datatypes.UiOptions;
import de.bwl.bwfla.common.datatypes.VdeNetworkEndpoint;
import de.bwl.bwfla.common.datatypes.VolatileResource;
import de.bwl.bwfla.common.datatypes.utils.EmulationEnvironmentHelper;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.exceptions.IllegalEmulatorStateException;
import de.bwl.bwfla.common.interfaces.EmulatorComponent;
import de.bwl.bwfla.common.services.guacplay.GuacDefs;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.ExtOpCode;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.SourceType;
import de.bwl.bwfla.common.services.guacplay.capture.ScreenShooter;
import de.bwl.bwfla.common.services.guacplay.net.GuacInterceptorChain;
import de.bwl.bwfla.common.services.guacplay.net.GuacTunnel;
import de.bwl.bwfla.common.services.guacplay.net.TunnelConfig;
import de.bwl.bwfla.common.services.guacplay.protocol.InstructionBuilder;
import de.bwl.bwfla.common.services.guacplay.record.SessionRecorder;
import de.bwl.bwfla.common.utils.BwflaFileUtils;
import de.bwl.bwfla.common.utils.EmulatorUtils;
import de.bwl.bwfla.common.utils.NetworkUtils;
import de.bwl.bwfla.common.utils.ObjectArchiveHelper;
import de.bwl.bwfla.common.utils.ProcessMonitor;
import de.bwl.bwfla.common.utils.ProcessRunner;
import de.bwl.bwfla.common.utils.SystemEnvironmentHelper;
import de.bwl.bwfla.common.utils.EmulatorUtils.XmountOutputFormat;
import de.bwl.bwfla.common.utils.XmountOptions;
import de.bwl.bwfla.emucomp.components.EaasComponentBean;
import de.bwl.bwfla.emucomp.components.emulators.IpcDefs.EventID;
import de.bwl.bwfla.emucomp.components.emulators.IpcDefs.MessageType;
import de.bwl.bwfla.emucomp.conf.EmucompSingleton;



/**
 * @author iv1004
 * 
 */
public abstract class EmulatorBean extends EaasComponentBean implements EmulatorComponent
{	
	protected final TunnelConfig			tunnelConfig			= new TunnelConfig();
	protected GuacTunnel					tunnel;

	protected Map<String, IConnector>		viewConnectors			= new ConcurrentHashMap<>();

	protected EmulatorBeanState				emuBeanState			= new EmulatorBeanState(EmuCompState.EMULATOR_UNDEFINED);

	protected EmulationEnvironment			emuEnvironment;
	private String							emuNativeConfig;
	protected Map<Integer, File>		    containers				= Collections.synchronizedMap(new HashMap<Integer, File>());

	protected final ProcessRunner			runner					= new ProcessRunner();
	protected ArrayList<ProcessRunner>		vdeProcesses			= new ArrayList<ProcessRunner>();

	protected Map<String, Binding>          bindings               = Collections.synchronizedMap(new HashMap<String, Binding>());
	protected Map<String, String>           bindingsMountCache     = Collections.synchronizedMap(new HashMap<String, String>());

	protected ArrayList<NetworkEndpoint>	networkConnections		= new ArrayList<NetworkEndpoint>();
	protected String						protocol;

	/** Emulator's configuration settings */
	protected final EmulatorConfig emuConfig = new EmulatorConfig();
	
	/* IPC for control messages */
	private IpcSocket ctlSocket = null;
	protected IpcMessageWriter ctlMsgWriter = null;
	protected IpcMessageReader ctlMsgReader = null;
	private IpcMessageQueue ctlMsgQueue = new IpcMessageQueue();
	private IpcEventSet ctlEvents = new IpcEventSet();
	protected String emuCtlSocketName = null;
	
	/* Session recording + replay members */
	private SessionRecorder recorder = null;
	private SessionPlayerWrapper player = null;

	/** Tool for capturing of screenshots. */
	private ScreenShooter scrshooter = null;

	/** Internal chain of IGuacInterceptors. */
	private final GuacInterceptorChain interceptors = new GuacInterceptorChain(2);

	/** Number of unprocessed messages, before message-processors start to block. */
	private static final int MESSAGE_BUFFER_CAPACITY = 4096;

	/** Filename for temporary trace-files. */
	private static final String TRACE_FILE = "session" + GuacDefs.TRACE_FILE_EXT;

	/* Supported protocol names */
	private static final String PROTOCOL_SDLONP  = "sdlonp";
	private static final String PROTOCOL_Y11     = "y11";


	public static EmulatorBean createEmulatorBean(EmulationEnvironment env)
	{
		try
		{
			String targetBean = env.getEmulator().getBean() + "Bean";
			Class<?> beanClass = Class.forName(EmulatorBean.class.getPackage().getName() + "." + targetBean);
			Constructor<?> beanConstructor = beanClass.getConstructor();
			return (EmulatorBean) beanConstructor.newInstance(new Object[] {});
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new IllegalStateException("object instantiation failed, check instance creation code");
		}
	}

	public static EmulatorBean createEmulatorBean(String config)
	{	
		EmulationEnvironment env = EmulationEnvironment.fromValue(config);
		return EmulatorBean.createEmulatorBean(env);
	}

	public boolean isLocalModeEnabled()
	{
		return EmucompSingleton.CONF.localmode;
	}
	
	public int getInactivityTimeout()
	{
		return EmucompSingleton.CONF.inactivityTimeout;
	}
	

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Client API
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String getEmulatorState()
	{
		final boolean isEmulatorInactive = ctlEvents.poll(EventID.CLIENT_INACTIVE);
		synchronized (emuBeanState) {
			if (isEmulatorInactive)
				emuBeanState.set(EmuCompState.EMULATOR_INACTIVE);
			
			return emuBeanState.get().value();
		}
	}

	public void initialize(String config) throws BWFLAException
	{
		synchronized (emuBeanState) {
			final EmuCompState curstate = emuBeanState.get();
			if (curstate != EmuCompState.EMULATOR_UNDEFINED) {
				String message = "Cannot initialize EmulatorBean!";
				throw new IllegalEmulatorStateException(message, curstate);
			}

			emuBeanState.set(EmuCompState.EMULATOR_BUSY);
		}

		// Create control sockets
		try {
			ctlSocket = IpcSocket.create(this.newCtlSocketName("srv"), true);
			ctlMsgWriter = new IpcMessageWriter(ctlSocket);
			ctlMsgReader = new IpcMessageReader(ctlSocket);
			emuCtlSocketName = this.newCtlSocketName("emu");
		}
		catch (Exception exception) {
			LOG.warning("Constructing control sockets failed!");
			exception.printStackTrace();
			emuBeanState.update(EmuCompState.EMULATOR_FAILED);
			return;
		}

		try
		{
			EmulationEnvironment env = EmulationEnvironment.fromValue(config);
			this.setRuntimeConfiguration(env);

		}
		catch(IllegalArgumentException e)
		{
			emuBeanState.update(EmuCompState.EMULATOR_CLIENT_FAULT);
			return;
		}
		catch(Throwable e)
		{
			e.printStackTrace();
			emuBeanState.update(EmuCompState.EMULATOR_FAILED);
			return;
		}
		
		// Prepare configuration for tunnels
		tunnelConfig.setGuacdHostname("localhost");
		tunnelConfig.setGuacdPort(TunnelConfig.GUACD_PORT);
		tunnelConfig.setInterceptor(interceptors);

		LOG.info("EmulatorBean for session " + this.getSessionId() + " initialized. Temporary directory created: " + tempDir.getAbsolutePath());
		emuBeanState.update(EmuCompState.EMULATOR_READY);
	}

	private void unmount()
	{
		ProcessRunner process = new ProcessRunner();
		File resFile = tempDir.toPath().resolve("bindings").toFile();
		if(resFile.exists() && resFile.isDirectory())
		{
			for(File f: tempDir.toPath().resolve("bindings").toFile().listFiles())
			{
				if(!f.getAbsolutePath().endsWith(".fuse"))
					continue;

				process.setCommand("fusermount");
				process.addArguments("-u", "-z");
				process.addArgument(f.toString());
				process.execute();
			}
			process.cleanup();
		}
	}
	
	public void destroy()
	{
		synchronized (emuBeanState)
		{
			final EmuCompState curstate = emuBeanState.get();
			if (curstate == EmuCompState.EMULATOR_UNDEFINED)
				return;

			if (curstate == EmuCompState.EMULATOR_BUSY) {
				LOG.severe("Destroying EmulatorBean while other operation is in-flight!");
				return;
			}

			emuBeanState.set(EmuCompState.EMULATOR_UNDEFINED);
		}

		this.stopInternal();

		// free container IDs and remove corresp. files
		for(File container: containers.values())
			container.delete();

		containers.clear();

		// disconnect connected connections
		for(NetworkEndpoint ep: this.networkConnections)
			ep.disconnect();

		// kill vde networking threads
		for(ProcessRunner subprocess : this.vdeProcesses)
		{
			if(subprocess.isProcessRunning())
				subprocess.stop();

			subprocess.cleanup();
		}

		unmount();
		
		// Stop screenshot-tool
		if (scrshooter != null)
			scrshooter.finish();

		// Stop and finalize session-recording
		if (recorder != null && !recorder.isFinished())
		{
			try {
				recorder.finish();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}

		// Cleanup the control sockets
		try {
			if (ctlSocket != null)
				ctlSocket.close();
		}
		catch (IOException exception) {
			exception.printStackTrace();
		}
		
		// Cleanup emulator's runner here
		runner.cleanup();

		try
		{
			FileUtils.deleteDirectory(tempDir);
			LOG.info("EmulatorBean for session " + this.getSessionId() + " destroyed. Temporary directory removed: " + tempDir.getAbsolutePath());
			tempDir = null;
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}

		// Collect garbage
		System.gc();
	}

	@Override
	public void start()
	{
		synchronized (emuBeanState)
		{
			final EmuCompState curstate = emuBeanState.get();
			if (curstate != EmuCompState.EMULATOR_READY && curstate != EmuCompState.EMULATOR_STOPPED) {
				LOG.warning("Cannot start emulator! Wrong state detected: " + curstate.value());
				return;
			}

			emuBeanState.set(EmuCompState.EMULATOR_BUSY);
		}

		if (this.isLocalModeEnabled())
			LOG.info("Local-mode enabled. Emulator will be started locally!");

		if (!runner.start()) {
			LOG.warning("Starting emulator failed!");
			emuBeanState.update(EmuCompState.EMULATOR_FAILED);
			return;
		}
		
		if (!this.waitUntilEmulatorCtlSocketReady(EmuCompState.EMULATOR_BUSY))
			return;
		
		if (!this.sendEmulatorConfig())
			return;

		if (!this.waitUntilEmulatorReady(EmuCompState.EMULATOR_BUSY))
			return;

		LOG.info("Emulator started in process " + runner.getProcessId());

		final Thread ctlSockObserver = new Thread()
		{
			@Override
			public void run()
			{
				while (emuBeanState.fetch() != EmuCompState.EMULATOR_UNDEFINED) {
					try {
						// Try to receive new message
						if (!ctlMsgReader.read(5000))
							continue;

						// Message could be read, queue it for further processing
						if (ctlMsgReader.isNotification())
							ctlEvents.add(ctlMsgReader.getEventID());
						else {
							final byte msgtype = ctlMsgReader.getMessageType();
							final byte[] msgdata = ctlMsgReader.getMessageData();
							ctlMsgQueue.put(msgtype, msgdata);
						}
					}
					catch (Exception exception) {
						if (emuBeanState.fetch() == EmuCompState.EMULATOR_UNDEFINED)
							break;  // Ignore problems when destroying session!
						
						LOG.warning("An error occured while reading from control-socket!");
						exception.printStackTrace();
					}
				}
			}
		};
		
		final Thread emuObserver = new Thread()
		{
			@Override
			public void run() 
			{
				try {
					EmulatorBean.this.attachClientToEmulator();
				}
				catch (Exception exception) {
					emuBeanState.update(EmuCompState.EMULATOR_FAILED);
					exception.printStackTrace();
				}

				runner.waitUntilFinished();
				runner.printStdOut();
				runner.printStdErr();
				
				// cleanup will be performed later by EmulatorBean.destroy()

				synchronized (emuBeanState)
				{
					if (EmulatorBean.this.isLocalModeEnabled()) {
						// In local-mode emulator will be terminated by the user,
						// without using our API. Set the correct state here!
						emuBeanState.set(EmuCompState.EMULATOR_STOPPED);
					}
					else {
						final EmuCompState curstate = emuBeanState.get();
						if (curstate == EmuCompState.EMULATOR_RUNNING) {
							LOG.warning("Emulator stopped unexpectedly!");
							// FIXME: setting here also to STOPPED, since there is currently no reliable way 
							// to determine (un-)successful termination depending on application exit code 
							emuBeanState.set(EmuCompState.EMULATOR_STOPPED);
						}
					}
				}
			}
		};

		ctlSockObserver.start();
		emuObserver.start();

		// Not in local mode?
		if (!this.isLocalModeEnabled()) {
			// TODO: Add an option to enable/disable the screenshot-tool!
			// Initialize the screenshot-tool
			scrshooter = new ScreenShooter(this.getSessionId(), 256);
			scrshooter.prepare();

			// Register the screenshot-tool
			interceptors.addInterceptor(scrshooter);
		}

		// Construct the tunnel
		tunnel = GuacTunnel.construct(tunnelConfig);
		if (tunnel == null) {
			LOG.warning("Constructing emulator's tunnel failed!");
			emuBeanState.update(EmuCompState.EMULATOR_FAILED);
			return;
		}

		if (!this.isLocalModeEnabled() && (player != null))
			player.start(tunnel, this.getSessionId(), runner.getProcessMonitor());

		emuBeanState.update(EmuCompState.EMULATOR_RUNNING);
	}

	@Override
	public void stop()
	{
		synchronized (emuBeanState)
		{
			final EmuCompState curstate = emuBeanState.get();
			if (curstate != EmuCompState.EMULATOR_RUNNING) {
				LOG.warning("Cannot stop emulator! Wrong state detected: " + curstate.value());
				return;
			}

			emuBeanState.set(EmuCompState.EMULATOR_BUSY);
		}

		this.stopInternal();

		emuBeanState.update(EmuCompState.EMULATOR_STOPPED);
	}

	private void stopInternal()
	{
		if (player != null) 
			player.stop();

		if (tunnel != null && tunnel.isOpen()) {
			try {
				tunnel.disconnect();
				tunnel.close();
			}
			catch (GuacamoleException e) {
				e.printStackTrace();
			}
		}

		if (runner.isProcessRunning()) {
			final int emuProcessId = runner.getProcessId();
			LOG.info("Stopping emulator " + emuProcessId + "...");
			
			try {
				// Send termination message
				ctlMsgWriter.begin(MessageType.TERMINATE);
				ctlMsgWriter.send(emuCtlSocketName);
				
				// Give emulator a chance to shutdown cleanly
				for (int i = 0; i < 10; ++i) {
					if (runner.isProcessFinished()) {
						LOG.info("Emulator " + emuProcessId + " stopped.");
						return;
					}
					
					Thread.sleep(500);
				}
			}
			catch (Exception exception) {
				exception.printStackTrace();
			}
			
			LOG.info("Emulator " + emuProcessId + " failed to shutdown cleanly! Killing it...");
			runner.stop();  // Try to kill the process
		}
	}

	@Override
	public String getRuntimeConfiguration() throws BWFLAException
	{
		synchronized (emuBeanState)
		{
			if (emuBeanState.get() == EmuCompState.EMULATOR_UNDEFINED) {
				String message = "Runtime configuration is not available in this state!";
				throw new IllegalEmulatorStateException(message, EmuCompState.EMULATOR_UNDEFINED);
			}
		}

		try
		{
			return this.emuEnvironment.value();
		}
		catch(JAXBException e)
		{
			throw new BWFLAException("an error occured at server during return data serialization", e);
		}
	}

	public Map<String, IConnector> getViewConnectors()
	{
		final EmuCompState curstate = emuBeanState.fetch();
		if (curstate != EmuCompState.EMULATOR_RUNNING) {
			LOG.warning("Cannot access view-connectors! Illegal state: " + curstate);
			return null;
		}

		return this.viewConnectors;
	}

	@Override
	public String getControlURL(ConnectionType type, AbstractCredentials credentials) throws BWFLAException
	{
		synchronized (emuBeanState)
		{
			final EmuCompState curstate = emuBeanState.get();

			if (curstate != EmuCompState.EMULATOR_RUNNING) 
			{
				String message = "Cannot construct control URL!";
				throw new IllegalEmulatorStateException(message, curstate);
			}
		}

		IConnector connector = this.viewConnectors.get(type.toString());

		if (connector == null)
		{
			switch(type)
			{
			case HTTP:
			case HTTPS:
				// TODO: analyze and use credentials
				String cookie = this.getSessionId();
				UiOptions uiopts = this.emuEnvironment.getUiOptions();
				Html5Options htmlopts = (uiopts != null) ? uiopts.getHtml5() : null;
				boolean pointerLockActive = (htmlopts != null) && htmlopts.isPointerLock();
				GuacTunnel clientTunnel = (player != null) ? player.getPlayerTunnel() : this.tunnel;
				connector = new HtmlConnector(cookie, clientTunnel, pointerLockActive, type.equals(ConnectionType.HTTPS));
				break;

			case VNC:
				// TODO: (if required) analyze and use credentials
				connector = new VncConnector(/* TODO: set params */);
				break;

			case RDP:
				// TODO: (if required) analyze and use credentials
				connector = new RdpConnector(/* TODO: set params */);
				break;

			default:
				throw new BWFLAException("could not find server implementation of a view-connector for this type: " + type.toString());
			}

			this.viewConnectors.put(type.toString(), connector);
		}

		return connector.toString();
	}

	@Override
	public Set<String> getColdplugableDrives()
	{
		// TODO: here read result from corresponding metadata  
		return new HashSet<String>();
	}

	@Override
	public Set<String> getHotplugableDrives()
	{
		// TODO: here read result from corresponding metadata
		return new HashSet<String>();
	}

	@Override
	public String saveEnvironment(String wsHost, String envName, String type) throws BWFLAException
	{
		synchronized (emuBeanState) {
			final EmuCompState curstate = emuBeanState.get();
			if (curstate == EmuCompState.EMULATOR_UNDEFINED) {
				String message = "Cannot save environment!";
				throw new IllegalEmulatorStateException(message, curstate);
			}
			else if(curstate == EmuCompState.EMULATOR_STOPPED)
				unmount();
		}

		Binding res = null;
		for (AbstractDataResource r : this.emuEnvironment.getAbstractDataResource()) {
			if (!(r instanceof VolatileResource)
					&& r.getId().equals("main_hdd")) {
				res = (Binding)r;
				break;
			}
		}

		if (res == null)
			return null;

		// get actual image path instead of fused
		String resStr;
		try {
			resStr = this.lookupResource(res.getId(), XmountOutputFormat.RAW);
		} catch (Exception e) {
			throw new BWFLAException("Could not resolve resource " + res.getId() + ".", e);
		}

		if (resStr == null) {
			throw new BWFLAException("Could not resolve resource "
					+ res.getId() + " for saving.");
		}

		Path diffPath = Paths.get(resStr);
		switch (res.getAccess()) {
		case COW:
			// the real cow2 image is one directory up ?? 
			diffPath = diffPath.resolveSibling("../" + diffPath.getFileName().toString().replaceAll("\\..+?$", ".cow"));
			break;

		case COPY:
			// do nothing, we're already working directly on the real file
			break;
		}

		EmulationEnvironment env = EmulationEnvironmentHelper.clean(this.emuEnvironment);
		env.getDescription().setTitle(envName);

		try {
			String conf = env.value();
			SystemEnvironmentHelper envHelper = new SystemEnvironmentHelper(
					wsHost);
			return envHelper.registerEnvironment(conf, diffPath.toFile(), type);
		} catch (JAXBException e) {
			throw new BWFLAException(
					"Cannot save environment due to serialization error.", e);
		}
	}

	@Override
	public int changeMedium(int containerId, String objReference) throws BWFLAException
	{
		try {
			LOG.info("change medium: " + objReference);
			Drive drive = this.emuEnvironment.getDrive().get(containerId);
			// detach the current medium
			this.connectDrive(drive, false);

			if (objReference == null || objReference.isEmpty()) {
				return containerId;
			}

			//            Resource res = new VolatileResource();
			//            res.setUrl(objReference);
			//            res.setId("attached_container_" + containerId);
			//            this.prepareResource(res);
			//            this.emuEnvironment.getBinding().add(res);


			drive.setData(objReference);
			//            this.emuEnvironment.getDrive().add(drive);

			boolean attachOk = (emuBeanState.fetch() == EmuCompState.EMULATOR_RUNNING) ? connectDrive(drive, true) : addDrive(drive);

			if(!attachOk)
				throw new BWFLAException("error occured in the last phase of device attachment"); 
		} catch (IndexOutOfBoundsException e) {
			throw new BWFLAException("Cannot change medium: invalid drive id given.", e);
		}
		// TODO: change disk in run-time
		return containerId;
	}

	@Override
	public int attachMedium(DataHandler data, String mediumType) throws BWFLAException
	{
		synchronized (emuBeanState)
		{
			final EmuCompState curstate = emuBeanState.get();

			if (curstate != EmuCompState.EMULATOR_READY && curstate != EmuCompState.EMULATOR_RUNNING) 
			{
				String message = "Cannot attach medium to emulator!";
				throw new IllegalEmulatorStateException(message, curstate);
			}
		}

		if (data == null)
			throw new BWFLAException("Data stream cannot be null!");

		Drive.DriveType type = Drive.DriveType.valueOf(mediumType.toUpperCase());
		Drive drive = EmulationEnvironmentHelper.findEmptyDrive(this.emuEnvironment, type);
		if (drive == null)
			throw new BWFLAException("No more free slots of this type are available: " + type);

		File objFile;
		try
		{
			objFile = BwflaFileUtils.streamToTmpFile(tempDir, data.getInputStream(), "digital_object_");
		}
		catch(IOException e)
		{
			throw new BWFLAException("an error occured while opening data stream or writing it to file", e);
		}

		File container = objFile;
		synchronized(container)
		{
			int id = this.emuEnvironment.getDrive().indexOf(drive);
			if(id == -1)
				throw new BWFLAException("could not determine container ID");

			VolatileResource res = new VolatileResource();
			res.setUrl("file://" + objFile.getAbsolutePath());
			res.setId("attached_container_" + id);
			try {
				this.prepareResource(res);
			} catch (IllegalArgumentException | IOException | JAXBException e) {
				throw new BWFLAException("Could not prepare the resource for this medium.", e);
			}
			this.emuEnvironment.getAbstractDataResource().add(res);

			drive.setData("binding://" + res.getId());

			boolean attachOk = (emuBeanState.fetch() == EmuCompState.EMULATOR_RUNNING) ? connectDrive(drive, true) : addDrive(drive);

			if(!attachOk)
				throw new BWFLAException("error occured in the last phase of device attachment"); 

			return id;
		}
	}

	@Override
	public DataHandler detachMedium(int containerId) throws BWFLAException
	{
		synchronized (emuBeanState)
		{
			final EmuCompState curstate = emuBeanState.get();
			if (curstate != EmuCompState.EMULATOR_READY && curstate != EmuCompState.EMULATOR_RUNNING && curstate != EmuCompState.EMULATOR_STOPPED) {
				String message = "Cannot detach medium from emulator!";
				throw new IllegalEmulatorStateException(message, curstate);
			}
		}

		List<AbstractDataResource> bindings = this.emuEnvironment.getAbstractDataResource();

		if(bindings != null)
			for(AbstractDataResource aBinding: bindings)
			{
				if(!(aBinding instanceof VolatileResource))
					continue;
				VolatileResource binding = (VolatileResource)aBinding;
				String id        = "attached_container_" + containerId;
				String bindingId = binding.getId();

				if(id.equalsIgnoreCase(bindingId))
					try
				{
						File containerFile = new File(binding.getResourcePath());
						if(containerFile.isDirectory())
							throw new BWFLAException("detached container is in format (FS-directory), which is currently not supported for detachment");

						if(containerFile.isFile())
							return new DataHandler(new FileDataSource(containerFile));
						else
							LOG.warning("missing proper container file at this location: " + containerFile.getAbsolutePath());
				}
				catch(Exception e)
				{
					e.printStackTrace();
					throw new BWFLAException("a server-side error occured, please try again later (see logs for details)");
				}
			}

		throw new BWFLAException("could not find container by this container id: " + containerId);
	}

	public void connectNic(String mac, String endpointData) throws BWFLAException
	{
		synchronized (emuBeanState)
		{
			final EmuCompState curstate = emuBeanState.get();
			if (curstate != EmuCompState.EMULATOR_READY && curstate != EmuCompState.EMULATOR_STOPPED)
				throw new IllegalEmulatorStateException("Cannot connect NIC!", curstate);
		}

		NetworkEndpoint endpoint;
		try
		{
			endpoint = NetworkEndpoint.fromValue(endpointData);
		}
		catch(JAXBException e)
		{
			throw new BWFLAException("an error occured during 'endpointData' deserialisation with JAX-B, check client input parameter");
		}

		Path switchPath = this.tempDir.toPath().resolve("nic_" + mac);
		if(!Files.exists(switchPath))
			throw new BWFLAException("client has passed a MAC address, which does not belong to this environment: " + mac);

		if(!(endpoint instanceof VdeNetworkEndpoint))
			throw new BWFLAException("this endpoint currently supports only 'vde' networking, exiting operation");

		VdeNetworkEndpoint localEndpoint = new VdeNetworkEndpoint(NetworkUtils.getHostAddress().getHostAddress(), switchPath.toString(), null);

		endpoint.connect(localEndpoint);
		this.networkConnections.add(endpoint);
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Protected
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	protected void setRuntimeConfiguration(EmulationEnvironment environment) throws BWFLAException
	{
		try {
			this.emuEnvironment = environment;
			LOG.info(emuEnvironment.value());
			for(AbstractDataResource resource: emuEnvironment.getAbstractDataResource())
			{
				LOG.info("abstract resource id: " + resource.getId());
				prepareResource(resource);
			}
			EmulationEnvironment.NativeConfig nativeConfig = emuEnvironment.getNativeConfig();
			this.prepareNativeConfig(nativeConfig);
			this.prepareEmulatorRunner();

			for(Drive drive: emuEnvironment.getDrive())
				prepareDrive(drive);

			for(Nic nic: emuEnvironment.getNic())
				prepareNic(nic);
			
			this.finishRuntimeConfiguration();
			
		} catch (IllegalArgumentException | IOException e) {
			throw new BWFLAException("Could not set runtime information.", e);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected String getNativeConfig()
	{
		return emuNativeConfig;
	}

	/** Must be overriden by subclasses to initialize the emulator's command. */
	protected abstract void prepareEmulatorRunner() throws BWFLAException;
	
	/** Callback for performing actions, deferred during runtime configuration. */
	protected void finishRuntimeConfiguration() throws BWFLAException
	{
		// Do nothing!
	}

	/**
	 * Determine the image format for a specified drive type for the current
	 * emulator.
	 * 
	 * This method should be overridden by any emulator that has specific
	 * file format needs (e.g. VirtualBox (yuck)).
	 * 
	 * @param driveType The drive type
	 * @return The desired image format for the specified drive type
	 */
	protected XmountOutputFormat getImageFormatForDriveType(DriveType driveType) {
		// as default, we use raw images for everything
		return XmountOutputFormat.RAW;
	}

	/** Setups the emulator's environment variables for running locally. */
	protected void setupEmulatorForY11()
	{
		protocol = PROTOCOL_Y11;

		final String tmpdir = tempDir.toString();
		final String emusocket = Paths.get(tmpdir, "sdlonp-iosocket-emu").toString();

		// Setup emulator's tunnel
		final GuacamoleConfiguration gconf = tunnelConfig.getGuacamoleConfiguration();
		gconf.setProtocol(PROTOCOL_SDLONP);
		gconf.setParameter("enable-audio", "false");
		gconf.setParameter("emu-iosocket", emusocket);

		// Setup emulator's environment
		runner.addEnvVariable("SDL_VIDEODRIVER", protocol);
		runner.addEnvVariable("SDL_SRVCTLSOCKET", ctlSocket.getName());
		runner.addEnvVariable("SDL_EMUCTLSOCKET", emuCtlSocketName);
		emuConfig.setIoSocket(emusocket);
		
		// TODO: should this parameter be read from meta-data?
		emuConfig.setInactivityTimeout(this.getInactivityTimeout());

		UiOptions uiopts = emuEnvironment.getUiOptions();
		if (uiopts == null)
			return;

		Html5Options html5 = uiopts.getHtml5();
		if (html5 == null)
			return;

		if (html5.isPointerLock())
			emuConfig.setRelativeMouse(true);
	}

	/** Setups the emulator's environment variables and tunnel for SDLONP-Protocol. */
	protected void setupEmulatorForSDLONP()
	{
		protocol = PROTOCOL_SDLONP;

		final String tmpdir = tempDir.toString();
		final String emusocket = Paths.get(tmpdir, "sdlonp-iosocket-emu").toString();

		// Setup emulator's tunnel
		final GuacamoleConfiguration gconf = tunnelConfig.getGuacamoleConfiguration();
		gconf.setProtocol(protocol);
		gconf.setParameter("enable-audio", "true");
		gconf.setParameter("emu-iosocket", emusocket);

		// Setup client configuration
		final GuacamoleClientInformation ginfo = tunnelConfig.getGuacamoleClientInformation();
		ginfo.getAudioMimetypes().add("audio/ogg");

		// Setup emulator's environment
		runner.addEnvVariable("SDL_AUDIODRIVER", protocol);
		runner.addEnvVariable("SDL_VIDEODRIVER", protocol);
		runner.addEnvVariable("SDL_SRVCTLSOCKET", ctlSocket.getName());
		runner.addEnvVariable("SDL_EMUCTLSOCKET", emuCtlSocketName);
		emuConfig.setIoSocket(emusocket);
		
		emuConfig.setInactivityTimeout(this.getInactivityTimeout());
		
		UiOptions uiopts = emuEnvironment.getUiOptions();
		if (uiopts != null) {
			Html5Options html5 = uiopts.getHtml5();
			if (html5 != null) {
				if (html5.isPointerLock())
					emuConfig.setRelativeMouse(true);

				String crtopt = html5.getCrt();
				if (crtopt != null && !crtopt.isEmpty()) {
					emuConfig.setCrtFilter("snes-ntsc");
					emuConfig.setCrtPreset("composite");
				}
			}

			InputOptions input = uiopts.getInput();
			if (input != null) {
				String kbdModel = input.getEmulatorKbdModel();
				if (kbdModel != null && !kbdModel.isEmpty())
					emuConfig.setKeyboardModel(kbdModel);
				
				String kbdLayout = input.getEmulatorKbdLayout();
				if (kbdLayout != null && !kbdLayout.isEmpty())
					emuConfig.setKeyboardLayout(kbdLayout);
				
				String clientKbdModel = input.getClientKbdModel();
				if (clientKbdModel != null && !clientKbdModel.isEmpty())
					emuConfig.setClientKeyboardModel(clientKbdModel);
				
				String clientKbdLayout = input.getClientKbdLayout();
				if (clientKbdLayout != null && !clientKbdLayout.isEmpty())
					emuConfig.setClientKeyboardLayout(clientKbdLayout);
			}
		}
	}

	/* ==================== Session Recording Helpers ==================== */

	public boolean prepareSessionRecorder() throws BWFLAException
	{
		if (recorder != null) {
			LOG.info("SessionRecorder already prepared.");
			return true;
		}

		if (player != null) {
			String message = "Initialization of SessionRecorder failed, "
					+ "because SessionReplayer is already running. "
					+ "Using both at the same time is not supported!";

			throw new BWFLAException(message);
		}

		// Create and initialize the recorder
		recorder = new SessionRecorder(this.getSessionId(), MESSAGE_BUFFER_CAPACITY);
		try {
			// Create and setup a temp-file for the recording
			Path tmpfile = Paths.get(tempDir.toString(), TRACE_FILE);
			recorder.prepare(tmpfile);
		}
		catch (IOException exception) {
			LOG.severe("Creation of output file for session-recording failed!");
			exception.printStackTrace();
			recorder = null;
			return false;
		}

		// Register the recorder as interceptor
		interceptors.addInterceptor(recorder);

		return true;
	}

	public void startSessionRecording() throws BWFLAException
	{
		this.ensureRecorderIsInitialized();
		recorder.start();
	}

	public void stopSessionRecording() throws BWFLAException
	{
		this.ensureRecorderIsInitialized();
		recorder.stop();
	}

	public boolean isRecordModeEnabled() throws BWFLAException
	{
		if (recorder == null)
			return false;

		return recorder.isRecording();
	}

	public void addActionFinishedMark()
	{
		//		this.ensureRecorderIsInitialized();

		InstructionBuilder ibuilder = new InstructionBuilder(16);
		ibuilder.start(ExtOpCode.ACTION_FINISHED);
		ibuilder.finish();

		recorder.postMessage(SourceType.INTERNAL, ibuilder.array(), 0, ibuilder.length());
	}

	/** Add a new metadata chunk to the trace-file. */
	public void defineTraceMetadataChunk(String tag, String comment) throws BWFLAException
	{
		this.ensureRecorderIsInitialized();
		recorder.defineMetadataChunk(tag, comment);
	}

	/** Add a key/value pair as metadata to the trace-file. */
	public void addTraceMetadataEntry(String ctag, String key, String value) throws BWFLAException
	{
		this.ensureRecorderIsInitialized();
		recorder.addMetadataEntry(ctag, key, value);
	}

	public String getSessionTrace() throws BWFLAException
	{
		this.ensureRecorderIsInitialized();

		try {
			recorder.finish();
		}
		catch (IOException exception) {
			LOG.severe("Finishing session-recording failed!");
			exception.printStackTrace();
			return null;
		}

		return recorder.toString();
	}

	private void ensureRecorderIsInitialized() throws BWFLAException
	{
		if (recorder == null)
			throw new BWFLAException("SessionRecorder is not initialized!");
	}


	/* ==================== Session Replay Helpers ==================== */

	public boolean prepareSessionPlayer(String trace, boolean headless) throws BWFLAException
	{
		if (player != null) {
			LOG.info("SessionPlayer already prepared.");
			return true;
		}

		if (recorder != null) {
			String message = "Initialization of SessionPlayer failed, "
					+ "because SessionRecorder is already running. "
					+ "Using both at the same time is not supported!";

			throw new BWFLAException(message);
		}

		Path file = Paths.get(tempDir.toString(), TRACE_FILE);
		try {
			FileUtils.writeStringToFile(file.toFile(), trace);
		}
		catch (IOException exception) {
			LOG.severe("An error occured while writing temporary session-trace!");
			exception.printStackTrace();
			return false;
		}

		player = new SessionPlayerWrapper(file, headless);

		return true;
	}

	public int getSessionPlayerProgress()
	{
		if (player == null)
			return 0;

		return player.getProgress();
	}

	public boolean isReplayModeEnabled()
	{
		if (player == null)
			return false;

		return player.isPlaying();
	}

	/* ==================== Monitoring API ==================== */

	@Override
	public boolean updateMonitorValues()
	{
		ProcessMonitor monitor = runner.getProcessMonitor();
		if (monitor == null) {
			// Process is currently not running!
			return false;
		}

		return monitor.update();
	}

	@Override
	public String getMonitorValue(ProcessMonitorVID id)
	{
		ProcessMonitor monitor = runner.getProcessMonitor();
		if (monitor == null) {
			// Process is currently not running!
			return ProcessMonitor.INVALID_VALUE;
		}

		return monitor.getValue(id);
	}

	@Override
	public List<String> getMonitorValues(Collection<ProcessMonitorVID> ids)
	{
		ProcessMonitor monitor = runner.getProcessMonitor();
		if (monitor == null) {
			// Process is currently not running!
			return ProcessMonitor.INVALID_VALUE_LIST;
		}

		return monitor.getValues(ids);
	}

	@Override
	public List<String> getAllMonitorValues()
	{
		ProcessMonitor monitor = runner.getProcessMonitor();
		if (monitor == null) {
			// Process is currently not running!
			return ProcessMonitor.INVALID_VALUE_LIST;
		}

		return monitor.getValues();
	}


	/* ==================== Screenshot API ==================== */

	public void takeScreenshot()
	{
		scrshooter.takeScreenshot();
	}

	public DataHandler getNextScreenshot()
	{
		byte[] data = scrshooter.getNextScreenshot();
		if (data == null)
			return null;

		return new DataHandler(data, "application/octet-stream");
	}


	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Utilities
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//
	//	private int allocateContainerId(Container container)
	//	{
	//		int freeId = -1;
	//		final int MAX_TRIES = 50;
	//		for(int i = 0; (i < MAX_TRIES) && (freeId == -1); ++i)
	//		{
	//			freeId = (new Random()).nextInt();
	//			if(containers.containsKey(freeId))
	//				freeId = -1;
	//			else
	//				containers.put(freeId, container);
	//		}
	//
	//		return freeId;
	//	}

	private boolean sendEmulatorConfig()
	{
		try {
			emuConfig.sendAllTo(ctlSocket, emuCtlSocketName);
		}
		catch (Exception exception) {
			LOG.warning("Sending configuration to emulator failed!");
			exception.printStackTrace();
			emuBeanState.update(EmuCompState.EMULATOR_FAILED);
			return false;
		}

		return true;
	}
	
	private boolean waitForReadyNotification(int expevent, String message, int timeout, EmuCompState expstate)
	{
		LOG.info(message);

		try {
			final int waittime = 1000;  // in ms
			int numretries = (timeout > waittime) ? timeout / waittime : 1;
			boolean isMsgAvailable = false;
			while (numretries > 0) {
				isMsgAvailable = ctlMsgReader.read(waittime);
				if (isMsgAvailable)
					break;
				
				if (emuBeanState.get() != expstate) {
					LOG.warning("Expected state changed, abort waiting for notification!");
					return false;
				}
				
				--numretries;
			}
			
			if (!isMsgAvailable) {
				LOG.warning("Reading from emulator timed out!");
				return false;
			}
			
			if (!ctlMsgReader.isNotification()) {
				LOG.warning("Received message was not a notification!");
				return false;
			}
			
			if (ctlMsgReader.getEventID() != expevent) {
				LOG.warning("Received an unexpected notification from emulator!");
				return false;
			}
		}
		catch (Exception exception) {
			LOG.warning("Failed to read a notification-message from emulator.");
			exception.printStackTrace();
			return false;
		}

		LOG.info("Received a ready-notification from emulator.");

		return true;
	}
	
	private boolean waitUntilEmulatorCtlSocketReady(EmuCompState expstate)
	{
		final int timeout = 15000;  // in ms

		final String message = "Waiting for emulator's control-socket to become ready...";
		boolean ok = this.waitForReadyNotification(EventID.EMULATOR_CTLSOCK_READY, message, timeout, expstate);
		if (!ok) {
			LOG.warning("Emulator's control socket is not reachable!");
			emuBeanState.update(EmuCompState.EMULATOR_FAILED);
		}
		
		return ok;
	}
	
	private boolean waitUntilEmulatorReady(EmuCompState expstate)
	{
		final int timeout = 15000;  // in ms

		final String message = "Waiting for emulator to become ready...";
		boolean ok = this.waitForReadyNotification(EventID.EMULATOR_READY, message, timeout, expstate);
		if (!ok) {
			LOG.warning("Emulator was not started properly!");
			emuBeanState.update(EmuCompState.EMULATOR_FAILED);
		}
		
		return ok;
	}
	
	private void attachClientToEmulator() throws IOException, InterruptedException
	{
		LOG.info("Attaching client to emulator...");

		ctlMsgWriter.begin(MessageType.ATTACH_CLIENT);
		ctlMsgWriter.send(emuCtlSocketName);

		final int timeout = 1000;
		int numretries = 15;
		
		// Wait for the attached-event from emulator
		while (numretries > 0) {
			if (ctlEvents.await(EventID.CLIENT_ATTACHED, timeout))
				return;  // Notification received!
			
			if (emuBeanState.get() != EmuCompState.EMULATOR_RUNNING) {
				LOG.warning("Expected state changed, abort attaching client to emulator!");
				return;
			}
			
			--numretries;
		}
		
		throw new IOException("Attaching client to emulator failed!");
	}
	
	private void detachClientFromEmulator() throws IOException, InterruptedException
	{
		LOG.info("Detaching client from emulator...");

		ctlMsgWriter.begin(MessageType.DETACH_CLIENT);
		ctlMsgWriter.send(emuCtlSocketName);

		// Wait for the detached-event from emulator
		if (!ctlEvents.await(EventID.CLIENT_DETACHED, 10000))
			throw new IOException("Detaching client from emulator failed!");
	}

	private String newCtlSocketName(String suffix)
	{
		return tempDir.getAbsolutePath() + "/sdlonp-ctlsocket-" + suffix;
	}
	

	/**************************************************************************
	 * 
	 * Here be Bindings
	 * 
	 **************************************************************************/

	/**
	 * Resolves a binding location of either the form
	 * binding://binding_id[/path/to/subres] or binding_id[/path/to/subres]. The
	 * binding_id is replaced with the actual filesystem location of the
	 * binding's mountpoint. The possible reference to the subresource is
	 * preserved in the returned string.
	 * 
	 * @param binding
	 *            A binding location
	 * @return The resolved path or null, if the binding cannot
	 *         be found
	 */
	protected String lookupResource(String binding,
			XmountOutputFormat outputFormat)
					throws BWFLAException, IOException	{
		if (binding == null || binding == "") {
			throw new IllegalArgumentException("Binding cannot be null or empty.");
		}
		if (outputFormat == null) {
			throw new IllegalArgumentException("outputFormat cannot be null.");
		}

		if (binding.startsWith("binding://")) {
			binding = binding.substring("binding://".length());
		}

		LOG.info("lookup resource: " + binding);

		// find the most specific key that we can find
		AbstractDataResource resource = null;
		String realBindingId = binding;

		while (resource == null && !realBindingId.isEmpty()) {
			resource = bindings.get(realBindingId);
			if (resource != null) {
				break;
			}

			int lastSlash = realBindingId.lastIndexOf("/");
			if (lastSlash < 0) {
				lastSlash = 0;
			}
			realBindingId = realBindingId.substring(0, lastSlash);
		}

		if (resource == null) {
			throw new BWFLAException("Could not find binding for resource " + binding);
		}

		Path resourceDir = this.tempDir.toPath().resolve("bindings"); 
		try {
			Files.createDirectories(resourceDir);
		} catch (IOException e) {
			throw new IOException("Cannot create bindings directory "
					+ resourceDir.toString() + ".", e);
		}

		// let's see if we have some sub-bindings that we need to resolve
		String subresPath = binding.replace(realBindingId,  "");

		// let's see if we already have the resource mounted
		String resourcePath = null;
		LOG.info("cache lookup: " + realBindingId + (subresPath.isEmpty() ? "" : "/"));

		if (this.bindingsMountCache.containsKey(realBindingId + (subresPath.isEmpty() ? "" : "/"))) {
			return this.bindingsMountCache.get(realBindingId + (subresPath.isEmpty() ? "" : "/")) + subresPath;
		} else {
			XmountOptions xmountOpts = new XmountOptions(outputFormat);
			if (resource instanceof VolatileResource) {
				VolatileResource vResource = (VolatileResource) resource;
				// The resource should be written to in-place, ignoring the
				// value of getAccess(), as it is a temporary copy of user-data

				// (TODO) Currently only file: transport is allowed here
				if(!vResource.getUrl().startsWith("file:"))
				{
					throw new IllegalArgumentException("Only 'file:' "
							+ "transport is allowed for injected objects/VolatileDrives.");
				}
				resourcePath = EmulatorUtils.connectBinding(vResource, resourceDir,xmountOpts);
				vResource.setResourcePath(resourcePath);
				// resourcePath = vResource.getUrl().substring("file://".length());
				if (resourcePath == null || !(new File(resourcePath).canRead())) {
					throw new BWFLAException("Binding target at location "
							+ vResource.getUrl() + " cannot be accessed.");
				}

			} else if (resource instanceof Binding) {
				Binding pResource = (Binding) resource;
				
				if(pResource.getFileSize() > 0)
				{
					LOG.info("Filesize: " + pResource.getFileSize());
					xmountOpts.setSize(pResource.getFileSize());
				}
				
				resourcePath = EmulatorUtils.connectBinding(pResource, resourceDir,
						xmountOpts);
				// resourcePath is now the base path for the binding we want to find
				if (resourcePath == null || !(new File(resourcePath).canRead())) {
					throw new BWFLAException("Binding target at location "
							+ pResource.getUrl() + " cannot be accessed.");
				}
				
				if(pResource.getLocalAlias() != null)
				{
					Path link = resourceDir.resolve(pResource.getLocalAlias());
					Files.deleteIfExists(link);
	                Files.createSymbolicLink(link, Paths.get(resourcePath));
	                resourcePath = link.toString();
				}
			} else {
				LOG.info("unhandled resource");
				return null;
			}
			this.bindingsMountCache.put(realBindingId, resourcePath);
		}

		// mount the iso file if we have subresources
		if (!subresPath.isEmpty()) {
			// fuseiso the resource file and register the path.
			try {
				Path subresMountpoint = resourceDir.resolve(resource.getId().replace("/", "_") + ".subres.fuse");
				EmulatorUtils.mountUdfFile(Paths.get(resourcePath), subresMountpoint);
				LOG.info("Successfully mounted ISO file " + resourcePath + " to "
						+ subresMountpoint.toString());
				// Path.toString() never returns the trailing slash, even for
				// directories. As it *always* is a directory, add it here.

				resourcePath = subresMountpoint.toString() + "/";
				this.bindingsMountCache.put(realBindingId + "/", resourcePath); 
				LOG.info("mount cache add: " + realBindingId + "/ - " + resourcePath);
			} catch (BWFLAException e) {
				// The operation that can fail is mountUdfFile() and it
				// explicitly
				// is ALLOWED to fail. So we can safely ignore this exception.
				LOG.info("Mounting ISO file failed, " + resourcePath
						+ " probably is not a valid ISO file (hence not subresources).");
			}

		}

		// now combine the remainder (subresPath) of the binding with the
		// actual path
		return resourcePath + subresPath;
	}

	protected String lookupResource(String binding, DriveType driveType)
			throws BWFLAException, IOException {
		return this.lookupResource(binding, this.getImageFormatForDriveType(driveType));
	}

	protected void prepareResource(AbstractDataResource resource) throws IllegalArgumentException, IOException, BWFLAException, JAXBException
	{
		if(resource.getId() == null || resource.getId().equals(""))
		{
			throw new IllegalArgumentException("Resource has no or empty id.");
		}

		if (resource instanceof Binding) {
			this.bindings.put(resource.getId(), (Binding)resource);
		} else if (resource instanceof ArchiveBinding) {
			// if the resource is an ArchiveBinding, query the archive and
			// add all entries from the file collection
			ArchiveBinding archive = (ArchiveBinding) resource;
			ObjectArchiveHelper helper = new ObjectArchiveHelper(archive.getArchiveHost());
			FileCollection fc = helper.getObjectReference(archive.getArchive(), archive.getId());
			if (fc == null || fc.label == null)
				throw new BWFLAException("Error retrieving object meta data");

			for (FileCollectionEntry link : fc.files) {
				if (link.getId() == null || link.getUrl() == null)
					continue;

				this.bindings.put(resource.getId() + "/" + link.getId(), link);
			}

		} else {
			throw new IllegalArgumentException("Resource is of unknown type.");
		}
	}

	/**************************************************************************
	 * 
	 * Here be Drives
	 * 
	 **************************************************************************/

	/**
	 * @param drive
	 */
	protected void prepareDrive(Drive drive)
	{
		// All drives *directly* work on a resource (binding) that has been
		// set up earlier, so no mounting, cow-ing or other tricks
		// are necessary here.
		addDrive(drive);

		// String img = null;
		//
		// FIXME: check if this is still necessary after refactoring (if yes,
		// refactor more)
		//
		// if (drive instanceof VolatileDrive) {
		// // The drive should be written to in-place, ignoring the
		// // value of getAccess(), as it is a temporary copy of user-data
		//
		// // (TODO) Currently only file: transport is allowed here
		// if (!drive.getData().startsWith("file:")) {
		// log.
		// warning("Only 'file:' transport is allowed for injected objects/VolatileDrives.");
		// continue;
		// }
		// // just use the file on the filesystem directly as is
		// img = drive.getData().replace("file://", "");
		// } else {

	}

	protected abstract boolean addDrive(Drive drive);

	protected abstract boolean connectDrive(Drive drive, boolean attach);

	/**************************************************************************
	 * 
	 * Here be Networks
	 * 
	 **************************************************************************/

	/**
	 * @param nic
	 */
	protected void prepareNic(Nic nic)
	{
		// create a vde_switch in hub mode
		// the switch can later be identified using the NIC's MAC address
		String switchName = "nic_" + nic.getHwaddress();

		ProcessRunner process = new ProcessRunner("vde_switch");
		process.addArgument("-hub");
		process.addArgument("-s");
		process.addArgument(this.tempDir.toPath().resolve(switchName).toString());
		if(!process.start())
			return; // Failure

		vdeProcesses.add(process);
		this.addNic(nic);
	}

	protected abstract boolean addNic(Nic nic);

	/**************************************************************************
	 * 
	 * Here be native config
	 * 
	 **************************************************************************/

	/**
	 * @param nativeConfig
	 */
	protected void prepareNativeConfig(EmulationEnvironment.NativeConfig nativeConfig)
	{
		if(nativeConfig != null)
		{
			String nativeString = nativeConfig.getValue();
			if(nativeConfig.getLinebreak() != null)
			{
				nativeString = nativeString.replace("\n", "").replace("\r", "");
				nativeString = nativeString.replace(nativeConfig.getLinebreak(), "\n");
			}

			// search for binding:// and replace all occurrences with the
			// actual path
			Pattern p = Pattern.compile("binding://(\\w*/?)");
			Matcher m = p.matcher(nativeString);
			StringBuffer sb = new StringBuffer();
			while(m.find())
			{
				String bindingPath;
				try {
					bindingPath = this.lookupResource(m.group(1), XmountOutputFormat.RAW);
				} catch (Exception e) {
					LOG.severe("lookupResource with " + m.group(1) + " failed.");
					e.printStackTrace();
					continue;
				}
				if(bindingPath == null)
				{
					LOG.severe("lookupResource with " + m.group(1) + " failed.");
					continue;
				}
				LOG.info(m.group(1));
				LOG.info("Replacing " + m.group(0) + " by " + bindingPath);
				m.appendReplacement(sb, bindingPath);
			}
			m.appendTail(sb);

			emuNativeConfig = sb.toString();
		}
	}
}
