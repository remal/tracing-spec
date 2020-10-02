#!/usr/bin/env bash
set -x -e +o pipefail

chmod -R 0777 .
./gradlew clean
./gradlew allClasses

export DISABLE_COMPILATION=true
./gradlew build
#./gradlew runAllTests
./gradlew sonarqube

git add --all

if [ -n "$TRAVIS_TAG" ]; then
    # building tag
    echo "Building tag"

elif [ -n "$TRAVIS_PULL_REQUEST" ] && [ "$TRAVIS_PULL_REQUEST" != "false" ]; then
    # building PR
    echo "Building PR"

else
    # building push
    echo "Building push"

    if [ "$TRAVIS_REPO_SLUG" == "remal/tracing-spec" ]; then
        ret=0
        git commit README.md -m "[skip ci] Update README" || ret=$?
        if [ $ret -eq 0 ]; then
            git remote set-url origin "https://${GITHUB_TOKEN}@github.com/remal/tracing-spec.git"
            git push
        fi
    fi
fi

git status
