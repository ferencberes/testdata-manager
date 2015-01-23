#!/bin/bash

thisDir="$(dirname $0)"
thisDir="$(readlink -f "$thisDir")"

echo "$thisDir"

pushd "$thisDir"
cd ./../..

rm -r ./logs/newlogs

cp -r ./resource/log_sample/als ./logs
mv ./logs/als ./logs/newlogs

#drop table if exists
./scripts/dropTable.sh als als_sample_table

#create als test table
./scripts/createTable.sh als als_sample_table

#list existing tables
./scripts/listTables.sh

#insert data into the newly created table
./scripts/insertTable.sh als als_sample_table

#create sample chart from the table
"$thisDir"/alsChartMaker_origi.sh

popd
