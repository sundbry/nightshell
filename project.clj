(defproject sundbry/nightshell "0.1.1-SNAPSHOT"
  :description "A debug repl derived from Nightcode and REDL"
  :dependencies [[nightcode "0.3.12"]
                 [org.clojure/clojure "1.6.0"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 ;[org.clojure/tools.namespace "0.2.5"]
                 [seesaw "1.4.4"]
                 ;[com.cemerick/pomegranate "0.3.0"]
                 [clj-stacktrace "0.2.7"]]
  :main ^:skip-aot nightshell.core)
