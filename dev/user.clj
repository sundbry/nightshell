(ns user
  (:require nightshell.core
            [nightshell.redl :refer [break]]))

(defn test-break
  []
  (+ 1 (break 1)))

(defn test-break-arg
  [arg]
  (+ arg (break 1)))

(defn run-test []
  (require 'nightshell.core)
  (nightshell.core/enable)
  (test-break)
  #_(test-break-arg (test-break)))