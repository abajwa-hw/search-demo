#!/bin/bash
# Usage: ./start_sbt.sh /root/search-demo /var/log/doc-crawler.log
#Path to demo root dir e.g. /root/search-demo
DEMO_ROOT=$1
#Logfile e.g. /var/log/doc-crawler.log
LOGFILE=$2

cd $DEMO_ROOT/document_crawler
#sbt run  >> $LOGFILE 2>&1 &
sbt run  >> $LOGFILE &
