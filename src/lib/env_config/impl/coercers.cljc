(ns env-config.impl.coercers
  (:require [clojure.string :as string]
            [clojure.edn :as edn]
            [env-config.impl.coerce :refer [->Coerced]]
            [env-config.impl.report :as report]
            [env-config.impl.helpers :refer [make-var-description string-starts-with?]]))

; -- standard coercers ------------------------------------------------------------------------------------------------------

(defn nil-coercer [_path val]
  (if (= (string/lower-case val) "nil")
    (->Coerced nil)))

(defn boolean-coercer [_path val]
  (condp = (string/lower-case val)
    "true" (->Coerced true)
    "false" (->Coerced false)
    nil))

(defn integer-coercer [_path val]
  #?(:clj (try
            (->Coerced (Integer/parseInt val))
            (catch NumberFormatException e))
     :cljs (let [v (js/parseInt val)]
             (when (number? v) v))))

(defn double-coercer [_path val]
  #?(:clj (try
            (->Coerced (Double/parseDouble val))
            (catch NumberFormatException e))
     :cljs (let [v (js/parseFloat val)]                                                                                       ; For more precision in JS use bignumber.js
             (when (number? v) v))))

(defn keyword-coercer [_path val]
  (if (string-starts-with? val ":")
    (->Coerced (keyword (subs val 1)))))

(defn symbol-coercer [_path val]
  (if (string-starts-with? val "'")
    (->Coerced (symbol (subs val 1)))))

(defn code-coercer [path val]
  (if (string-starts-with? val "~")
    (let [code (subs val 1)]
      (try
        (->Coerced (edn/read-string code))
        (catch #?(:clj Throwable :cljs js/Error) e
            (report/report-warning! (str "unable to read-string from " (make-var-description (meta path)) ", "
                                         "attempted to eval code: '" code "', "
                                         "got problem: " #?(:clj (.getMessage e) :cljs (.-message e)) "."))
          :omit)))))

; -- default coercers -------------------------------------------------------------------------------------------------------

(def default-coercers
  [nil-coercer
   boolean-coercer
   integer-coercer                                                                                                            ; must go before double-coercer
   double-coercer
   keyword-coercer
   symbol-coercer
   code-coercer])
