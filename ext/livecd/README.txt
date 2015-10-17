To build the LiveCD the "Ubuntu Customization Kit" is used.

=> Install uck package
	apt-get install uck

=> Change line 358 of /usr/lib/uck/remaster-live-cd.sh to
	cp -d /etc/resolv.conf "$REMASTER_DIR/etc/resolv.conf" ||


To build a new LiveCD:

=> Download an Ubuntu desktop-image to be used as base.
   NOTE: UCK works only with the desktop image-flavor!

=> Create a RAM-Disk to speed up the image building:
   mkdir <UCK-TMPDIR>
   sudo mount -t tmpfs -o size=50% none <UCK-TMPDIR>

=> Start uck-gui and follow the dialog:  uck-gui <UCK-TMPDIR>

   NOTE: The kernel in the base-image must match the kernel version
   of the host, or the intallation of VirtualBox package will fail!
   For Ubuntu 12.04 download: ubuntu-12.04.1-desktop-amd64.iso

=> Select yes, when asked wheter to perform customization step.

=> Select yes, when asked about hybrid ISO/USB image.

=> Wait, until the dialog "Please choose customization action" appears.

=> Now open a terminal on the host and execute:
   cd <BWFLA_HOME>/ext/livecd
   ./build-livecd.sh <UCK-TMPDIR>

=> When done, select "Continue building" item in the dialog.

When finished, the customized image will be located in:
<UCK-TMPDIR>/remaster-new-files/livecd.iso

