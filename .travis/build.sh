#!/usr/bin/env bash
set -e +o pipefail

chmod -R 0777 .
./gradlew build
./gradlew runAllTests
./gradlew sonarqube


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
