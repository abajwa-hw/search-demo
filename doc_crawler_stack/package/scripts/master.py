import sys, os, pwd, signal, time
from resource_management import *
from subprocess import call

class Master(Script):
  def install(self, env):
    # Install packages listed in metainfo.xml
    self.install_packages(env)
    self.configure(env)
    import params

    Execute('~/search-demo/document_crawler_stack/package/scripts/setup.sh > ~/doc-crawler-setup.log')


  def configure(self, env):
    import params
    env.set_params(params)

  def stop(self, env):
    Execute('cd /root/search-demo/document_crawler;sbt stop;')
      
  def start(self, env):
    import params
    Execute('cd /root/search-demo/document_crawler;sbt run;')

  def status(self, env):
    Execute('cd /root/search-demo/document_crawler;sbt status;')


if __name__ == "__main__":
  Master().execute()
