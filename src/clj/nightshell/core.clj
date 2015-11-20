(ns nightshell.core
  (:require [nightshell.redl :as redl]
            [nightshell.seesaw :refer [spawn-root-window spawn-break-window init-once]])
  (:gen-class))

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

(defmacro macro-eval
  [expr]
  `(try 
     {:value ~expr}
     (catch Throwable e#
       {:exception e#})))

(defmacro macro-return
  [result]
  {:pre [(symbol? result)]}
  `(if (some? (:exception ~result))
     (throw (:exception ~result))
     (:value ~result)))

(defmacro continue
  "Invoke this from inside a debug repl to return up a level.
  
  If no value is provided, the corresponding `(break argument)` will return
  its argument or `nil` if invoked as `(break)`. If a value is provided,
  `break` will return that value and discard the provided argument."
  ([]
   `(redl/continue* redl/no-arg))
  ([expr]
    `(redl/continue* (macro-eval ~expr))))

(defmacro local-bindings
  "Produces a map of the names of local bindings to their values."
  []
  (let [symbols (keys &env)]
    (zipmap (map (fn [sym] `(quote ~sym)) symbols) symbols)))

(defmacro break
  "Invoke this to drop into a new sub-repl. It will automatically capture
  the locals visible from the place it is invoked. To return from the `break`
  statement, call `continue`. See continue for details on the return value
  of break."
  ([]
   `(break nil))
  ([expr]
   `(let [initial-result# (macro-eval ~expr) ; returns {:value, :exception}
          bindings# (merge
                      (local-bindings)
                      {(symbol "return") (fn [] (macro-return initial-result#))}) ; bind a fn to return the expr value
          debug-result# (redl/break-with-window* bindings#) ; returns result or nil
          result# (or debug-result# initial-result#)]
      (macro-return result#))))

(defn breakpoint
  "Break wrapped in a function."
  ([] (break))
  ([value] (break value)))
          
(defmacro catch-break
  "Invoke break only if we catch an exception on the forms"
  [& forms]
  `(try ~@forms
    (catch Throwable e#
      (break (throw e#)))))
