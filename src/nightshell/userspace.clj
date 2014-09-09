(ns nightshell.userspace
  (:require 
    #_[cemerick.pomegranate :as pomegranate]
    [leiningen.core.project :as core-project]
    [leiningen.core.classpath :refer [get-classpath]]
    #_[clojure.tools.namespace.repl :as ns-repl]))

(defn project
  []
  (core-project/read))

#_(defn initializer! 
  [proj]
  (let [cpaths (get-classpath proj)
        refresh-dirs (:source-paths proj)]
    (fn []  
      (doseq [cpath cpaths]
        (pomegranate/add-classpath cpath))
      (apply ns-repl/set-refresh-dirs refresh-dirs)
      (ns-repl/refresh-all))))