#!/bin/bash
set -e

USAGE='USAGE: hdd_create_hfs.sh size_mb image.hfs'
if [ $# -ne 2 ]; then echo -e "$USAGE"; exit 30; fi
SIZE=$1; IMAGE=$2; EXISTED_BEFORE=1

if [ -z "$SIZE" ] || [ -z "$IMAGE" ]
then
    echo "$USAGE"
    exit 40
fi

# CLEANUP FUNCTION:
trap cleanup EXIT ERR INT QUIT TERM
cleanup()
{
	CODE=$?
    set +e
    [ $CODE -ne 0 ] && [ $EXISTED_BEFORE -ne 1 ] && rm -f "$IMAGE"
	exit $CODE
}

# CREATING HDD FILE:
[ ! -f "$IMAGE" ] && EXISTED_BEFORE=0
dd conv=excl if=/dev/zero of="$IMAGE" bs=1M count=$SIZE

# FORMAT DISK IN HFS:
hformat "$IMAGE"
