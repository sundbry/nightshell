nightshell
==========

A graphical debug repl derived from Nightcode and REDL

## Installation

Download from git and install into your local Maven repository.
Add [sundbry/nightshell "0.1.0-SNAPSHOT"] as a project dependency.

## Usage

Include breakpoints in your code.
```clj
(use '[nightshell.core :only [break]])

(defn foo []
  (break "Bar!"))
```

In your REPL session,

```clj
user=> (require 'nightshell.core)
user=> (nightshell.core/enable)
user=> (myapp/-main)
```
