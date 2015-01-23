#!/bin/bash

thisDir="$(dirname $0)"
thisDir="$(readlink -f "$thisDir")"

pushd "$thisDir"
cd ..
if [ -d config ]; then
  echo "The /config folder already exists!"
  echo "Delete it first, to regenerate it."
else
  cp -r ./resource/config_sample ./config
  echo "Please set the config files correctly in /config directory.."
  echo "Do not enable version control on /config directory!"
fi
popd
