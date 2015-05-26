(ns nightshell.userspace
  (:require 
    [leiningen.core.project :as core-project]
    [leiningen.core.classpath :refer [get-classpath]]))

(defn project
  []
  (core-project/read))

(defn initializer! 
  [proj]
  (let [main-ns (:main proj)]
    (fn []
      (when main-ns
        (require main-ns)
        (ns main-ns)))))
