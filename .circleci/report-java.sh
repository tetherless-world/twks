#!/bin/bash

set -e

MAVEN_OPTS="-Xmx3500M" \
  mvn \
  -DskipTests -DstagingDirectory=$PWD/site \
  site site:stage
tar cf site.tar site
bzip2 -9 site.tar
