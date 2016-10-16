(ns env-config.impl.coercers
  (:require [clojure.string :as string]
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
  (try
    (->Coerced (Integer/parseInt val))
    (catch NumberFormatException e)))

(defn double-coercer [_path val]
  (try
    (->Coerced (Double/parseDouble val))
    (catch NumberFormatException e)))

(defn keyword-coercer [_path val]
  (if (string-starts-with? val ":")
    (->Coerced (keyword (.substring val 1)))))

(defn symbol-coercer [_path val]
  (if (string-starts-with? val "'")
    (->Coerced (symbol (.substring val 1)))))

(defn code-coercer [path val]
  (if (string-starts-with? val "~")
    (let [code (.substring val 1)]
      (try
        (->Coerced (read-string code))                                                                                        ; TODO: should we rather use edn/read-string here?
        (catch Throwable e
          (report/report-warning! (str "unable to read-string from " (make-var-description (meta path)) ", "
                                       "attempted to eval code: '" code "', "
                                       "got problem: " (.getMessage e) "."))
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
