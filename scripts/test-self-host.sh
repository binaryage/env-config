#!/usr/bin/env bash

set -e

pushd `dirname "${BASH_SOURCE[0]}"` > /dev/null
source "./config.sh"

pushd "$ROOT"

echo
echo "Running self-host tests against $(lumo --help | head -n 1 | xargs echo -n)"
echo "-----------------------------------------------------------------------------------------------------------------------"
lein with-profile +self-host tach lumo self-host-test-build

echo
echo "Running self-host tests against $(planck --help | head -n 1 | xargs echo -n)"
echo "-----------------------------------------------------------------------------------------------------------------------"
lein with-profile +self-host tach planck self-host-test-build

popd

popd
