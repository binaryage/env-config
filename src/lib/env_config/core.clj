(ns env-config.core
  (:require [env-config.impl.read :as read]
            [env-config.impl.coerce :as coerce]))

; -- public api -------------------------------------------------------------------------------------------------------------

(def default-coercers coerce/default-coercers)

(defn read-config [prefix vars]
  (read/read-config prefix vars))

(defn coerce-config [config & [coercers]]
  (coerce/coerce-config config (or coercers default-coercers)))

(defn make-config [prefix vars & [coercers]]
  (-> (read-config prefix vars)
      (coerce-config coercers)))
