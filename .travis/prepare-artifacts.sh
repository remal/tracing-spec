#!/usr/bin/env bash
set -x -e +o pipefail

ARTIFACTS_FILE=./.artifacts.zip

# shellcheck disable=SC2207
FILES=($(find . -name 'build' -type d | sort))
if [ ${#FILES[@]} -ge 1 ]; then
    echo zip -r -9 "$ARTIFACTS_FILE" "${FILES[@]}"
    zip -r -9 "$ARTIFACTS_FILE" "${FILES[@]}" > /dev/null
fi
