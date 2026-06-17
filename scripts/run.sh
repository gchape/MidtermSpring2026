#!/usr/bin/env sh
set -eu

cd "$(dirname "$0")/.."

./mvnw clean compile

./mvnw -q exec:java