(ns nightshell.seesaw.utils
  (:import
    [org.fife.ui.rsyntaxtextarea SyntaxConstants]))

(def styles {"as"         SyntaxConstants/SYNTAX_STYLE_ACTIONSCRIPT
             "asm"        SyntaxConstants/SYNTAX_STYLE_ASSEMBLER_X86
             "bat"        SyntaxConstants/SYNTAX_STYLE_WINDOWS_BATCH
             "c"          SyntaxConstants/SYNTAX_STYLE_C
             "cc"         SyntaxConstants/SYNTAX_STYLE_C
             "cl"         SyntaxConstants/SYNTAX_STYLE_LISP
             "cpp"        SyntaxConstants/SYNTAX_STYLE_CPLUSPLUS
             "css"        SyntaxConstants/SYNTAX_STYLE_CSS
             "clj"        SyntaxConstants/SYNTAX_STYLE_CLOJURE
             "cljs"       SyntaxConstants/SYNTAX_STYLE_CLOJURE
             "cljx"       SyntaxConstants/SYNTAX_STYLE_CLOJURE
             "cs"         SyntaxConstants/SYNTAX_STYLE_CSHARP
             "dtd"        SyntaxConstants/SYNTAX_STYLE_DTD
             "edn"        SyntaxConstants/SYNTAX_STYLE_CLOJURE
             "groovy"     SyntaxConstants/SYNTAX_STYLE_GROOVY
             "h"          SyntaxConstants/SYNTAX_STYLE_C
             "hpp"        SyntaxConstants/SYNTAX_STYLE_CPLUSPLUS
             "htm"        SyntaxConstants/SYNTAX_STYLE_HTML
             "html"       SyntaxConstants/SYNTAX_STYLE_HTML
             "java"       SyntaxConstants/SYNTAX_STYLE_JAVA
             "js"         SyntaxConstants/SYNTAX_STYLE_JAVASCRIPT
             "json"       SyntaxConstants/SYNTAX_STYLE_JAVASCRIPT
             "jsp"        SyntaxConstants/SYNTAX_STYLE_JSP
             "jspx"       SyntaxConstants/SYNTAX_STYLE_JSP
             "lisp"       SyntaxConstants/SYNTAX_STYLE_LISP
             "lua"        SyntaxConstants/SYNTAX_STYLE_LUA
             "makefile"   SyntaxConstants/SYNTAX_STYLE_MAKEFILE
             "markdown"   SyntaxConstants/SYNTAX_STYLE_NONE
             "md"         SyntaxConstants/SYNTAX_STYLE_NONE
             "mustache"   SyntaxConstants/SYNTAX_STYLE_NONE
             "pas"        SyntaxConstants/SYNTAX_STYLE_DELPHI
             "properties" SyntaxConstants/SYNTAX_STYLE_PROPERTIES_FILE
             "php"        SyntaxConstants/SYNTAX_STYLE_PHP
             "pl"         SyntaxConstants/SYNTAX_STYLE_PERL
             "pm"         SyntaxConstants/SYNTAX_STYLE_PERL
             "py"         SyntaxConstants/SYNTAX_STYLE_PYTHON
             "rb"         SyntaxConstants/SYNTAX_STYLE_RUBY
             "s"          SyntaxConstants/SYNTAX_STYLE_ASSEMBLER_X86
             "sbt"        SyntaxConstants/SYNTAX_STYLE_SCALA
             "scala"      SyntaxConstants/SYNTAX_STYLE_SCALA
             "sh"         SyntaxConstants/SYNTAX_STYLE_UNIX_SHELL
             "sql"        SyntaxConstants/SYNTAX_STYLE_SQL
             "tcl"        SyntaxConstants/SYNTAX_STYLE_TCL
             "tex"        SyntaxConstants/SYNTAX_STYLE_LATEX
             "txt"        SyntaxConstants/SYNTAX_STYLE_NONE
             "xhtml"      SyntaxConstants/SYNTAX_STYLE_XML
             "xml"        SyntaxConstants/SYNTAX_STYLE_XML})

(defn- get-string
  [string-key]
  (name string-key))

(defn set-accessible-name!
  [widget a-name]
  (-> widget
      .getAccessibleContext
      (.setAccessibleName (if (keyword? a-name) (get-string a-name) a-name))))
