(ns leiningen.nightshell
  (:require
    [leiningen.core.project :as proj]
    [leiningen.core.eval :refer [eval-in-project]]
    [leiningen.repl :as lein-repl]))

#_(def ^:private nsh-profile
  {:dependencies [['sundbry/nightshell "0.1.0-SNAPSHOT"]
                  ['nightcode "0.3.11-SNAPSHOT"]
                  ;[org.clojure/clojure "1.6.0"]
                  ['org.clojure/core.async "0.1.338.0-5c5012-alpha"]
                  ['org.clojure/tools.namespace "0.2.5"]
                  ['seesaw "1.4.4"]                  
                  ['com.cemerick/pomegranate "0.3.0"]]})

(def ^:private nsh-profile
  {:dependencies [['sundbry/nightshell "0.1.0-SNAPSHOT"]]})

(defn nightshell
  "Launch nightshell and load breakpoint code into the project."
  [project & args]
  (let [profile (or (:nightshell (:profiles project)) nsh-profile)
        project (proj/merge-profiles project [profile])]
      (eval-in-project project 
                       `(nightshell.core/-main)
                       `(require 'nightshell.core))))

#_(defn nightshell
  "Launch nightshell and load breakpoint code into the project."
  [project & args]
  (let [profile (or (:nightshell (:profiles project)) nsh-profile)
        project (proj/merge-profiles project [profile])]
    (lein-repl/repl project)))
      ;(eval-in-project project 
       ;                `(nightshell.core/-main)
        ;               `(require 'nightshell.core))))
