(ns env-config.impl.read
  (:require [clojure.string :as string]
            [env-config.impl.report :as report]))

; -- helpers ----------------------------------------------------------------------------------------------------------------

(defn canonical-name [name]
  (-> name
      (string/lower-case)
      (string/replace "_" "-")))

(defn name-to-keyword [name]
  (-> name
      (canonical-name)
      (keyword)))

(defn normalize-value [value]
  (str value))                                                                                                                ; environ values are expected to be strings, but user could pass anything to read-config

(defn get-name-segments [name]
  (string/split name #"/"))

(defn matching-var-name? [prefix name]
  (string/starts-with? name prefix))

(defn strip-prefix [prefix name]
  (assert (string/starts-with? name prefix))
  (.substring name (count prefix)))

(defn get-maps-only [m k]
  (let [v (get m k)]
    (if (map? v)                                                                                                              ; TODO: issue a warning when overwriting previous non-map value?
      v)))

(defn overwriting-assoc-in [m [k & ks] v]
  (if ks
    (assoc m k (overwriting-assoc-in (get-maps-only m k) ks v))
    (assoc m k v)))

; -- reducers ---------------------------------------------------------------------------------------------------------------

(defn filterer-for-matching-vars [prefix v var-name var-value]
  (or
    (let [prefix+name (canonical-name var-name)]
      (if (matching-var-name? prefix prefix+name)
        (let [value (normalize-value var-value)
              name (strip-prefix prefix prefix+name)]
          (conj v (with-meta [name value] {:var-name var-name :var-value var-value})))))
    v))

(defn config-builder
  ([] {})
  ([config item]
   (let [[name value] item]
     (let [segments (get-name-segments name)
           ks (map name-to-keyword segments)
           new-config (overwriting-assoc-in config ks value)
           new-metadata (overwriting-assoc-in (meta config) ks (meta item))]
       (with-meta new-config new-metadata)))))

; -- reader -----------------------------------------------------------------------------------------------------------------

(defn naked-read-config [prefix vars]
  (let [canonical-prefix (str (canonical-name prefix) "/")]
    (->> vars
         (reduce-kv (partial filterer-for-matching-vars canonical-prefix) [])
         (sort-by first)                                                                                                      ; we want lexicographical sorting, longer (nested) names overwrite shorter ones
         (reduce config-builder {}))))

(defn read-config [prefix vars]
  (try
    (naked-read-config prefix vars)
    (catch Throwable e
      (report/report-error! (str "internal error in read-config: " (.getMessage e))))))
