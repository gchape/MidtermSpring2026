#!/usr/bin/env sh
set -eu

cd "$(dirname "$0")/.."

./mvnw -q compile exec:java -Dexec.args="$*"