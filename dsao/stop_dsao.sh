#!/bin/bash

# Find the Process ID (PID) of the running DSAO application
PID=$(pgrep -fl 'java -jar' |  awk '{print $1}')

# Check if the PID is found
if [ -z "$PID" ]; then
  echo "DSAO application not running or process not found."
else
  echo "Sending graceful shutdown to DSAO application with PID: $PID"
    # Gracefully shutdown by sending SIGTERM
	kill -15 $PID  # SIGTERM is signal 15
fi
