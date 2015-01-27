#!/bin/bash
set -e 

#Path to stack  e.g. /var/lib/ambari-server/resources/stacks/HDP/2.2/services/doc_crawler_stack
STACK_PATH=$1
#Path to start.jar e.g. /opt/solr
SOLR_PATH=$2
#Path to demo root dir e.g. /root/search-demo
DEMO_ROOT=$3
#Logfile e.g. /var/log/doc-crawler.log
LOGFILE=$4

if [ ! -d "$SOLR_PATH" ]
then
    #solr is not on 2.2 but is installed on sandbox 
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

set +e
sudo -u hdfs hdfs dfs -test -d /user/solr/data/rfi_raw
set -e
if [ $? -eq 0 ]
then
	sudo -u hdfs hdfs dfs -rmr /user/solr/data/rfi_raw
fi
sudo -u hdfs hdfs dfs -mkdir -p /user/solr/data/rfi_raw
sudo -u hdfs hdfs dfs -chown solr /user/solr
sudo -u hdfs hdfs dfs -chmod -R 777 /user

#Move search docs to HDFS
cd $DEMO_ROOT
if [ -d RFIsForSolr ]
then
	rm -rf RFIsForSolr
fi
unzip RFIsForSolr.zip
cd RFIsForSolr

#remove spaces
find . -iname '* *' -execdir bash -c 'mv "$1" "${1// /_}"' _ {} \;

hadoop fs -put * /user/solr/data/rfi_raw/


#Setup Solr
echo "Setting up Solr and core at $SOLR_PATH"
cd $SOLR_PATH/solr
cp -r example hdp 
rm -rf hdp/example* hdp/multicore

if [ ! -d "$SOLR_PATH/solr/hdp/solr/rawdocs/"]; then
	if [ -d "./hdp/solr/collection1" ]; then
		mv hdp/solr/collection1 hdp/solr/rawdocs
	else
		mv hdp/solr/hdp1 hdp/solr/rawdocs
	fi	
fi	
rm -f hdp/solr/rawdocs/core.properties


#replace files from git
/bin/cp -f $DEMO_ROOT/document_crawler/artifacts/solrconfig.xml  $SOLR_PATH/solr/hdp/solr/rawdocs/conf/solrconfig.xml
/bin/cp -f $DEMO_ROOT/document_crawler/artifacts/schema.xml $SOLR_PATH/solr/hdp/solr/rawdocs/conf/schema.xml

echo "$STACK_PATH/package/scripts/start_solr.sh $SOLR_PATH $LOGFILE"
$STACK_PATH/package/scripts/start_solr.sh $SOLR_PATH $LOGFILE
echo "Waiting 10 seconds for Solr to come up"
sleep 10
echo "Creating Solr core called rawdocs"
curl "http://localhost:8983/solr/admin/cores?action=CREATE&name=rawdocs&instanceDir=$SOLR_PATH/solr/hdp/solr/rawdocs/"

#open Solr 
#http://sandbox.hortonworks.com:8983/solr/


cd /root
wget http://package.mapr.com/tools/search/lucidworks-hadoop-1.2.0-0-0.tar.gz
tar xvzf lucidworks-hadoop-1.2.0-0-0.tar.gz
cp lucidworks-hadoop-1.2.0-0-0/hadoop/hadoop-lws-job-1.2.0-0-0.jar /tmp
echo "starting mapreduce job"
yarn jar /tmp/hadoop-lws-job-1.2.0-0-0.jar com.lucidworks.hadoop.ingest.IngestJob -Dlww.commit.on.close=true -Dadd.subdirectories=true -cls com.lucidworks.hadoop.ingest.DirectoryIngestMapper -c rawdocs -i /user/solr/data/rfi_raw/ -of com.lucidworks.hadoop.io.LWMapRedOutputFormat -s http://sandbox.hortonworks.com:8983/solr

echo "setup banana"
cd $SOLR_PATH
git clone https://github.com/LucidWorks/banana.git
mv $SOLR_PATH/banana $SOLR_PATH/solr-4.7.2/hdp/solr-webapp/webapp/	

echo "change collection1 to rawdocs..."
sed -i 's/collection1/rawdocs/g' $SOLR_PATH/solr-4.7.2/hdp/solr-webapp/webapp/banana/src/app/dashboards/default.json


echo "Installing sbt, nodejs, npm ..."
#install sbt, nojejs, npm
curl https://bintray.com/sbt/rpm/rpm > bintray-sbt-rpm.repo
mv bintray-sbt-rpm.repo /etc/yum.repos.d/
yum install -y sbt nodejs npm
echo "Completed sbt, nodejs, npm install"

#Moved these to setup method in master.py

#echo "Starting bower install..."
cd $DEMO_ROOT/document_crawler/src/main/webapp
npm install -g bower
bower install --allow-root --config.interactive=false $DEMO_ROOT/coe-int-master/
echo "Completed bower install"

#echo "Starting npm imstall..."
cd  $DEMO_ROOT/document_crawler/src/main/webapp
npm install
echo "Stack installed successfully"

exit 0

#Moved these to start method in master.py

#Run server
#cd $DEMO_ROOT/document_crawler
#sbt run

#open sandbox.hortonworks.com:9090
