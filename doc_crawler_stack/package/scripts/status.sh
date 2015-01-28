#!/bin/bash
set -e 

#return 1 only if both solr and sbt are up
#RET=[[ `ps -ef | grep "sb[t]" | wc -l` && `ps -ef | grep "start.ja[r]" | wc -l` ]]

nc -tz localhost 9090
#if web server is up, this should return 0
RET1=$?
#if sbt is up, this should return > 0
RET2=`ps -ef | grep "sb[t]" | wc -l`
#if solr is up this should return > 0
RET3=`ps -ef | grep "start.ja[r]" | wc -l`

echo "sbt status is $RET2"
echo "solr status is $RET3"

# if services are both up...
if [ $RET1 -eq 0 -a $RET2 -gt 0 -a $RET3 -gt 0 ]
then
	PID1=`ps -ef | grep "sb[t] run" | awk '{print $2}'`
	echo "Service (pid $PID1) is running..."
	exit 0
else 
	exit 1
fi


