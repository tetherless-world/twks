#!/bin/bash
mvn -DskipTests -Darguments=-DskipTests release:rollback
