(ns nightshell.seesaw.editors
  (:require
    [clojure.java.io :as io]
    [seesaw.core :as s]
    [nightshell.seesaw.ui :as ui]
    [nightshell.seesaw.utils :as utils])
  (:import
    [nightshell.seesaw.ui JConsole]
    [org.fife.ui.rsyntaxtextarea TextEditorPane Theme]))

(def font-size (atom 14))

(defn- set-font-size!
  [text-area size]
  (.setFont text-area (-> text-area .getFont (.deriveFont (float size))))
  (s/request-focus! text-area))

(defn- apply-settings!
  [text-area]
  (-> @ui/theme-resource
      io/input-stream
      Theme/load
      (.apply text-area))
  (set-font-size! text-area @font-size))

(defn create-text-area
  []
  (doto (proxy [TextEditorPane] []
          (setMarginLineEnabled [enabled?]
            (proxy-super setMarginLineEnabled enabled?))
          (setMarginLinePosition [size]
            (proxy-super setMarginLinePosition size))
          (processKeyBinding [ks e condition pressed]
            (proxy-super processKeyBinding ks e condition pressed)))
    (.setAntiAliasingEnabled true)
    apply-settings!))

(defn create-console
  [extension]
  (let [text-area (create-text-area)]
    (doto text-area
      (.setSyntaxEditingStyle (get utils/styles extension))
      (.setLineWrap true)) 
    (nightshell.seesaw.ui.JConsole. text-area)))
