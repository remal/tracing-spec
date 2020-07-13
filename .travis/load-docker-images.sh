#!/usr/bin/env bash
set -e +o pipefail

DIR="$HOME/.docker-cache"
FILE="$DIR/images.tar"
if [ ! -f "$FILE" ]; then
    exit 0
fi

echo docker load -i "$FILE"
docker load -i "$FILE"
