#!/bin/sh

vboxmanage list vms | grep $1 | wc -l

#out=""
#while [ -z "$out" ]; do
#out=$(vboxmanage list vms | grep $1)
#done

