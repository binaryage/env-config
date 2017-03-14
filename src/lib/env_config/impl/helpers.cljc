(ns env-config.impl.helpers)

; don't want to bring full medley dependency
; https://github.com/weavejester/medley/blob/d8be5e2c45c61f4846953a5187e623e0ebe800f0/src/medley/core.cljc#L11
(defn dissoc-in
  "Dissociate a value in a nested assocative structure, identified by a sequence
  of keys. Any collections left empty by the operation will be dissociated from
  their containing structures."
  [m ks]
  (if-let [[k & ks] (seq ks)]
    (if (seq ks)
      (let [v (dissoc-in (get m k) ks)]
        (if (empty? v)
          (dissoc m k)
          (assoc m k v)))
      (dissoc m k))
    m))

(defn ^:dynamic make-var-description [var-meta]
  (let [orig-name (or (:var-name var-meta) "?")
        orig-value (:var-value var-meta)]
    (str "variable '" orig-name "' with value " (pr-str orig-value))))

; for Clojure 1.7
(defn string-starts-with?
  "True if s starts with substr."
  [^CharSequence s ^String substr]
  (.startsWith (.toString s) substr))
