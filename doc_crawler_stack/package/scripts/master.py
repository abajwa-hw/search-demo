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
    Execute('echo "\n\n\n\n"' + params.stack_dir + '/package/scripts/setup.sh >> ' + params.stack_log)

    #Now setup npm/bower  
    #Execute('echo "Starting bower install..." >> ' + params.stack_log)
    #Execute('cd /root/search-demo/document_crawler/src/main/webapp; npm install -g bower ;  >> ' + params.stack_log)
    #Execute('cd /root/search-demo/document_crawler/src/main/webapp; bower install --allow-root --config.interactive=false /root/search-demo/coe-int-master/ ;  >> ' + params.stack_log)
    #Execute('echo "Completed bower install" >> ' + params.stack_log)
    #Execute('cd /root/search-demo/document_crawler/src/main/webapp; npm install  >> ' + params.stack_log)
    #Execute('echo "Stack installed successfully"')


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
