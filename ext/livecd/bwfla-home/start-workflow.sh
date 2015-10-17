#!/bin/bash

echo "Please wait. Connecting to local server..."

# Check that the server is reachable
url="http://localhost:8080/faces/pages/workflow-local/WF_L_0.xhtml"
for i in {1..60}; do
	status=$(curl --head -s -w "%{http_code}\n" -o /dev/null $url)
	if [ "$status" = "200" ]; then
		echo "EMIL-Server is reachable."
		echo "Starting Local-Workflow..."
		exec startx
		echo "Local-Workflow terminated."
		exit 0
	fi
	echo -n "|"
	sleep 1
done
echo ""
echo "Could not start local-workflow! EMIL-Server was not started properly."

