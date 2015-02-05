#!/bin/bash

thisDir="$(dirname $0)"
thisDir="$(readlink -f "$thisDir")"
classPath=hu.sztaki.testdata_manager.runner.TestRunner

pushd "$thisDir"

mainDir="$(dirname "$thisDir")"
mainDir="$(readlink -f "$mainDir")"

chartName=test_divs
tableName=ALS_TEST_2014_10_08
qInput=-
k_feature=10
lmb=0.01

programs="AlsTuple:40:sampledb2b.csv.txt|AlsTuple:40:sampledb2c.csv.txt|CustomAlsTuple:40:sampledb2b.csv.txt|CustomAlsTuple:40:sampledb2c.csv.txt"
iterations=1:2:4:6:8:10

echo "$mainDir"

if [ "$#" == "0" ]; then
  pushd "$thisDir"
  java -classpath ./../target/testdata-manager-0.1-jar-with-dependencies.jar "$classPath" "$mainDir" chart als "$chartName" "$tableName" "$qInput" "$lmb" "$k_feature" "$iterations" "$programs"
  popd
else
  echo "Parameters must be set inside the scripts!"
fi

popd
