(ns sjmackenzie.boot-nix
  {:boot/export-tasks true}
  (:require
    [boot.core :as core :refer :all]
    [clojure.java.io :as io]
    [clojure.string :as string]
    [clojure.pprint :as pprint]
    [boot.util :as util]))

(deftask nixos
  "Creates a Nix Expression enumerating all this project's dependencies"
  []
  (defn repo-url [repo-desc] (if (string? repo-desc) repo-desc (:url repo-desc)))
  (def repositories (apply hash-map (flatten (:repositories (core/get-env)))))
  (def orig-repo-map (map
                       (fn [[repo-alias desc]] (vector repo-alias (repo-url desc)))
                       repositories))
  (def extra-repos ["central" "https://repo1.maven.org/maven2/"])
  (def repo-map (conj orig-repo-map extra-repos ))
  (def resolved-deps
    (boot.pod/with-call-worker
      (boot.aether/resolve-dependency-jars ~(core/get-env))))
  (def repo-file-names
    {:maven "/_maven.repositories"
     :remote "/_remote.repositories"})
  (defn find-repo-file [file-path]
    (let [exists (fn [repo-file] (.exists (io/as-file (str file-path repo-file))))]
      (cond
        (exists (:maven repo-file-names)) (slurp (str file-path (:maven repo-file-names)))
        (exists (:remote repo-file-names)) (slurp (str file-path (:remote repo-file-names))))))
  (defn find-repo-server [repo-file-contents]
    (filter string?
            (map
              (fn [[k v]] (when (re-find (re-pattern k) repo-file-contents) v))
              repo-map)))
  (defn extract-repo [file-path]
    (let [repo-file-contents (find-repo-file file-path)
          repo-server (find-repo-server repo-file-contents)]
      (first repo-server)))
  (defn create-url [jar]
    (let [file-path (string/join "/" (butlast (string/split jar #"/")))
          base-path (drop 5 (string/split file-path #"/"))
          base-url (str (extract-repo file-path) )
          url (str base-url (string/join "/" base-path))]
      url))
  (defn destructure-dep [jar]
    (let [extless-file-name (second (re-find #"(.*).jar" (last (string/split jar #"/"))))
          exts [".jar" ".jar.sha1" ".pom" ".pom.sha1"]
          base-url (create-url jar)
          needed-files (str (string/join "\n"
                                         (map #(str base-url "/" %)
                                              (map #(str extless-file-name %) exts))) "\n")]
      needed-files))
  (defn extract-urls [deps]
    (mapv #(destructure-dep %) deps))
  (defn write-nix-expression []
    (spit "./deps.nix" (apply str (extract-urls resolved-deps))))
  (write-nix-expression)
  #_(pprint/pprint (core/get-env)))
