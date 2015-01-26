#!/bin/bash
set -e 

if [ ! -d "/opt/solr" ]; then
    #solr is not on 2.2 but is installed on sandbox 
	adduser solr
	mkdir /opt/solr
	chown solr /opt/solr

	sudo -u hdfs hdfs dfs -mkdir -p /user/solr
	sudo -u hdfs hdfs dfs -mkdir -p /user/solr/data
	
	#setup solr
	cd /opt/solr
	wget -q http://apache.mirror.gtcomm.net/lucene/solr/4.7.2/solr-4.7.2.tgz
	tar -xvzf solr-4.7.2.tgz
	ln -s solr-4.7.2 solr
fi

sudo -u hdfs hdfs dfs -mkdir -p /user/solr/data/rfi_raw
sudo -u hdfs hdfs dfs -chown solr /user/solr
sudo -u hdfs hdfs dfs -chmod -R 777 /user

#Move search docs to HDFS
cd /root/search-demo
unzip RFIsForSolr.zip
cd RFIsForSolr

#remove spaces
find . -iname '* *' -execdir bash -c 'mv "$1" "${1// /_}"' _ {} \;

hadoop fs -put * /user/solr/data/rfi_raw/


#Setup Solr
cd /opt/solr/solr
cp -r example hdp 
rm -rf hdp/example* hdp/multicore
if [ -d "./hdp/solr/collection1" ]; then
	mv hdp/solr/collection1 hdp/solr/rawdocs
else
	mv hdp/solr/hdp1 hdp/solr/rawdocs
fi	
rm -f hdp/solr/rawdocs/core.properties


#replace files from git
/bin/cp -f /root/search-demo/document_crawler/artifacts/solrconfig.xml  /opt/solr/solr/hdp/solr/rawdocs/conf/solrconfig.xml
/bin/cp -f /root/search-demo/document_crawler/artifacts/schema.xml /opt/solr/solr/hdp/solr/rawdocs/conf/schema.xml

echo "Starting Solr"
cd /opt/solr/solr/hdp
nohup java -jar start.jar &
sleep 10
#Create core called rawdocs
curl "http://localhost:8983/solr/admin/cores?action=CREATE&name=rawdocs&instanceDir=/opt/solr/solr/hdp/solr/rawdocs/"

#open Solr 
#http://sandbox.hortonworks.com:8983/solr/


cd /root
wget http://package.mapr.com/tools/search/lucidworks-hadoop-1.2.0-0-0.tar.gz
tar xvzf lucidworks-hadoop-1.2.0-0-0.tar.gz
cp lucidworks-hadoop-1.2.0-0-0/hadoop/hadoop-lws-job-1.2.0-0-0.jar /tmp
echo "starting mapreduce job"
yarn jar /tmp/hadoop-lws-job-1.2.0-0-0.jar com.lucidworks.hadoop.ingest.IngestJob -Dlww.commit.on.close=true -Dadd.subdirectories=true -cls com.lucidworks.hadoop.ingest.DirectoryIngestMapper -c rawdocs -i /user/solr/data/rfi_raw/ -of com.lucidworks.hadoop.io.LWMapRedOutputFormat -s http://sandbox.hortonworks.com:8983/solr

echo "setup banana"
cd /opt/solr
git clone https://github.com/LucidWorks/banana.git
mv /opt/solr/banana /opt/solr/solr-4.7.2/hdp/solr-webapp/webapp/	

echo "change collection1 to rawdocs..."
sed -i 's/collection1/rawdocs/g' /opt/solr/solr-4.7.2/hdp/solr-webapp/webapp/banana/src/app/dashboards/default.json


echo "Installing sbt, nodejs, npm ..."
#install sbt, nojejs, npm
curl https://bintray.com/sbt/rpm/rpm > bintray-sbt-rpm.repo
mv bintray-sbt-rpm.repo /etc/yum.repos.d/
yum install -y sbt nodejs npm
echo "Completed sbt, nodejs, npm install"

echo "Starting bower install..."
cd /root/search-demo/document_crawler/src/main/webapp
npm install -g bower
bower install --allow-root --config.interactive=false /root/search-demo/coe-int-master/
echo "Completed bower install"

echo "Starting npm imstall..."
cd  /root/search-demo/document_crawler/src/main/webapp
npm install
echo "Stack installed successfully"
exit 0

#Run server
#cd /root/search-demo/document_crawler
#sbt run

#open sandbox.hortonworks.com:9090
