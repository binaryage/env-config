(ns env-config.impl.logging)

(defn logp [level message]
  (case level
    :debug (js/console.log message)
    :info (js/console.info message)
    :warn (js/console.warn message)
    :error (js/console.error message)
    :fatal (js/console.error message)))

; -- public -----------------------------------------------------------------------------------------------------------------

(defn reporter [messages]
  (doseq [[level message] messages]
    (logp level message)))
