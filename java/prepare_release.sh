#!/bin/bash
# http://maven.apache.org/maven-release/maven-release-plugin/examples/prepare-release.html
# This step doesn't upload anything, so it doesn't need to limit the list of modules.
# Don't use the release profile so that all of the modules get versioned together
mvn -DskipTests -Darguments=-DskipTests release:clean release:prepare
