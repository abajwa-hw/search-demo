#!/bin/bash
set -e 

nc -tz localhost 9090 > /dev/null 2>&1
#if web server is up, this should return 0
RET1=$?
#if sbt is up, this should return > 0
RET2=`ps -ef | grep "sb[t]" | wc -l`



# if services are both up...
if [ $RET1 -eq 0 -a $RET2 -gt 0 ]
then
	PID1=`ps -ef | grep "sb[t] run" | awk '{print $2}'`
	echo "Service (pid $PID1) is running..."
	exit 0
else 
	exit 1
fi


