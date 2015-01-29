#!/bin/bash

thisDir="$(dirname $0)"
thisDir="$(readlink -f "$thisDir")"
#classPath=hu.sztaki.testdata_manager.runner.TestDataManagerRunner
classPath=hu.sztaki.testdata_manager.runner.TestRunner

mainDir="$(dirname "$thisDir")"
mainDir="$(readlink -f "$mainDir")"

chartName=attila_test
#tableName=PAGERANK_TEST_2014_10_08
tableName=attila_test
dampening=0.85
epsilon=0.0001

programs="PageRankForSinks:40:web-Google.csv.txt|CustomPageRankSpargel:40:web-Google.csv.txt|SpargelPageRank:40:web-Google.csv.txt"
iterations=10

if [ "$#" == "0" ]; then
  pushd "$thisDir"
  java -classpath ./../target/testdata-manager-0.1-jar-with-dependencies.jar "$classPath" "$mainDir" pagerank "$chartName" "$tableName" "$dampening" "$epsilon" "$iterations" "$programs"
  popd
else
  echo "Parameters must be set inside the scripts!"
fi
