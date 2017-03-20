(ns env-config.tests.main
  (:require #?(:clj [clojure.test :refer [deftest testing is are]]
               :cljs [cljs.test :refer-macros [deftest testing is are]])
            [clojure.string :as string]
            [env-config.core :refer [read-config coerce-config make-config
                                     make-config-with-logging default-coercers]]
            [env-config.impl.coerce :refer [->Coerced]]))

(deftest test-reading
  (testing "basic configs"
    (let [vars {"MY-PROJECT/A"       "s"
                "MY_PROJECT/B"       42
                "MY_PROJECT/C_Cx"    "normalize key"
                "MY_PROJECT/D"       :keyword-val
                "MY_PROJECT/A1/B1"   :nested-key
                "my_project/A1/B2"   "someval"
                "My_Project/A1/B2"   "overwrite"
                "My_project/A1"      "should-not-destroy-subkeys"
                "MY_PROJECT__A1__B3" "double-underscore"
                "MY_PROJECT--A1-_B4" "double-dash/mixed-dash-underscore"}
          expected-config {:a    "s"
                           :b    "42"
                           :c-cx "normalize key"
                           :d    ":keyword-val"
                           :a1   {:b1 ":nested-key"
                                  :b2 "overwrite"
                                  :b3 "double-underscore"
                                  :b4 "double-dash/mixed-dash-underscore"}}]
      (is (= expected-config (read-config "my-project" vars)))))
  (testing "nested prefix"
    (is (= {:v "1"} (read-config "project/sub-project/x/y" {"project/sub-project/x/y/v" "1"
                                                            "project/sub-project/x/y"   "should ignore"
                                                            "project/sub-project/x"     "outside"})))))

(deftest test-coercion
  (testing "basic coercion"
    (let [config {:a  "string"
                  :b  "42"
                  :c  "4.2"
                  :d  ":key"
                  :e  "~{:m {:x \"xxx\"}}"
                  :f  "nil"
                  :g  "true"
                  :h  "false"
                  :i  "True"
                  :j  "FALSE"
                  :k  "'sym"
                  :l  "#!@#xxx"
                  :m  "\\\\x"
                  :n  "~#!@#xxx"
                  :o  "~\"true\""
                  :z1 {:z2 {:z3 "nested"}}}
          expected-coercion {:a  "string"
                             :b  42
                             :c  4.2
                             :d  :key
                             :e  {:m {:x "xxx"}}
                             :f  nil
                             :g  true
                             :h  false
                             :i  true
                             :j  false
                             :k  'sym
                             :l  "#!@#xxx"
                             :m  "\\\\x"
                             ; :n should be omitted due to eval error
                             :o  "true"
                             :z1 {:z2 {:z3 "nested"}}}]
      (is (= expected-coercion (coerce-config config))))))

(deftest test-top-level-api
  (testing "make-config"
    (are [vars config] (= config (make-config "project" vars))
      {"project/var" "42"} {:var 42}
      {"project/x/y/z" "nil"} {:x {:y {:z nil}}}))
  (testing "make-config with empty coercions"
    (are [vars config] (= config (make-config "project" vars []))
      {"project/var" "42"} {:var "42"}
      {"project/x/y/z" "nil"} {:x {:y {:z "nil"}}}))
  (testing "make-config with custom coercers"
    (let [my-path-based-coercer (fn [path val]
                                  (if (= (first path) :coerce-me)
                                    (->Coerced (str "!" val))))]
      (are [vars config] (= config (make-config "project" vars [my-path-based-coercer]))
        {"project/dont-coerce-me" "42"} {:dont-coerce-me "42"}
        {"project/coerce-me" "42"} {:coerce-me "!42"}
        {"project/coerce-me/x" "1"} {:coerce-me {:x "!1"}}
        {"project/coerce-me/x/y" "s"} {:coerce-me {:x {:y "!s"}}})))
  (testing "make-config-with-logging"
    (let [reports-atom (atom [])]
      (make-config-with-logging "p" {"p/c" "~#!@#xxx"} default-coercers (fn [reports] (reset! reports-atom reports)))
      (is (= :warn (-> @reports-atom ffirst)))
      (is (re-find #"unable to read-string from variable 'p/c' with value \"~#!@#xxx\"" (-> @reports-atom first second))))))

(deftest test-problems
  (testing "invalid code"
    (is (= {} (make-config "p" {"p/c" "~#!@#xxx"}))))
  (testing "naming conflicts"
    (let [reports (atom [])
          vars {"p/a"     "1"
                "p/a/b"   "2"
                "p/a/b/c" "3"}
          expected {:a {:b {:c 3}}}
          errors [[:warn "env-config: naming conflict: the variable 'p/a' with value \"1\" was shadowed by the variable 'p/a/b' with value \"2\". A variable name must not be a prefix of another variable name."]
                  [:warn "env-config: naming conflict: the variable 'p/a/b' with value \"2\" was shadowed by the variable 'p/a/b/c' with value \"3\". A variable name must not be a prefix of another variable name."]]]
      (is (= expected (make-config "p" vars default-coercers reports)))
      (is (= errors @reports)))))
