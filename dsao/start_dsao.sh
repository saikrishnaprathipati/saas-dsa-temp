#!/bin/bash

# Get the hostname of the machine
HOSTNAME=$(hostname)

if [ $HOSTNAME == "s5867v" ]; then
	SPRING_PROFILE="dev1"
elif [ $HOSTNAME == "s5960v" ]; then
    SPRING_PROFILE="dev2"
elif [ $HOSTNAME == "s5885v" ]; then
    SPRING_PROFILE="sit1"
elif [ $HOSTNAME == "s6095a" ]; then
    SPRING_PROFILE="sit2"	
elif [ $HOSTNAME == "sedsh356a" ] || [ $HOSTNAME == "sedsh357a" ] || [ $HOSTNAME == "sedsh358a" ] || [ $HOSTNAME == "sedsh359a"  ]; then sedsh359a
    SPRING_PROFILE="saasperf"
elif [ $HOSTNAME == "sedsh315a" ] || [ $HOSTNAME == "sedsh316a" ] || [ $HOSTNAME == "sedsh317a" ] || [ $HOSTNAME == "sedsh318a" ]; then
    SPRING_PROFILE="saasprod"	
else
    SPRING_PROFILE="dev1"
fi

echo "Setting SPRING_PROFILE: $SPRING_PROFILE"

# Define the path to DSAO JAR file 
JAR_PATH=$(ls -t $dsao*.jar | head -n 1)
echo "Latest DSAO jar file is: $JAR_PATH"

# Find the Process ID (PID) of the running DSAO application
APP_PID=$(pgrep -fl 'java -jar' |  awk '{print $1}')

# Check if the PID is found
if [ -z "$APP_PID" ]; then
  if [ $HOSTNAME == "sedsh315a" ] || [ $HOSTNAME == "sedsh356a" ] || [ $HOSTNAME == "s5885v" ]; then 
    echo "Starting DSAO application in batch profile..."
	nohup java -jar $JAR_PATH --spring.profiles.active=$SPRING_PROFILE --dsa.batchjob.enabled=true > nohup.out 2>&1 &
  else 	
	echo "Starting DSAO application without batch profile..."
	nohup java -jar $JAR_PATH --spring.profiles.active=$SPRING_PROFILE > nohup.out 2>&1 &
  fi
else
  echo "Please run STOP script. DSAO application already running with PID: $APP_PID"
fi
