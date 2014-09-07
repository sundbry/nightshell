(defproject sundbry/nightshell "0.1.0-SNAPSHOT"
  :description "A debug repl derived from Nightcode and REDL"
  :dependencies [[nightcode "0.3.11-SNAPSHOT"]
                 ;[org.clojure/clojure "1.6.0"]
                 [org.clojure/core.async "0.1.338.0-5c5012-alpha"]
                 [org.clojure/tools.namespace "0.2.5"]
                 [seesaw "1.4.4"]
                 [com.cemerick/pomegranate "0.3.0"]]
  ;:uberjar-exclusions [#"clojure-clr.*\.zip"]
  ;:javac-options ["-target" "1.6" "-source" "1.6" "-Xlint:-options"]
  ;:aot [nightshell.core]
  ;:main nightshelll.core)
  :eval-in-leiningen true)
