#!/usr/bin/env sh
set -e

mkdir -p $HOME/.docker-images

docker images -a --filter='dangling=false' --format '{{.Repository}}:{{.Tag}}' | while read IMAGE; do
    ESCAPED_IMAGE=`echo $IMAGE | sed 's#/#:#g'`
    FILE=$HOME/.docker-images/$ESCAPED_IMAGE.tar.gz
    sem --will-cite --id docker-save -j 8 "docker save $IMAGE | gzip -9 > $FILE; echo Saved $IMAGE to $FILE"
done

sem --will-cite --id docker-save --wait
