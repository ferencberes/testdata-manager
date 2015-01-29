#!/bin/bash

thisDir="$(dirname $0)"
thisDir="$(readlink -f "$thisDir")"
#classPath=hu.sztaki.testdata_manager.runner.TestDataManagerRunner
classPath=hu.sztaki.testdata_manager.runner.TestRunner

pushd "$thisDir"

scriptDir="$(dirname "$thisDir")"
scriptDir="$(readlink -f "$scriptDir")"

mainDir="$(dirname "$scriptDir")"
mainDir="$(readlink -f "$mainDir")"

chartName=als_sample_chart
tableName=als_sample_table
qInput=-
k_feature=10
lmb=0.01

programs="AlsTuple:40:sampledb2b.csv.txt|AlsTuple:40:sampledb2c.csv.txt|CustomAlsTuple:40:sampledb2b.csv.txt|CustomAlsTuple:40:sampledb2c.csv.txt"
iterations=1:2:4:6:8:10


if [ "$#" == "0" ]; then
  pushd "$thisDir"
  java -classpath ./../../target/testdata-manager-0.1-jar-with-dependencies.jar "$classPath" "$mainDir" als "$chartName" "$tableName" "$qInput" "$lmb" "$k_feature" "$iterations" "$programs"
  popd
else
  echo "Parameters must be set inside the scripts!"
fi

popd
