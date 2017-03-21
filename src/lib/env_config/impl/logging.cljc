(ns env-config.impl.logging)

; we don't want to introduce hard dependency on clojure.tools.logging
; that is why resolve this dynamically

#?(:clj (def logging-ns-sym 'clojure.tools.logging))
#?(:clj (def logging-api (atom nil)))

; -- logging api resolution -------------------------------------------------------------------------------------------------

#?(:clj
   (defn try-resolve-logging-symbol [sym]
     (try
       (ns-resolve logging-ns-sym sym)
       (catch Throwable e
           nil))))

#?(:clj
   (defn try-resolve-logging-fn [sym]
     (let [v (try-resolve-logging-symbol sym)]
       (if (var? v)
         (if-let [f (var-get v)]
           (if (fn? f)
             f))))))

#?(:clj
   (defn get-logging-api []
     (let [api @logging-api]
       (if (nil? api)
         (if-let [api (try-resolve-logging-fn 'logp)]
           (reset! logging-api api)
           (reset! logging-api false))
         api))))

#?(:cljs
   (defn logp
     [level message]
     (condp = level
       :debug (js/console.log message)
       :info (js/console.info message)
       :warn (js/console.warn message)
       :error (js/console.error message)
       :fatal (js/console.error message))))

; -- macros -----------------------------------------------------------------------------------------------------------------

; note that clojure.tools.logging/logp is a macro,
; so we have to generate our api call via our own macro
#?(:clj
   (defmacro gen-log [level-sym message-sym]
     (assert (symbol? level-sym))
     (assert (symbol? message-sym))
     (if-let [api (get-logging-api)]
       (api &env &form level-sym message-sym))))

; -- public -----------------------------------------------------------------------------------------------------------------

(defn reporter [messages]
  #?(:clj (if (some? @logging-api)
            (doseq [[level message] messages]
              (gen-log level message)))
     :cljs (doseq [[level message] messages]
             (logp level message))))
