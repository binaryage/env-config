(ns env-config.impl.read
  (:require [clojure.string :as string]
            [env-config.impl.report :as report]
            [env-config.impl.helpers :refer [make-var-description]]))

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

(defn report-naming-conflict! [item1 item2]
  (report/report-warning! (str "naming conflict: the " (make-var-description (meta item1)) " "
                               "was shadowed by the " (make-var-description (meta item2)) ". "
                               "A variable name must not be a prefix of another variable name.")))

; -- reducers ---------------------------------------------------------------------------------------------------------------

(defn filterer-for-matching-vars [prefix state var-name var-value]
  (or
    (let [prefix+name (canonical-name var-name)]
      (if (matching-var-name? prefix prefix+name)
        (let [value (normalize-value var-value)
              name (strip-prefix prefix prefix+name)]
          (conj state (with-meta [name value] {:var-name var-name :var-value var-value})))))
    state))

(defn common-prefix? [s prefix]
  (if (some? prefix)
    (string/starts-with? s (str prefix "/"))))

(defn filterer-for-naming-conflicts [state item]
  (let [prev-item (last state)]
    (if (common-prefix? (first item) (first prev-item))
      (do
        (report-naming-conflict! prev-item item)
        (conj (vec (butlast state)) item))
      (conj state item))))

(defn config-builder
  ([] {})
  ([config item]
   (let [[name value] item]
     (let [segments (get-name-segments name)
           ks (map name-to-keyword segments)
           new-config (assoc-in config ks value)
           new-metadata (assoc-in (meta config) ks (meta item))]
       (with-meta new-config new-metadata)))))

; -- reader -----------------------------------------------------------------------------------------------------------------

(defn naked-read-config [prefix vars]
  (let [canonical-prefix (str (canonical-name prefix) "/")]
    (->> vars
         (reduce-kv (partial filterer-for-matching-vars canonical-prefix) [])
         (sort-by first)                                                                                                      ; we want lexicographical sorting, longer (nested) names overwrite shorter ones
         (reduce filterer-for-naming-conflicts [])
         (reduce config-builder {}))))

(defn read-config [prefix vars]
  (try
    (naked-read-config prefix vars)
    (catch Throwable e
      (report/report-error! (str "internal error in read-config: " (.getMessage e))))))
