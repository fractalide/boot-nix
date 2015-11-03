# boot-nix

A boot task to generate a Nix builder script.

This script will create a local maven repo inside the `/nix/store`, which contains
all the compile-time dependencies of the boot environment.

Boot projects which use tasks which try to download dependencies at runtime will fail.


## Usage

Include this boot task into your project:

```
(set-env!
  :dependencies '[[fractalide/boot-nix "0.1.0-SNAPSHOT"]])

(require
  '[fractalide.boot-nix :refer [nixos]])
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

## Example `app-name-m2-deps.nix`

```
{stdenv, curl}:

stdenv.mkDerivation {
  name = "app-name-m2-deps";
  builder = ./.fetch-deps.sh;

  outputHashAlgo = "sha256";
  outputHashMode = "recursive";
  outputHash = "03qjq481ly5ajynlr9iqvrjra5fvv2jz4wp2f3in5vnxa61inrrk";

  buildInputs = [ curl ];

  impureEnvVars = ["http_proxy" "https_proxy" "ftp_proxy" "all_proxy" "no_proxy"];
}

```

In your `default.nix` you should have something like this:

```
{ stdenv, lib, makeWrapper, curl, boot }:
let
  mavenRepo = import ./app-name-m2-deps.nix { inherit stdenv curl; };
in stdenv.mkDerivation rec {

  preConfigure = ''
    export BOOT_LOCAL_REPO="${mavenRepo}"
    ...
  ''
  ...
}

```
TODO support downloading deps behind a password wall
