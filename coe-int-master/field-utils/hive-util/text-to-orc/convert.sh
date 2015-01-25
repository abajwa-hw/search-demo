#!/bin/bash

# Wrapper script for ctl.groovy

cd `dirname $0`

. ./util-env.sh

./convert.groovy "$@" -mh $METASTORE_HOST -mu $METASTORE_USER -mp $METASTORE_PASSWORD -md $METASTORE_DATABASE