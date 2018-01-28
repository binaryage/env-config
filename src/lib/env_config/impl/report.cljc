(ns env-config.impl.report
  (:require [env-config.impl.logging :as logging]))

(def ^:dynamic *message-prefix* "env-config: ")
(def ^:dynamic *reports* nil)                                                                                                 ; should be bound if reporting is desired

; -- reporting --------------------------------------------------------------------------------------------------------------

(defn apply-prefix [message]
  (str *message-prefix* message))

(defn report-warning! [message]
  (if *reports*
    (swap! *reports* conj [:warn (apply-prefix message)]))
  nil)

(defn report-error! [message]
  (if *reports*
    (swap! *reports* conj [:error (apply-prefix message)]))
  nil)

; -- standard logging -------------------------------------------------------------------------------------------------------

(defn log-reports-if-needed! [reports & [reporter]]
  (when (and (or (nil? reporter) (fn? reporter)) (not (empty? reports)))
    (let [effective-reporter (or reporter logging/reporter)]
      (effective-reporter reports))))
