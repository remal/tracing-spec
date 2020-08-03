#!/usr/bin/env bash
set -e +o pipefail

chmod -R 0777 .
./gradlew build runAllTests sonarqube


if [ ! -z "$TRAVIS_TAG" ]; then
    # building tag
    echo "building tag"

elif [ -z "$TRAVIS_PULL_REQUEST" ] || [ "$TRAVIS_PULL_REQUEST" != "false" ]; then
    # building PR
    echo "building PR"

else
    # building push
    echo "building push"
fi
