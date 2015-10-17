#!/bin/bash

exe() { echo "=> $@" ; eval "$@" ; }

basedir=$1
if [ ! -n "$basedir" ] || [ ! -d "$basedir" ]
then
	echo "No LiveCD base-directory specified!"
	exit 1
fi

srcdir="$(readlink -f $basedir/bwfla-home)"
dstroot="$(readlink -f $basedir/bwfla-root)"
exe rm -r $dstroot
exe mkdir $dstroot

echo -e "\n===== Compile and install QEMU ====="
exe cd /tmp
exe git clone --depth=1 -b stable-2.2 git://git.qemu.org/qemu.git
exe cd qemu
exe ./configure --target-list=i386-softmmu,x86_64-softmmu --prefix=$dstroot/usr/local
exe make -j $(($(nproc) + 1))
exe make install
exe cd /tmp
exe rm -r -f qemu
exe rm $dstroot/usr/local/bin/qemu-img
exe rm $dstroot/usr/local/bin/qemu-nbd


echo -e "\n===== Compile and install bwFLA ====="
exe cd /tmp
exe git clone --depth=1 -b Issue_522 git@132.230.4.15:bw-fla bwfla
exe cd bwfla

echo "Prepare configuration files..."

# Set the deployment-timeout parameter to 5 minutes (5*60 seconds)
echo "./ext/appserver/standalone/configuration/standalone.xml"
sed -i 's#\(.*<deployment-scanner.*\)\(/>\)#\1 deployment-timeout="300"\2#' \
		./ext/appserver/standalone/configuration/standalone.xml

echo "Build and deploy bwFLA..."
exe cd src/root
exe cp build.properties{.template,}
sed -i "s|appserver.address=.*|appserver.address=localhost|g" build.properties
sed -i "s|tmpdir.location=.*|tmpdir.location=/tmp/bwfla-tmp|g" build.properties
exe mvn install
exe ant reinstall:db

echo "Copy server and DB to user's template directory..."
srvdir=$dstroot/etc/skel/bwfla-server
exe mkdir -p $srvdir
exe cd ../../ext
exe cp -r -v appserver $srvdir
exe cp -r -v database $srvdir
exe cd /tmp
exe rm -r -f bwfla

echo "Copy additional files to user's template directory..."
exe cp -r -v $srcdir/.[^.]* $dstroot/etc/skel/
exe cp -r -v $srcdir/* $dstroot/etc/skel/

echo -e "\n===== DONE =====\n"

