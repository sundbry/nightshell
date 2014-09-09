(ns nightshell.core
  (:require [nightcode.customizations :as custom]
            [nightcode.editors :as editors]
            [nightcode.shortcuts :as shortcuts]
            [nightcode.ui :as ui]
            [nightcode.window :as window]
            [nightcode.utils :as utils]
            [nightcode.sandbox :as sandbox]
            [nightshell.thread :refer [start-thread!]]
            [nightshell.redl :as redl]
            [nightshell.debug :as debug]
            [seesaw.core :as s]
            clojure.stacktrace
            [clj-stacktrace.repl :as strace]
            [clj-stacktrace.core :refer [parse-trace-elem]]))

(defn- get-console-text-area
  [console]
  (.getView (.getViewport console)))

(defn- run-repl!
  "Starts a REPL thread."
  [in-out repl-handle end-callback]
  (start-thread! in-out 
                      (if repl-handle
                        (debug/breakpoint-repl repl-handle)
                        (debug/repl))
                      (end-callback)))

(defn- create-repl-pane
  "Returns the pane with the REPL."
  [console]
  (let [pane (s/config! console :id :repl-console)]
    (utils/set-accessible-name! (.getTextArea pane) :repl-console)
    ; return the repl pane
    pane))

(defn- start-repl-pane
  [pane console repl-handle repl-terminate-callback]
  (letfn [(run! [& _]
                (s/request-focus! (get-console-text-area console))
                (run-repl! (ui/get-io! console) repl-handle repl-terminate-callback))]
    ; start the repl
    (run!)
    ; create a shortcut to restart the repl
    (when-not (sandbox/get-dir)
      (shortcuts/create-hints! pane)
      (shortcuts/create-mappings! pane {:repl-console run!}))))

(defn- create-stack-pane
  [thread-context]
  (let [stack-console (editors/create-console "clj")
        [stack-in stack-out] (ui/get-io! stack-console)]
    (binding [*out* stack-out]
      (println (count (:stack-trace thread-context))))  
    ;(strace/pst-elems-on stack-out false (map parse-trace-elem (:stack-trace thread-context)))
    (doto (get-console-text-area stack-console)
      (.setReadOnly true)
      (.disable))
    ;(.close stack-in)
    ;(.close stack-out)
    stack-console))

(defn create-break-window
  [repl-handle thread-context]
  (let [console (editors/create-console "clj")
        stack-pane (create-stack-pane thread-context)
        repl-pane (create-repl-pane console)
        frame (s/frame :title "Nightshell [breakpoint]"
                       :content (s/top-bottom-split stack-pane repl-pane)
                       :on-close :nothing ; can not close break point windows
                       :size [800 :by 300])]
    (start-repl-pane repl-pane console repl-handle #(s/dispose! frame))
    #_(doto frame
      ; set various window properties
      window/enable-full-screen!
      window/add-listener!)
    [frame stack-pane repl-pane]))
  
(defn spawn-break-window
  [repl-handle thread-context]
  (s/invoke-later
    (do
      (let [[win stack console] (create-break-window repl-handle thread-context)]                
        (s/show! win)
        (s/scroll! stack :to :top)
        (s/request-focus! (get-console-text-area console))))))

(defn create-root-window
  []
  (let [console (editors/create-console "clj")
        pane (create-repl-pane console)
        frame (s/frame :title "Nightshell"
                       :content pane
                       :on-close :exit ; can not close break point windows
                       :size [800 :by 600])]
    (start-repl-pane pane console nil #(s/dispose! frame))
    (doto frame
      ; set various window properties
      window/enable-full-screen!
      window/add-listener!)
    [frame]))

(defn enable
  []  
  (swap! ui/root
         (fn [cur-root]
           (if (nil? cur-root)
             (s/frame)
             cur-root))) 
  (reset! redl/spawn-repl-window spawn-break-window)
  true)

(defn disable
  []
  (reset! redl/spawn-repl-window nil)
  false)

(defn -main [& args]
  ; listen for keys while modifier is down
  (shortcuts/listen-for-shortcuts!
    (fn [key-code]
      (case key-code
        ; Q
        81 (window/confirm-exit-app!)
        ; else
        false)))
  ; this will give us a nice dark theme by default, or allow a lighter theme
  ; by adding "-s light" to the command line invocation
  (window/set-theme! (custom/parse-args args))
  (reset! ui/root (first (create-root-window)))
  (enable)
  (s/invoke-later
    (s/show! @ui/root)))
