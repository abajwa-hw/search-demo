#!/bin/bash
set -e 

#return 1 only if both solr and sbt are up
#RET=[[ `ps -ef | grep "sb[t]" | wc -l` && `ps -ef | grep "start.ja[r]" | wc -l` ]]
RET1=`ps -ef | grep "sb[t]" | wc -l`
RET2=`ps -ef | grep "start.ja[r]" | wc -l`

echo "sbt status is $RET1"
echo "solr status is $RET2"

# if services are both up...
if [[ $RET1 -gt 0 && $RET2 -gt 0 ]]
then
	PID1=`ps -ef | grep "sb[t] run" | awk '{print $2}'`
	echo "Service (pid $PID1) is running..."
	exit 0
else 
	exit 1
fi


