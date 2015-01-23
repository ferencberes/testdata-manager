#!/bin/bash

thisDir="$(dirname $0)"
thisDir="$(readlink -f "$thisDir")"

pushd "$thisDir"

#build project and create jar:
mvn clean package

echo "Create /chart, /logs  directories..."
mkdir -p "$thisDir"/charts
mkdir -p "$thisDir"/logs/newlogs
echo "These directories does not need to be version controled."

./scripts/generate_config.sh

newLogPathLine="log_dir:"$thisDir"/logs/newlogs"
echo "$newLogPathLine" > ./config/filePath.conf

echo "After you have deployed a mysql server and created a schema for the testing, set the proper parameters in /config scripts!"
popd
