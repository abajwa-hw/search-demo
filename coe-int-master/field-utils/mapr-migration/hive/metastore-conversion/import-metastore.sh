#!/bin/bash

cd `dirname $0`

while [ $# -gt 0 ]; do
  case "$1" in
    --target-db)
      shift
      TARGET_DB=$1
      shift
      ;;
    --mysql-dump-file)
      shift
      MYSQL_DUMP_FILE=$1
      shift
      ;;
    --target-namenode)
      shift
      NAMENODE=$1
      shift
      ;;
    --help)
      echo "Usage: $0 --target-db <target mysql database> --mysql-dump-file <sql export file> --target-namenode <namenode>"
      exit -1
      ;;
    *)
      break
      ;;
  esac
done

if [ "$TARGET_DB" == "" ] | [ "$MYSQL_DUMP_FILE" == "" ] | [ "$NAMENODE" == "" ]; then
    echo "Missing required values"
    echo "Usage: $0 --target-db <target mysql database> --mysql-dump-file <sql export file> --target-namenode <namenode>"
fi

if [ ! -f /usr/lib/hive/scripts/metastore/upgrade/mysql/upgrade-0.12.0-to-0.13.0.mysql.sql ]; then
    echo "This must be run from a machine that has hive installed, we could not find: "
    echo "   /usr/lib/hive/scripts/metastore/upgrade/mysql/upgrade-0.12.0-to-0.13.0.mysql.sql to do the upgrade"
    exit -1
fi

SOURCE=maprfs:
# Note: The target hostname needs to escape the . with a \
HOSTNAME=`echo $NAMENODE | sed 's/\./\\./g'`
TARGET=hdfs://$HOSTNAME
SED_CMD='s!$SOURCE!$TARGET!g'

eval "sed $SED_CMD $MYSQL_DUMP_FILE > $MYSQL_DUMP_FILE.new"

# Import dump file
mysql -u root -e "drop database $TARGET_DB"

mysql -u root -e "create database $TARGET_DB"

mysql -u root $TARGET_DB < $MYSQL_DUMP_FILE.new

pushd /usr/lib/hive/scripts/metastore/upgrade/mysql

UPGRADE_SCRIPT=upgrade-0.12.0-to-0.13.0.mysql.sql

mysql -u root $TARGET_DB < $UPGRADE_SCRIPT

popd

# Fix behavioral change with 'decimal' type declared in Hive 0.11/0.12.
mysql -u root $TARGET_DB < decimal_pre-13_fix.sql

# SQL to grant rights to Database Tables.
mysql -u root -e "GRANT ALL ON $TARGET_DB.* TO 'hiveuser'@'%'" $TARGET_DB