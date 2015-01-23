#!/bin/bash

thisDir="$(dirname $0)"
thisDir="$(readlink -f "$thisDir")"
classPath=hu.sztaki.testdata_manager.runner.TestDataManagerRunner

mainDir="$(dirname "$thisDir")"
mainDir="$(readlink -f "$mainDir")"

if [ "$#" == "1" ]; then
  pushd "$thisDir"
  java -classpath ./../target/testdata-manager-0.1-jar-with-dependencies.jar "$classPath" "$mainDir" drop "$1"
  popd
else 
  echo "Usage: <tableName>"
fi
