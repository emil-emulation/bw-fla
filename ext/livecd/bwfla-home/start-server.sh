#!/bin/bash

cd ./bwfla-server/appserver/bin
export DISPLAY=:0; ./standalone.sh -b 0.0.0.0 &> ~/bwfla-server.log

