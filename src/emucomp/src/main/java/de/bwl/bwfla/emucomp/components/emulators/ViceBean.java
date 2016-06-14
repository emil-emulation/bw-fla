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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import de.bwl.bwfla.common.datatypes.Binding;
import de.bwl.bwfla.common.datatypes.Drive;
import de.bwl.bwfla.common.datatypes.Nic;
import de.bwl.bwfla.common.datatypes.Drive.DriveType;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.EmulatorUtils.XmountOutputFormat;


public abstract class ViceBean extends EmulatorBean
{
	private static final int FLOPPY_DRIVES_NUM = 4;
	private static final int FLOPPY_DRIVE_BASE_ID     = 8;
	
	private int numAttachedDrives = 0;
	
	@Override
	public void prepareEmulatorRunner() throws BWFLAException
	{
		Path images = null;
		try {
			images = Paths.get(tempDir.getAbsolutePath(), "images");
			Files.createDirectory(images);
			
			// Create local alias-links for image bindings,
			// to be used with emulator's attach/detach GUI 
			for (Map.Entry<String, Binding> entry : bindings.entrySet()) {
				final String name = entry.getKey();
				final Binding binding = entry.getValue();
				LOG.info("Creating alias-link for binding: " + name);
				try {
					Path imgpath = Paths.get(this.lookupResource("binding://" + name, XmountOutputFormat.RAW));
					String imgname = binding.getLocalAlias();
					if (imgname == null || imgname.isEmpty()) {
						LOG.info("No alias set, skipping: " + name);
						continue;
					}

					Path imglink = images.resolve(imgname);
					Files.createSymbolicLink(imglink, imgpath);
				} catch (IOException exception) {
					LOG.warning("Creating alias-link for binding '" + name + "' failed!");
					exception.printStackTrace();
				}
			}
		}
		catch (IOException exception) {
			LOG.warning("Creating images directory failed!");
			exception.printStackTrace();
		}
		
		// Common setup for all VICE emulators.
		// Expected to be called by subclasses!
		
		runner.setWorkingDirectory(tempDir.toPath());
		runner.addArgument("-verbose");
		runner.addArgument("-sound");
		
		if (this.isLocalModeEnabled()) {
			LOG.warning("Local-mode is currently not supported for VICE!");;
		}
		else {
			// Set same audio parameters as libsdl, or else
			// VICE refuses to initialize sound device!
			runner.addArguments("-sounddev", "sdl");
			runner.addArguments("-soundrate", "22050");
			runner.addArguments("-soundoutput", "2");
			
			// Disable hardware-scaling (OpenGL)!
			runner.addArgument("+VICIIhwscale");
			
			// Disable borders around emulator's output
			runner.addArguments("-VICIIborders", "3");
			
			this.setupEmulatorForSDLONP();
		}
		
		runner.addArguments("-chdir", images.toString());
		
		String config = this.getNativeConfig();
		if (config != null && !config.isEmpty()) {
			String[] tokens = config.trim().split("\\s+");
			for (String token : tokens) {
				if (token.isEmpty())
					continue;
				runner.addArgument(token.trim());
			}
		}
	}

	@Override
	public boolean addDrive(Drive drive)
	{
		if (drive == null || (drive.getData() == null)) {
            LOG.warning("Drive doesn't contain an image, attach canceled.");
            return false;
        }

        try {
        	final String binding = this.getDriveBinding(drive);
	        if (binding == null || binding.isEmpty())
	        	return true;  // No disk-image to inject!
	        
	        // VICE supports:
	        //     => Floppy images in D64 and X64 formats, using devices 8-11
	   	    //     => Tape images in T64 format, using device 1

	        if (drive.getType() == DriveType.FLOPPY) {
	        	if (numAttachedDrives >= FLOPPY_DRIVES_NUM)
	        		return false;  // All devices occupied!
	        	
	        	String dnum = Integer.toString(FLOPPY_DRIVE_BASE_ID + numAttachedDrives);
	        	runner.addArgument("-", dnum);
	        	runner.addArgument(binding);
	        	
	        	if (drive.isBoot()) {
	        		// VICE uses always device 8 for autostart!
	        		runner.addArguments("-autostart", binding);
	        	}
	        	
	        	++numAttachedDrives;
	        }
	        // TODO: add proper tape support!
	        //else if (drive.getType() == DriveType.TAPE) {
	        //	runner.addArguments("-1", binding);
	        //}
	        else {
	        	throw new IllegalArgumentException("Unsupported drive type specified: " + drive.getType());
	        }
        }
        catch (Exception exception) {
        	LOG.warning("Adding drive failed!");
        	exception.printStackTrace();
        	return false;
        }
        
		return true;
	}

	@Override
	public boolean connectDrive(Drive drive, boolean connect)
	{
		LOG.severe("operation unsupported yet: " + this.getClass().getEnclosingMethod().getName());
		return false;
	}


	protected boolean addNic(Nic nic)
	{
		LOG.severe("operation unsupported yet: " + this.getClass().getEnclosingMethod().getName());
		return false;
	}
	
	
	/* ==================== Internal Helpers ==================== */
	
	private String getDriveBinding(Drive drive) throws BWFLAException, IOException
	{
		return this.lookupResource(drive.getData(), this.getImageFormatForDriveType(drive.getType()));
	}
}