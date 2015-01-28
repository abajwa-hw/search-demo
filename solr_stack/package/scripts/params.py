#!/usr/bin/env python
from resource_management.libraries.functions.version import format_hdp_stack_version, compare_versions
from resource_management import *

# server configurations
config = Script.get_config()

stack_dir = config['configurations']['solr-config']['solr.stack.dir']
stack_log = config['configurations']['solr-config']['solr.log']
solr_dir = config['configurations']['solr-config']['solr.dir']
