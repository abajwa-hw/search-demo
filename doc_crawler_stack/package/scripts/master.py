import sys, os, pwd, signal, time
from resource_management import *
from subprocess import call

class Master(Script):
  def install(self, env):
    # Install packages listed in metainfo.xml
    self.install_packages(env)
    self.configure(env)
    import params
    

    #run setup script
    #Execute(params.stack_dir + '/package/scripts/setup.sh '+ params.stack_dir + ' ' + params.solr_dir + ' ' + params.demo_dir + ' ' + params.stack_log +' >> ' + params.stack_log)


	#if HDFS dir (/user/solr/data/rfi_raw) already exists, remove it and then create it
    Execute('set +e; sudo -u hdfs hdfs dfs -test -d /user/solr/data/rfi_raw; if [ $? -eq 0 ]; then sudo -u hdfs hdfs dfs -rmr /user/solr/data/rfi_raw; fi')
    Execute('sudo -u hdfs hdfs dfs -mkdir -p /user/solr/data/rfi_raw')
    Execute('sudo -u hdfs hdfs dfs -chown solr /user/solr')
    Execute('sudo -u hdfs hdfs dfs -chmod -R 777 /user')
    

    #e.g. if zipfilepath was set to /root/search-demo/search-docs.zip then this would be /root/search-demo/
    #zipfiledir = os.path.dirname(params.demo_zipfilepath) + os.sep
    #e.g. search-docs.zip
    #zipfilenameext = os.path.basename(params.demo_zipfilepath)
    #e.g. search-docs
    #zipfilename=os.path.splitext(zipfilenameext)[0]
    #e.g. /root/search-demo/search-docs
    #zipdirpath = zipfiledir + zipfilename
    
    Execute('echo Emptying staging dir' +params.demo_searchdir + ' >> ' + params.stack_log )
    Execute('rm -rf '+params.demo_searchdir)
        
    #unzip the sample zip into searchdir and replace all spaces in filename with _    
    Execute('unzip '+params.demo_samplezip+' -d '+ params.demo_searchdir)
    Execute('find '+params.demo_searchdir+' -iname \'* *\' -execdir bash -c \'mv "$1" "${1// /_}"\' _ {} \;' )

    Execute ('echo "Copying search docs to HDFS under /user/solr/data/rfi_raw" >> ' + params.stack_log)
    Execute('hadoop fs -put '+params.demo_searchdir+'/* /user/solr/data/rfi_raw/ >> ' + params.stack_log)


    #Setup Solr
    Execute ('echo "Configuring Solr core" >> ' + params.stack_log)    
    Execute('cd '+params.solr_dir+'/solr; cp -r example hdp ; rm -rf hdp/example* hdp/multicore')
    Execute ('cd '+params.solr_dir+'/solr; if [ -d "./hdp/solr/collection1" ]; then rsync -av hdp/solr/collection1/* hdp/solr/rawdocs; else rsync -av hdp/solr/hdp1/* hdp/solr/rawdocs; fi ')
    Execute ('cd '+params.solr_dir+'/solr; rm -f hdp/solr/rawdocs/core.properties ; rm -f hdp/solr/rawdocs/core.properties')
    

    #replace files from git
    Execute('/bin/cp -f '+params.demo_dir+'/document_crawler/artifacts/solrconfig.xml  '+params.solr_dir+'/solr/hdp/solr/rawdocs/conf/solrconfig.xml')
    Execute('/bin/cp -f '+params.demo_dir+'/document_crawler/artifacts/schema.xml '+params.solr_dir+'/solr/hdp/solr/rawdocs/conf/schema.xml')

	#replace Solr HDFS home param in solrconfig.xml, escaping slashes
    escapedSolrHome = params.solr_hdfshome.replace('/','\\/')
    Execute('sed -i "s#?__SOLR_HDFS_HOME__#'+params.solr_hdfshome+'#g" ' + params.solr_dir+'/solr/hdp/solr/rawdocs/conf/solrconfig.xml')
        
    Execute('echo "Checking if Solr is running...." >>' + params.stack_log)
    Execute(params.stack_dir + '/package/scripts/start_solr.sh ' + params.solr_dir + ' ' + params.stack_log)
    Execute('sleep 10')
    #Create core called rawdocs
    Execute('curl "http://localhost:8983/solr/admin/cores?action=CREATE&name=rawdocs&instanceDir='+params.solr_dir+'/solr/hdp/solr/rawdocs/"')


    Execute ('echo "Downloading lucidworks jar" >> ' + params.stack_log)    
    Execute('cd /root ; wget http://package.mapr.com/tools/search/lucidworks-hadoop-1.2.0-0-0.tar.gz ; tar xvzf *.tar.gz ; cp lucidworks-hadoop*/hadoop/hadoop-lws-job-*.jar /tmp ; ')
    Execute('echo "starting mapreduce job"  >> ' + params.stack_log)
    Execute('yarn jar /tmp/hadoop-lws-job-1.2.0-0-0.jar com.lucidworks.hadoop.ingest.IngestJob -Dlww.commit.on.close=true -Dadd.subdirectories=true -cls com.lucidworks.hadoop.ingest.DirectoryIngestMapper -c rawdocs -i /user/solr/data/rfi_raw/ -of com.lucidworks.hadoop.io.LWMapRedOutputFormat -s http://sandbox.hortonworks.com:8983/solr')

    Execute('echo "setting up banana" >> '  + params.stack_log)
    #Execute('cd /opt/solr ; git clone https://github.com/LucidWorks/banana.git ; rsync -av '+params.solr_dir+'/banana/* '+params.solr_dir+'/solr-4.7.2/hdp/solr-webapp/webapp/')
    Execute('cd /opt/solr ; git clone https://github.com/LucidWorks/banana.git ; mv '+params.solr_dir+'/banana/ '+params.solr_dir+'/solr-4.7.2/hdp/solr-webapp/webapp/')


    Execute('echo "change collection1 to rawdocs..." >> '  + params.stack_log)
    Execute('sed -i "s/collection1/rawdocs/g" ' + params.solr_dir + '/solr-4.7.2/hdp/solr-webapp/webapp/banana/src/app/dashboards/default.json')


    Execute('echo "Installing sbt, nodejs, npm ..." >> ' +  params.stack_log)
    Execute('curl https://bintray.com/sbt/rpm/rpm > /root/bintray-sbt-rpm.repo')
    Execute('mv /root/bintray-sbt-rpm.repo /etc/yum.repos.d/')
    Execute('yum install -y sbt nodejs npm >> ' + params.stack_log)
    Execute('echo "Completed sbt, nodejs, npm install"')
    
    Execute('echo "Starting bower install..." >> ' + params.stack_log)
    Execute('cd ' + params.demo_dir + '/document_crawler/src/main/webapp' + ' ; ' + \
                                      'npm install -g bower >> ' + params.stack_log + ' ; ' + \
                                      'bower install --allow-root --config.interactive=false ' + params.demo_dir + '/coe-int-master/ >> ' + params.stack_log + ' ; ')
    #Execute('npm install -g bower >> ' + params.stack_log)
    #Execute('bower install --allow-root --config.interactive=false $DEMO_ROOT/coe-int-master/ >> ' + params.stack_log)
    Execute('echo "Completed bower install" >> '  + params.stack_log)

    Execute('echo "Starting npm imstall..." >> '  + params.stack_log)
    Execute('cd  ' + params.demo_dir + '/document_crawler/src/main/webapp' + ' ;' + \
    									'npm install >> ' + params.stack_log + ' ; ')

    Execute(params.stack_dir + '/package/scripts/start.sh ' + params.solr_dir + ' ' + params.demo_dir + ' ' + params.stack_log +' >> ' + params.stack_log)
    #sleep until service is up - first time can take upto 5min
    Execute('RET=1; until [[ $RET -eq 0 ]]; do echo "Sleeping until service is up..." ; sleep 5; nc -tz localhost 9090 > /dev/null 2>&1; RET=$?; done')

    Execute('echo "Stack installed and started successfully" >> ' +  params.stack_log)


  def configure(self, env):
    import params
    #env.set_params(params)

  def stop(self, env):
    import params
    Execute(params.stack_dir + '/package/scripts/stop.sh >> ' + params.stack_log)
      
  def start(self, env):
    import params
    Execute(params.stack_dir + '/package/scripts/start.sh ' + params.solr_dir + ' ' + params.demo_dir + ' ' + params.stack_log +' >> ' + params.stack_log)
	

  def status(self, env):
    import params
    Execute('nc -tz localhost 9090 > /dev/null 2>&1')
    #Execute(params.stack_dir + '/package/scripts/status.sh >> ' + params.stack_log)

if __name__ == "__main__":
  Master().execute()
