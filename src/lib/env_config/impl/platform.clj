(ns env-config.impl.platform
  "Platform dependent code: clojure implementation.")

; a backport for Clojure 1.7
(defn string-starts-with?
  "True if s starts with substr."
  [^CharSequence s ^String substr]
  (.startsWith (.toString s) substr))

(defn get-ex-message [e]
  (.getMessage e))
