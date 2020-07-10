#!/usr/bin/env sh
set -e

if [ ! -d $HOME/.docker-images ]; then
    exit 0
fi

ls $HOME/.docker-images/*.tar.gz | while read FILE; do
    sem --will-cite --id docker-load -j 8 "zcat $FILE | docker load; echo Loaded $FILE"
done

sem --will-cite --id docker-load --wait
