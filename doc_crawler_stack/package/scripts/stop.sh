#!/bin/bash
set -e 

echo "Stopping sbt"
ps -ef | grep "sb[t]" | awk '{print $2}' | xargs kill

echo "Stopping Solr"
ps -ef | grep "start.ja[r]" | awk '{print $2}' | xargs kill

echo "Done"



