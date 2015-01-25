#!/bin/bash

# RUN AS root

# Make adjustments to the protocol_prefix.sh file to control source and dest. filesystems.
# Get the SOURCE and TARGET protocol prefix's
if [ -f ../misc/protocol_prefix.sh ]; then
. ../misc/protocol_prefix.sh
else
    echo "Couldn't find ../misc/protocol_prefix.sh.  Needed to set cluster name information for transfers"
    exit -1
fi

MODE="update"
DRY_RUN="no"
DELETE="no"
if [ ! -d ../logs ]; then
    mkdir ../logs
fi
DIR_LIST_FILE=../misc/dir_list.txt

TRANSFER_AUDIT_LOG=../logs/transfer.`date +'%Y%m%d-%H%M'`.log

while [ $# -gt 0 ]; do
  case "$1" in
    --dir-list)
      shift
      DIR_LIST_FILE=$1
      shift
      ;;
    --section)
      shift
      SECTION=$1
      shift
      ;;
    --mode)
      shift
      MODE=$1
      shift
      ;;
    --delete)
      DELETE="yes"
      shift
      ;;
    --bandwidth)
      shift
      BANDWIDTH=$1
      shift
      ;;
    --nohup)
      NOHUP='yes'
      shift
      ;;
    --dryrun)
      DRYRUN="yes"
      shift
      ;;
    --help)
      echo "Usage: $0 --dir-list <list-file> | [--section <section>] [--delete] [--mode initial|update(default)] [--nohup] [--dryrun]]"
      exit -1
      ;;
    *)
      break
      ;;
  esac
done

echo "+++ Transfer Log is: $TRANSFER_AUDIT_LOG +++"
echo "===== Script Parameters ====="
echo "DIR_LIST_FILE: $DIR_LIST_FILE"
echo "SECTION: $SECTION"
if [ "$BANDWIDTH" != "" ]; then
    echo "BANDWIDTH    : $BANDWIDTH"
fi
echo "MODE         : $MODE"
echo "DRYRUN       : $DRYRUN"
echo "============================="

echo "===== Script Parameters =====" > $TRANSFER_AUDIT_LOG
if [ "$DIR_LIST_FILE" != "" ]; then
    echo "DIR_LIST_FILE: $DIR_LIST_FILE" >> $TRANSFER_AUDIT_LOG
fi
if [ "$BANDWIDTH" != "" ]; then
    echo "BANDWIDTH    : $BANDWIDTH" >> $TRANSFER_AUDIT_LOG
fi
echo "MODE         : $MODE" >> $TRANSFER_AUDIT_LOG
echo "DRYRUN       : $DRYRUN" >> $TRANSFER_AUDIT_LOG
echo "=============================" >> $TRANSFER_AUDIT_LOG

if [ "$DIR_LIST_FILE" == "" ] | [ "$SECTION" == "" ]; then
    echo "Missing --dir-list and/or --section"
    echo "Usage: $0 --dir-list <list-file> | [--delete] [--mode initial|update(default)] [--nohup] [--dryrun]]"
    exit -1
fi

if [ ! -f "$DIR_LIST_FILE" ]; then
    echo "Directory list file NOT FOUND: $DIR_LIST_FILE"
    echo "Usage: $0 --dir-list 33<list-file> | [--delete] [--mode initial|update(default)] [--nohup] [--dryrun]]"
    exit -1
fi

# CUR_DIR=`pwd`
# APP_DIR=`dirname $0`
# 
# pushd $APP_DIR

PREFIX="HADOOP_USER_NAME=$SUPERUSER"
CMD="hadoop distcp"
# Bandwidth Control
if [ "$BANDWIDTH" != "" ]; then
  CMD="$CMD -bandwidth $BANDWIDTH_LIMIT"
fi

if [ "$DELETE" == "yes" ]; then
  CMD="$CMD -delete"
fi

TRANSFER_LOG_BASE=/transfer_log

TRANSFER_DATE=`date +'%Y%m%d-%H%M'`
TRANSFER_LOG="$TRANSFER_LOG_BASE/$MODE/$SECTION/$TRANSFER_DATE"

SED_PATTERN='1,/'$SECTION'/d;/\[/,$d;/^$/d;p'

#echo "$SED_PATTERN"
#echo "$SED_CMD"

exec< <(sed -n $SED_PATTERN $DIR_LIST_FILE)

VAR=0

while read line ; do
    ar=( $line )
    ((VAR=VAR+1))
    # Skip Comment lines
    s1=$line
    s2=#

    if [ "${s1:0:${#s2}}" != "$s2" ]; then

        TRANSFER_LOG_RUN=$TRANSFER_LOG"_"$VAR

        echo "==========================================================="
        echo "Transferring Data for directory : ${ar[0]}"
        echo "Job Log(on hdfs)                : $TRANSFER_LOG_RUN"
        echo "==========================================================="

        echo "===========================================================" >> $TRANSFER_AUDIT_LOG
        echo "Transferring Data for directory : ${ar[0]}" >> $TRANSFER_AUDIT_LOG
        echo "Job Log(on hdfs)                : $TRANSFER_LOG_RUN" >> $TRANSFER_AUDIT_LOG
        echo "===========================================================" >> $TRANSFER_AUDIT_LOG

        # Control Parallelism
        PART="-log $TRANSFER_LOG_RUN -i -pugp"

        # Preserve User and permissions.
        # Beware the Superuser problem.  Who ever runs this process (HADOOP_USER_NAME)
        #    needs to be a superuser on BOTH clusters.
        #PART="$PART -i -pugp"

        # Initial or Update
        if [ "$MODE" != "initial" ]; then
          PART="$PART -update"
        fi

        PART="$PART $SOURCE${ar[0]} $TARGET${ar[0]}"

        if [ "$DRYRUN" == "yes" ]; then
            echo "DRYRUN Command: $CMD $PART > /tmp/nohup_transfer_$TRANSFER_DATE.log"
            echo "DRYRUN Command: $CMD $PART > /tmp/nohup_transfer_$TRANSFER_DATE.log" >> $TRANSFER_AUDIT_LOG
            #echo "DRYRUN Command: $PREFIX $CMD $PART"
            #echo "DRYRUN Command: $PREFIX $CMD $PART" >> $TRANSFER_AUDIT_LOG
        else
            echo "$CMD $PART > /tmp/nohup_transfer_$TRANSFER_DATE.log" >> $TRANSFER_AUDIT_LOG
#                 eval "nohup $PREFIX $CMD $PART > /tmp/nohup_transfer_$VAR.log &"
            if [ "$NOHUP" == "yes" ]; then
                eval "nohup $CMD $PART >> /tmp/nohup_transfer_$TRANSFER_DATE.log &"
            else
                eval "$CMD $PART >> /tmp/transfer_$TRANSFER_DATE.log"
            fi
            #echo "$PREFIX $CMD $PART" >> $TRANSFER_AUDIT_LOG
            #eval "nohup $PREFIX $CMD $PART &"
            echo "===========================================================" >> $TRANSFER_AUDIT_LOG
        fi
    fi
done
