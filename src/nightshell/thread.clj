(ns nightshell.thread
  (:require [nightcode.utils :as utils]))

(defn redirect-io
  [[in out] func]
  (binding [*out* out
            *err* out
            *in* in]
    (func)))

(defn start-thread!*
  [in-out func]
  (letfn [(catcher []
                   (try
                     (func)
                     (catch Exception e 
                       (when (utils/read-pref :print-stack-traces true)
                         (.printStackTrace e))
                       (some-> (.getMessage e) println))))]
    (let [thread (Thread. (fn [] (redirect-io in-out catcher)))]
      (.start thread)
      thread)))
         

(defmacro start-thread!
  [in-out & body]
  `(start-thread!* ~in-out (fn [] ~@body)))
