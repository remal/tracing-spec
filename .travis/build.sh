#!/usr/bin/env bash
set -x -e +o pipefail

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

chmod -R 0777 .
./gradlew clean
#./gradlew allClasses
#export DISABLE_COMPILATION=true
#./gradlew build
#./gradlew runAllTests
#./gradlew sonarqube
