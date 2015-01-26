#!/bin/bash
set -e 

#If solr already started, skip the start
SOLR_PIDS=`ps -ef | grep "start.ja[r]" | wc -l`
if [ $SOLR_PIDS -eq 0 ]
then
	echo "Solr not running. Starting Solr..."
	cd /opt/solr/solr/hdp
	nohup java -jar start.jar >> /var/log/solr.log &
else
	echo "Solr already running..."
fi


echo "Starting sbt..."
cd /root/search-demo/document_crawler
nohup sbt run >> /var/log/doc-crawler-setup.log &

exit 0


