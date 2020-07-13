#!/usr/bin/env bash
set -e +o pipefail

ARTIFACTS_DIR=./.artifacts
mkdir -p "$ARTIFACTS_DIR"

while read -r DIR; do
    PARENT=$(dirname "$DIR")
    PARENT_RELATIVE_PATH=${PARENT:2}
    PARENT_DIR="$ARTIFACTS_DIR/$PARENT_RELATIVE_PATH"
    mkdir -p "$PARENT_DIR"

    REAL_DIR=$(realpath "$DIR")
    REAL_PARENT_DIR=$(realpath "$PARENT_DIR")
    echo cp -r "$REAL_DIR" "$REAL_PARENT_DIR"
    cp  -r "$REAL_DIR" "$REAL_PARENT_DIR"
done < <(find . -name 'build' -type d)
