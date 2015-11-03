(ns fractalide.boot-nix
  {:boot/export-tasks true}
  (:require
    [boot.core :as core :refer [deftask with-pre-wrap]]
    [clojure.java.io :as io]
    [clojure.string :as string]
    [clojure.pprint :as pprint]
    [boot.util :as util]))

(def shell-function
  "
  source $stdenv/setup
  header \"fetching deps\"

  function fetchArtifact {
  repoUrl=\"$1\"
  repoPath=\"$2\"
  url=\"$repoUrl/$repoPath\"
  echo \"Fetching $url\"
  mkdir -p $(dirname $out/$repoPath)
  curl --fail --location --insecure --retry 3 --max-redirs 20 \"$url\" --output \"$out/$repoPath\"
  # add -SL for artifacts behind a password wall
  }
  \n\n")

(defn repo-url [repo-desc] (if (string? repo-desc) repo-desc (:url repo-desc)))

(def repositories (apply hash-map (flatten (:repositories (core/get-env)))))

(def orig-repo-map (map
                     ; TODO: add repo password support
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
          ; TODO: add repo password support
          (map
            (fn [[k v]] (when (re-find (re-pattern k) repo-file-contents) v))
            repo-map)))

(defn extract-repo [file-path]
  (let [repo-file-contents (find-repo-file file-path)
        repo-server (find-repo-server repo-file-contents)]
    (first repo-server)))

(defn create-sh-cmd-params [jar]
  (let [file-path (string/join "/" (butlast (string/split jar #"/")))
        base-path (drop 5 (string/split file-path #"/"))
        base-url (str (extract-repo file-path) )
        sh-cmd-param (str "fetchArtifact " base-url " " (string/join "/" base-path))]
    sh-cmd-param))

(defn create-sh-params [jar]
  (let [extless-basename (second (re-find #"(.*).jar" (last (string/split jar #"/"))))
        file-extensions [".jar" ".jar.sha1" ".pom" ".pom.sha1"]
        sh-cmd-params (create-sh-cmd-params jar)
        sh-cmd-params-with-exts (str (string/join "\n"
                                                  (map #(str sh-cmd-params "/" %)
                                                       (map #(str extless-basename %) file-extensions))) "\n")]
    sh-cmd-params-with-exts))

(defn shell-params []
  (map #(create-sh-params %) resolved-deps))

(defn write-nix-expression []
  (spit "./.fetch-deps.sh"(str shell-function (apply str (shell-params)) "\n\nstopNest\n")))

(deftask nixos
  "Generates a NixOS Expression by enumerating over all this project's dependencies"
  []
  (with-pre-wrap fileset
                 (write-nix-expression)
                 fileset))
