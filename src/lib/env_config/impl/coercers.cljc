(ns env-config.impl.coercers
  (:require [clojure.string :as string]
            #?(:clj [clojure.edn :as edn]
               :cljs [cljs.reader :as edn])
            [env-config.impl.coerce :refer [->Coerced]]
            [env-config.impl.report :as report]
            [env-config.impl.helpers :refer [make-var-description string-starts-with?]]))

; -- standard coercers ------------------------------------------------------------------------------------------------------

#?(:cljs
   (defn number-or-nil
     "Return x or nil if it is not a number.
  We need this in JS land because (number? NaN) => true (!!)."
     [x]
     (when (and (not (js/isNaN x)) (number? x))
       x)))

(defn nil-coercer [_path val]
  (when (= (string/lower-case val) "nil")
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
     :cljs (when-let [v (number-or-nil (js/parseInt val))]
             (->Coerced v))))

(defn double-coercer [_path val]
  #?(:clj (try
            (->Coerced (Double/parseDouble val))
            (catch NumberFormatException e))
     :cljs (when-let [v (number-or-nil (js/parseFloat val))]
             (->Coerced v))))                                                                                                 ; For more precision in JS use bignumber.js

(defn keyword-coercer [_path val]
  (when (string-starts-with? val ":")
    (->Coerced (keyword (subs val 1)))))

(defn symbol-coercer [_path val]
  (when (string-starts-with? val "'")
    (->Coerced (symbol (subs val 1)))))

#?(:cljs
   (defn custom-read-string
     "Necessary in order to be sure that cljs.reader/read-string either
  throws or returns :omit in case of error."
     [s]
     (let [r (edn/push-back-reader s)]
       (edn/read r true nil false))))

(defn code-coercer [path val]
  (when (string-starts-with? val "~")
    (let [code (subs val 1)]
      (try
        (->Coerced #?(:clj (edn/read-string code)
                      :cljs (custom-read-string code)))
        (catch #?(:clj Throwable :cljs js/Error) e
            (report/report-warning! (str "unable to read-string from " (make-var-description (meta path)) ", "
                                         "attempted to eval code: '" code "', "
                                         "got problem: " #?(:clj (.getMessage e) :cljs (.-message e)) "."))
          :omit)))))

; -- default coercers -------------------------------------------------------------------------------------------------------

(def default-coercers
  [nil-coercer
   boolean-coercer
   #?@(:clj [integer-coercer double-coercer]                                                                                  ; order counts
       :cljs [double-coercer integer-coercer])
   keyword-coercer
   symbol-coercer
   code-coercer])
