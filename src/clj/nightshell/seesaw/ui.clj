(ns nightshell.seesaw.ui
  (:require
    [clojure.java.io :as io])
  (:import 
    [clojure.lang LineNumberingPushbackReader]))

(def theme-resource (atom (io/resource "dark.xml")))

(def root (atom nil))

(defn get-io
  "Returns the Reader and Writer for the given console object."
  [console]
  [(LineNumberingPushbackReader. (.getIn console))
   (.getOut console)])
