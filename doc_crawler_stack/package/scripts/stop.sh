#!/bin/bash
set -e 

if [[ `ps -ef | grep "sb[t]" | wc -l` -gt 0 ]] 
then
	echo "Stopping sbt"
	ps -ef | grep "sb[t]" | awk '{print $2}' | xargs kill
fi

echo "Done"

exit 0

