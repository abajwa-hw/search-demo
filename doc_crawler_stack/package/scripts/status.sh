#!/bin/bash
set -e 

#return 1 only if both solr and sbt are up
#RET=[[ `ps -ef | grep "sb[t]" | wc -l` && `ps -ef | grep "start.ja[r]" | wc -l` ]]
RET1=`ps -ef | grep "sb[t]" | wc -l`
RET2=`ps -ef | grep "start.ja[r]" | wc -l`

if [[ $RET1 -gt 0 && $RET2 -gt 0 ]]
then
	echo 1
else 
	echo 0
fi


