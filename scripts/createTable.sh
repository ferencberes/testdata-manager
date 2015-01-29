#!/bin/bash

thisDir="$(dirname $0)"
thisDir="$(readlink -f "$thisDir")"
#classPath=hu.sztaki.testdata_manager.runner.TestDataManagerRunner
classPath=hu.sztaki.testdata_manager.runner.TestRunner

mainDir="$(dirname "$thisDir")"
mainDir="$(readlink -f "$mainDir")"

if [ "$#" == "2" ]; then
  pushd "$thisDir"
  java -classpath ./../target/testdata-manager-0.1-jar-with-dependencies.jar "$classPath" "$mainDir" create "$1" "$2"
  popd
else 
  echo "Usage: <als/pagerank> <tableName>"
fi
