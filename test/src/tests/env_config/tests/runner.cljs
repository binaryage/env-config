(ns env-config.tests.runner
  (:require [cljs.test :refer-macros [run-tests]]
            [env-config.tests.main]))

(enable-console-print!)

(run-tests 'env-config.tests.main)
