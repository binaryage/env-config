(ns env-config.version)

(def current-version "0.1.3-SNAPSHOT")                                                                                        ; this should match our project.clj

(defmacro get-current-version []
  current-version)
