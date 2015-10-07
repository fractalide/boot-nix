# boot-nix

A boot task to generate a Nix builder script.

This script will create a local maven repo inside the `/nix/store`, which contains
all the compile-time dependencies of the boot environment.

Boot projects which use tasks which try to download dependencies at runtime will fail.


## Usage

Include this boot task into your project:

```
(set-env!
  :dependencies '[[exicon/boot-nix "0.1.0-SNAPSHOT"]])

(require
  '[exicon.boot-nix :refer [nixos]])
```

Run

```
boot nixos
```

It should generate a `./.fetch-deps.sh` script which can be included in to your nix builder file.

To install into the local maven repo, you can use

```
boot build-jar
```


TODO provide example nix expression to show usage of the generated script
