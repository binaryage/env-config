(ns env-config.version)

(def current-version "0.2.1")                                                                                        ; this should match our project.clj

(defmacro get-current-version []
  current-version)
