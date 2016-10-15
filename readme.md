# env-config

[![GitHub license](https://img.shields.io/github/license/binaryage/env-config.svg)](license.txt) 
[![Clojars Project](https://img.shields.io/clojars/v/binaryage/env-config.svg)](https://clojars.org/binaryage/env-config) 
[![Travis](https://img.shields.io/travis/binaryage/env-config.svg)](https://travis-ci.org/binaryage/env-config) 

This is a Clojure(Script) library for enabling easy and consistent config map overrides via environment variables.

This is useful for library authors who want to add more flexible configurability to their libraries.

Usually this is done via a config map specifying keywords with individual config values.
This config map can be specified via build configuration. For example in ClojureScript it 
could be passed via `:compiler > :external-config`. 

Sometimes, for ad-hoc tweaks, it would be preferable to be able to override some config values 
by defining environment variables instead of touching build tool configuration (which is usually under source control).

This library helps you do that consistently:

  1. we define a naming scheme how env variables map to config keys
  2. we define a coercion protocol which determines how strings from env variables are converted to Clojure data

TBD
