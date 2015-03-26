# Index and Ingest Apache Access Log

## Create HBase Tables

     $ hbase shell
	 hbase> create 'access_logs', 'request'
## Ceate Solr Instance and Core

Create the solr user and add a password

    # adduser solr
    # passwd solr

Create a location for the installation

    # mkdir /opt/solr
    # chown solr:solr /opt/solr

### Download and Install

SU to the solr user

    # su solr
    $ cd /opt/solr

Download Solr from http://lucene.apache.org/solr/downloads.html

    $ wget http://archive.apache.org/dist/lucene/solr/4.7.2/solr-4.7.2.zip
    

Unzip Solr

    $ unzip solr-4.7.2.zip

Now, add the Apache Access Logs core once you've created the directory and put in the starting solrconfig.xml, and schema.xml (found in ./solr/schema.xml of this project).

    $ mkdir /opt/solr/solr-4.7.2/example/solr/access_logs
    $ cp -ra /opt/solr/solr-4.7.2/example/solr/collection1/conf /opt/solr/solr-4.7.2/example/solr/access_logs/conf
    $ cp $THIS_GITHUB_PROJECT_PATH/data/schema.xml /opt/solr/solr-4.7.2/example/solr/access_logs/conf
    $ cd /opt/solr/solr-4.7.2/example
    $ java -jar start.jar

Now from the Solr Admin UI (http://yourhost:8983/solr)

 * Click on *Core Admin* from the left-side menu
 * Click on the *Add Core* button
 * Fill in the form with the following data

<table style="border: 1px solid black">
    <tr><td><strong>name</strong></td><td>access_logs</td></tr>
    <tr><td><strong>instanceDir</strong></td><td>/opt/solr/solr-4.7.2/example/solr/access_logs</td></tr>
    <tr><td><strong>dataDir</strong></td><td>data</td></tr>
    <tr><td><strong>config</strong></td><td>solrconfig.xml</td></tr>
    <tr><td><strong>schema</strong></td><td>schema.xml</td></tr>
</table>
 
## Loading Data
### Download Dependencies:

Each of these downloaded jars should be placed in your home directory.

* DataFu - http://central.maven.org/maven2/com/linkedin/datafu/datafu/1.2.0/datafu-1.2.0.jar
* Lucidworks Jar - https://dl.dropboxusercontent.com/u/44331179/hadoop-lws-job-1.2.0-0-0.jar

### Load Data to HDFS

    hadoop fs -mkdir -p /user/paul/data/apache/access

400 rows of data have been created in the $THIS_GITHUB_PROJECT_PATH/data folder.  Upload it to the cluster in your home directory.

    hadoop fs -put logs_400.gz /user/paul/data/apache/access/data.gz

### Running the index script

A Pig script has been created to both index and store the rows into the SolrCore, and the HBase table.  Once it's uploaded to your home directory, edit it to make sure the ZK quorum, paths to the data, and paths to the previously uploaded jars are correct.  When that is complete, run it.

    pig -f index_ingest_logs.pig