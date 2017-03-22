(ns env-config.impl.macros
  (:require-macros [env-config.impl.macros]))

; note this namespace exists so that we can rely on simple :refer ns form in ClojureScript
; and avoid reader conditionals for :refer-macros
; see http://dev.clojure.org/jira/browse/CLJS-1507
