## Search workshop
This demo is part of a Search webinar.

The webinar recording and slides are available at http://hortonworks.com/partners/learn

#### Demo overview


##### Setup 


These setup steps are only needed first time

- Download HDP 2.1 sandbox VM image (Hortonworks_Sandbox_2.1.ova) from [Hortonworks website](http://hortonworks.com/products/hortonworks-sandbox/)
- Import Hortonworks_Sandbox_2.1.ova into VMWare and configure its memory size to be at least 8GB RAM 
- Find the IP address of the VM and add an entry into your machines hosts file e.g.
```
192.168.191.241 sandbox.hortonworks.com sandbox    
```
- Connect to the VM via SSH (password hadoop)
```
ssh root@sandbox.hortonworks.com
```
- Pull latest code/scripts
```
git clone https://github.com/abajwa-hw/search-demo.git	
```

- Setup solr user and HDFS dir
```
adduser solr
mkdir /opt/solr
chown solr /opt/solr

sudo -u hdfs hdfs dfs -mkdir -p /user/solr
sudo -u hdfs hdfs dfs -mkdir -p /user/solr/data
sudo -u hdfs hdfs dfs -mkdir -p /user/solr/data/rfi_raw
sudo -u hdfs hdfs dfs -chown solr /user/solr
sudo -u hdfs hdfs dfs -chmod -R 777 /user
```
- Copy the documents to HDFS
```
#Move search docs to HDFS
cd /root/search-demo
unzip RFIsForSolr.zip
cd RFIsForSolr

#remove spaces
for file in *; do mv "$file" `echo $file | tr ' ' '_'` ; done
hadoop fs -put * /user/solr/data/rfi_raw/
```


- Setup Solr
```
#setup solr
cd /opt/solr
wget -q http://apache.mirror.gtcomm.net/lucene/solr/4.7.2/solr-4.7.2.tgz
tar -xvzf solr-4.7.2.tgz
ln -s solr-4.7.2 solr
cd solr
cp -r example hdp 
rm -rf hdp/example* hdp/multicore
mv hdp/solr/collection1 hdp/solr/rawdocs
rm -f hdp/solr/rawdocs/core.properties


#replace files from git
cd /root/search-demo
/bin/cp -f ./document_crawler/artifacts/solrconfig.xml  /opt/solr/solr/hdp/solr/rawdocs/conf/solrconfig.xml
/bin/cp -f ./document_crawler/artifacts/schema.xml /opt/solr/solr/hdp/solr/rawdocs/conf/schema.xml

#Start Solr
cd /opt/solr/solr/hdp
nohup java -jar start.jar &
sleep 10
#Create core called rawdocs
curl "http://localhost:8983/solr/admin/cores?action=CREATE&name=rawdocs&instanceDir=/opt/solr/solr/hdp/solr/rawdocs/"

#open Solr 
#http://sandbox.hortonworks.com:8983/solr/
```

- Use Lucidworks jar to run mapreduce job. This will create seqence file, parse with Apache Tika from binary docs, build index and store index on HDFS
```
#cp /root/search-demo/document_crawler/artifacts/lucidworks-hadoop-1.2.0-0-0.tar.gz /root
cd /root
wget http://package.mapr.com/tools/search/lucidworks-hadoop-1.2.0-0-0.tar.gz
tar xvzf lucidworks-hadoop-1.2.0-0-0.tar.gz
cp lucidworks-hadoop-1.2.0-0-0/hadoop/hadoop-lws-job-1.2.0-0-0.jar /tmp
yarn jar /tmp/hadoop-lws-job-1.2.0-0-0.jar com.lucidworks.hadoop.ingest.IngestJob -Dlww.commit.on.close=true -Dadd.subdirectories=true -cls com.lucidworks.hadoop.ingest.DirectoryIngestMapper -c rawdocs -i /user/solr/data/rfi_raw/ -of com.lucidworks.hadoop.io.LWMapRedOutputFormat -s http://sandbox.hortonworks.com:8983/solr
```

- Setup Banana
```
cd /opt/solr
git clone https://github.com/LucidWorks/banana.git
mv /opt/solr/banana /opt/solr/solr-4.7.2/hdp/solr-webapp/webapp/	

#vi /opt/solr/solr-4.7.2/hdp/solr-webapp/webapp/banana/src/config.js
#no changes needed

#change collection1 to rawdocs
sed -i 's/collection1/rawdocs/g' /opt/solr/solr-4.7.2/hdp/solr-webapp/webapp/banana/src/app/dashboards/default.json

#Now banana should come up 
#http://sandbox.hortonworks.com:8983/solr/banana/src/index.html#/dashboard
```

- Setup search web app
```
#install sbt, nojejs, npm
curl https://bintray.com/sbt/rpm/rpm > bintray-sbt-rpm.repo
mv bintray-sbt-rpm.repo /etc/yum.repos.d/
yum install -y sbt nodejs npm

#Need to automate this
cd /root/search-demo/document_crawler/src/main/webapp
npm install -g bower
bower install --allow-root --config.interactive=false /root/search-demo/coe-int-master/
#(Choose the Version of Angular JS that mentions "Hortonworks Assembly UI" as the dependent component.)

#post setup
/root/search-demo/document_crawler/src/main/webapp/bower.json
{
  "name": "HortonworksAssemblyFrameworkUI",
  "description": "Hortonworks Assembly Framework UI",
  "version": "0.0.1",
  "homepage": "https://github.com/hortonworks/coe-int",
  "license": "Apache 2",
  "private": true,
  "dependencies": {
    "angular": "1.2.x",
    "angular-route": "1.2.x",
    "angular-loader": "1.2.x",
    "angular-cookies": "1.2.x",
    "angular-resource": "1.2.x",
    "angular-mocks": "~1.2.x",
    "angular-bootstrap": "~0.11.0",
    "angular-websocket": "~0.0.5",
    "angular-sanitize": "1.2.x",
    "bootstrap": "~3.1.1",
    "html5-boilerplate": "~4.3.0",
    "jquery": "~1.9.1",
    "angular-google-chart": "~0.0.11",
    "angular-google-maps": "~1.2.1",
    "angular-leaflet-directive": "~0.7.8"
  },
  "resolutions": {
    "angular": "1.2.x"
  }
}


cd  /root/search-demo/document_crawler/src/main/webapp
npm install

cd /root/search-demo/document_crawler
sbt run

#open sandbox.hortonworks.com:9090

```


##### What to try next?

