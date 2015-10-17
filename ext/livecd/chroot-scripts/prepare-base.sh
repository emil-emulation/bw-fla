#!/bin/bash

export LC_ALL=C
mkdir $HOME/.gvfs


exe() { echo "=> $@" ; eval "$@" ; }

sources_list="/etc/apt/sources.list"

echo -e "\n===== Add missing repositories to $sources_list ====="
exe 'echo "deb http://archive.ubuntu.com/ubuntu/ precise universe" >> $sources_list'
exe 'echo "deb http://security.ubuntu.com/ubuntu/ precise-security universe" >> $sources_list'
exe 'echo "deb http://archive.ubuntu.com/ubuntu/ precise-updates universe" >> $sources_list'
exe 'echo "deb http://archive.ubuntu.com/ubuntu/ precise multiverse" >> $sources_list'
exe 'echo "deb http://security.ubuntu.com/ubuntu/ precise-security multiverse" >> $sources_list'
exe 'echo "deb http://archive.ubuntu.com/ubuntu/ precise-updates multiverse" >> $sources_list'
exe 'echo "deb-src http://archive.ubuntu.com/ubuntu/ precise main restricted" >> $sources_list'
exe 'echo "deb-src http://archive.ubuntu.com/ubuntu/ precise-updates main restricted" >> $sources_list'
exe 'echo "deb-src http://archive.ubuntu.com/ubuntu/ precise universe" >> $sources_list'
exe 'echo "deb-src http://archive.ubuntu.com/ubuntu/ precise-updates universe" >> $sources_list'
exe 'echo "deb-src http://archive.ubuntu.com/ubuntu/ precise multiverse" >> $sources_list'
exe 'echo "deb-src http://archive.ubuntu.com/ubuntu/ precise-updates multiverse" >> $sources_list'
exe apt-get update

echo -e "\n===== Uninstall unneeded packages ====="
exe apt-get purge -y \
	ubuntu-desktop \
	baobab \
	bc \
	checkbox-qt \
	doc-base \
	eog \
	evince \
	file-roller \
	foomatic-db-compressed-ppds \
	foomatic-filters \
	gcalctool \
	gedit \
	genisoimage \
	ghostscript-x \
	gnome-control-center \
	gnome-font-viewer \
	gnome-media\
	gnome-menus \
	gnome-nettool \
	gnome-power-manager \
	gnome-screenshot \
	gnome-session \
	gnome-session-canberra \
	gnome-system-log \
	gnome-system-monitor \
	gucharmap \
	language-selector-gnome \
	launchpad-integration \
	libatk-adaptor \
	libatk-adaptor-schemas \
	libxp6 \
	lightdm \
	nautilus \
	nautilus-sendto \
	notify-osd \
	openprinting-ppds \
	printer-driver-pnm2ppa \
	rfkill \
	seahorse \
	software-center \
	software-properties-gtk \
	ssh-askpass-gnome \
	system-config-printer-gnome \
	ubuntu-artwork \
	ubuntu-sounds \
	unity \
	unity-2d \
	unity-greeter \
	update-notifier \
	yelp \
	activity-log-manager-control-center \
	aisleriot \
	app-install-data-partner \
	apport-gtk \
	bluez \
	bluez-alsa \
	bluez-cups \
	bluez-gstreamer \
	branding-ubuntu \
	brasero \
	brltty \
	cups \
	cups-bsd \
	cups-client \
	deja-dup \
	empathy \
	example-content \
	firefox \
	firefox-gnome-support \
	fonts-kacst-one \
	fonts-khmeros-core \
	fonts-lao \
	fonts-nanum \
	fonts-takao-pgothic \
	fonts-thai-tlwg \
	ginn \
	gnome-accessibility-themes \
	gnome-bluetooth \
	gnome-disk-utility \
	gnome-orca \
	gnome-screensaver \
	gnome-sudoku \
	gnomine \
	gwibber \
	hplip \
	jockey-gtk \
	landscape-client-ui-install \
	libgail-common \
	libqt4-sql-sqlite \
	libreoffice-calc \
	libreoffice-gnome \
	libreoffice-help-en-us \
	libreoffice-impress \
	libreoffice-math \
	libreoffice-style-human \
	libreoffice-writer \
	mahjongg \
	nautilus-share \
	overlay-scrollbar \
	plymouth-theme-ubuntu-logo \
	printer-driver-c2esp \
	printer-driver-foo2zjs \
	printer-driver-min12xxw \
	printer-driver-ptouch \
	printer-driver-pxljr \
	printer-driver-sag-gdi \
	printer-driver-splix \
	pulseaudio-module-bluetooth \
	qt-at-spi \
	remmina \
	rhythmbox \
	rhythmbox-plugin-magnatune \
	rhythmbox-ubuntuone \
	shotwell \
	simple-scan \
	sni-qt \
	telepathy-idle \
	thunderbird \
	thunderbird-gnome-support \
	totem \
	totem-mozilla \
	transmission-gtk \
	ttf-indic-fonts-core \
	ttf-punjabi-fonts \
	ubuntu-docs \
	ubuntuone-client-gnome \
	ubuntuone-installer \
	usb-creator-gtk \
	vino \
	whoopsie \
	xul-ext-ubufox \
	ubiquity \
	libreoffice-common \
	brasero-common \
	empathy-common \
	gnome-games-data

exe apt-get autoremove -y
exe rm /etc/skel/examples.desktop

echo -e "\n===== Update kernel ====="
exe apt-get install -y linux-generic
echo "Removing old kernel packages..."
dpkg -l linux-* | awk '/^ii/{ print $2}' | grep -v -e `uname -r | cut -f1,2 -d"-"` | grep -e [0-9] | grep -E "(image|headers)" | xargs apt-get purge -y

echo -e "\n===== Upgrade system ====="
exe apt-get upgrade -y

echo -e "\n===== Install required packages ====="
exe apt-get install -y chromium-browser openjdk-7-jre curl

echo -e "\n===== Cleanup ====="
exe apt-get autoremove -y
exe apt-get clean

echo -e "\n===== DONE =====\n"

