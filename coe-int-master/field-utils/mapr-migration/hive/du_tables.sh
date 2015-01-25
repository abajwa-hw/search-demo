#!/bin/bash

# Will traverse the Hive Metastore for Tables in TextFile Format and check the size of the directory the table is in.

OUTPUT=output_list.txt;

while [ $# -gt 0 ]; do
  case "$1" in
    --output)
      shift
      OUTPUT=$1
      shift
      ;;
  esac
done

echo "OUTPUT" > $OUTPUT
for db in `hive -e 'show databases'`;do
    echo "Database: $db"

    for table in `hive -e "use $db;show tables"`;do
        #FOUND=`hive -e "use $db;show create table $table" | grep 'TextInputFormat\|hdfs\:\/\/' | wc -l`
        unset LINE
        while read line
        do
            nextline=( $line )
            LINE+=( $nextline )
        done < <(hive -e "use $db;show create table $table" | grep "TextInputFormat\|hdfs\:\/\/")

        echo "${LINE[0]}"
        echo "${LINE[1]}"

#        if [[ "${LINE[0]}" == *TextInputFormat* ]]; then
            if [[ "${LINE[1]}" == *hdfs* ]]; then
#                 echo "Table: $table is a TextInputFormat and is location at: ${LINE[1]}"
                # Remove Whitespace
                TRIM_DIR=`echo ${LINE[1]} | tr -d ' '`
                # Get Length of String
                LEN=${#TRIM_DIR}
                LEN=`expr $LEN - 1`
                # Cut out the "'" from the string before building hdfs command
                F_DIR=`echo $TRIM_DIR | cut -c 2-$LEN`
                CMD="hdfs dfs -du -s $F_DIR"
                echo $CMD
                # Pipe Results of command into variable.
                SIZE=$($CMD)
                echo "$db $table ${LINE[0]} $SIZE" >> $OUTPUT
            fi
#         else
#             echo "Table: $table is NOT a TextInputFormat"

#         fi
    done

done