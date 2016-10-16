(ns env-config.core
  (:require [env-config.impl.read :as read]
            [env-config.impl.coerce :as coerce]
            [env-config.impl.coercers :as coercers]
            [env-config.impl.report :as report]))

; -- public api -------------------------------------------------------------------------------------------------------------

(def default-coercers coercers/default-coercers)

(defn read-config [prefix vars]
  (read/read-config prefix vars))

(defn coerce-config [config & [coercers]]
  (coerce/coerce-config config (or coercers default-coercers)))

(defn prepare-config [prefix vars coercers]
  (-> (read-config prefix vars)
      (coerce-config coercers)))

(defn make-config [prefix vars & [coercers reports-atom]]
  (binding [report/*reports* reports-atom]
    (prepare-config prefix vars coercers)))

(defn make-config-with-logging [prefix vars & [coercers reporter]]
  (let [reports-atom (atom [])
        config (make-config prefix vars coercers reports-atom)]
    (report/log-reports-if-needed! @reports-atom reporter)
    config))
