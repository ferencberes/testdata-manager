#!/bin/bash

thisDir="$(dirname $0)"
thisDir="$(readlink -f "$thisDir")"
classPath=hu.sztaki.testdata_manager.runner.TestDataManagerRunner

pushd "$thisDir"

scriptDir="$(dirname "$thisDir")"
scriptDir="$(readlink -f "$scriptDir")"

mainDir="$(dirname "$scriptDir")"
mainDir="$(readlink -f "$mainDir")"


chartName=pagerank_sample_chart
tableName=pagerank_sample_table
dampening=0.85
epsilon=0.0001

programs="PageRankForSinks:40:web-Google.csv.txt|CustomPageRank4:40:web-Google.csv.txt"
iterations=1:2:4:6:8:10

if [ "$#" == "0" ]; then
  pushd "$thisDir"
  java -classpath ./../../target/testdata-manager-0.1-jar-with-dependencies.jar "$classPath" "$mainDir" pagerank "$chartName" "$tableName" "$dampening" "$epsilon" "$iterations" "$programs"
  popd
else
  echo "Parameters must be set inside the scripts!"
fi

popd
