#!/usr/bin/env python
from resource_management.libraries.functions.version import format_hdp_stack_version, compare_versions
from resource_management import *

# server configurations
config = Script.get_config()

stack_dir = config['configurations']['doc-crawler-config']['stack.dir']
stack_log = config['configurations']['doc-crawler-config']['stack.log']
demo_dir = config['configurations']['doc-crawler-config']['demo.dir']
solr_dir = config['configurations']['doc-crawler-config']['solr.dir']
demo_searchdir = config['configurations']['doc-crawler-config']['demo.searchdir']
demo_samplezip = config['configurations']['doc-crawler-config']['demo.samplezip']
solr_hdfshome = config['configurations']['doc-crawler-config']['solr.hdfs.home']

