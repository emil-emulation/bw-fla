#!/bin/bash

# Runs a specified command in a child process,
# writing its process-id into a file and
# redirecting its stdin, stdout and stderr.
#
# Script's inputs:
#     $1: file, that should contain the process-id
#     $2: file, indicating a running state
#     $3: command to run in a child process
#     $@: command's arguments

pidfile="$1"
runfile="$2"
command="$3"

# Remove the first 3 arguments
shift 3

# Helper, responsible for cleanup
cleanup()
{
	# Save current exit-code
	exitcode="$?"

	# Remove the running-file:
	# This indicates, that the process is terminated
	rm -f "$runfile"

	if [ ! -z "$command_pid" ];
	then
		kill -9 "$command_pid" &>/dev/null
	fi

	# Return the process' exit-code
	exit "$exitcode"
}

# Setup an exit-trap
trap cleanup EXIT QUIT INT TERM

# Start the command in a child process, redirecting
# stdin, stdout and stderr to parent process
$command "$@" <&0 1>&1 2>&2 &

# Save 'PID' of last command
command_pid="$!"

# Write 'PID' to its file
echo "$command_pid" > "$pidfile"

# Create file, representing a running-state
touch "$runfile"

# Join the parent process with child
# and wait until child finishes
wait "$command_pid"
