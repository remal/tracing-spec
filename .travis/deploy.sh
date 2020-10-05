#!/usr/bin/env bash
set -x -e +o pipefail

export DISABLE_COMPILATION=true
export DISABLE_JAR_TASKS=true

#./retry ./gradlew publishToOssrh
#./retry ./gradlew releaseNexusRepositories

RELEASE_ASSET=./build/tracing-spec-app.jar
RELEASE_ASSET_DIR=$(dirname $RELEASE_ASSET)
RELEASE_ASSET_NAME=$(basename $RELEASE_ASSET)
mkdir -p "$RELEASE_ASSET_DIR"
cp ./tracing-spec-application/build/libs/tracing-spec-application-*-fatjar.jar "$RELEASE_ASSET"
gh release upload "$TRAVIS_TAG" "$RELEASE_ASSET"

./.travis/deploy-pages.sh
