(set-env!
  :source-paths #{"src"}
  :dependencies '[[adzerk/bootlaces "0.1.9" :scope "test"]
                  [boot/core "2.1.2"  :scope "compile"]])

(require
  '[adzerk.bootlaces :refer :all]
;  '[sjmackenzie.boot-nix :refer :all]
  )

(def +ver+ "0.1.0")
(def +version+ (str +ver+ "-SNAPSHOT"))
(bootlaces! +version+)

(task-options!
  pom {:project     'sjmackenzie/boot-nix
       :version     +version+
       :description "Boot task to create a deps.nix used for NixOS"
       :url         "https://github.com/sjmackenzie/boot-nix"
       :scm         {:url "https://github.com/sjmackenzie/boot-nix"}
       :license     {"Eclipse Public License"
                     "http://www.eclipse.org/legal/epl-v10.html"}})
