#!/bin/bash
mvn -DskipTests -Darguments=-DskipTests -P release release:rollback
