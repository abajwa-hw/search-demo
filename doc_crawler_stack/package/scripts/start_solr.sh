#!/bin/bash
# Usage: ./start_solr.sh /opt/solr /var/log/doc-crawler.log
#Path to start.jar  e.g. /opt/solr
SOLR_ROOT=$1
#Logfile e.g. /var/log/doc-crawler.log
LOGFILE=$2

cd $SOLR_ROOT/solr/hdp
java -jar start.jar >> $LOGFILE &

