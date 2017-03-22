(ns env-config.impl.logging)

; we don't want to introduce hard dependency on clojure.tools.logging
; that is why resolve this dynamically

(def logging-ns-sym 'clojure.tools.logging)
(def logging-api (atom nil))

; -- logging api resolution -------------------------------------------------------------------------------------------------

(defn try-resolve-logging-symbol [sym]
  (try
    (ns-resolve logging-ns-sym sym)
    (catch Throwable e
      nil)))

(defn try-resolve-logging-fn [sym]
  (let [v (try-resolve-logging-symbol sym)]
    (if (var? v)
      (if-let [f (var-get v)]
        (if (fn? f)
          f)))))

(defn get-logging-api []
  (let [api @logging-api]
    (if (nil? api)
      (if-let [api (try-resolve-logging-fn 'logp)]
        (reset! logging-api api)
        (reset! logging-api false))
      api)))

; -- macros -----------------------------------------------------------------------------------------------------------------

; note that clojure.tools.logging/logp is a macro,
; so we have to generate our api call via our own macro
(defmacro gen-log [level-sym message-sym]
  (assert (symbol? level-sym))
  (assert (symbol? message-sym))
  (if-let [api (get-logging-api)]
    (api &env &form level-sym message-sym)))

; -- public -----------------------------------------------------------------------------------------------------------------

(defn reporter [messages]
  (if (some? @logging-api)
    (doseq [[level message] messages]
      (gen-log level message))))
