## Search workshop
This demo is part of a Search webinar.

The webinar recording and slides are available at http://hortonworks.com/partners/learn

#### Demo overview


##### Setup 

These setup steps are only needed first time

- Download HDP 2.2 sandbox VM image (Sandbox_HDP_2.2_VMware.ova) from [Hortonworks website](http://hortonworks.com/products/hortonworks-sandbox/)
- Import Sandbox_HDP_2.2_VMware.ova into VMWare and configure its memory size to be at least 8GB RAM 
- Find the IP address of the VM and add an entry into your machines hosts file e.g.
```
192.168.191.241 sandbox.hortonworks.com sandbox    
```
- Connect to the VM via SSH (password hadoop)
```
ssh root@sandbox.hortonworks.com
```

- Install Maven
```
mkdir /usr/share/maven
cd /usr/share/maven
wget http://mirrors.koehn.com/apache/maven/maven-3/3.2.5/binaries/apache-maven-3.2.5-bin.tar.gz
tar xvzf apache-maven-3.2.5-bin.tar.gz
ln -s /usr/share/maven/apache-maven-3.2.5/ /usr/share/maven/latest
echo 'M2_HOME=/usr/share/maven/latest' >> ~/.bashrc
echo 'M2=$M2_HOME/bin' >> ~/.bashrc
echo 'PATH=$PATH:$M2' >> ~/.bashrc
export M2_HOME=/usr/share/maven/latest
export M2=$M2_HOME/bin
export PATH=$PATH:$M2
```

- Pull latest code/sample documents and copy Solr and 'Doc Crawler' Ambari stack to the services dir
```
cd
git clone https://github.com/abajwa-hw/search-demo.git	

cp -R ~/search-demo/doc_crawler_stack /var/lib/ambari-server/resources/stacks/HDP/2.2/services/
cp -R ~/search-demo/solr_stack /var/lib/ambari-server/resources/stacks/HDP/2.2/services/
```
- Compile the view and copy jar to Ambari views dir
```
cd ~/search-demo/doc_crawler_view

#Tell maven to compile against ambari jar
mvn install:install-file -Dfile=/usr/lib/ambari-server/ambari-views-1.7.0.169.jar -DgroupId=org.apache.ambari -DartifactId=ambari-views -Dversion=1.3.0-SNAPSHOT -Dpackaging=jar

#Compile view
mvn clean package

#move jar to Ambari dir
cp target/*.jar /var/lib/ambari-server/resources/views
```

- Restart Ambari
```
#on HDP 2.2 sandbox
sudo service ambari restart

#on other HDP 2.2 setups
sudo service ambari-server restart
```
- Now launch Ambari and add the Solr service via "Actions" > "Add service". Proceed with remaining wizard screens and click Deploy
  - ![Image](../master/screenshots/solr-service.png?raw=true)

- Next, add the "Document crawler" service via "Actions" > "Add service".
  - ![Image](../master/screenshots/doc-crawler-service.png?raw=true)
  - Configure the service if desired and click Deploy
  - ![Image](../master/screenshots/configure-service.png?raw=true)
  
- This will install and start the Document Crawler   
  - ![Image](../master/screenshots/service-installation.png?raw=true)

- Tail the log file to get detailed status. When you see ```Binding to /0.0.0.0:9090```, then the app is up
```
tail -f /var/log/doc-crawler.log
```
  
- Once its up, you can access the demo from within Ambari via the "Document Crawler" view or at the url below:
http://sandbox.hortonworks.com:9090
![Image](../master/screenshots/document-crawler.png?raw=true)

- You can also access Solr webapp at the url below:
http://sandbox.hortonworks.com:8983/solr/#/rawdocs

- In case you need to remove the Document Crawler stack from Ambari in the future, run below and then restart Ambari:
```
curl -u admin:admin -i -H 'X-Requested-By: ambari' -X DELETE http://sandbox.hortonworks.com:8080/api/v1/clusters/Sandbox/services/DOCCRAWLER
``` 






##### Manual setup instructions

- Setup solr user and HDFS dir
```
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

cd /root/search-demo/document_crawler/src/main/webapp
npm install -g bower
bower install --allow-root --config.interactive=false /root/search-demo/coe-int-master/

cd  /root/search-demo/document_crawler/src/main/webapp
npm install

cd /root/search-demo/document_crawler
sbt run

#open sandbox.hortonworks.com:9090

```


##### What to try next?

