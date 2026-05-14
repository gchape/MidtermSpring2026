#!/usr/bin/env sh
set -eu

./mvnw compile -q
java --enable-preview \
     -cp target/classes \
     Main "$@"
