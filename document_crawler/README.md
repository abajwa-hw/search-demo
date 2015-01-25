# Document Crawler

Authors: Paul Codding, Joseph Niemiec, Piotr Pruski

## Background

## Installation & Configuration

### Step 1: Enable NFS Access to HDFS in HDP 2.2

<b>1. Configure settings for HDFS NFS gateway.</b>

NFS gateway uses the same configurations as used by the NameNode and DataNode. Configure the following properties based on your application's requirements:

Edit the hdfs-site.xml file on your NFS gateway machine and modify the following property:

```
<property>
  <name>dfs.namenode.accesstime.precision</name>
  <value>3600000</value>
  <description>The access time for HDFS file is precise up to this value. 
               The default value is 1 hour. Setting a value of 0 disables
               access times for HDFS.
  </description>
</property>
```

Alternatively use Ambari to override the <code>dfs.namenode.accesstime.precision</code> property under <b>Advanced hdfs-site</b> and <b>Save</b> the changes.

![dfs.namenode.accesstime.precision](img/dfs.namenode.accesstime.precision.png?raw=true)

Add the following properties to hdfs-site.xml file:

```
<property>    
    <name>dfs.nfs3.dump.dir</name>    
    <value>/tmp/.hdfs-nfs</value> 
</property>

<property>    
    <name>dfs.nfs.exports.allowed.hosts</name>    
    <value>* rw</value> 
</property>
```

Once again you can do this through Ambari rather than editing the xml file. Under <b>Custon hdfs-site</b> section make the following changes and <b>Save</b>.

![dfs.nfs](img/dfs.nfs.png?raw=true)

<i>By default, the export can be mounted by any client. You must update this property to control access. The value string contains the machine name and access privilege, separated by whitespace characters. The machine name can be in single host, wildcard, or IPv4 network format. The access privilege uses rw or ro to specify readwrite or readonly access to exports. If you do not specifiy an access privilege, the default machine access to exports is readonly. Separate machine dentries by ;. For example, 192.168.0.0/22 rw ; host*.example.com ; host1.test.org ro;</i>

<b>2. The user running the NFS gateway must be able to proxy all of the users using the NFS mounts.</b>

For example, if you will run the gateway services as root, set the following properties in the Custom core-site section of the HDFS Configs in Ambari. Otherwise replace "root" with the user that will be running the gateway.

![hadoop.proxyuser](img/hadoop.proxyuser.png?raw=true)

<b>3. Restart the cluster services for HDFS, MapReduce2, and YARN.</b>

If you had made the changes through Ambari originally you will automatically see a prompt that a restart is required for certain components.

![restart_services](img/restart_services.png?raw=true)

Click on Restart All for each one of these.

![restart_required](img/restart_required.png?raw=true)

<b>4. Install the NFS Server bits for the Linux OS.</b>

```
yum install nfs* -y
```

<b>5. Start the NFS gateway service.</b>

Three daemons are required to provide NFS service: rpcbind (or portmap), mountd and nfsd. The NFS gateway process has both nfsd and mountd. It shares the HDFS root "/" as the only export. We recommend using the portmap included in NFS gateway package as shown below:

You need to stop the native Linux nfs/rpcbind/portmap services provided by the platform and then start the Hadoop enabled version.

```
service nfs stop
service rpcbind stop
```

Start the included portmap package (needs root privileges):

```
hadoop-daemon.sh start portmap
```

Start mountd and nfsd.

No root privileges are required for this command. However, verify that the user starting the Hadoop cluster and the user starting the NFS gateway are same.

```
hadoop-daemon.sh start nfs3
```

<b>6. Create a user on your client machine that matches a user in the HDP environment.</b>

NFS gateway in this release uses AUTH_UNIX style authentication which means that the the login user on the client is the same user that NFS passes to the HDFS. For example, if the NFS client has current user as admin, when the user accesses the mounted directory, NFS gateway will access HDFS as user admin. To access HDFS as hdfs user, you must first switch the current user to hdfs on the client system before accessing the mounted directory.

Create a user hdfs with the same UID as in the HDP cluster. For example:

```
sudo -i
mkdir /Users/hdfs
dscl . create /Users/hdfs
dscl . create /Users/hdfs RealName "hdfs"
dscl . create /Users/hdfs hint "Password Hint"
dscl . passwd /Users/hdfs hdfs
dscl . create /Users/hdfs UniqueID 506
dscl . create /Users/hdfs PrimaryGroupID 501
dscl . create /Users/hdfs UserShell /bin/bash
dscl . create /Users/hdfs NFSHomeDirectory /Users/hdfs
chown -R hdfs:guest /Users/hdfs
```

<b>7. Mount HDFS as a file system on local client machine.</b>

```
mount -t nfs -o vers=3,proto=tcp,nolock $server:/  $mount_point
```

Now you can browse HDFS as if it was part of the local filesystem.

The following example shows how to mount the filesystem and to load data into HDFS. It take a file from the local disk, RFI.zip, and loads it into the hdfs user directory on HDFS file system. On this local machine, HDFS will be mounted at /mnt/.

![nfs_load](img/nfs_load.png?raw=true)

You can verify the file is actually on HDFS with the hadoop fs command with the client tools.

![verify_hdfs](img/verify_hdfs.png?raw=true)


### Step 2: Setup Solr & Banana

<b> 1. Make Solr User on Local Linux System <b/>
```
adduser solr
mkdir /opt/solr
chown solr /opt/solr
```

<b>2. Navigate to the Solr folder & Download Solr 4.7.2</b>
```
cd /opt/solr

wget -q http://apache.mirror.gtcomm.net/lucene/solr/4.7.2/solr-4.7.2.tgz
```

<b>3. UnTar Solr and Move some folders </b>
```
tar -xvzf solr-4.7.2.tgz
ln -s solr-4.7.2 solr
cd solr
cp -r example hdp 
rm -rf hdp/examle* hdp/multicore
mv hdp/solr/collection1 hdp/solr/rawdocs
rm hdp/solr/rawdocs/core.properties
```

<b>4. Update solrconfig.xml and replace schema.xml</>

Using the solrconfig.xml from the artifacts directory search for 'NAMENODEHOSTNAME' and update with your correct NAMENODEHOSTNAME. Then move the config into /opt/solr/solr/hdp/solr/rawdocs/conf/solrconfig.xml overwriting the existing solrconfig.  

Using the schema.xml from the artifacts directory replace the existing schema.xml located in /opt/solr/solr/hdp/solr/rawdocs/conf/  

<b>5. Create Hadoop HDFS users <b/>

Create Solr user Folder on HDFS
```/Users/jniemiec/Documents/GIT/sedev/demo-artifacts/document_crawler/README.md
su hdfs
hdfs dfs -mkdir -p /user/solr
hdfs dfs -chown solr /user/solr
```

<b>6. Start Solr </b>
```
cd /opt/solr/solr/hdp
nohup java -jar start.jar &
```

After starting Solr navigate to your web browser to http://HOSTNAME:8983/solr/ and see if its online. 


<b>8. Create Solr Core </b>
If all if working correctly Solr is now running and your at the webui. Click on 'Core Admin' and then 'Add Core'. Create a new core only changing the values below. 


```
name: rawdocs 
instanceDir: /opt/solr/solr/hdp/solr/rawdocs/
```

Leave all the rest of the properties the same and click 'Add Core'


### Step 3: Index Documents

<b> 1. Download Hadoop Connector </b>
Use Jar hadoop-lws-job-1.2.0-0-0.jar, other versions both newer and older appear to have issues. 


<b> 2. Run the DirectoryCrawler Job </b>
Change the value passed for '-i' below to the directory with all of the documents you wish to index. Make sure to pass the wildcard '*' at the end of the '-i' value inorder to get all documents in the directory. 


```
yarn jar /tmp/hadoop-lws-job-1.2.0-0-0.jar com.lucidworks.hadoop.ingest.IngestJob -Dlww.commit.on.close=true -Dadd.subdirectories=true -cls com.lucidworks.hadoop.ingest.DirectoryIngestMapper -c rawdocs -i /user/solr/data/rfi_raw/ -of com.lucidworks.hadoop.io.LWMapRedOutputFormat -s http://HOSTNAME:8983/solr
```

### Step 4: Install and Configure Banana

<b>1. Get Banana </b>

```
git clone https://github.com/LucidWorks/banana.git
```

<b>2. Run Banana Web App within your existing Solr instance </b>
Move the banana folder created in step 1 into $SOLR_HOME/example/solr-webapp/webapp/

```
mv /opt/solr/banana /opt/solr/solr-4.7.2/hdp/solr-webapp/webapp/
```

<b>3. Browse to http://<solr_server>:<port_number>/solr/banana/src/index.html#/dashboard</b>

<b>4. Modify default config's</b>

Edit <b>banana/src/config.js</b> and <b>banana/src/app/dashboards/default.json</b> to enter the hostname and port that you are using.

You will also need to update the collection name to 'rawdocs'. 

If creating a time-series dashboard, you'll also need to update the Time Field value to a field with type date.

### Step 5: Install and Configure the Custom UI

Install sbt, and npm

    cd src/main/webapp
	npm install

Choose the Version of Angular JS that mentions "Hortonowrks Assembly UI" as the dependent component.

	cd -

Edit the configuration file to point to your Solr instance:

	vi src/main/resources/application.conf
	
Start the server in restart mode so it will poll for changes and restart itself

    sbt ~re-start
	
Navigate to `http://localhost:9090`.