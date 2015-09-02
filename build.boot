(set-env!
  :source-paths #{"src"}
  :dependencies '[[org.clojure/clojure      "1.6.0"  :scope "compile"]
                 [boot/core "2.1.2"  :scope "compile"]
                 [com.cemerick/pomegranate "0.3.0"  :scope "compile"]])

(require
 '[sjmackenzie.boot-nix :refer :all])

(def +version+ "0.0.1")

(task-options!
  pom {:project     'sjmackenzie/boot-nix
       :version     +version+
       :description "Boot task to create a deps.nix used for NixOS"
       :url         "https://github.com/sjmackenzie/boot-nix"
       :scm         {:url "https://github.com/sjmackenzie/boot-nix"}
       :license     {"Eclipse Public License"
                     "http://www.eclipse.org/legal/epl-v10.html"}})
