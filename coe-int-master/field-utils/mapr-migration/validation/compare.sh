#!/bin/bash

#Default
DIR_LIST=../misc/dir_list.txt

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
    --help)
      echo "Usage: $0 --dir-list <list-file> --section <section>"
      exit -1
      ;;
    *)
      break
      ;;
  esac
done

echo $DIR_LIST

if [ "$DIR_LIST" == "" ] | [ "$SECTION" == "" ]; then
    echo "Missing --dir-list and/or --section"
    echo "Usage: $0 --dir-list <list-file> --section <section>"
    exit -1
fi


if [ "$DIR_LIST" != "" ]; then
    # Remove old results
    if [ -d "results/$SECTION" ]; then
      rm -rf results/$SECTION
    fi

    SED_PATTERN='1,/'$SECTION'/d;/\[/,$d;/^$/d;p'

    exec< <(sed -n $SED_PATTERN $DIR_LIST)

    #exec< ${DIR_LIST}

    while read line ; do
        # Skip Comment lines
        s1=$line
        s2=#

       # Skip Commented lines
        if [ "${s1:0:${#s2}}" != "$s2" ]; then

            DIRECTORY=`echo $line | sed 's/\//\\_/g' | sed 's|^_||g'`

            echo "Processing base files for $DIRECTORY"

            HDP_ROOT="hdp/"$SECTION"/"$DIRECTORY".txt"
            MAPR_ROOT="mapr/"$SECTION"/"$DIRECTORY".txt"

            if [ ! -f $HDP_ROOT.gz ] || [ ! -f $MAPR_ROOT.gz ]
            then
                echo "Missing compare file(s). Check that $HDP_ROOT and $MAPR_ROOT are present"
            else

                echo $HDP_ROOT
                echo $MAPR_ROOT

                echo "Prepping HDP"
                # Remove Directories before comparison
                gunzip -c "$HDP_ROOT".gz | egrep -v ^d > "$HDP_ROOT".fo
                # Filter fields and sort results by file
                awk < "$HDP_ROOT".fo '{ print $1, $3, $4, $5, $8 }' | sort -k 5 > $HDP_ROOT".prep"

                echo "Prepping MapR"
                # Remove Directories before comparison
                gunzip -c "$MAPR_ROOT".gz | egrep -v ^d  > "$MAPR_ROOT".fo
                # Filter fields and sort results by file
                awk < "$MAPR_ROOT".fo '{ print $1, $3, $4, $5, $8 }' | sort -k 5 > $MAPR_ROOT".prep"

                if [ ! -d results"/"$SECTION ]
                then
                    mkdir -p results"/"$SECTION
                fi
                echo 'Building Diff File: results/'$SECTION'/'$DIRECTORY'_chk_results.txt'
                diff $HDP_ROOT".prep" $MAPR_ROOT".prep" > "results/"$SECTION"/"$DIRECTORY"_chk_results.txt"

                GZIP_RESULT_FILE="results/"$SECTION"/"$DIRECTORY"_chk_results.txt.gz"
                if [ -f $GZIP_RESULT_FILE ];
                then
                    echo "Found previous diff... removing.."
                    rm -f $GZIP_RESULT_FILE
                fi

                gzip "results/"$SECTION"/"$DIRECTORY"_chk_results.txt"

                # Cleanup
                rm -f $HDP_ROOT".prep" $MAPR_ROOT".prep" "$HDP_ROOT".fo "$MAPR_ROOT".fo
            fi

        fi
    done

fi
