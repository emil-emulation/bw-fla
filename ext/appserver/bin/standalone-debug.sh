#!/bin/sh

# Enable remote debugging support in JBoss server

JAVA_DEBUG_OPTS="-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=n"

. `dirname "$0"`/standalone.sh

