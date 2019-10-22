#!/bin/bash

set -e

MAVEN_OPTS="-Xmx3500M" mvn -q install -DskipTests
cd examples/client
mvn -q package -DskipTests
