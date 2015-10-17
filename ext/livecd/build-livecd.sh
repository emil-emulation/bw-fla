#!/bin/bash

set -e

ucktmp=$1
if [ ! -n "$ucktmp" ] || [ ! -d "$ucktmp" ]
then
	echo "No working directory for UCK specified!"
	echo "Usage: $0 <UCK-TMPDIR>"
	exit 1
fi

imgdir="$(readlink -f $ucktmp/remaster-root)"
basedir="$(readlink -f $(dirname $0))"
scripts="$basedir/host-scripts"

echo "Start building LiveCD..."

$scripts/prepare-bwfla-root.sh $basedir
$scripts/copy-bwfla-scripts.sh $basedir $imgdir

echo -e "\n##### Running chroot scripts #####"

sudo chroot $imgdir /bin/bash /bwfla-scripts/prepare-base.sh
sudo chroot $imgdir /bin/bash /bwfla-scripts/prepare-bwfla.sh

echo -e "##### Finished chroot scripts #####\n"

$scripts/copy-bwfla-root.sh $basedir $imgdir
$scripts/finalize-image.sh $basedir $imgdir

echo "DONE"

