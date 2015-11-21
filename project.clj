(defproject sundbry/nightshell "0.1.5"
  :description "A debug repl derived from Nightcode and REDL"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/core.async "0.2.371"]
                 [seesaw "1.4.5"]
                 [com.fifesoft/rsyntaxtextarea "2.5.6"]
                 [com.github.insubstantial/substance "7.3"]
                 [clj-stacktrace "0.2.8"]]
  :source-paths ["src/clj"]
  :java-source-paths ["src/java"]
  :aot :all
  :main nightshell.core)
