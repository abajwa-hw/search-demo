#!/bin/bash
MODE=$1
HDFS_DIR="/user/solr/data/rfi_raw/"
if [ "$MODE" == "clean" ]
then
	echo "Cleaning HDFS doc dir: $HDFS_DIR"
	sudo -u hdfs hdfs dfs -rmr $HDFS_DIR/*
	find /root/search-demo/search-docs -iname '* *' -execdir bash -c 'mv "$1" "${1// /_}"' _ {} \;
	echo "Copying search docs from /root/search-demo/search-docs to HDFS under $HDFS_DIR"
	hadoop fs -put /root/search-demo/search-docs/* $HDFS_DIR
fi
echo "Starting indexing of HDFS dir: "
yarn jar /tmp/hadoop-lws-job-1.2.0-0-0.jar com.lucidworks.hadoop.ingest.IngestJob -Dlww.commit.on.close=true -Dadd.subdirectories=true -cls com.lucidworks.hadoop.ingest.DirectoryIngestMapper -c rawdocs -i $HDFS_DIR -of com.lucidworks.hadoop.io.LWMapRedOutputFormat -s http://sandbox.hortonworks.com:8983/solr


