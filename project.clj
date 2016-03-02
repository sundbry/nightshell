(defproject sundbry/nightshell "0.1.6-SNAPSHOT"
  :description "A debug repl derived from Nightcode and REDL"
  :dependencies [[org.clojure/core.async "0.2.374"]
                 [seesaw "1.4.5"]
                 [com.fifesoft/rsyntaxtextarea "2.5.8"]
                 [com.github.insubstantial/substance "7.3"]
                 [clj-stacktrace "0.2.8"]]
  :profiles
  {:1.6
   {:dependencies
    [[org.clojure/clojure "1.6.0"]]}
   :1.7
   {:dependencies
    [[org.clojure/clojure "1.7.0"]]}}
  :source-paths ["src/clj"]
  :java-source-paths ["src/java"]
  :main ^:skip-aot nightshell.core)
