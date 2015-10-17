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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;
import java.net.Socket;
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
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.activation.DataHandler;
import javax.xml.bind.JAXBException;
import org.apache.commons.io.FileUtils;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.protocol.GuacamoleClientInformation;
import org.glyptodon.guacamole.protocol.GuacamoleConfiguration;
import de.bwl.bwfla.common.datatypes.AbstractCredentials;
import de.bwl.bwfla.common.datatypes.ConnectionType;
import de.bwl.bwfla.common.datatypes.Drive;
import de.bwl.bwfla.common.datatypes.EmuCompState;
import de.bwl.bwfla.common.datatypes.EmulationEnvironment;
import de.bwl.bwfla.common.datatypes.Html5Options;
import de.bwl.bwfla.common.datatypes.InputOptions;
import de.bwl.bwfla.common.datatypes.NetworkEndpoint;
import de.bwl.bwfla.common.datatypes.Nic;
import de.bwl.bwfla.common.datatypes.ProcessMonitorVID;
import de.bwl.bwfla.common.datatypes.Resource;
import de.bwl.bwfla.common.datatypes.UiOptions;
import de.bwl.bwfla.common.datatypes.VdeNetworkEndpoint;
import de.bwl.bwfla.common.datatypes.VolatileResource;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.exceptions.IllegalEmulatorStateException;
import de.bwl.bwfla.common.interfaces.EmulatorComponent;
import de.bwl.bwfla.common.services.container.types.Container;
import de.bwl.bwfla.common.services.guacplay.GuacDefs;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.ExtOpCode;
import de.bwl.bwfla.common.services.guacplay.GuacDefs.SourceType;
import de.bwl.bwfla.common.services.guacplay.capture.ScreenShooter;
import de.bwl.bwfla.common.services.guacplay.net.GuacInterceptorChain;
import de.bwl.bwfla.common.services.guacplay.net.GuacTunnel;
import de.bwl.bwfla.common.services.guacplay.net.PlayerTunnel;
import de.bwl.bwfla.common.services.guacplay.net.TunnelConfig;
import de.bwl.bwfla.common.services.guacplay.protocol.InstructionBuilder;
import de.bwl.bwfla.common.services.guacplay.record.SessionRecorder;
import de.bwl.bwfla.common.utils.BwflaFileDataSource;
import de.bwl.bwfla.common.utils.EmulatorUtils;
import de.bwl.bwfla.common.utils.NetworkUtils;
import de.bwl.bwfla.common.utils.ProcessMonitor;
import de.bwl.bwfla.common.utils.ProcessRunner;
import de.bwl.bwfla.common.utils.SystemEnvironmentHelper;
import de.bwl.bwfla.emucomp.components.EaasComponentBean;
import de.bwl.bwfla.emucomp.conf.EmucompSingleton;
import eu.planets_project.services.datatypes.DigitalObject;



/**
 * @author iv1004
 * 
 */
public abstract class EmulatorBean extends EaasComponentBean implements EmulatorComponent
{	
	private static final long				TMP_FILES_SIZE_LIMIT	= (100L) * 1024L * 1024L * 1024L;

	protected final TunnelConfig			tunnelConfig			= new TunnelConfig();
	protected GuacTunnel					tunnel;

	protected Map<String, IConnector>		viewConnectors			= new ConcurrentHashMap<>();

	protected EmulatorBeanState				emuBeanState			= new EmulatorBeanState(EmuCompState.EMULATOR_UNDEFINED);

	protected EmulationEnvironment			emuEnvironment;
	private String							emuNativeConfig;
	protected Map<Integer, Container>		containers				= Collections.synchronizedMap(new HashMap<Integer, Container>());

	protected final ProcessRunner			runner					= new ProcessRunner();
	protected ArrayList<ProcessRunner>		vdeProcesses			= new ArrayList<ProcessRunner>();

	protected Map<String, String>			bindings				= Collections.synchronizedMap(new HashMap<String, String>());
	protected ArrayList<NetworkEndpoint>	networkConnections		= new ArrayList<NetworkEndpoint>();
	protected String						protocol;

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

	/** If true then SDLONP-Protocol will use domain-sockets, else named pipes.  */
	private static final boolean USE_DOMAINSOCKET_TRANSPORT = true;

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

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Client API
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String getEmulatorState()
	{
		return emuBeanState.fetch().value();
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

	public void destroy()
	{
		boolean unstopped = false;

		synchronized (emuBeanState)
		{
			final EmuCompState curstate = emuBeanState.get();
			if (curstate == EmuCompState.EMULATOR_UNDEFINED)
				return;

			if (curstate == EmuCompState.EMULATOR_BUSY) {
				LOG.severe("Destroying EmulatorBean while other operation is in-flight!");
				return;
			}

			else if (curstate == EmuCompState.EMULATOR_RUNNING);
			unstopped = true;

			emuBeanState.set(EmuCompState.EMULATOR_UNDEFINED);
		}

		if (unstopped)
			this.stopInternal();

		// free container IDs and remove corresp. files
		for(Container container: containers.values())
			container.getFile().delete();

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

		ProcessRunner process = new ProcessRunner();
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

		// Stop screenshot-tool
		if (scrshooter != null)
			scrshooter.finish();

		// Stop and finalize session-recording
		if (recorder != null && !recorder.isFinished())
			try 
		{
				recorder.finish();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}


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
		}

		if (!this.waitUntilEmulatorReady()) {
			LOG.severe("Emulator was not started properly!");
			emuBeanState.update(EmuCompState.EMULATOR_FAILED);
			return;
		}

		LOG.info("Emulator started in process " + runner.getProcessId());

		final Thread observer = new Thread()
		{
			@Override
			public void run() 
			{
				runner.waitUntilFinished();
				runner.printStdOut();
				runner.printStdErr();
				runner.cleanup();

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
		observer.start();
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

		runner.stop();

		if (tunnel != null && tunnel.isOpen()) {
			try {
				tunnel.disconnect();
				tunnel.close();
			}
			catch (GuacamoleException e) {
				e.printStackTrace();
			}
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
			e.printStackTrace();
			throw new BWFLAException("an error occured at server during return data serialization");
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
			case HTML:
				// TODO: analyze and use credentials
				String cookie = this.getSessionId();
				UiOptions uiopts = this.emuEnvironment.getUiOptions();
				Html5Options htmlopts = (uiopts != null) ? uiopts.getHtml5() : null;
				boolean pointerLockActive = (htmlopts != null) && htmlopts.isPointerLock();
				GuacTunnel clientTunnel = (player != null) ? player.getPlayerTunnel() : this.tunnel;
				connector = new HtmlConnector(cookie, clientTunnel, pointerLockActive);
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
		synchronized (emuBeanState)
		{
			final EmuCompState curstate = emuBeanState.get();
			if (curstate == EmuCompState.EMULATOR_UNDEFINED) {
				String message = "Cannot save environment!";
				throw new IllegalEmulatorStateException(message, curstate);
			}
		}

		Resource res = null;
		for(Resource r : this.emuEnvironment.getBinding())
		{
			if(!(r instanceof VolatileResource) && r.getId().equals("main_hdd"))
			{
				res = r;
				break;
			}
		}

		if(res == null)
			return null;

		// get actual image path instead of fused
		String resStr = this.lookupResource(res.getId()); 
		if(resStr == null)
		{
			LOG.severe("could not resolve resource " + res.getId());
			return null;
		}

		Path diffPath = Paths.get(resStr);
		switch(res.getAccess())
		{
		case COW:
			// the real cow2 image is one directory up
			diffPath = diffPath.resolveSibling("../" + diffPath.getFileName());
			break;

		case COPY:
			// do nothing, we're already working directly on the real file
			break;
		}

		this.emuEnvironment.getDescription().setTitle(envName);
		String conf = this.getRuntimeConfiguration();
		LOG.info(conf);
		LOG.info(diffPath.toString());

		SystemEnvironmentHelper envHelper = new SystemEnvironmentHelper(wsHost);
		return envHelper.registerEnvironment(conf, diffPath.toFile(), type);
	}

	@Override
	public int changeMedium(int containerId, String objReference) throws BWFLAException
	{
		// TODO: change disk in run-time
		return -1;
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
		Drive drive = this.allocateDrive(type);
		
		if (drive == null)
			throw new BWFLAException("No more free slots of this type are available: " + type);

		File objFile;
		try
		{
			objFile = this.writeToTmpDir(data.getInputStream(), "digital_object_");
		}
		catch(IOException e)
		{
			e.printStackTrace();
			throw new BWFLAException("an error occured while opening data stream or writing it to file");
		}

		Container container = new Container();
		
		synchronized(container)
		{
			container.setFile(objFile);
			int id = allocateContainerId(container);
			if(id == -1)
				throw new BWFLAException("could not allocate container ID");

			Resource res = new VolatileResource();
			res.setUrl("file://" + objFile.getAbsolutePath());
			res.setId("attached_container_" + id);
			this.prepareResource(res);
			this.emuEnvironment.getBinding().add(res);

			drive.setData("binding://" + res.getId());
			this.emuEnvironment.getDrive().add(drive);

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

		Container container = containers.remove(containerId);
		if (container == null)
			throw new BWFLAException("no container found by this container id: " + containerId);

		synchronized(container)
		{
			// TODO: implement normal- and hot-unplugging of emulator block devices, depending on the case
			if(container.getFile().isFile())
				return new DataHandler(new BwflaFileDataSource(container.getFile()));
			else
				throw new BWFLAException("detached container is in format (FS-directory), which is currently not supported for detachment");
		}
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
		this.emuEnvironment = environment;
		for(Resource resource: emuEnvironment.getBinding())
			prepareResource(resource);

		EmulationEnvironment.NativeConfig nativeConfig = emuEnvironment.getNativeConfig();
		this.prepareNativeConfig(nativeConfig);
		this.prepareEmulatorRunner();
		
		for(Drive drive: emuEnvironment.getDrive())
			prepareDrive(drive);

		for(Nic nic: emuEnvironment.getNic())
			prepareNic(nic);			
	}

	protected String getNativeConfig()
	{
		return emuNativeConfig;
	}

	/** Must be overriden by subclasses to initialize the emulator's command. */
	protected abstract void prepareEmulatorRunner() throws BWFLAException;

	/** Setups the emulator's environment variables for running locally. */
	protected void setupEmulatorForY11()
	{
		protocol = PROTOCOL_Y11;

		final String tmpdir = tempDir.toString();
		final String emusocket = Paths.get(tmpdir, "sdlonp-emu-iosocket").toString();
		
		// Setup emulator's tunnel
		final GuacamoleConfiguration gconf = tunnelConfig.getGuacamoleConfiguration();
		gconf.setProtocol(PROTOCOL_SDLONP);
		gconf.setParameter("enable-audio", "false");
		gconf.setParameter("emu-iosocket", emusocket);

		// Setup emulator's environment
		runner.addEnvVariable("SDL_VIDEODRIVER", protocol);
		runner.addEnvVariable("SDL_IOSOCKET", emusocket);

		UiOptions uiopts = emuEnvironment.getUiOptions();
		if (uiopts == null)
			return;

		Html5Options html5 = uiopts.getHtml5();
		if (html5 == null)
			return;

		if (html5.isPointerLock())
			runner.addEnvVariable("SDL_RELATIVE_MOUSE", "true");
	}

	/** Setups the emulator's environment variables and tunnel for SDLONP-Protocol. */
	protected void setupEmulatorForSDLONP()
	{
		protocol = PROTOCOL_SDLONP;

		final String tmpdir = tempDir.toString();
		final String emusocket = Paths.get(tmpdir, "sdlonp-emu-iosocket").toString();
		final String emuinput = Paths.get(tmpdir, "sdlonp-emu-input").toString();
		final String emuoutput = Paths.get(tmpdir, "sdlonp-emu-output").toString();

		// Setup emulator's tunnel
		final GuacamoleConfiguration gconf = tunnelConfig.getGuacamoleConfiguration();
		gconf.setProtocol(protocol);
		gconf.setParameter("enable-audio", "true");
		if (!USE_DOMAINSOCKET_TRANSPORT) {
			gconf.setParameter("emu-input", emuinput);
			gconf.setParameter("emu-output", emuoutput);
		}
		else gconf.setParameter("emu-iosocket", emusocket);

		// Setup client configuration
		final GuacamoleClientInformation ginfo = tunnelConfig.getGuacamoleClientInformation();
		ginfo.getAudioMimetypes().add("audio/ogg");

		// Setup emulator's environment
		runner.addEnvVariable("SDL_AUDIODRIVER", protocol);
		runner.addEnvVariable("SDL_VIDEODRIVER", protocol);
		if (!USE_DOMAINSOCKET_TRANSPORT) {
			runner.addEnvVariable("SDL_INPUTSTREAM", emuinput);
			runner.addEnvVariable("SDL_OUTPUTSTREAM", emuoutput);
		}
		else runner.addEnvVariable("SDL_IOSOCKET", emusocket);

		UiOptions uiopts = emuEnvironment.getUiOptions();
		if (uiopts != null)
		{
			Html5Options html5 = uiopts.getHtml5();
			if (html5 != null)
			{
				if (html5.isPointerLock())
					runner.addEnvVariable("SDL_RELATIVE_MOUSE", "true");

				String crtopt = html5.getCrt();
				if (crtopt != null && !crtopt.isEmpty()) {
					runner.addEnvVariable("SDL_NTSCFILTER", "snes-ntsc");
					runner.addEnvVariable("SDL_NTSCPRESET", "composite");	
				}
			}
			
			InputOptions input = uiopts.getInput();
			if(input != null)
			{
				if(input.getEmulatorKbdModel() != null && !input.getEmulatorKbdModel().isEmpty())
					runner.addEnvVariable("SDL_KBMODEL", input.getEmulatorKbdModel());
				
				if(input.getEmulatorKbdLayout() != null && !input.getEmulatorKbdLayout().isEmpty())
					runner.addEnvVariable("SDL_KBLAYOUT", input.getEmulatorKbdLayout());
				
				if(input.getClientKbdModel() != null && !input.getClientKbdModel().isEmpty())
					runner.addEnvVariable("SDL_CLIENT_KBMODEL", input.getClientKbdModel());
				
				if(input.getClientKbdLayout() != null && !input.getClientKbdLayout().isEmpty())
					runner.addEnvVariable("SDL_CLIENT_KBLAYOUT", input.getClientKbdLayout());
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

		PlayerTunnel clientTunnel = (headless) ? null : new PlayerTunnel(MESSAGE_BUFFER_CAPACITY);
		player = new SessionPlayerWrapper(file, clientTunnel);

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

	protected File writeImgToTmpDir(File img)
	{
		if(img == null || !img.exists())
			return null;

		File result = null;
		try
		{
			InputStream in = new FileInputStream(img);
			result = writeToTmpDir(in, img.getName());
			in.close();

		}
		catch(IOException e)
		{
			LOG.severe(e.getMessage());
		}
		return result;
	}

	protected File writeDoToTmpDir(DigitalObject obj)
	{
		if(obj == null || obj.getContent() == null)
			return null;

		String name = obj.getTitle();
		if(name == null || name.isEmpty())
			name = "digitalobject";

		File result = null;
		try
		{
			InputStream in = obj.getContent().getInputStream();
			result = writeToTmpDir(in, name);
			in.close();
		}
		catch(IOException e)
		{
			LOG.severe(e.getMessage());
		}

		return result;
	}

	protected File writeToTmpDir(InputStream istream, String name) throws IOException
	{
		final int BUFFER_SIZE = 1024 * 1024;   // == 1MB
		
		File tmpfile = File.createTempFile(name, null, this.tempDir);
		OutputStream ostream = new FileOutputStream(tmpfile);
		byte[] buffer = new byte[BUFFER_SIZE];
		int numBytesReadTotal = 0;
		int numBytesRead = 0;
		
		try {
			// Try to copy input bytes to the output
			while ((numBytesRead = istream.read(buffer)) > 0) {
				numBytesReadTotal += numBytesRead;
				if (numBytesReadTotal > TMP_FILES_SIZE_LIMIT)
					throw new IOException("Input data exceeds the allowed limit of " + TMP_FILES_SIZE_LIMIT + " bytes.");
				
				ostream.write(buffer, 0, numBytesRead);
			}
			
			ostream.flush();
		}
		catch (IOException exception) {
			// Something gone wrong! Retries are not possible here,
			// since we don't know much about the stream's state.
			final String filename = tmpfile.getAbsolutePath();
			LOG.severe("Writing input-data to temporary file '" + filename + "' failed!");
			if (tempDir.getUsableSpace() < (10 * BUFFER_SIZE))
				LOG.info("It seems, that we are running out of disk-space.");
			
			LOG.info("Removing incomplete file '" + filename + "'");
			tmpfile.delete();
			throw exception;
		}
		finally {
			istream.close();
			ostream.close();
		}
		
		return tmpfile;
	}

	private int allocateContainerId(Container container)
	{
		int freeId = -1;
		final int MAX_TRIES = 50;
		for(int i = 0; (i < MAX_TRIES) && (freeId == -1); ++i)
		{
			freeId = (new Random()).nextInt();
			if(containers.containsKey(freeId))
				freeId = -1;
			else
				containers.put(freeId, container);
		}

		return freeId;
	}

	/* grr execptions make things complicated */
	private static boolean isPortOpen(Socket sock, InetSocketAddress isa) {
		try {
			sock.connect(isa, 100);
			sock.close();
			return true;
		} catch (IOException e) {
			// log.info(e.getMessage());
			return false;
		}
	}

	private boolean waitForTcpPort(String ip, int port, int maxtrys) {
		for (int i = 0; i < maxtrys; i++) {
			Socket sock = new Socket();
			InetSocketAddress isa = new InetSocketAddress(ip, port);
			if (isPortOpen(sock, isa))
				return true;
			try {
				Thread.sleep(100);
			} catch (Exception e) {
			}
		}
		return false;
	}

	private boolean waitUntilEmulatorReady()
	{
		final long timeout = 300L;
		final int retries = 10;
		
		GuacamoleConfiguration gconf = tunnelConfig.getGuacamoleConfiguration();
		if (protocol.contentEquals(PROTOCOL_SDLONP)) {
			if (USE_DOMAINSOCKET_TRANSPORT) {
				LOG.info("Waiting for emulator's input/output socket to become constructed...");
				String output = gconf.getParameter("emu-iosocket");
				if (!EmulatorBean.checkFileExistence(output, retries, timeout)) {
					LOG.severe("Emulator's input/output socket was not found:  " + output);
					return false;
				}

				LOG.info("Emulator's input/output socket found.");
			}
			else {
				LOG.info("Waiting for emulator's input/output pipes to become constructed...");
				String output = gconf.getParameter("emu-output");
				if (!EmulatorBean.checkFileExistence(output, retries, timeout)) {
					LOG.severe("Emulator's output pipe was not found:  " + output);
					return false;
				}

				LOG.info("Emulator's input/output pipes found.");
			}
		}
		else if (protocol.contentEquals(PROTOCOL_Y11)) {
			LOG.info("Waiting for emulator's input/output socket to become constructed...");
			String output = gconf.getParameter("emu-iosocket");
			if (!EmulatorBean.checkFileExistence(output, retries, timeout)) {
				LOG.severe("Emulator's input/output socket was not found:  " + output);
				return false;
			}

			LOG.info("Emulator's input/output socket found.");
		}
		else {
			LOG.severe("unsupported protocol type: " + protocol);
			return false;
		}
		
		return true;
	}

	private static boolean checkFileExistence(String filename, int retries, long timeout)
	{
		Path file = Paths.get(filename);
		while (--retries >= 0) {

			if (Files.exists(file))
				return true;

			try {
				Thread.sleep(timeout);
			}
			catch (InterruptedException e) {
				break;
			}
		}

		return false;
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
	 * @return The resolved local filesystem path or null, if the binding cannot
	 *         be found
	 */
	protected String lookupResource(String binding)
	{
		if(binding == null)
			return null;

		if(binding.startsWith("binding://"))
			binding = binding.substring("binding://".length());

		int idx = binding.indexOf("/") >= 0 ? binding.indexOf("/") : binding.length() - 1;
		String binding_name = binding.substring(0, idx + 1);
		String subresource = binding.substring(idx + 1);

		String path = this.bindings.get(binding_name);
		if(path == null)
			return null;

		return path + subresource;
	}

	protected boolean prepareResource(Resource resource)
	{
		if(resource.getId() == null || resource.getId().equals(""))
		{
			LOG.warning("Resource has no id, ignoring this binding as it cannot be referenced.");
			return false;
		}

		Path resourceDir = this.tempDir.toPath().resolve("bindings");
		try
		{
			Files.createDirectories(resourceDir);
		}
		catch(IOException e1)
		{
			LOG.severe("Cannot create bindings directory, connecting binding cancelled: " + resourceDir.toString());
			return false;
		}

		String res = null;
		if(resource instanceof VolatileResource)
		{
			// The resource should be written to in-place, ignoring the
			// value of getAccess(), as it is a temporary copy of user-data

			// (TODO) Currently only file: transport is allowed here
			if(!resource.getUrl().startsWith("file:"))
			{
				LOG.warning("Only 'file:' transport is allowed for injected objects/VolatileDrives.");
				return false;
			}
			res = resource.getUrl().substring("file://".length());
		}
		else
		{
			res = EmulatorUtils.connectBinding(resource, resourceDir);
		}

		if(res == null || !(new File(res).canRead()))
		{
			LOG.warning("Binding target at location " + resource.getUrl() + " cannot be accessed.");
			return false;
		}
		this.bindings.put(resource.getId(), res);

		// res can now be used directly as binding://res, for
		// binding://res/-like URLs, try to mount it as ISO container
		// and fail silently if the file is not an ISO

		// fuseiso the resource file and register the path.
		Path subresMountpoint = resourceDir.resolve(resource.getId() + ".subres.fuse");
		if(EmulatorUtils.mountUdfFile(Paths.get(res), subresMountpoint))
		{
			LOG.info("Successfully mounted UDF file " + res + " to " + subresMountpoint.toString());
			// Path.toString() never returns the trailing slash, even for
			// directories. As it *always* is a directory, add it here.
			this.bindings.put(resource.getId() + "/", subresMountpoint.toString() + "/");
		}

		return true;
	}

	/**************************************************************************
	 * 
	 * Here be Drives
	 * 
	 **************************************************************************/

	final protected Drive allocateDrive(Drive.DriveType type)
	{
		for(Drive d: this.emuEnvironment.getDrive()) 
            if(d.getType().equals(type) && (d.getData() == null || d.getData().isEmpty())) 
            	return d;
		
		LOG.info("can't find empty drive for type " + type);
		
		return null;
	}

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
				String bindingPath = this.lookupResource(m.group(1));
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
