#!/bin/bash

# Compare via -count, the directories for a converted orc, against the directories from the original source.

while [ $# -gt 0 ]; do
  case "$1" in
    --partition-list)
      shift
      PARTITION_LIST=$1
      shift
      ;;
    --original-base)
      shift
      ORIGINAL_BASE=$1
      shift
      ;;
    --orc-base)
      shift
      ORC_BASE=$1
      shift
      ;;
    --help)
      echo "Usage: $0 --partition-list <list-file>"
      exit -1
      ;;
    *)
      break
      ;;
  esac
done

if [ "$PARTITION_LIST" != "" ]; then
    exec< ${DIR_LIST}

    while read line ; do
        ar=( $line )

        # Skip Comment lines
        s1=$line
        s2=#

        if [ "${s1:0:${#s2}}" != "$s2" ]; then
            ORIG_CMD="hdfs dfs -count $ORIGINAL_BASE/${ar[0]}"
            ORC_CMD="hdfs dfs -count $ORC_BASE/${ar[0]}"
            echo $ORIG_CMD
            echo $ORC_CMD
            ORIG_VALUE=eval "$ORIG_CMD"
            ORC_VALUE=eval "$ORC_CMD"
        fi
    done

fi
