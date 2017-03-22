(ns env-config.impl.platform
  "Platform dependent code: ClojureScript implementation."
  (:require [clojure.string :as string]
            [cljs.reader :as edn]
            [env-config.impl.types :refer [->Coerced]]))

(def string-starts-with? string/starts-with?)

(defn get-ex-message [e]
  (or (.-message e) "?"))

(defn read-code-string [code]
  (let [r (edn/push-back-reader code)]
    (edn/read r true nil false)))                                                                                             ; throws in case of errors

(defn coerce-integer [val]
  (if (re-matches #"(\+|\-)?([0-9]+|Infinity)" val)                                                                           ; see https://developer.mozilla.org/en/docs/Web/JavaScript/Reference/Global_Objects/parseInt
    (let [parsed-int (js/parseInt val 10)]
      (if-not (js/isNaN parsed-int)
        (->Coerced parsed-int)))))

(defn coerce-double [val]
  (if (re-matches #"(\+|\-)?([0-9]+(\.[0-9]+)?|Infinity)" val)                                                                ; see https://developer.mozilla.org/en/docs/Web/JavaScript/Reference/Global_Objects/parseFloat
    (let [parsed-float (js/parseFloat val)]
      (if-not (js/isNaN parsed-float)
        (->Coerced parsed-float)))))
