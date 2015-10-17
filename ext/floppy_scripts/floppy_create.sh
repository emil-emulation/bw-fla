#!/bin/bash
set -o errexit
set -o noclobber


# PARAM/VAR HANDLING:
usage='floppy_create.sh floppy.img'
if [ "$#" -eq 0 ]; then echo -e "$usage"; exit 0; fi
floppy=$1

# CREATING THE FLOPPY
dd if=/dev/zero of="$floppy" bs=1K count=1440
/sbin/mkdosfs "$floppy"

echo "FLOPPY CREATED"
