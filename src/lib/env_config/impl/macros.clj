(ns env-config.impl.macros)

(defn cljs-env? [env]
  (some? (:ns env)))

(defn transform-catch-all [ex-type form]
  (if (and (list? form) (= (first form) 'catch-all))
    (concat `(catch ~ex-type) (rest form))
    form))

(defmacro try* [& body]
  "The purpose of this macro is to expand try-catch based on platform

    (try* ... (catch-all e ...) ...)

  will expand to

    (try ... (catch Throwable e ...)) in Clojure
    (try ... (catch :default e ...)) in ClojureScript"
  (let [ex-type (if (cljs-env? &env) :default 'Throwable)]
    `(try
       ~@(map (partial transform-catch-all ex-type) body))))

(defmacro catch-all [& body]
  (assert "not reachable"))                                                                                                   ; just a dummy implementation to make Cursive happy
