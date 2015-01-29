## Search workshop
This demo is part of a Search webinar.

The webinar recording and slides are available at http://hortonworks.com/partners/learn

#### Demo overview

- Apache Solr + Lucidworks Connector Document Search
  - Apache Solr provides a REST-like interface for searching indexed data. 
  - The search syntax follows the pattern of 'field:search query'. Those fields correspond to the schema defined in the Apache Solr Core. 
  - Lucidworks provides a connector allowing users to index multi-structured document data such as PDF, Docx, VSD, and XLSX.
  - Authors: Paul Codding, Joseph Niemiec, Piotr Pruski. Automation of setup via Ambari services and views: Ali Bajwa

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
- Pull latest code/sample documents and setup Solr and 'Doc Crawler' Ambari stacks and 'Doc Crawler' View
```
cd /root
git clone https://github.com/abajwa-hw/search-demo.git
~/search-demo/run_demo.sh
```

- After script completes, login to Ambari (http://sandbox.hortonworks.com:8080) and add the Solr service via from the 'Actions' dropdown menu in the bottom left of the Ambari dashboard:
  - On bottom left -> Actions -> Add service
  - ![Image](../master/screenshots/addservice.png?raw=true)
  - Now select check **Solr** -> Next
  - ![Image](../master/screenshots/solr-service.png?raw=true)
  - Click Next -> Next -> Deploy. Once completed, click Next -> Complete to exit the wizard.
  - Once installed you should see below at the bottom of your Ambari stack
  - ![Image](../master/screenshots/solr-status.png?raw=true)

- Next, add the "Document crawler" service the same way
  - On bottom left -> Actions -> Add service  check **Document Crawler** -> Next -> Next -> Next -> Deploy. Once completed, click Next -> Complete to exit the wizard.
  - ![Image](../master/screenshots/doc-crawler-service.png?raw=true)
  - Configure the service if desired and click Deploy. This may run for about 10-15 min depending on our network connection (only first time)
  - ![Image](../master/screenshots/configure-service.png?raw=true)
  
- This will install and start the Document Crawler   
  - ![Image](../master/screenshots/service-installation.png?raw=true)

- Tail the log file to get detailed status. When you see ```Binding to /0.0.0.0:9090```, then the app is up
```
tail -f /var/log/doc-crawler.log
```
  
- Once its up, you can access the demo from within Ambari via the "Document Crawler" view or by opening http://sandbox.hortonworks.com:9090
  - ![Image](../master/screenshots/doc-crawler-view.png?raw=true)
  - Enter a search query and click Search button. Open a document and notice the searched term will be highlighted within it.
  - ![Image](../master/screenshots/document-crawler.png?raw=true)

- You can also access Solr webapp at the url below:
http://sandbox.hortonworks.com:8983/solr/#/rawdocs


##### Post setup
 - Once the demo is setup the first time, to restart it (e.g. after VM reboot), simply start the "Document Crawler" service from Ambari and open the "Document Crawler" view 


##### Try your own documents

- After the Document Crawler is working, you can FTP your own document zip and run below to clear the HDFS dir and create new index using the new zip
```
/bin/rm -rf ~/search-demo/search-docs/*
unzip /path/to/my/docs.zip -d ~/search-demo/search-docs/
~/search-demo/regenerate_solr.sh clean
```
Now go back to the Document Crawler view and run some queries

- Alternatively, you can [create an HDFS mount on your Mac](https://github.com/abajwa-hw/search-demo/tree/master/document_crawler) and drag/drop the documents directly to /user/solr/data/rfi_raw dir in HDFS. In this scenario, you would run the script without the 'clean' argument to just run the mapreduce job without cleaning HDFS
```
~/search-demo/regenerate_solr.sh
```
Now go back to the Document Crawler view and run some queries

##### Other things to try

- Create a Banana dashboard webapp. Banana should be accessible here
http://sandbox.hortonworks.com:8983/solr/banana/src/index.html#/dashboard
 
##### Removal
 - In case you need to remove the Solr/Document Crawler stacks from Ambari in the future, run below and then restart Ambari:
```
curl -u admin:admin -i -H 'X-Requested-By: ambari' -X DELETE http://sandbox.hortonworks.com:8080/api/v1/clusters/Sandbox/services/SOLR

curl -u admin:admin -i -H 'X-Requested-By: ambari' -X DELETE http://sandbox.hortonworks.com:8080/api/v1/clusters/Sandbox/services/DOCCRAWLER
``` 