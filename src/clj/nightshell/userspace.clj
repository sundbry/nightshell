#_(ns nightshell.userspace
  (:require 
    [leiningen.core.project :as core-project]
    [leiningen.core.classpath :refer [get-classpath]]))

#_(defn project
  []
  (core-project/read))

#_(defn initializer! 
  [proj]
  (let [main-ns (:main proj)]
    (fn []
      (when main-ns
        (require main-ns)
        (ns main-ns)))))
