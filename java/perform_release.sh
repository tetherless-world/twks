#!/bin/bash
# release:perform automatically activates the release profile
mvn -DskipTests -Darguments=-DskipTests release:perform
