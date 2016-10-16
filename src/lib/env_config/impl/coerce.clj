(ns env-config.impl.coerce
  (:require [clojure.string :as string]
            [env-config.impl.report :as report]
            [env-config.impl.helpers :refer [dissoc-in make-var-description string-starts-with?]]))

(deftype Coerced [val])

; -- coercion machinery -----------------------------------------------------------------------------------------------------

(defn valid-coercion-result? [result]
  (or (nil? result)                                                                                                           ; this means "not interested"
      (= :omit result)                                                                                                        ; this means "ignore value because of an error"
      (instance? Coerced result)))                                                                                            ; this wraps coercion result

(defn coerce [path val coercer]
  (try
    (let [result (coercer path val)]
      (if (valid-coercion-result? result)
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
    val))                                                                                                                     ; when no coercer applies, we return it as a string value

(defn push-key [state key]
  (update state :keys conj key))

(defn store-keys [state]
  (:keys state))

(defn restore-keys [state keys]
  (assoc state :keys keys))

(defn coercion-worker [coercers state key val]
  (if (map? val)
    (let [current-keys (store-keys state)
          new-state (reduce-kv (:reducer state) (push-key state key) val)]
      (restore-keys new-state current-keys))
    (let [path (conj (:keys state) key)
          metadata (get-in (:metadata state) path)
          coerced-val (apply-coercers coercers (with-meta path metadata) val)]
      (if (= ::omit coerced-val)
        (update state :config dissoc-in path)
        (update state :config assoc-in path coerced-val)))))

; -- coercer ----------------------------------------------------------------------------------------------------------------

(defn naked-coerce-config [config coercers]
  (let [reducer (partial coercion-worker coercers)
        init {:keys     []
              :reducer  reducer
              :metadata (meta config)
              :config   {}}]
    (:config (reduce-kv reducer init config))))

(defn coerce-config [config coercers]
  (try
    (naked-coerce-config config coercers)
    (catch Throwable e
      (report/report-error! (str "internal error in coerce-config: " (.getMessage e))))))
