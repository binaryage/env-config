# env-config

[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](license.txt) 
[![Clojars Project](https://img.shields.io/clojars/v/binaryage/env-config.svg)](https://clojars.org/binaryage/env-config) 
[![Travis](https://img.shields.io/travis/binaryage/env-config.svg)](https://travis-ci.org/binaryage/env-config) 

This is a Clojure(Script) library for enabling easy and consistent config map overrides via environment variables.

This is useful for library authors who want to add some flexibility how their libraries can be configured.

## Intro

Usually library configuration is achieved via a config map specifying keywords with individual config values.
This config map can be provided directly (e.g. passed via an api call), via a build configuration or by some other means. 
For example in ClojureScript it could be passed via `:compiler > :external-config`.

Sometimes for ad-hoc tweaks it would be preferable to be able to override config values 
by defining environment variables instead of touching build tool configuration (which is usually under source control).

This library helps you do that consistently:

  1. we define a naming scheme how env variables map to config keys
  2. we define a coercion protocol which determines how strings from env variables are converted to Clojure values
  
### Example

We want to support nested config maps. Let's look at example env variables with some nesting:
 
    OOPS/COMPILER/MAX_ITERATIONS=10
    OOPS/RUNTIME/DEBUG=true
    OOPS/RUNTIME/WELCOME-MESSAGE=hello
    OOPS/RUNTIME/DATA=~{:some (str "data" " via" " read-string")}
    OOPS/RUNTIME/KEY=:some-keyword
    OOPS/RUNTIME=something   <= this will cause a naming conflic warning
    
A call to `(env-config.core/make-config "oops" (get-env-vars))` will return:
 
    {:compiler {:max-iterations 10}
     :runtime {:debug true
               :welcome-message "hello"
               :key :some-keyword
               :data {:some "data via read-string"}}

You can observe several properties:

  1. forward slashes are used as separators
  2. to follow Clojure conventions, names are converted to lower-case and underscores turned into dashes
  2. prefix "oops" is stripped because it was specified as a library prefix
  3. values are naturally coerced to booleans, numbers, keywords, etc.
  4. you can use full power of `read-string` if you prepend value with `~`

Also please note that existence of a variable name which is a prefix of another variable name will cause
naming conflict warning and will be ignored (`OOPS/RUNTIME` is prefix of `OOPS/RUNTIME/DEBUG` in our example above).

Some shells [like Bash](http://stackoverflow.com/a/2821183/84283) do not allow slashes in variable names, you can use two underscores instead of a slash.

### Integration

You probably want to merge the config coming from env-config over your standard config coming from a build tool.

For inspiration look at [the commit](https://github.com/binaryage/cljs-oops/commit/1a2a1794f59e47710b5c9e025a420ed25db4d4ed) 
which integrated env-config into cljs-oops library.

Please note that `make-config-with-logging` does not read environment directly. You have to pass it a map with variables.

I used this simple implementation to get them:
```
(defn get-env-vars []
  (-> {}
      (into (System/getenv))
      (into (System/getProperties))))
```

### Logging

I needed a way how to report issues with naming conflicts or for example problems when evaluting values via read-string.

I didn't want to introduce another dependency so I decided to build internal subsystem for collecting "reports". It is up
to you to inspect reports and communicate them somehow. 

For convenience I have implemented a helper function which dynamically checks for availability of `clojure.tools.logging`
 and uses it for logging reports. 
 
To get standard logging for free include dependency on `clojure.tools.logging` into your project and use `make-config-with-logging` 
to obtain your configs.

### Coercion

We provide a [standard set of coercion handlers](https://github.com/binaryage/env-config/blob/master/src/lib/env_config/impl/coercers.clj). 
As you can see from the `default-coercers` list the rules are pretty simple. You might want to provide your own handlers.
 
#### Writing own coercion handlers

Coercion handlers are asked in the order in which they were specified to `make-config`. 
Each handler is passed key path in the config map and raw string value coming from environment. 

The handler should answer either: 

  1. `nil` which means "I'm not interested, pass it to someone else"
  2. `:omit` which means "ignore this value due to an error"
  3. a value wrapped in `Coerced` instance (to distinguish it from `nil` and `:omit`)
  
If no handler was interested we use the raw value as-is.

Look at the example of the most complex standard coercer:

```clojure
(defn code-coercer [path val]
  (if (string-starts-with? val "~")
    (let [code (.substring val 1)]
      (try
        (->Coerced (read-string code))
        (catch Throwable e
          (report/report-warning! (str "unable to read-string from " (make-var-description (meta path)) ", "
                                       "attempted to eval code: '" code "', "
                                       "got problem: " (.getMessage e) "."))
          :omit)))))
```

Please note that the `path` vector has attached some metadata with original raw values which may be handy when 
reporting warnings/errors. You should use `env-config.impl.report` functionality to report errors in a standard way.

### FAQ

> My shell does not support variable names with slashes. What now?

You can use two underscores instead of a slash. Or alternatively you might want to use `env` command to launch your command with
defined variables without shell naming restrictions. See [this stack overflow answer](http://unix.stackexchange.com/a/93533/188074).
 
For example:
 
    env OOPS/COMPILER/MAX_ITERATIONS=10 OOPS/RUNTIME/DEBUG=true command
 
I personally use [fish shell](https://fishshell.com) and prefer slashes to visually communicate the nested config structure.

> Can this be used in self-hosted mode?

Yes, thanks to [arichiardi](https://github.com/arichiardi). Since v0.2.0 you can use this library to configure scripts running
 under [Planck](https://github.com/mfikes/planck) or [Lumo](https://github.com/anmonteiro/lumo).
