(ns nightshell.core
  (:require [nightshell.redl :as redl]
            [nightshell.seesaw :refer [spawn-root-window spawn-break-window init-once]]))

(defn enable
  []  
  (init-once)
  (reset! redl/spawn-repl-window spawn-break-window)
  true)

(defn disable
  []
  (reset! redl/spawn-repl-window nil)
  false)

(defn -main [& args]
  (enable)
  (spawn-root-window args))
