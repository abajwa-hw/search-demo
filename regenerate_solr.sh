#!/bin/bash
echo "Cleaning HDFS doc dir: /user/solr/data/rfi_raw"
sudo -u hdfs hdfs dfs -rmr /user/solr/data/rfi_raw/*
find /root/search-demo/search-docs -iname '* *' -execdir bash -c 'mv "$1" "${1// /_}"' _ {} \;
echo "Copying search docs from /root/search-demo/search-docs to HDFS under /user/solr/data/rfi_raw"
hadoop fs -put /root/search-demo/search-docs/* /user/solr/data/rfi_raw/
yarn jar /tmp/hadoop-lws-job-1.2.0-0-0.jar com.lucidworks.hadoop.ingest.IngestJob -Dlww.commit.on.close=true -Dadd.subdirectories=true -cls com.lucidworks.hadoop.ingest.DirectoryIngestMapper -c rawdocs -i /user/solr/data/rfi_raw/ -of com.lucidworks.hadoop.io.LWMapRedOutputFormat -s http://sandbox.hortonworks.com:8983/solr


