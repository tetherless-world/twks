#!/bin/bash

set -e

rm -fr ~/.m2/repository/edu/rpi/tw/twks
MAVEN_OPTS="-Xmx3500M" \
  mvn \
  -DargLine=\"-Dtwks.serverBaseUrl=http://twks-server:8080\" -Dtwks-client.skipTests=false -DstagingDirectory=$PWD/site \
  clean package site site:stage
tar cf site.tar site
bzip2 -9 site.tar
mkdir surefire-reports
mv java/cli/target/surefire-reports/* surefire-reports
mv java/ext/target/surefire-reports/* surefire-reports
mv java/factory/target/surefire-reports/* surefire-reports
mv java/nanopub/target/surefire-reports/* surefire-reports
mv java/server/target/surefire-reports/* surefire-reports
mv java/tdb/target/surefire-reports/* surefire-reports
mv java/uri/target/surefire-reports/* surefire-reports
tar cf surefire-reports
bzip2 -9 surefire-reports.tar
