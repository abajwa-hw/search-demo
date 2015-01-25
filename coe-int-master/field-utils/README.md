# Field Utilities

Various utilities that will help the fields with HDP installations

## Hive Utils

### Text-to-Orc

Customers upgrading Hadoop often use TextFiles as the basis for their tables.  This script runs against the MySQL Datasource for the Metastore and will generate all of the Hive scripts you'll need to manage migrating a selected set of tables.

## Operations

[Log Archive Script](operations/hdp-log-archive.sh) is a script that can be deployed to every HDP host to manage HDP log retention.

[NFS Init Script](operations/hdp-nfs3) is a service script that can be installed set to run a bootup with "service" to ensure your NFS Gateway is running after a reboot.

[Rack Awareness](operations/rack) is a template that can be used to configure Rack Awareness in HDP.

[Security KeyTab Assistant](operations/security) is a script designed to run against the output from Ambari while configuring Kerberos to help with the creation and deployment of the keytabs.

## Performance

### Teragen-sort

Scripts to help shake-down a cluster with Teragen/Sort.  These scripts should be adjusted for account for the size of the cluster, and will run through 5 different iterations of Teragen/Sort with different "block sizes".