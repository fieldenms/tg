#!/usr/bin/env sh

## This script can be used to perform a quick experimental run of benchmarks.
## For example, to identify runtime errors early on.

java -jar target/benchmarks.jar -i 1 -r 0 -wi 0 -foe true -f 0 -t 1 "$@"
