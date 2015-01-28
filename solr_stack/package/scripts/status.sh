#!/bin/bash
set -e 

#return 1 only if both solr and sbt are up
#RET=[[ `ps -ef | grep "sb[t]" | wc -l` && `ps -ef | grep "start.ja[r]" | wc -l` ]]

nc -tz localhost 8983 > /dev/null 2>&1
#if Solr is up, this should return 0
RET1=$?

#if solr is up this should return > 0
RET2=`ps -ef | grep "start.ja[r]" | wc -l`


# if both checks pass ...
if [ $RET1 -eq 0 -a $RET2 -gt 0 ]
then
	PID=`ps -ef | grep "start.ja[r]" | awk '{print $2}'`
	echo "Service (pid $PID) is running..."
	exit 0
else 
	exit 1
fi


