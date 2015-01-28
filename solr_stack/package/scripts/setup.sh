#!/bin/bash
set -e 

#Path to start.jar e.g. /opt/solr
SOLR_PATH=$1


if [ ! -d "$SOLR_PATH" ]
then
    echo "solr is not on 2.2 but is installed on sandbox"
	adduser solr
	mkdir $SOLR_PATH
	chown solr $SOLR_PATH

	sudo -u hdfs hdfs dfs -mkdir -p /user/solr
	sudo -u hdfs hdfs dfs -mkdir -p /user/solr/data
	
	#setup solr
	cd $SOLR_PATH
	wget -q http://apache.mirror.gtcomm.net/lucene/solr/4.7.2/solr-4.7.2.tgz
	tar -xvzf solr-4.7.2.tgz
	ln -s solr-4.7.2 solr
fi

exit 0