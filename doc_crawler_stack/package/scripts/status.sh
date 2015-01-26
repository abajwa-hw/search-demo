#!/bin/bash
set -e 

#return 1 only if both solr and sbt are up
RET=(ps -ef | grep "sb[t]" | wc -l) && (ps -ef | grep "start.ja[r] | wc -l")

exit $RET


