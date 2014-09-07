(ns nightrepl.userspace
  (:require 
    [cemerick.pomegranate :as pomegranate]
    [leiningen.core.project :as project]
    [leiningen.core.classpath :refer [get-classpath]]
    [clojure.tools.namespace.repl :as ns-repl]))

(defn initializer! 
  [handle]
  (let [path "."
        proj (project/read)
        cpaths (get-classpath proj)
        refresh-dirs (:source-paths proj)
        main-ns (:main proj)]
    (fn []  
      (doseq [cpath cpaths]
        (pomegranate/add-classpath cpath))
      ;(require '[clojure.tools.namespace.repl])
      (apply ns-repl/set-refresh-dirs refresh-dirs)
      (ns-repl/refresh-all)
      (ns main-ns))))