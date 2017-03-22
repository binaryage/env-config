(ns env-config.impl.coercers
  (:require [clojure.string :as string]
            [env-config.impl.types :refer [->Coerced]]
            [env-config.impl.macros :refer [try* catch-all]]
            [env-config.impl.report :as report]
            [env-config.impl.helpers :refer [make-var-description]]
            [env-config.impl.platform :refer [string-starts-with? get-ex-message read-code-string
                                              coerce-integer coerce-double]]))

; -- standard coercers ------------------------------------------------------------------------------------------------------

(defn nil-coercer [_path val]
  (when (= (string/lower-case val) "nil")
    (->Coerced nil)))

(defn boolean-coercer [_path val]
  (condp = (string/lower-case val)
    "true" (->Coerced true)
    "false" (->Coerced false)
    nil))

(defn integer-coercer [_path val]
  (coerce-integer val))

(defn double-coercer [_path val]
  (coerce-double val))

(defn keyword-coercer [_path val]
  (when (string-starts-with? val ":")
    (->Coerced (keyword (subs val 1)))))

(defn symbol-coercer [_path val]
  (when (string-starts-with? val "'")
    (->Coerced (symbol (subs val 1)))))

(defn code-coercer [path val]
  (when (string-starts-with? val "~")
    (let [code (subs val 1)]
      (try*
        (->Coerced (read-code-string code))
        (catch-all e
          (report/report-warning! (str "unable to read-string from " (make-var-description (meta path)) ", "
                                       "attempted to eval code: '" code "', "
                                       "got problem: " (get-ex-message e) "."))
          :omit)))))

; -- default coercers -------------------------------------------------------------------------------------------------------

(def default-coercers
  [nil-coercer
   boolean-coercer
   integer-coercer
   double-coercer                                                                                                             ; order counts
   keyword-coercer
   symbol-coercer
   code-coercer])
