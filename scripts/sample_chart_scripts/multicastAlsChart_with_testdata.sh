#!/bin/bash

thisDir="$(dirname $0)"
thisDir="$(readlink -f "$thisDir")"

echo "$thisDir"

pushd "$thisDir"
cd ./../..

rm -r ./logs/newlogs

cp -r ./resource/log_sample/multicast_als ./logs
mv ./logs/multicast_als ./logs/newlogs

#drop table if exists
./scripts/dropTable.sh multicast_als mc_als_sample_table

#create als test table
./scripts/createTable.sh multicast_als mc_als_sample_table

#list existing tables
./scripts/listTables.sh

#insert data into the newly created table
./scripts/insertTable.sh multicast_als mc_als_sample_table

#create sample chart from the table
"$thisDir"/multicastAlsChartMaker_origi.sh

popd
