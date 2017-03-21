#!/usr/bin/env bash

set -e

pushd `dirname "${BASH_SOURCE[0]}"` > /dev/null
source "./config.sh"

pushd "$ROOT"

echo ""
echo "Running tests against Clojure 1.9"
echo "-----------------------------------------------------------------------------------------------------------------------"
lein test

echo ""
echo "Running tests against Clojure 1.8"
echo "-----------------------------------------------------------------------------------------------------------------------"
lein with-profile +clojure18 test

echo ""
echo "Running tests against Clojure 1.7"
echo "-----------------------------------------------------------------------------------------------------------------------"
lein with-profile +clojure17 test

echo ""
echo "Running self-host tests"
echo "-----------------------------------------------------------------------------------------------------------------------"
lein tach lumo

popd

popd
