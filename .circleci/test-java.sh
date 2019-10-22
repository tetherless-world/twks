#!/bin/bash

set -e

rm -fr ~/.m2/repository/edu/rpi/tw/twks
MAVEN_OPTS="-Xmx3500M" \
  mvn \
  -DargLine=\"-Dtwks.serverBaseUrl=http://twks-server:8080\" -Dtwks-client.skipTests=false \
  test
mkdir surefire-reports
mv cli/target/surefire-reports/* surefire-reports
mv ext/target/surefire-reports/* surefire-reports
mv factory/target/surefire-reports/* surefire-reports
mv nanopub/target/surefire-reports/* surefire-reports
mv server/target/surefire-reports/* surefire-reports
mv tdb/target/surefire-reports/* surefire-reports
mv uri/target/surefire-reports/* surefire-reports
tar cf surefire-reports.tar surefire-reports
bzip2 -9 surefire-reports.tar
