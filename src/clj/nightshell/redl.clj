(ns nightshell.redl
  (:require [clojure.core.async :as async]
            clojure.stacktrace
            clj-stacktrace.repl) 
  (:use [clojure.repl :only [pst]] 
        [clojure.pprint :only [pprint]]))

(def spawn-repl-window (atom nil))

#_(defn dbg
  [& vals]
  (.println (System/out) "<DEBUG>")
  (doseq [val vals]
    (.println (java.lang.System/out) (str val))
    (.flush System/out))
  (.println (System/out) "</DEBUG>")
  (.flush System/out))

(defn dbg [& vals] nil)

(def pretty-print
  "DEPRPECATED; set `print-fn` directly.
   This var determines whether repl results are `println`ed or `pprint`ed."
  (atom true))

(def print-fn
  "Allows you to configure what function is used to print the result of computations.
   This function should take one argument, the expression's value."
  (atom (fn [arg]
          (if @pretty-print
            (pprint arg)
            (println arg)))))

;This var tracks how many nested breaks there are active
(def ^:dynamic *repl-depth* 0)
(def ^:dynamic *repl-continue* nil)
;This var is used by the eval-with-locals subsystem
(declare ^:dynamic *locals*)

(defn view-locals
  []
  *locals*)

(defn eval-with-locals
  "Evals a form with given locals. The locals should be a map of symbols to
  values."
  [locals form]
  (binding [*locals* locals]
    (eval
      `(let ~(vec (mapcat #(list % `(*locals* '~%)) (keys locals)))
         ~form))))

(defn eval-with-state-and-locals
  "Evaluates a form with a given state and local binding. The local binding
  is the return value of using the `local-bindings` macro at the place
  you wish to capture the locals. State is a map containing the
  repl state, which has the following keys: `:*1`, `:*2`, `:*3`, `:*e`,
  and `:ns`. It will return the updated state, along with the extra keys
  `:out`, `:err`, and `:result`, which will be bound to all the stdout and
  stderr of the evaluated form, and it's value. The value will also be
  included `pprint`ed in stdout for display convenience."
  [form state locals]
  (let [out-writer (java.io.StringWriter.)
        err-writer (java.io.StringWriter.)]
    (binding [*ns* *ns*
              *out* (java.io.PrintWriter. out-writer)
              *err* (java.io.PrintWriter. err-writer)
              *1 (:*1 state)
              *2 (:*2 state)
              *3 (:*3 state)
              *e (:*e state)]
      (with-redefs [;clojure.core/print-sequential reply.hacks.printing/print-sequential
                    clojure.repl/pst clj-stacktrace.repl/pst] 
        (let [ex (atom nil)
              repl-thread (atom (Thread/currentThread))
              result (try
                       (in-ns (:ns state))
                       (eval-with-locals locals (first form))
                       (catch Throwable t
                         (do
                           (doto (clojure.main/repl-exception t)
                             (pst)) 
                           (reset! ex t))))
              result-out (@print-fn result)]
          (reset! repl-thread nil)
          (if (= ::continue result)
            nil ; terminate state
            (->
              (if-let [e @ex]
                (assoc state :*e e :result ::error)
                (assoc state
                       :result result-out
                       :*1 result-out
                       :*2 *1
                       :*3 *2))
              (assoc 
                :ns (ns-name *ns*)
                :repl-depth *repl-depth*
                :out (str out-writer)
                :err (str err-writer)))))))))
  
(def ^:dynamic *repl-input*)
(def ^:dynamic *repl-output*)

(defn late-bound-repl-loop
  "This creates a repl loop with the given initial state and, optionally,
  locals. This is late-bound because it uses repl-input and repl-output,
  call `deref` such that it is possible for an evaluated form to recursively
  create another late-bound-repl-loop."
  [state locals]
  (loop [state state]
    (let [state' (eval-with-state-and-locals
                   (async/<!! *repl-input*) state locals)]
      (if (some? state')     
        (do
          (async/>!! *repl-output* state')
          (recur state'))
        (do
          (async/close! *repl-input*)
          (async/close! *repl-output*))))))

(defn eval-worker
  "Creates an eval worker thread that can transfer its IO control
   down the stack. Returns `[in out thread]`, where `in` and `out`
   are the channels to send and recieve messages, and `thread` is
   a promise containing the actual worked Thread, for debugging."
  ([ns] (eval-worker {:ns ns} {}))
  ([state locals]
    (let [in (async/chan)
          out (async/chan)
          thread (promise)
          depth *repl-depth*
          continue-chan *repl-continue*]
      ;;TODO: make this thread called "repl-%id"?
      (async/thread
        (deliver thread (Thread/currentThread))
        (binding [*repl-input* in
                  *repl-output* out
                  *repl-depth* depth
                  *repl-continue* continue-chan]
          (late-bound-repl-loop state locals)))
      [in out @thread])))

(def supervisor-ids (atom 0))
(def supervisors (atom {}))

(defn do-wait
  [latest-state worker-out out]
  (let [t (async/timeout 1000)]
    (async/alt!!
      worker-out
      ([state]
        (if (some? state)
          (async/>!! out state)
          (async/close! out))
        [false state])
      ;t ; disable timeouts
      #_([_]
       (async/>!! out (assoc latest-state
                             :out "Worker has not yet finished computation. Try meta commands [wait, stack, interrupt, stop, help].\n"
                             :err ""))
       [true latest-state]))))

(defn do-stacktrace
  [state ^Thread thread out]
  (let [stacktrace (.getStackTrace thread)
        pretty (with-out-str
                 (doseq [ste stacktrace]
                   (clojure.stacktrace/print-trace-element ste)
                   (println)))]
    (async/>!! out (assoc state
                          :out (str "Stack trace of thread: "
                                    (.getName thread) "\n\n"
                                    pretty)
                          :err ""))))

(defn print-help
  [state out]
  (async/>!! out (assoc state
                        :out "Enter wait, stack, interrupt, or stop\n"
                        :err "")))
(defn eval-supervisor
  "Creates an eval supervisor thread that will create and drive
   an eval-worker. If the worker becomes unresponsive, the
   supervisor allows meta-control of the worker (stop, threadump, wait).

   Returns the supervisor id, which allows it to be controlled in
   the repl."
  ([ns] (eval-supervisor {:ns ns} {}))
  ([state locals]
    (let [id (swap! supervisor-ids inc)
          in (async/chan)
          out (async/chan)
          [worker-in' worker-out' thread' :as worker] (eval-worker state locals)
          ;; We'll store the worker's info in an atom, so that we can
          ;; change it during a hard stop of the thread.
          ;; This broke when I tried to pass it in the loop/recur
          worker-in (atom worker-in')
          worker-out (atom worker-out')
          thread (atom thread')]
      (swap! supervisors assoc id [in out])
      (async/thread
        (loop [busy false
               latest-state state]
          (let [form (async/<!! in)]
            (if busy
              ;; When busy, try doing an op
              (condp = form
                "wait" (let [[busy state] (do-wait latest-state @worker-out out)]
                         (recur busy state))
                "stack" (do (do-stacktrace latest-state @thread out)
                          (recur true latest-state))
                "interrupt" (do (.interrupt @thread) 
                              (let [[busy state] (do-wait latest-state @worker-out out)]
                                (recur busy state)))
                "stop" (do (async/>!! out (assoc latest-state
                                                 :out "Stopped thread, creating new worker...\n"
                                                 :err ""
                                                 :repl-depth 0))
                         (.stop @thread) 
                         ;; Make a new worker thread
                         (let [[worker-in' worker-out' thread' :as worker] (eval-worker (:ns latest-state))]
                           (reset! worker-in worker-in')
                           (reset! worker-out worker-out')
                           (reset! thread thread'))
                         (recur false {:ns (:ns latest-state)}))
                (do (print-help latest-state out)
                  (recur true latest-state)))
              ;; Not busy, do a new eval
              (do (async/>!! @worker-in form)
                (let [[busy state] (do-wait latest-state @worker-out out)]
                  (when (some? state)
                    (recur busy state))))))))
      id)))

(defn make-repl
  [ns]
  (eval-supervisor ns))

(defn repl-eval-form
  "Takes a repl id and a form, and evaluates that form on the given repl."
  [repl form]
  (if-let [[input output] (@supervisors repl)]
    (do 
      (async/>!! input [form])
      (let [result (async/<!! output)]
        (when result
          (select-keys result [:out :err :repl-depth :ns]))))
    (do
      {:ns 'user
       :out "This repl doesn't exist. You must start a new one.\n"
       :err ""})))

(defn repl-eval
  [repl form-str]
  (repl-eval-form repl (read-string form-str)))

(defn- truncate-reverse-stack-bottom
  [stack-trace]
  (if-let [ste (first stack-trace)]
    (if (= "nightshell.redl$eval_with_locals" (.getClassName ste))
      (rest stack-trace)
      (recur (rest stack-trace)))
    nil))

(defn- truncate-stack-trace-bottom
  [stack-trace]
  (when-let [bottom (truncate-reverse-stack-bottom (reverse stack-trace))]
    (reverse bottom)))

(defn- truncate-stack-trace-top
  [stack-trace]
  (if-let [ste (first stack-trace)]
    (if (= "nightshell.redl$break_with_window_STAR_" (.getClassName ste))
      (rest stack-trace)
      (recur (rest stack-trace)))
    []))

(defn- truncate-stack-trace
  [stack-trace]
  (let [bottom (-> stack-trace truncate-stack-trace-top)
        middle (-> bottom truncate-stack-trace-bottom)
        trunc (if (some? middle) ; if we truncated off the bottom
                middle
                bottom)]
    trunc))

(defn- thread-context
  [thread]
  {:thread thread
   :stack-trace (truncate-stack-trace (.getStackTrace thread))})

#_(defn break-in-repl*
  "Invoke this to drop into a new sub-repl, which
  can return into the parent repl at any time. Must supply
  locals, that will be in scope in the new subrepl."
  [locals]
  (when-not (bound? #'*repl-output*)
    (throw (ex-info "You cannot call break outside of a REDL repl." {})))
  (try
    (binding [*repl-depth* (inc *repl-depth*)]
      (async/>!! *repl-output* {:out "Encountered break, waiting for input...\n"
                                :err ""
                                :ns (ns-name *ns*)
                                :repl-depth *repl-depth*})
      (late-bound-repl-loop {:ns (ns-name *ns*)
                             :*1 *1 :*2 *2 :*3 *3 :*e *e}
                            locals))
    (catch clojure.lang.ExceptionInfo ex
      (assert (contains? (ex-data ex) ::continue))
      (::continue (ex-data ex)))))

(defn break-with-window*
  "Invoke this to drop into a new sub-repl, which
  can return into the parent repl at any time. Must supply
  locals, that will be in scope in the new subrepl."
  [locals]
  (when-let [break-interact @spawn-repl-window]
    (binding [*repl-depth* (inc *repl-depth*)
              *repl-continue* (async/chan)]
      (let [break-repl (eval-supervisor
                         {:ns (ns-name *ns*)
                          :*1 *1 :*2 *2 :*3 *3 :*e *e}
                         locals)]
        (break-interact break-repl (thread-context (Thread/currentThread)))
        (let [result (async/<!! *repl-continue*)]    
          (when-not (= ::no-arg result)
            ; return nil iff no result from user interaction
            result))))))

(defn continue*
  [value]
  (when (zero? *repl-depth*)
    (throw (ex-info "Cannot call continue when not in a break statement!" {})))
  (async/>!! *repl-continue* value)
  ::continue)
