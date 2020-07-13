#!/usr/bin/env bash

set -e +o pipefail

DIR="$HOME/.docker-cache"
mkdir -p "$DIR"

FILE="$DIR/images.tar"
rm -rf "$FILE"

IMAGES=(`docker images -a --filter='dangling=false' --format '{{.Repository}}:{{.Tag}}' | sort`)
if [ ! -z "$IMAGES" ]; then
    echo docker save ${IMAGES[@]} \> "$FILE"
    docker save ${IMAGES[@]} > "$FILE"
fi
