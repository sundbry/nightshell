(defproject sundbry/nightshell "0.1.3-SNAPSHOT"
  :description "A debug repl derived from Nightcode and REDL"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [seesaw "1.4.5"]
                 [com.fifesoft/rsyntaxtextarea "2.5.6"]
                 [com.github.insubstantial/substance "7.3"]
                 [clj-stacktrace "0.2.7"]]
  :source-paths ["src/clj"]
  :java-source-paths ["src/java"]
  :aot :all
  :main nightshell.core)
