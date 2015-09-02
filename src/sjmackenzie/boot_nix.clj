(ns sjmackenzie.boot-nix
  {:boot/export-tasks true}
  (:require
    [boot.core :as core :refer :all]
    [clojure.java.io             :as io]
    [clojure.string              :as string]
    [clojure.pprint              :as pprint]
    [cemerick.pomegranate.aether :as aether]
    [boot.util                   :as util]))

(deftask nixos
  "Creates a Nix Expression enumerating all this project's dependencies"
  []

  (def repositories (:repositories (core/get-env)))
  (def resolved-deps
    (boot.pod/with-call-worker
      (boot.aether/resolve-dependencies ~(core/get-env))))

  (def repos {:maven "/_maven.repositories"
                     :remote "/_remote.repositories"})

  (defn find-repo-file [file-path]
    (let [exists (fn [repo-file] (.exists (io/as-file (str file-path repo-file))))]
      (cond
        (exists (:maven repos)) (slurp (str file-path (:maven repos)))
        (exists (:remote repos)) (slurp (str file-path (:remote repos))))))

  #_(defn filter-)

  (defn find-repo-server [repo-file-contents]
    ()
    (first (filter (fn [[k v]]
                     (when (re-find (re-pattern k) repo-file-contents) v)) repositories)))

  (defn extract-repo [file-path file-name]
    (let [repo-file-contents (find-repo-file file-path)
          repo-server (find-repo-server repo-file-contents)
          ]
      repo-server)
    )

  (defn create-url [jar]
    (let [file-path (string/join "/" (butlast (string/split jar #"/")))
          base-url (str (extract-repo file-path (last (string/split jar #"/"))) )
          ]
      base-url))

  (defn destructure-dep [dep]
    (let [name+version (:dep dep)
          fullname (str (first name+version))
          shortname (second(string/split fullname #"/"))
          version (str(second name+version))
          jar (:jar dep)
          base-url (create-url jar)
          ]
      base-url))

  (defn format-resolved-deps [deps]
     (map #(destructure-dep %) deps))

 #_(print (format-resolved-deps resolved-deps))

(pprint/pprint repositories)

  )
