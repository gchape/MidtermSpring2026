#!/usr/bin/env sh
# Deterministic smoke check: bot-only game with a fixed seed.
# Asserts that at least one player finishes with a non-zero score.
set -eu

cd "$(dirname "$0")/.."

mvn -q compile

OUTPUT=$(java --enable-preview -cp target/classes Main --quiet --seed 42 --games 3)

echo "$OUTPUT"

# The final scores block always ends with "PlayerName: N".
if echo "$OUTPUT" | grep -qE ': [1-9][0-9]*$'; then
    echo "SMOKE OK: at least one non-zero score found."
    exit 0
else
    echo "SMOKE FAIL: all scores are zero — no game completed with a winner." >&2
    exit 1
fi