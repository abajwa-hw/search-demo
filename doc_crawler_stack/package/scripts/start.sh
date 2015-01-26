#!/bin/bash
set -e 
#Path to start.jar e.g. /opt/solr
SOLR_PATH=$1
#Path to demo root dir e.g. /root/search-demo
DEMO_ROOT=$2
#Logfile e.g. /var/log/doc-crawler.log
LOGFILE=$3

#If solr already started, skip the start
SOLR_PIDS=`ps -ef | grep "start.ja[r]" | wc -l`
if [ $SOLR_PIDS -eq 0 ]
then
	echo "Solr not running. Starting Solr..."
	./start_solr.sh $SOLR_PATH $LOGFILE
else
	echo "Solr already running..."
fi


echo "Checking sbt..."
SBT_PIDS=`ps -ef | grep "sb[t]" | wc -l`
if [ $SBT_PIDS -eq 0 ]
then
	./start_sbt.sh $DEMO_ROOT $LOGFILE
	echo "Starting sbt..."
else
	echo "Sbt already running..."
fi
exit 0



