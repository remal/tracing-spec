#!/usr/bin/env bash
set -x -e +o pipefail

chmod -R 0777 .
./gradlew allClasses

export DISABLE_COMPILATION=true
./gradlew build
./gradlew runAllTests

export DISABLE_JAR_TASKS=true
./gradlew sonarqube


git add --all

if [ -n "$TRAVIS_TAG" ]; then
    echo "Tag has been built"

elif [ -n "$TRAVIS_PULL_REQUEST" ] && [ "$TRAVIS_PULL_REQUEST" != "false" ]; then
    echo "PR has been built"

elif [ -n "$TRAVIS_BRANCH" ]; then
    echo "Push has been built"

    if [ "$TRAVIS_REPO_SLUG" == "remal/tracing-spec" ]; then
        ret=0
        git commit --short -o -m "[skip ci] Update README" README.md example-graph.png || ret=$?
        if [ $ret -eq 0 ]; then
            git remote set-url origin "https://${GITHUB_TOKEN}@github.com/remal/tracing-spec.git"
            git push origin "HEAD:$TRAVIS_BRANCH"
        fi
    fi
fi
