#!/bin/bash
set -e 


if [[ `ps -ef | grep "start.ja[r]" | wc -l` -gt 0 ]] 
then
	echo "Stopping Solr"
	ps -ef | grep "start.ja[r]" | awk '{print $2}' | xargs kill
fi

echo "Done"

exit 0

