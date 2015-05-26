(ns nightshell.seesaw.window
  (:require
    [seesaw.core :as s]
    [nightshell.seesaw.ui :as ui])
  (:import 
    ;[java.awt Window]
    ;[java.awt.event WindowAdapter]
    ;[java.lang.reflect InvocationHandler Proxy]
    [org.pushingpixels.substance.api SubstanceLookAndFeel]
    [org.pushingpixels.substance.api.skin GraphiteSkin]))

(defn set-theme!
  "Sets the theme based on the command line arguments."
  [args]
  (s/native!)
  (let [{:keys [skin-object theme-resource]} args]
    (when theme-resource (reset! ui/theme-resource theme-resource))
    (SubstanceLookAndFeel/setSkin (or skin-object (GraphiteSkin.)))))
