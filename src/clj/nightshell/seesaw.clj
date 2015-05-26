(ns nightshell.seesaw
  (:require    
    [seesaw.core :as s]
    [clj-stacktrace.core :refer [parse-trace-elem]]
    [clj-stacktrace.repl :refer [pst-elem-str source-str method-str]]
    [nightshell.thread :refer [start-thread!]]
    [nightshell.debug :as debug]
    [nightshell.seesaw.editors :as editors]
    [nightshell.seesaw.ui :as ui]
    [nightshell.seesaw.utils :as utils]
    [nightshell.seesaw.window :as window]))

(defn- get-console-text-area
  [console]
  (.getView (.getViewport console)))

(defn- create-stack-pane
  []
  (let [text-area (editors/create-text-area)]
    (doto text-area
      (.setSyntaxEditingStyle (get utils/styles "xml"))
      (.setLineWrap false)
      (.setReadOnly true)
      (.setRows 6)
      (.disable))
    (s/scrollable text-area)))

(defn- fill-stack-pane!
  [stack-console thread-context]
  (let [content (with-out-str
                  (doseq [ste (map parse-trace-elem (:stack-trace thread-context))]
                    (println (str (method-str ste) " <" (source-str ste) ">"))))]
    (s/text! (get-console-text-area stack-console) content)
    (s/scroll! stack-console :to :bottom)))

(defn- create-repl-pane
  "Returns the pane with the REPL."
  [console]
  (let [pane (s/config! console :id :repl-console)]
    (utils/set-accessible-name! (.getTextArea pane) :repl-console)
    ; return the repl pane
    pane))

(defn- run-repl!
  "Starts a REPL thread."
  [in-out repl-handle end-callback]
  (start-thread! in-out 
                      (if repl-handle
                        (debug/breakpoint-repl repl-handle)
                        (debug/repl))
                      (end-callback)))

(defn- start-repl-pane!
  [pane console repl-handle repl-terminate-callback]
  (letfn [(run! [& _]
                (s/request-focus! (get-console-text-area console))
                (run-repl! (ui/get-io console) repl-handle repl-terminate-callback))]
    ; start the repl
    (run!)))

(defn create-break-window
  [repl-handle stack-pane repl-pane]
  (let [frame (s/frame :title "Nightshell [breakpoint]"
                       :content (s/top-bottom-split stack-pane repl-pane)
                       :on-close :nothing ; can not close break point windows
                       :size [800 :by 300])] 
    frame))

(defn spawn-break-window
  [repl-handle thread-context]
  (s/invoke-later
    (do
      (let [stack-pane (create-stack-pane)
            console (editors/create-console "clj")
            repl-pane (create-repl-pane console)
            win (create-break-window repl-handle stack-pane repl-pane)]
        (s/show! win)
        (fill-stack-pane! stack-pane thread-context)
        (start-repl-pane! repl-pane console repl-handle #(s/dispose! win))))))

(defn create-root-window
  [repl-pane]
  (let [frame (s/frame :title "Nightshell"
                       :content repl-pane
                       :on-close :exit ; can not close break point windows
                       :size [800 :by 600])]
    frame))

(defn spawn-root-window
  [args]
  ; this will give us a nice dark theme by default, or allow a lighter theme
  ; by adding "-s light" to the command line invocation
  (window/set-theme! {})
  (let [console (editors/create-console "clj")
        repl-pane (create-repl-pane console)
        win (create-root-window repl-pane)]
    (s/invoke-later
      (s/show! win)
      (start-repl-pane! repl-pane console nil #(s/dispose! win)))))

(defn init-once
  []
  (swap! ui/root
         (fn [cur-root]
           (if (nil? cur-root)
             (s/frame)
             cur-root))))
