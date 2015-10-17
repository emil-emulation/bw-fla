#!/bin/bash
set -o errexit



# PARAMS/VARS HANDLING:
usage='floppy_io.sh op floppy io'
if [ "$#" -eq 0 ]; then echo -e "$usage"; exit 0; fi
op="$1"
floppy="$2"
shift 2
mpt=''
dev=''



# CLEANUP HANDLING
trap cleanup ERR INT KILL TERM QUIT
cleanup()
{
	code=$?

	set +e
	sync
	pumount "$dev"
	sync
	losetup -d "$dev"
	sync
	rm -r -f "$mpt"
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



# BIND AND MOUNT FLOPPY
if [ ! -e "$floppy" ]; then false; fi
# FIXME: lock the floppy file
getdev --show -f "$floppy"
mpt="/media/.${$}${RANDOM}.tmp"
pmount "$dev" $(basename "$mpt");



# DATA INPUT/OUTPUT
if [ "$op" = 'i' ]; then cp -r -n "$@" "$mpt";
elif [ "$op" = 'e' ]; then cp -r -n "$mpt"/* "$1";
else false; fi
echo "I/O SUCCESS"
cleanup
