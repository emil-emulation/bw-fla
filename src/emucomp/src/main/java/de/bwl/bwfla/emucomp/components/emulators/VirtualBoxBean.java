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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.ejb.Stateful;
import de.bwl.bwfla.common.datatypes.Drive;
import de.bwl.bwfla.common.datatypes.Nic;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.ProcessRunner;
import de.bwl.bwfla.emucomp.conf.EmucompSingleton;


/**
 * 
 * @author Johann Latocha <johann.latocha@rz.uni-freiburg.de>
 * @author Dennis Wehrle <dennis.wehrle@rz.uni-freiburg.de>
 * 
 */
@Stateful
public class VirtualBoxBean extends EmulatorBean
{
	private String vboxmanage_bin = null;
	private File vboxhome;
	
	
	private String vmname = null;
//	private String hddImageWithPath = null;
//	private String importDir = null;
//	private File vboxdir;
	
//	private String unitId = null;
//	private String hddImage = null;
//	private String controllerName = null;
//	private String deviceNumber = null;
//	private String portNumber = null;
//	private String dvdcontrollerName = null;
//	private String dvddeviceNumber = null;
//	private String dvdportNumber = null;
//	private String standardImage = null;


//	@Override
//	protected void prepareEmulatorRunner()
//	{
//		// get VBoxHeadless executable
//		File vboxheadless = new File(properties.getProperty("vboxheadless_executable"));
//		if (vboxheadless == null || !vboxheadless.exists()) {
//			throw new BWFLAException("VBoxHeadless executable not found! Make sure you have specified "
//							+ "a valid path to your executable in the corresponding 'properties' file");
//		}
//
//		importDir = properties.getProperty("import_dir");
//		if (importDir == null)
//			throw new BWFLAException("import_dir property not set. abort!");
//
//		String vboxhome = properties.getProperty("vboxhome");
//		if (vboxhome == null)
//			throw new BWFLAException("vboxhome property not set. abort!");
//
//		vboxdir = new File(vboxhome);
//		if (!vboxdir.exists())
//			vboxdir.mkdir();
//		
//		// form valid filename for the import
//		{
//			Random generator = new Random();
//			vmname = String.valueOf(Math.abs(generator.nextInt()));
//		}
//		
//		ProcessRunner process = new ProcessRunner();
//		process.setCommand("mkdir");
//		process.addArgument("-p");
//		process.addArgument(importDir + vmname);
//		if (process.start())
//			process.waitUntilFinished();
//		
//		process.setCommand("vboxmanage");
//		process.addArgument("setproperty");
//		process.addArgument("machinefolder");
//		process.addArgument(vboxhome);
//		if (process.start())
//			process.waitUntilFinished();
//		
////		String config = this.getNativeConfig();
////		if (config != null) {
////			File ovfConfig = new File(importDir + vmname + File.separator + vmname + ".ovf");
////			try {
////				// check existance
////				Files.write(Base64.decodeBase64(config), ovfConfig);
////				VirtualBoxEnvironment virtualBoxEnvironment = new VirtualBoxEnvironment(ovfConfig);
////				hddImage = virtualBoxEnvironment.getHddImage();
////				controllerName = virtualBoxEnvironment.getStorageControllerName();
////				deviceNumber = virtualBoxEnvironment.getAttachedDeviceDevice();
////				portNumber = virtualBoxEnvironment.getAttachedDevicePort();
////				dvdcontrollerName = virtualBoxEnvironment.getDVDStorageControllerName();
////				dvddeviceNumber = virtualBoxEnvironment.getDVDAttachedDeviceDevice();
////				dvdportNumber = virtualBoxEnvironment.getDVDAttachedDevicePort();
////			} catch (IOException e) {
////				e.printStackTrace();
////			}
////		}
//		
//		// Initialize the process runner
//		runner.setCommand(vboxheadless.getAbsolutePath());
//		runner.addArgument("--vrde on");
//		runner.addArgument("--vrdeproperty");
//		runner.addArgument("TCP/Ports=" + String.valueOf(port));
//		runner.addEnvVariable("VBOX_USER_HOME", vboxhome);
//		
//		this.setupEmulatorForRDP();
//		
//		log.info("VirtualBox set up");
//	}
//

//    @Override
//    protected VolatileDrive allocateDrive(DriveType type, Drive proto)
//    {
//        // Note: Qemu only supports 2 floppy drives (on same bus)
//        //       and 2 ide controllers (with 2 units each).
//
//        VolatileDrive result = new VolatileDrive();
//        result.setType(type);
//        result.setBoot(false);
//        result.setPlugged(true);
//// FIXME
////        result.setTransport(Resource.TransportType.FILE);
//
//        switch (type) {
//        case FLOPPY:
//            result.setIface("floppy");
//            // find first available floppy connector
//
//            // Logic: 2 - (0+1) = 1 => 0 taken, use 1
//            //        2 - (1+1) = 0 => 1 taken, use 0
//            //        2 - (0+1) - (1+1) = -1 => both taken, no drive available
//            int possibleUnit = 2;
//            for (Drive d : this.emuEnvironment.getDrive()) {
//                if (d.getType().equals(Drive.DriveType.FLOPPY)) {
//                    possibleUnit -= Integer.parseInt(d.getUnit()) + 1;
//                }
//            }
//            if (possibleUnit >= 2) {
//                // all connectors available, use first
//                possibleUnit = 0;
//            }
//            if (possibleUnit < 0) {
//                // no drive available
//                return null;
//            }
//
//            result.setBus("0");
//            result.setUnit(Integer.toString(possibleUnit));
//
//            return result;
//        case DISK:
//        case CDROM:
//            // HDDs and CD drives both go to the ide bus
//            result.setIface("ide");
//
//            // same logic as for floppy drives, only for two busses now
//            int possibleBus0 = 2;
//            int possibleBus1 = 2;
//            for (Drive d : this.emuEnvironment.getDrive())
//            {
//                if (d.getIface().equals("ide"))
//                {
//                    if (d.getBus().equals("0")) {
//                        possibleBus0 -= Integer.parseInt(d.getUnit()) + 1;
//                    }
//                    if (d.getBus().equals("1")) {
//                        possibleBus1 -= Integer.parseInt(d.getUnit()) + 1;
//                    }
//                }
//            }
//            if (possibleBus0 >= 2) {
//                possibleBus0 = 0;
//            }
//            if (possibleBus1 >= 2) {
//                possibleBus1 = 0;
//            }
//            if (possibleBus0 == 0 || possibleBus0 == 1) {
//                // connector on bus 0 available
//                result.setBus("0");
//                result.setUnit(Integer.toString(possibleBus0));
//            } else if (possibleBus1 == 0 || possibleBus1 == 1) {
//                // connector on bus 1 available
//                result.setBus("1");
//                result.setUnit(Integer.toString(possibleBus1));
//            } else {
//                // no ide drive available
//                return null;
//            }
//            
//            return result;
//        }
//        return null;
//    }
	
	@Override
	protected void prepareEmulatorRunner() throws BWFLAException {
		// setup bean prerequisites
		File file = new File(EmucompSingleton.CONF.vboxBean.vboxManageExec);
		if (file == null || !file.exists())
			throw new BWFLAException(
					"VBoxManage executable not found! Make sure you have specified a valid path to your "
							+ "executable in the corresponding 'properties' file");
		vboxmanage_bin = file.getAbsolutePath();
		vboxhome = new File(System.getProperty("java.io.tmpdir") + File.separator + "vbox");
		if (!vboxhome.exists()) {
			if (!vboxhome.mkdirs()) {
				throw new BWFLAException("Failed to create temporary vboxhome directory: " + vboxhome.toString());
			}
		}
		
		// create and register new VM
		vmname = this.tempDir.getName();
		vboxmanage("createvm", "--name", vmname,
				               "--basefolder", tempDir.getAbsolutePath(),
				               "--register");
        // set boot order
        vboxmanage("modifyvm", vmname, "--boot1", "disk",
                                       "--boot2", "dvd",
                                       "--boot3", "floppy",
                                       "--boot4", "net");

		if (this.emuEnvironment.getEmulator() != null &&
		    this.emuEnvironment.getEmulator().getMachine() != null &&
		    this.emuEnvironment.getEmulator().getMachine().getValue() != null) {
			String machDef = this.emuEnvironment.getEmulator().getMachine().getValue();
			for (String parameter : machDef.split("(\r?\n)+")) {
				if (parameter.trim().isEmpty()) {
					continue;
				}
				String[] args = parameter.trim().split("\\s+", 2);
				if (args.length > 1) {
					if (args[0].startsWith("--")) {
						vboxmanage("modifyvm", vmname, args[0], args[1]);
					} else {
						vboxmanage("modifyvm", vmname, "--" + args[0], args[1]);
					}
				} else if (args.length == 1) {
					vboxmanage("modifyvm", vmname, args[0], "");
				}
			}
		} else {
			// default vm configuration
			vboxmanage("modifyvm", vmname,  "--hwvirtex", "on",
											"--memory", "1024",
											"--ioapic", "on",
											"--mouse", "usbtablet",
											"--audio", "alsa",
											"--audiocontroller", "ac97");
		}
		vboxmanage("storagectl", vmname, "--name", "ide_1",
										 "--add", "ide",
										 "--bootable", "on");
		vboxmanage("storagectl", vmname, "--name", "fdc_1",
									     "--add", "floppy",
									     "--bootable", "on");

		// apply native configuration
		String nativeConfig = this.getNativeConfig();
		if (nativeConfig != null && !nativeConfig.isEmpty()) {
			String[] tokens = nativeConfig.trim().split("\n");
			for (String token : tokens)
			{
				token = token.trim();
				if(token.isEmpty())
						continue;
				vboxmanage(token.replace("$VMUUID", vmname).split("\\s+"));
			}
		}
		
		// setup emulator runner for the new VM
		// get VBoxHeadless executable
		File vboxheadless = new File(EmucompSingleton.CONF.vboxBean.vboxHeadlessExec);
		if (vboxheadless == null || !vboxheadless.exists()) {
			throw new BWFLAException("VBoxHeadless executable not found! Make sure you have specified "
							+ "a valid path to your executable in the corresponding 'properties' file");
		}
		
		runner.setCommand(vboxheadless.getAbsolutePath());
		runner.addArgument("--startvm");
		runner.addArgument(vmname);
		if (this.isLocalModeEnabled()) {
			runner.addArgument("--fullscreen");
			runner.addArgument("--fullscreenresize");
		}

		// HINT: if not running vboxsdl, configure and use the
		// 		 VRDE/RDP remote desktop protocol
		if (this.isLocalModeEnabled())
			this.setupEmulatorForY11();
		else if (vboxheadless.toString().toLowerCase().contains("vboxsdl")) {
			this.setupEmulatorForSDLONP();
		} else {
			throw new BWFLAException("unsupported protocol type: " + super.protocol);
		}

		this.runner.addEnvVariable("VBOX_USER_HOME", vboxhome.getAbsolutePath());
	}
	
	@Override
	public void destroy() {
		vboxmanage("unregistervm", vmname);
		super.destroy();
	}
	

	protected void vboxmanage(List<String> args) {
		ProcessRunner proc = new ProcessRunner(this.vboxmanage_bin);
		proc.addArguments(args);
		proc.addEnvVariable("VBOX_USER_HOME", vboxhome.getAbsolutePath());
		proc.execute();
	}
	
	protected void vboxmanage(String...args) {
		vboxmanage(new ArrayList<String>(Arrays.asList(args)));
	}
	
	@Override
	protected boolean addDrive(Drive drive) {
		if (drive == null) {
			LOG.warning("Drive is null, attach cancelled.");
			return false;
		}
		String imagePath = this.lookupResource(drive.getData());
		if (imagePath == null) {
			LOG.warning("Drive doesn't reference a valid binding, attach cancelled.");
			return false;
		}
		
		List<String> args = new ArrayList<String>();
		args.add("storageattach");
		args.add(vmname);

		switch (drive.getType()) {
		case FLOPPY:
			try {
				Path link = this.tempDir.toPath().resolve(
						Paths.get(imagePath).getFileName().toString() + ".img");
				Files.createSymbolicLink(link, new File(imagePath).toPath());
				imagePath = link.toString();
			} catch (IOException e) {
				LOG.warning("Cannot create .img link for vbox, attach cancelled.");
				return false;
			}			
			args.add("--storagectl");
			args.add("fdc_1");
			
			args.add("--type");
			args.add("fdd");

			break;

		case DISK:
			args.add("--storagectl");
			args.add("ide_1");
			
			args.add("--type");
			args.add("hdd");

			args.add("--setuuid");
			args.add("");
			
			break;

		case CDROM:
			try {
				Path link = this.tempDir.toPath().resolve(
						Paths.get(imagePath).getFileName().toString() + ".iso");
				Files.createSymbolicLink(link, new File(imagePath).toPath());
				imagePath = link.toString();
			} catch (IOException e) {
				LOG.warning("Cannot create .iso link for vbox, attach cancelled.");
				return false;
			}			
			args.add("--storagectl");
			args.add("ide_1");
			
			args.add("--type");
			args.add("dvddrive");

			args.add("--setuuid");
			args.add("");
			
			break;


		default:
			LOG.severe("Device type '" + drive.getType() + "' not supported yet.");
			return false;
		}
		args.addAll(Arrays.asList("--port", drive.getBus(),
                                  "--device", drive.getUnit(),
                                  "--medium", imagePath
                                 ));
		
		vboxmanage(args);
		return true;
	}

	@Override
	protected boolean connectDrive(Drive drive, boolean attach) {
		LOG.warning("Hotplug is not yet supported by this emulator.");
		// TODO implement hotplug
		return false;
	}
	
	@Override
	protected boolean addNic(Nic nic) {
		if (nic == null) {
			LOG.warning("NIC is null, attach canceled.");
			return false;
		}
		
		vboxmanage("modifyvm", vmname, "--nic1", "generic");
		vboxmanage("modifyvm", vmname, "--nicgenericdrv1", "VDE");
		vboxmanage("modifyvm", vmname, "--nicproperty1", "network=" + this.tempDir.toPath().resolve("nic_" + nic.getHwaddress()));
		vboxmanage("modifyvm", vmname, "--macaddress1", nic.getHwaddress().replace(":", ""));

		return true;
	}
//
//	@Override
//	protected ProcessRunner getEmulatorRunner() {
//		List<String> cmd = new ArrayList<String>();
//		
//		this.setupEmulatorForRDP();
//
//		// get VBoxSDL executable
//		File file = new File(properties.getProperty("vboxheadless_executable"));
//		if (file == null || !file.exists())
//			throw new BWFLAException(
//					"VBoxSDL executable not found! Make sure you have specified a valid path to your "
//							+ "executable in the corresponding 'properties' file");
//
//		cmd.add(0, file.getAbsolutePath());
//		cmd.add("--startvm");
//		cmd.add(vmname);
//		
//		// only necessary (and only possible) when using VBoxHeadless
//		// TODO (hint): remove these 4 lines if you switch to VBoxSDL
//		cmd.add("--vrde");
//		cmd.add("on");
//		cmd.add("--vrdeproperty");
//		cmd.add("TCP/Ports=" + String.valueOf(port));
//
//		log.info("Command: " + cmd.toString());
//		runner = new ProcessRunner(cmd);
//		return runner;
//	}
// 
//	@Override
//	protected void addNativeConfig(String config) {
//
//	@Override
//	protected void prepareDrive(Drive drive)
//	{
//		super.prepareDrive(drive);
//		if (drive.getData().equals(""))
//			return;
//		
//		if (drive.getType().equals(Drive.DriveType.DISK)) {
//			File img = new File(this.lookupResource(drive.getData()));
//			File vbox = this.prepareVboxImage(img);
//			
//			// Set the path to VM for the runner
//			String vboxPath = vbox.getAbsolutePath();
//			runner.addArgument("-s");
//			runner.addArgument(vboxPath);
//			
//			// this.images.put(drive.getUrl(), img.toPath());
////				drive.setFile(vbox);
//			// coldPlug(drive);
//		}
//		else
//			connectDrive(drive, true);
//	}
//	
//	private File prepareVboxImage(File img) 
//	{
//		if (img == null || !img.exists())
//			return null;
//
////		hddImageWithPath = img.getParent() + File.separator + hddImage;
//		hddImageWithPath = img.getAbsolutePath();
//
//		ProcessRunner process = new ProcessRunner(vboxmanage);
//		process.addArgument("internalcommands");
//		process.addArgument("sethduuid");
//		process.addArgument(hddImageWithPath);
//		if (process.start())
//			process.waitUntilFinishedVerbose();
//		
//		// copy standard image to importDir + rename it appropriately (name is extracted from the .ovf config)
//		
////		_cmd.add("cp");
////		_cmd.add(standardImage);
////		_cmd.add(importDir + vmname + File.separator + hddImage);
////		pr = doRun(_cmd);
////		if(pr == null)
////			return null;
////		_cmd.clear();
////		
////		// get unit id
////		// we need this id in order to define the right storagepath for the hdd image
//////		String cmd = vboxmanage + " import -n " + img.getAbsolutePath();
////		_cmd.add(vboxmanage);
////		_cmd.add("import");
////		_cmd.add("-n");
////		_cmd.add(importDir + vmname + File.separator + vmname + ".ovf"); 
////		pr = doRun(_cmd);
////		if(pr == null)
////			return null;
////		_cmd.clear();
////		
////		String result = pr.getProcessOutputAsString();
////		int index = result.indexOf("Hard disk image");
////		String out =  result.substring(index - 4, index - 2);
////		unitId = out.replaceAll("[\\D]", "");
////		log.info("unit id = " + unitId);
//
//		final String disk = vboxdir + File.separator + vmname + File.separator + vmname;
//		
////		// import virtualbox appliance
//		process.setCommand(vboxmanage);
//		process.addArgument("import");
//		// option to keep all nat mac adresses
//		process.addArgument("--options keepnatmacs");
//		process.addArgument(importDir + vmname + File.separator + vmname + ".ovf");
//		process.addArgument("--vsys 0");
//		process.addArgument("--vmname");
//		process.addArgument(vmname);
////		process.addArgument("--unit 0");
////		process.addArgument("--disk");
////		process.addArgument(disk + ".vmdk");
//		if (!process.start() || (process.waitUntilFinishedVerbose() != 0))
//			return null;
//		
////		process.setCommand(vboxmanage);
////		process.addArgument("storageattach");
////		process.addArgument(vmname);
////		process.addArgument("--storagectl");
////		process.addArgument(controllerName);
////		process.addArgument("--port");
////		process.addArgument(portNumber);
////		process.addArgument("--device");
////		process.addArgument(deviceNumber);
////		process.addArgument("--type");
////		process.addArgument("hdd");
////		process.addArgument("--medium");
////		process.addArgument("emptydrive");
////		process.addArgument.add("--forceunmount");
////		if (!process.start() || (process.waitUntilFinishedVerbose() != 0))
////			return null;			
////			
////		// remove standard hdd image from virtualbox
////		// WE HAVE TO DO THIS, OTHERWISE WE CAN'T IMPORT THIS APPLIANCE TWICE!!!
////		// THIS IS BECAUSE IF VIRTUALBOX KNOWS THIS DISK, IT WON'T IMPORT THE APPLIANCE.
////		process.setCommand("vboxmanage");
////		process.addArgument("closemedium");
////		process.addArgument("disk");
////		process.addArgument(disk + ".vmdk");
////		if (!process.start() || (process.waitUntilFinishedVerbose() != 0))
////			return null;	
////		
////		// attach real hdd image as multitype image
////		String cmd = vboxmanage + " storageattach " +  vmname + " --storagectl " + controllerName + " --port " + portNumber 
////				+ " --device " + deviceNumber + " --type hdd --medium " + hddImageWithPath + " --mtype multiattach";
//		process.setCommand(vboxmanage);
//		process.addArgument("storageattach");
//		process.addArgument(vmname);
//		process.addArgument("--storagectl IDE");
//		process.addArgument("--port 0");
//		process.addArgument("--device 0");
//		process.addArgument("--type hdd");
//		process.addArgument("--medium");
//		process.addArgument(hddImageWithPath);
//		//	+ " --device " + deviceNumber + " --type hdd --medium " + hddImageWithPath + " --mtype multiattach";
//		
//		if (!process.start())
//			return null;
//		
//		String error = "";
//		try {
//			process.waitUntilFinished(false, false);
//			error = process.getStdErrString();
//			if (!error.isEmpty())
//				log.info("Process STDERR:  " + error);
//		}
//		catch (IOException e) {
//			e.printStackTrace();
//		}
//		
//		process.cleanup();
//		
//		if (error.contains("is locked for reading by another task") || error.contains("Cannot register the hard disk"))
//		{
//			log.info("this hdd image is already used => just attach this image (without multiattach)");
//			
//			process.setCommand(vboxmanage);
//			process.addArgument("storageattach");
//			process.addArgument(vmname);
//			process.addArgument("--storagectl IDE");
//			process.addArgument("--port 0");
//			process.addArgument("--device 0");
//			process.addArgument("--type hdd");
//			process.addArgument("--medium");
//			process.addArgument(hddImageWithPath);
////				" --device " + deviceNumber + " --type hdd --medium " + hddImageWithPath;
//			
//			if (!process.start() || (process.waitUntilFinishedVerbose() != 0))
//				return null;
//		}
//		
//		File vboxresult = new File(disk + ".vbox");
//		return (vboxresult.exists() && vboxresult.isFile()) ? vboxresult : null;
//	}
//
//	@Override
//	public boolean stop() 
//	{
//		// force vm shutdown
//		ProcessRunner process = new ProcessRunner(vboxmanage);
//		process.addEnvVariables(runner.getEnvVariables());
//		process.addArgument("controlvm");
//		process.addArgument(vmname);
//		process.addArgument("poweroff");
//		if (process.start())
//			process.waitUntilFinishedVerbose();
//		
//		return super.stop();
//	}
//	
//	@Override
//	@PreDestroy
//	public void freeResources() 
//	{
//		ProcessRunner process = new ProcessRunner();
//		process.addEnvVariables(runner.getEnvVariables());
//
////		/* check if the vm is still running */
////		// force vm shutdown
////		process.setCommand(vboxmanage, true);
////		process.addArgument("controlvm");
////		process.addArgument(vmname);
////		process.addArgument("poweroff");
////		if (process.start())
////			process.waitUntilFinished();
//
////		// we have to wait until the vm is powerd off, otherwise unregister will fail
////		_cmd.add("sleep");
////		_cmd.add("2");
////		log.info(_cmd.toString());
////		pr = new ProcessRunner(_cmd);
////		t = new Thread(pr);
////		pr.setEnviroment(this.exeEnvironment);
////		t.start();
////		t.join();
////		_cmd.clear();
//		
//		// unregister and delete vm
//		process.setCommand(vboxmanage, true);
//		process.addArgument("unregistervm");
//
//		// --delete will try to also remove the harddisk image file
//		// which qemu-fuse will not allow, so clean up the stuff later
//
//		// process.addArgument("--delete");
//		process.addArgument(vmname);
//		if (process.start())
//			process.waitUntilFinishedVerbose();
//
//		// remove "root" hdd image (the image which was imported with mtype = multiattach)
//		// Problem: we don't know when to remove the image, because it could be possible that
//		// an other instance is still using this image ....
//		// This will fail until the last instance removes this image successfully.
//		process.setCommand(vboxmanage, true);
//		process.addArgument("closemedium");
//		process.addArgument("disk");
//		process.addArgument(hddImageWithPath);
//		if (process.start())
//			process.waitUntilFinishedVerbose();
//
//		// remove virtualbox VM configuration directory
//		// remove temporary dir
//		File confDir = this.vboxdir.toPath().resolve(vmname).toFile();
//		if (confDir != null && confDir.exists()) {
//			try {
//				FileUtils.deleteDirectory(confDir);
//			} catch (IOException e) {
//				log.severe(e.getMessage());
//			}
//		}
//
//		// the super method also cleans up the harddisk image and the
//		// fuse instances
//		super.freeResources();
//	}
//
//	protected void vboxmanage(String...args)
//	{
//		ProcessRunner process = new ProcessRunner(vboxmanage);
//		process.addEnvVariables(runner.getEnvVariables());
//		for (String arg : args)
//			process.addArgument(arg);
//		
//		if (process.start())
//			process.waitUntilFinishedVerbose();
//	}
//
//	@Override
//	protected boolean addNic(Nic nic) {
//		if (nic == null) {
//			log.warning("NIC is null, attach canceled.");
//			return false;
//		}
//		
//		vboxmanage("modifyvm", vmname, "--nic1", "generic");
//		vboxmanage("modifyvm", vmname, "--nicgenericdrv1", "VDE");
//		vboxmanage("modifyvm", vmname, "--nicproperty1", "network=" + this.tempDir.toPath().resolve("nic_" + nic.getHwaddress()));
//		vboxmanage("modifyvm", vmname, "--macaddress1", nic.getHwaddress().replace(":", ""));
//
//		return true;
//	}
}
