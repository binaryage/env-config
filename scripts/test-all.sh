#!/usr/bin/env bash

set -e

pushd `dirname "${BASH_SOURCE[0]}"` > /dev/null
source "./config.sh"

cd "$ROOT"

echo
echo "Running tests against Clojure 1.9"
echo "-----------------------------------------------------------------------------------------------------------------------"
lein test

echo
echo "Running tests against Clojure 1.8"
echo "-----------------------------------------------------------------------------------------------------------------------"
lein with-profile +clojure18 test

echo
echo "Running tests against Clojure 1.7"
echo "-----------------------------------------------------------------------------------------------------------------------"
lein with-profile +clojure17 test

echo
echo "Running self-host tests against $(planck --help | head -n 1 | xargs echo -n)"
echo "-----------------------------------------------------------------------------------------------------------------------"
lein with-profile +self-host tach planck self-host-test-build

echo "Running self-host tests against $(lumo --help | head -n 1 | xargs echo -n)"
echo "-----------------------------------------------------------------------------------------------------------------------"
lein with-profile +self-host tach lumo self-host-test-build

popd
