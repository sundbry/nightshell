(defproject sundbry/nightshell "0.1.6"
  :description "A debug repl derived from Nightcode and REDL"
  :dependencies
  [[org.clojure/clojure "1.10.0"]
   [org.clojure/core.async "0.4.490"]
   [seesaw "1.5.0"]
   [com.fifesoft/rsyntaxtextarea "3.0.3"]
   [com.github.insubstantial/substance "7.3"]
   [clj-stacktrace "0.2.8"]]
  :source-paths ["src/clj"]
  :java-source-paths ["src/java"]
  :main ^:skip-aot nightshell.core)
