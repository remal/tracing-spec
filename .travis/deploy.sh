#!/usr/bin/env bash
set -x -e +o pipefail

export DISABLE_COMPILATION=true
export DISABLE_JAR_TASKS=true

#./retry ./gradlew publishToOssrh
#./retry ./gradlew releaseNexusRepositories

RELEASE_ASSET=./build/tracing-spec-app.jar
gh release upload "$TRAVIS_TAG" "$RELEASE_ASSET"

./.travis/deploy-pages.sh
