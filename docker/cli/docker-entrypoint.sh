#!/bin/bash

java -Dedu.rpi.tw.twks.client.baseUrl=\"http://twks-server:8080\" -jar /twks-cli-current.jar $@
