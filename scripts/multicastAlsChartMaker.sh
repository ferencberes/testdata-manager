#!/bin/bash

thisDir="$(dirname $0)"
thisDir="$(readlink -f "$thisDir")"
classPath=hu.sztaki.testdata_manager.runner.TestDataManagerRunner

pushd "$thisDir"

scriptDir="$(dirname "$thisDir")"
scriptDir="$(readlink -f "$scriptDir")"

mainDir="$(dirname "$scriptDir")"
mainDir="$(readlink -f "$mainDir")"

chartName=mc_als_sample_chart
tableName=mc_als_sample_table
solver=jama
k_feature=10
lmb=0.01

#programs="AlsWithMap:numOfTasks:input:mc_version|AlsWithMap:numTasks:input:mc_version| ..."
programs="AlsWithMap:4:sampledb2d.csv.txt:0"

#iterations=iter1:iter2: ...
iterations=1


if [ "$#" == "0" ]; then
  pushd "$thisDir"
  java -classpath ./../../target/testdata-manager-0.1-jar-with-dependencies.jar "$classPath" "$mainDir" multicast_als "$chartName" "$tableName" "$solver" "$lmb" "$k_feature" "$iterations" "$programs"
  popd
else
  echo "Parameters must be set inside the scripts!"
fi

popd
