import sys, os, pwd, signal, time
from resource_management import *
from subprocess import call

class Master(Script):
  def install(self, env):
    # Install packages listed in metainfo.xml
    self.install_packages(env)
    self.configure(env)
    import params
    
    #First run setup script which has simple shell setup
    #Execute(params.stack_dir + '/package/scripts/setup.sh >> ' + params.stack_log)


#if [ ! -d "/opt/solr" ]; then
#    #solr is not on 2.2 but is installed on sandbox 
#    adduser solr
#    mkdir /opt/solr
#    chown solr /opt/solr

#    sudo -u hdfs hdfs dfs -mkdir -p /user/solr
#    sudo -u hdfs hdfs dfs -mkdir -p /user/solr/data
    
#    #setup solr
#    cd /opt/solr
#    wget -q http://apache.mirror.gtcomm.net/lucene/solr/4.7.2/solr-4.7.2.tgz
#    tar -xvzf solr-4.7.2.tgz
#    ln -s solr-4.7.2 solr
#fi

    Execute('sudo -u hdfs hdfs dfs -mkdir -p /user/solr/data/rfi_raw')
    Execute('sudo -u hdfs hdfs dfs -chown solr /user/solr')
    Execute('sudo -u hdfs hdfs dfs -chmod -R 777 /user')

    #Move search docs to HDFS
    Execute('rm -rf /root/search-demo/RFIsForSolr')
    Execute('hadoop fs -rm -R /user/solr/data/rfi_raw/*')
    Execute('cd /root/search-demo;unzip RFIsForSolr.zip;cd RFIsForSolr;find . -iname \'* *\' -execdir bash -c \'mv "$1" "${1// /_}"\' _ {} \;' )
    Execute('hadoop fs -put /root/search-demo/RFIsForSolr/* /user/solr/data/rfi_raw/')


#Setup Solr
    Execute('cd /opt/solr/solr; cp -r example hdp ; rm -rf hdp/example* hdp/multicore ; mv hdp/solr/hdp1 hdp/solr/rawdocs ; rm -f hdp/solr/rawdocs/core.properties ; rm -f hdp/solr/rawdocs/core.properties')
    
#if [ -d "./hdp/solr/collection1" ]; then
#    mv hdp/solr/collection1 hdp/solr/rawdocs
#else
#    mv hdp/solr/hdp1 hdp/solr/rawdocs
#fi    
    

    #replace files from git
    Execute('/bin/cp -f /root/search-demo/document_crawler/artifacts/solrconfig.xml  /opt/solr/solr/hdp/solr/rawdocs/conf/solrconfig.xml')
    Execute('/bin/cp -f /root/search-demo/document_crawler/artifacts/schema.xml /opt/solr/solr/hdp/solr/rawdocs/conf/schema.xml')

    Execute('echo "Starting Solr"')
    #nohup java -jar start.jar >> /var/log/doc-crawler.log &
    #Execute('cd /opt/solr/solr/hdp ; nohup java -jar start.jar >> /var/log/doc-crawler.log 2>&1 ;')
    Execute(params.stack_dir + '/package/scripts/start_solr.sh')
    Execute('sleep 10')
    #Create core called rawdocs
    Execute('curl "http://localhost:8983/solr/admin/cores?action=CREATE&name=rawdocs&instanceDir=/opt/solr/solr/hdp/solr/rawdocs/"')



    Execute('cd /root ; wget http://package.mapr.com/tools/search/lucidworks-hadoop-1.2.0-0-0.tar.gz ; tar xvzf *.tar.gz ; cp lucidworks-hadoop*/hadoop/hadoop-lws-job-*.jar /tmp ; ')
    Execute('echo "starting mapreduce job"')
    Execute('yarn jar /tmp/hadoop-lws-job-1.2.0-0-0.jar com.lucidworks.hadoop.ingest.IngestJob -Dlww.commit.on.close=true -Dadd.subdirectories=true -cls com.lucidworks.hadoop.ingest.DirectoryIngestMapper -c rawdocs -i /user/solr/data/rfi_raw/ -of com.lucidworks.hadoop.io.LWMapRedOutputFormat -s http://sandbox.hortonworks.com:8983/solr')

    Execute('echo "setup banana"')

    Execute('cd /opt/solr ; git clone https://github.com/LucidWorks/banana.git ; mv /opt/solr/banana /opt/solr/solr-4.7.2/hdp/solr-webapp/webapp/')


    Execute('echo "change collection1 to rawdocs..."')
    Execute('sed -i "s/collection1/rawdocs/g" /opt/solr/solr-4.7.2/hdp/solr-webapp/webapp/banana/src/app/dashboards/default.json')


    Execute('echo "Installing sbt, nodejs, npm ..."')
    Execute('curl https://bintray.com/sbt/rpm/rpm > /root/bintray-sbt-rpm.repo')
    Execute('mv /root/bintray-sbt-rpm.repo /etc/yum.repos.d/')
    Execute('yum install -y sbt nodejs npm')
    Execute('echo "Completed sbt, nodejs, npm install"')



    #Now setup npm/bower  
    Execute('echo "Starting bower install..." >> ' + params.stack_log)
    Execute('cd /root/search-demo/document_crawler/src/main/webapp; npm install -g bower ;  >> ' + params.stack_log)
    Execute('cd /root/search-demo/document_crawler/src/main/webapp; bower install --allow-root --config.interactive=false /root/search-demo/assembly-ui ;  >> ' + params.stack_log)
    Execute('echo "Completed bower install" >> ' + params.stack_log)
    Execute('cd /root/search-demo/document_crawler/src/main/webapp; npm install  >> ' + params.stack_log)
    Execute('echo "Stack installed successfully"')


  def configure(self, env):
    import params
    env.set_params(params)

  def stop(self, env):
    import params
    Execute(params.stack_dir + '/package/scripts/stop.sh >> ' + params.stack_log)
      
  def start(self, env):
    import params
    Execute(params.stack_dir + '/package/scripts/start.sh >> ' + params.stack_log)
    

  def status(self, env):
    import params
    Execute(params.stack_dir + '/package/scripts/status.sh >> ' + params.stack_log)

if __name__ == "__main__":
  Master().execute()
