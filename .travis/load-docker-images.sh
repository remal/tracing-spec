#!/usr/bin/env sh
set -e

DIR="$HOME/.docker-cache"
FILE="$DIR/images.tar"
if [ ! -f "$FILE" ]; then
    exit 0
fi

echo docker load -i "$FILE"
docker load -i "$FILE"
