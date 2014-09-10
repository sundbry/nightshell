nightshell
==========

A graphical debug repl derived from Nightcode and REDL

## Installation

Download from git and install into your local Maven repository.
Add it as a project dependency:
```clj
[sundbry/nightshell "0.1.0-SNAPSHOT"] 
```

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
