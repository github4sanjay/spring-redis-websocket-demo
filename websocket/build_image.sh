#!/usr/bin/env bash

rm -rf build
./gradlew clean build -x test
docker build -t example-websocket:0.0.1-SNAPSHOT -f Dockerfile .