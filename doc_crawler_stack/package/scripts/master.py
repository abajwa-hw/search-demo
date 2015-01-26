import sys, os, pwd, signal, time
from resource_management import *
from subprocess import call

class Master(Script):
  def install(self, env):
    # Install packages listed in metainfo.xml
    self.install_packages(env)
    self.configure(env)
    import params

    Execute('~/search-demo/doc_crawler_stack/package/scripts/setup.sh > ~/doc-crawler-setup.log')


  def configure(self, env):
    import params
    env.set_params(params)

  def stop(self, env):
    Execute('ps -ef | grep "sbt ru[n]" | awk '{print $2}' | xargs kill')
    Execute('ps -ef | grep "start.ja[r]" | awk '{print $2}' | xargs kill')
      
  def start(self, env):
    import params
    Execute('cd /root/search-demo/document_crawler; nohup sbt run >> ~/doc-crawler-setup.log &;')
	Execute('cd /opt/solr/solr/hdp; nohup java -jar start.jar >> ~/solr.log &;')
	

  def status(self, env):
    Execute('(ps -ef | grep "sbt ru[n]" | wc -l) && (ps -ef | grep "start.ja[r] | wc -l")')


if __name__ == "__main__":
  Master().execute()
