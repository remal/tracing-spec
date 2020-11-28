#!/usr/bin/env bash
set -x -e +o pipefail

if [ -z "$TRAVIS_TAG" ] && [[ "$TRAVIS_COMMIT_MESSAGE" == "[push-back]"* ]]; then
    echo "Skip building push-back commits"
    exit
fi


chmod -R 0777 .
./gradlew allClasses

export DISABLE_COMPILATION=true
./gradlew build
./gradlew runAllTests

export DISABLE_JAR_TASKS=true
./gradlew sonarqube


if [ -n "$TRAVIS_TAG" ]; then
    echo "Tag has been built"

elif [ -n "$TRAVIS_PULL_REQUEST" ] && [ "$TRAVIS_PULL_REQUEST" != "false" ]; then
    echo "PR has been built"

elif [ -n "$TRAVIS_BRANCH" ]; then
    echo "Push has been built"

    if [ "$TRAVIS_REPO_SLUG" == "remal/tracing-spec" ]; then
        git add --all

        ret=0
        git commit --no-status -o -m "[push-back] Update IDEA settings" .idea || ret=$?
        if [ $ret -eq 0 ]; then
            git remote set-url origin "https://${GITHUB_TOKEN}@github.com/$TRAVIS_REPO_SLUG.git"
            git push origin "HEAD:$TRAVIS_BRANCH"
        fi
        git commit --no-status -o -m "[push-back] Update documentation" README.md example-graph.png || ret=$?
        if [ $ret -eq 0 ]; then
            git remote set-url origin "https://${GITHUB_TOKEN}@github.com/$TRAVIS_REPO_SLUG.git"
            git push origin "HEAD:$TRAVIS_BRANCH"
        fi
    fi
fi
