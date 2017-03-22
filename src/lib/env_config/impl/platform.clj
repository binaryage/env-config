(ns env-config.impl.platform
  "Platform dependent code: clojure implementation."
  (:require [clojure.edn :as edn]
            [env-config.impl.types :refer [->Coerced]]))

; a backport for Clojure 1.7
(defn string-starts-with?
  "True if s starts with substr."
  [^CharSequence s ^String substr]
  (.startsWith (.toString s) substr))

(defn get-ex-message [e]
  (.getMessage e))

(defn read-code-string [code]
  (edn/read-string code))

(defn coerce-integer [val]
  (try
    (->Coerced (Integer/parseInt val))
    (catch NumberFormatException e)))

(defn coerce-double [val]
  (try
    (->Coerced (Double/parseDouble val))
    (catch NumberFormatException e)))
