(ns env-config.impl.coerce
  (:require [clojure.string :as string]
            [env-config.impl.report :as report]
            [env-config.impl.helpers :refer [dissoc-in]]))

(deftype Coerced [val])

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
  (if (string/starts-with? val ":")
    (->Coerced (keyword (.substring val 1)))))

(defn symbol-coercer [_path val]
  (if (string/starts-with? val "'")
    (->Coerced (symbol (.substring val 1)))))

(defn code-coercer [path val]
  (if (string/starts-with? val "~")
    (let [code (.substring val 1)]
      (try
        (->Coerced (read-string code))                                                                                        ; TODO: should we rather use edn/read-string here?
        (catch Throwable e
          (report/report-warning! (str "unable to read-string for config path " (pr-str path) ", "
                                       "invalid code: '" code "', "
                                       "problem: " (.getMessage e) "."))
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

; -- coercer machinery ------------------------------------------------------------------------------------------------------

(defn valid-result? [result]
  (or (nil? result)
      (= :omit result)
      (instance? Coerced result)))

(defn coerce [path val coercer]
  (try
    (let [result (coercer path val)]
      (if (valid-result? result)
        result
        (throw (ex-info (str "coercer returned an unexpected result: " result " (" (type result) "), "
                             "allowed are nil, :omit and Coerced instances") {}))))
    (catch Throwable e
      (report/report-error! (str "problem with coercer " coercer ": " (.getMessage e) ".")))))

(defn apply-coercers [coercers path val]
  (if-let [result (some (partial coerce path val) coercers)]
    (if (= :omit result)
      ::omit
      (.val result))
    val))

(defn push-key [state key]
  (update state :keys conj key))

(defn store-keys [state]
  (:keys state))

(defn restore-keys [state keys]
  (assoc state :keys keys))

(defn coercer-worker [coercers state key val]
  (if (map? val)
    (let [current-keys (store-keys state)
          new-state (reduce-kv (:reducer state) (push-key state key) val)]
      (restore-keys new-state current-keys))
    (let [path (conj (:keys state) key)
          coerced-val (apply-coercers coercers path val)]
      (if (= ::omit coerced-val)
        (update state :config dissoc-in path)
        (update state :config assoc-in path coerced-val)))))

; -- coercer ----------------------------------------------------------------------------------------------------------------

(defn naked-coerce-config [config coercers]
  (let [reducer (partial coercer-worker coercers)
        init {:keys    []
              :reducer reducer
              :config  {}}]
    (:config (reduce-kv reducer init config))))

(defn coerce-config [config coercers]
  (try
    (naked-coerce-config config coercers)
    (catch Throwable e
      (report/report-error! (str "internal error in coerce-config: " (.getMessage e))))))
