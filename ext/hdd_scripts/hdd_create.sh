#!/bin/bash
set -o errexit



# PARAM/VAR HANDLING:
usage='hdd_create.sh fsys size hdd'
if [ "$#" -eq 0 ]; then echo -e "$usage"; exit 0; fi
fsys="$1"
size="$2"
hdd="$3"
dev=''
fd=''
ok=0



# CLEANUP HANDLING
cleanup()
{
	code=$?

	set +e
	sync; losetup -d "$dev"; sync;
	exec {fd}>&-
	if [ "$ok" -ne 1 ]; then rm -f "$hdd"; fi
	set -e
	
	exit $code
}



# GET FREE LOOP DEVICE
getdev()
{
	secs=60

	set +e	
	for (( i=0; i<$secs; ++i )) 
	do
		dev=$(losetup "$@")
		if [ -b "$dev" ]; then return 0; fi
		sleep 1	
	done
	set -e

	false
}



# CREATING HDD FILE:
exec {fd}>"$hdd"
# FIXME: lock the hdd file
trap cleanup ERR INT KILL TERM QUIT
dd if=/dev/zero of=/dev/fd/"$fd" bs=256K count=$(($size*4))



# MAKING PARTITION:
getdev --show -f "$hdd"
sfdisk -D "$dev" << EOF
,,$fsys
EOF



# FORMATTING PARTITION:
sync; losetup -d "$dev"; sync;
getdev --show -f -o 32256 "$hdd"

fmt=""
case $fsys in
	6) fmt="mkdosfs $dev";;
	b) fmt="mkdosfs -F 32 $dev";;
	af)fmt="mkfs.hfs -h $dev";;
	*) false
esac
$fmt


ok=1
echo "HDD CREATED"
cleanup
