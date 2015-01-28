#!/bin/bash
set -e 
#Path to start.jar e.g. /opt/solr
SOLR_PATH=$1

#Logfile e.g. /var/log/solr.log
LOGFILE=$2


#If solr already started, skip the start
SOLR_PIDS=`ps -ef | grep "start.ja[r]" | wc -l`
if [ $SOLR_PIDS -eq 0 ]
then
	echo "Solr not running. Starting Solr..."
	cd $SOLR_PATH/solr/hdp
	nohup java -jar start.jar >> $LOGFILE 2>&1 &
else
	echo "Solr already running..."
fi

exit 0



