(ns env-config.impl.types)

(defprotocol IValueWrapper
  (get-value [this]))

(deftype Coerced [val]
  IValueWrapper
  (get-value [this] val))

(defn coerced? [val]
  (instance? Coerced val))
