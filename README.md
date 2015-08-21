nightshell
==========

A graphical debug repl derived from Nightcode and REDL

## Installation


Add to your ~/.lein/profiles.clj

```clj
{:nightshell
 {:plugins
  []
  :dependencies
  [[sundbry/nightshell "0.1.4"]]
  :injections
  [(require 'nightshell.core)
   (nightshell.core/enable)]
  ; You may need to increase JVM perm space to load the classes
  :jvm-opts ["-XX:PermSize=256m"]}}
```

## Usage

Include breakpoints in your code.
```clj
(defn foo []
  (nightshell.core/break "Bar!"))
```

When a breakpoint is encountered, an interactive REPL window will pop up. You can use 
```clj
(return)
```
to inspect the value at the breakpoint, and 
```clj
(nightshell.core/continue my-value)
```
To continue execution from the breakpoint.
