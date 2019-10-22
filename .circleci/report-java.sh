#!/bin/bash

set -e

MAVEN_OPTS="-Xmx3500M" \
  mvn \
  -q \
  -DskipTests -DstagingDirectory=$PWD/site \
  site site:stage
tar cf site.tar site
bzip2 -9 site.tar
