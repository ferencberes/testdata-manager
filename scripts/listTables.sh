#!/bin/bash

thisDir="$(dirname $0)"
thisDir="$(readlink -f "$thisDir")"
#classPath=hu.sztaki.testdata_manager.runner.TestDataManagerRunner
classPath=hu.sztaki.testdata_manager.runner.TestRunner

mainDir="$(dirname "$thisDir")"
mainDir="$(readlink -f "$mainDir")"

if [ "$#" == "0" ]; then
  pushd "$thisDir"
  java -classpath ./../target/testdata-manager-0.1-jar-with-dependencies.jar "$classPath" "$mainDir" list
  popd
else
  echo "No argument is passed to this script."
fi
