(ns env-config.impl.platform
  "Platform dependent code: self-host implementation."
  (:require [clojure.string :as string]))

(def string-starts-with? string/starts-with?)
