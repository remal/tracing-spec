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
cp ./tracing-spec-application/libs/tracing-spec-application-*-fatjar.jar "$RELEASE_ASSET"

VERSION=${TRAVIS_TAG//ver-/}
curl --data-binary @"$RELEASE_ASSET" -H "Authorization: token $GITHUB_TOKEN" -H "Content-Type: application/octet-stream" "https://uploads.github.com/repos/$TRAVIS_REPO_SLUG/releases/$VERSION/assets?name=$RELEASE_ASSET_NAME"

./.travis/deploy-pages.sh
