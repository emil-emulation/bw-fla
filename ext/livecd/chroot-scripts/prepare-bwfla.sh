#!/bin/bash

export LC_ALL=C
mkdir $HOME/.gvfs

exe() { echo "=> $@" ; eval "$@" ; }

sources_list="/etc/apt/sources.list"

echo -e "\n===== Add missing repositories ====="
echo "deb http://packages.bw-fla.uni-freiburg.de/ precise bwfla" > /etc/apt/sources.list.d/bwfla.list
echo -e "Package: *\nPin: release c=bwfla\nPin-Priority: 1001" > /etc/apt/preferences.d/pin-bwfla.pref
exe apt-get update

echo -e "\n===== Uninstall conflicting packages ====="
exe apt-get purge -y qemu-common qemu-kvm qemu-utils

echo -e "\n===== Install required packages ====="
exe apt-get install -y chromium-browser openjdk-7-jre curl
exe ln -s /usr/lib/insserv/insserv /sbin/insserv
exe apt-get install -y --force-yes basilisk2 dosbox sheepshaver hatari virtualbox virtualbox-guest-dkms libfreerdp-plugins-standard libfreerdp1 libguac-client-rdp0 libguac-client-vnc0 libguac-client-sdlonp0 guacd libsdl1.2debian libvncserver0 qemu-utils fuseiso

echo -e "\n===== Install custom boot-splash ====="
exe apt-get install -y plymouth-theme-spinfinity
echo "Modify boot-splash logo..."
cp -v /bwfla-scripts/data/emil-logo.png /lib/plymouth/ubuntu_logo.png
sed -i 's|title=.*|title= EMIL-LiveCD|' /lib/plymouth/themes/ubuntu-text/ubuntu-text.plymouth
echo "Activating kernel-modules for video output..."
echo FRAMEBUFFER=y >> /etc/initramfs-tools/conf.d/splash
kmods=/etc/initramfs-tools/modules
echo 'drm' >> $kmods
echo 'nouveau' >> $kmods
echo 'radeon' >> $kmods
echo 'vboxvideo' >> $kmods
echo 'intel_agp' >> $kmods
echo 'i915' >> $kmods
exe update-initramfs -c -k all

echo -e "\n===== Cleanup ====="
exe apt-get autoremove -y
exe apt-get clean

echo -e "\n===== Setup auto-start ====="

echo "Generate bwFLA start-up service..."
echo '
description "bwFLA start-up service"
start on runlevel [2345]
stop on runlevel [!2345]
respawn
respawn limit 3 120
console owner
script
	/bin/su "ubuntu" -c "cd ~/bwfla-server/appserver/bin/; export DISPLAY=:0; ./standalone.sh -b 0.0.0.0 &> ~/bwfla-server.log"
end script' > /etc/init/bwfla-server.conf

echo -e "\n===== DONE =====\n"

