#!/bin/bash

# distp from Mapr to HDP (root user)
# Make adjustments to the protocol_prefix.sh file to control source and dest. filesystems.
# Get the SOURCE and TARGET protocol prefix's

if [ `whoami` != "root" ]; then
    echo "Should be run as root, since this is the 'control' superuser between the two clusters"
    exit -1
fi

# Change to the shells directory.
cd=`dirname $0`

if [ -f ../misc/protocol_prefix.sh ]; then
. ../misc/protocol_prefix.sh
else
    echo "Couldn't find ../misc/protocol_prefix.sh.  Needed to set cluster name information for transfers"
    exit -1
fi

hadoop distcp -i -pugp -delete -update $SOURCE/user/root/validation/mapr $TARGET/user/root/validation/mapr

if [ -d mapr ]; then
    rm -rf mapr
fi

hdfs dfs -get validation/mapr .