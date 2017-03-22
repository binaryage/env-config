(defproject binaryage/env-config "0.1.2-SNAPSHOT"
  :description "Clojure(Script) library for config map overrides via environment variables."
  :url "https://github.com/binaryage/env-config"
  :license {:name         "MIT License"
            :url          "http://opensource.org/licenses/MIT"
            :distribution :repo}
  :scm {:name "git"
        :url  "https://github.com/binaryage/env-config"}

  :dependencies [[org.clojure/clojure "1.9.0-alpha15" :scope "provided"]
                 [org.clojure/clojurescript "1.9.473" :scope "test"]
                 [org.clojure/tools.logging "0.3.1" :scope "test"]]

  :clean-targets ^{:protect false} ["target"
                                    "test/resources/.compiled"]

  :plugins [[lein-shell "0.5.0"]]

  ; this is just for IntelliJ + Cursive to play well
  :source-paths ["src/lib"]
  :test-paths ["test/src/tests"]
  :resource-paths ^:replace ["scripts"]

  :cljsbuild {:builds {}}                                                                                                     ; prevent https://github.com/emezeske/lein-cljsbuild/issues/413

  :profiles {:nuke-aliases
             {:aliases ^:replace {}}

             :dev
             {:plugins [[com.jakemccrary/lein-test-refresh "0.17.0"]
                        [lein-tach "0.2.0"]
                        [lein-cljsbuild "1.1.5"]]}

             :lib
             ^{:pom-scope :provided}                                                                                          ; ! to overcome default jar/pom behaviour, our :dependencies replacement would be ignored for some reason
             [:nuke-aliases
              {:dependencies   ~(let [project-str (or
                                                    (try (slurp "project.clj") (catch Throwable _ nil))
                                                    (try (slurp "/Users/darwin/code/env-config/project.clj") (catch Throwable _ nil)))
                                      project (->> project-str read-string (drop 3) (apply hash-map))
                                      test-dep? #(->> % (drop 2) (apply hash-map) :scope (= "test"))
                                      non-test-deps (remove test-dep? (:dependencies project))]
                                  (with-meta (vec non-test-deps) {:replace true}))                                            ; so ugly!
               :source-paths   ^:replace ["src/lib"]
               :resource-paths ^:replace []
               :test-paths     ^:replace []}]

             :clojure18
             {:dependencies [[org.clojure/clojure "1.8.0" :scope "provided"]]}

             :clojure17
             {:dependencies [[org.clojure/clojure "1.7.0" :scope "provided"]]}

             :self-host
             {:cljsbuild {:builds [{:id           "self-host-test-build"
                                    :source-paths ["src/lib"
                                                   "test/src/tests"]
                                    :compiler     {:output-to     "test/resources/.compiled/tests.js"
                                                   :main          'env-config.tests.runner
                                                   :target        :nodejs
                                                   :optimizations :none}}]}
              :tach      {:debug?                               false
                          :force-non-zero-exit-on-test-failure? true}}}

  :aliases {"install"        ["do"
                              ["shell" "scripts/prepare-jar.sh"]
                              ["shell" "scripts/local-install.sh"]]
            "test-all"       ["shell" "scripts/test-all.sh"]
            "test-self-host" ["shell" "scripts/test-self-host.sh"]
            "jar"            ["shell" "scripts/prepare-jar.sh"]
            "deploy"         ["shell" "scripts/deploy-clojars.sh"]
            "release"        ["do"
                              ["clean"]
                              ["shell" "scripts/check-versions.sh"]
                              ["shell" "scripts/prepare-jar.sh"]
                              ["shell" "scripts/check-release.sh"]
                              ["shell" "scripts/deploy-clojars.sh"]]})
