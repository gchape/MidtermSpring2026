#!/usr/bin/env sh
set -eu

cd "$(dirname "$0")/.."

./mvnw clean compile

java -cp target/classes Main