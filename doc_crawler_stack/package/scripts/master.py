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
    Execute(params.stack_dir + '/package/scripts/setup.sh '+ params.stack_dir + ' ' + params.solr_dir + ' ' + params.demo_dir + ' ' + params.stack_log +' >> ' + params.stack_log)



  def configure(self, env):
    import params
    env.set_params(params)

  def stop(self, env):
    import params
    Execute(params.stack_dir + '/package/scripts/stop.sh >> ' + params.stack_log)
      
  def start(self, env):
    import params
    Execute(params.stack_dir + '/package/scripts/start.sh ' + params.solr_dir + ' ' + params.demo_dir + ' ' + params.stack_log +' >> ' + params.stack_log)
	

  def status(self, env):
    import params
    Execute(params.stack_dir + '/package/scripts/status.sh >> ' + params.stack_log)

if __name__ == "__main__":
  Master().execute()
