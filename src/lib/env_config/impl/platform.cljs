(ns env-config.impl.platform
  "Platform dependent code: self-host implementation."
  (:require [clojure.string :as string]
            [cljs.reader :as edn]))

(def string-starts-with? string/starts-with?)

(defn get-ex-message [e]
  (or (.-message e) "?"))

(defn read-code-string [code]
  (let [r (edn/push-back-reader code)]
    (edn/read r true nil false)))                                                                                             ; throws in case of errors
