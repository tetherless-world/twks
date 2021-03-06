#!/bin/bash

set -e

rm -fr ~/.m2/repository/edu/rpi/tw/twks
MAVEN_OPTS="-Xmx3500M" \
  mvn \
  -q \
  -DargLine=\"-Dtwks.serverBaseUrl=http://twks-server:8080\" -Dtwks-rest-client.skipTests=false \
  test
mkdir surefire-reports
mv abc/target/surefire-reports/* surefire-reports
mv cli/target/surefire-reports/* surefire-reports
mv client/direct/target/surefire-reports/* surefire-reports
mv client/rest/target/surefire-reports/* surefire-reports
mv ext/target/surefire-reports/* surefire-reports
mv factory/target/surefire-reports/* surefire-reports
mv mem/target/surefire-reports/* surefire-reports
mv nanopub/target/surefire-reports/* surefire-reports
mv servlet/target/surefire-reports/* surefire-reports
mv tdb/target/surefire-reports/* surefire-reports
mv uri/target/surefire-reports/* surefire-reports
tar cf surefire-reports.tar surefire-reports
bzip2 -9 surefire-reports.tar
