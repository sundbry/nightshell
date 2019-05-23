(ns nightshell.test
  (:require 
    [clojure.test :refer :all]
    [nightshell.core :refer [break breakpoint catch-break]]))

(defn with-enabled
  [test-fn]
  (nightshell.core/enable)
  (test-fn))

(use-fixtures :once with-enabled)

(defn instruct
  [msg]
  (println msg)
  (flush))

(defn fail
  []
  (throw (ex-info "This function FAILS!" {})))

#_(deftest test-break
  (instruct "Return 1")
  (is 2 (+ 1 (break 1))))

#_(deftest test-break-inner
  (instruct "Return 1")
  (is 2 (+ 1 (break (break 1))))) ; 1 + 1 + 1

(deftest test-catch-break-through
  (instruct "Nothing should break here.")
  (is (= 2 (+ 1 (catch-break 1))))
  (instruct "Now things are going to start to break... (nightshell.core/continue 1)")
  (is (= 2 (+ 1 (catch-break (throw (Exception. "You should continue with 1 here")))))))

(deftest test-break-exception
  (instruct "Breakpoint should catch an exception. Contnue with it.")
  (is (thrown? clojure.lang.ExceptionInfo (+ 1 (breakpoint (fail)))))
  (instruct "Breakpoint should catch an exception. Contnue with 1 this tme.")
  (is (= 2 (+ 1 (break (fail)))))
  (instruct "OK that's done"))

#_(deftest test-break-user-exception
    (instruct "Throw an exception in the repl. Contnue with 1")
    (is (= 2 (+ 1 (break 1)))))
