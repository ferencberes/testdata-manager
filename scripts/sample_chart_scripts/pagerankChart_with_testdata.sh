#!/bin/bash

thisDir="$(dirname $0)"
thisDir="$(readlink -f "$thisDir")"

echo "$thisDir"

pushd "$thisDir"
cd ./../..

rm -r ./logs/newlogs

cp -r ./src/main/resources/log_sample/pagerank ./logs
mv ./logs/pagerank ./logs/newlogs

#drop table if exists
./scripts/dropTable.sh pagerank pagerank_sample_table

#create als test table
./scripts/createTable.sh pagerank pagerank_sample_table

#list existing tables
#./scripts/listTables.sh

#insert data into the newly created table
./scripts/insertTable.sh pagerank pagerank_sample_table

#create sample chart from the table
"$thisDir"/pagerankChartMaker_origi.sh

popd
