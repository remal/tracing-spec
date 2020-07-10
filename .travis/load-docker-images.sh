#!/usr/bin/env sh
set -e

if [ ! -d "$HOME/.docker-images" ]; then
    exit 0
fi

find "$HOME/.docker-images" -name '*.tar.gz' -type f | while read -r FILE; do
    sem --will-cite --id docker-load -j 8 "zcat '$FILE' | docker load; echo '    from $FILE'"
done

sem --will-cite --id docker-load --wait
