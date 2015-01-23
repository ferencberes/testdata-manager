#!/bin/bash

thisDir="$(dirname $0)"
thisDir="$(readlink -f "$thisDir")"

pushd "$thisDir"
echo "A correct mysql server setup is needed for this test! "

#als chart test
./scripts/sample_chart_scripts/alsChart_with_testdata.sh

#pagerank chart test
./scripts/sample_chart_scripts/pagerankChart_with_testdata.sh

#multicast als chart test
./scripts/sample_chart_scripts/multicastAlsChart_with_testdata.sh

echo "Result .html files can be found in /charts directory."
popd
