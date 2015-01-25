#!/bin/sh

# To integrate distcp with MapR, you need to run the yarn services as the "mapr" superuser, which is "root".

# You will need to shutdown "Resource Manager" and ALL "Node Managers" for the cluster via Ambari (if that is
# what's controlling the services.

# Use these commands to start the service(s) on the appropriate boxes.

# Resource Manager
su -l root -c "export HADOOP_LIBEXEC_DIR=/usr/lib/hadoop/libexec && /usr/lib/hadoop-yarn/sbin/yarn-daemon.sh --config /etc/hadoop/conf start resourcemanager"

# Node Manager
su -l root -c "export HADOOP_LIBEXEC_DIR=/usr/lib/hadoop/libexec && /usr/lib/hadoop-yarn/sbin/yarn-daemon.sh --config /etc/hadoop/conf start nodemanager"
