(ns env-config.impl.platform
  "Platform dependent code: self-host implementation."
  (:require [clojure.string :as string]
            [cljs.reader :as edn]
            [env-config.impl.types :refer [->Coerced]]))

(def string-starts-with? string/starts-with?)

(defn get-ex-message [e]
  (or (.-message e) "?"))

(defn read-code-string [code]
  (let [r (edn/push-back-reader code)]
    (edn/read r true nil false)))                                                                                             ; throws in case of errors

(defn number-or-nil
  "Return x or nil if it is not a number.
We need this in JS land because (number? NaN) => true (!!)."
  [x]
  (when (and (not (js/isNaN x)) (number? x))
    x))

(defn coerce-integer [val]
  (when-let [v (number-or-nil (js/parseInt val))]
    (->Coerced v)))

(defn coerce-double [val]
  (when-let [v (number-or-nil (js/parseFloat val))]
    (->Coerced v)))
