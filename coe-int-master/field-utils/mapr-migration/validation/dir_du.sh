#!/bin/bash
##############################
# Build the files required for comparison/validation.  Use a driver file to build them.
##############################

#Default
DIR_LIST=../misc/dir_list.txt

#Hadoop Version (default 2)
HADOOP_VER=2
VENDOR=hdp

while [ $# -gt 0 ]; do
  case "$1" in
    --dir-list)
      shift
      DIR_LIST=$1
      shift
      ;;
    --section)
      shift
      SECTION=$1
      shift
      ;;
    --vendor)
      shift
      VENDOR=$1
      shift
      ;;
    --help)
      echo "Usage: $0 --dir-list <list-file> --section <section> --vendor <hdp|mapr|cdh>"
      exit -1
      ;;
    *)
      break
      ;;
  esac
done

if [ "$DIR_LIST" == "" ] | [ "$SECTION" == "" ]; then
    echo "Missing --dir-list and/or --section"
      echo "Usage: $0 --dir-list <list-file> --section <section> --vendor <hdp|mapr|cdh>"
    exit -1
fi

if [ "$VENDOR" == "" ]; then
    echo "Missing VENDOR"
    echo "Usage: $0 --dir-list <list-file> --section <section> --vendor <hdp|mapr|cdh>"
    exit -1
else

    if [ -d $VENDOR"/"$SECTION ]; then
        rm -rf $VENDOR"/"$SECTION
    fi
    mkdir -p $VENDOR"/"$SECTION
    if [ "$VENDOR" == "mapr" ]; then
        HADOOP_VER=1
    fi
fi

echo "Processing directory listing from: $DIR_LIST, section: $SECTION for Vendor: $VENDOR"

if [ "$DIR_LIST" != "" ]; then

    SED_PATTERN='1,/'$SECTION'/d;/\[/,$d;/^$/d;p'

    exec< <(sed -n $SED_PATTERN $DIR_LIST)

    #exec< ${DIR_LIST}

    while read line ; do
        # Skip Comment lines
        s1=$line
        s2=#

        # Skip Commented lines
        if [ "${s1:0:${#s2}}" != "$s2" ]; then

            COMPARE_FILENAME=`echo $line | sed 's/\//\\_/g' | sed 's|^_||g'`

            echo "Building Directory listing for $line"
            if [ "$HADOOP_VER" == "1" ]; then
                HDFS_LSR_CMD="hadoop fs -lsr $line > $VENDOR/$SECTION/$COMPARE_FILENAME.txt"
            else
                HDFS_LSR_CMD="hdfs dfs -ls -R $line > $VENDOR/$SECTION/$COMPARE_FILENAME.txt"
            fi
            echo "Running command: $HDFS_LSR_CMD"
            eval "$HDFS_LSR_CMD"
            GZIP_COMPARE_FILE=$VENDOR"/"$SECTION"/"$COMPARE_FILENAME".txt.gz"
            if [ -f $GZIP_COMPARE_FILE ]; then
                echo "Found previous compare file... removing.."
                rm -f $GZIP_COMPARE_FILE
            fi
            eval "gzip $VENDOR/$SECTION/$COMPARE_FILENAME.txt"
        fi
    done

fi
