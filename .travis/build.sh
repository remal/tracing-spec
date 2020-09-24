#!/usr/bin/env bash
set -x -e +o pipefail

chmod -R 0777 .
./gradlew build runAllTests
./gradlew sonarqube -Pdisable-compilation=true


if [ -n "$TRAVIS_TAG" ]; then
    # building tag
    echo "Building tag"

elif [ -n "$TRAVIS_PULL_REQUEST" ] && [ "$TRAVIS_PULL_REQUEST" != "false" ]; then
    # building PR
    echo "Building PR"

else
    # building push
    echo "Building push"
fi
