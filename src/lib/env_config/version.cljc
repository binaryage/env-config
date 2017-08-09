(ns env-config.version)

(def current-version "0.2.2")                                                                                        ; this should match our project.clj

(defmacro get-current-version []
  current-version)
