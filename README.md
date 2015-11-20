nightshell
==========

A graphical debug repl derived from Nightcode and REDL.
Use it to launch breakpoints in interactive REPL windows.

## Installation


Create a :nightshell user profile in ~/.lein/profiles.clj

```clj
{:nightshell
 {:plugins
  []
  :dependencies
  [[sundbry/nightshell "0.1.4"]]
  :injections
  [(require 'nightshell.core)
   (nightshell.core/enable)]}}
```

## Usage

Run with your nightshell profile included:
`lein with-profile nightshell repl`

```clj
; Enable breakpoints
(nightshell.core/enable)

; Create 
(defn foo [msg]
  (nightshell.core/break (str "Hello" msg)))

; Run your program
(foo "world!")

; Inspect the return value of a breakpoint
(return)

; Continue execution after a breakpoint
(nightshell.core/continue my-value)

; Disable breakpoints (useful for escaping loops)
(nightshell.core/disable)

; Catch exceptions in a breakpoint
(nightshell.core/catch-break (throw (Exception. "Zomg!")))
```
