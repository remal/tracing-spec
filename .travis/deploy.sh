#!/usr/bin/env bash
set -x -e +o pipefail

export DISABLE_COMPILATION=true
export DISABLE_JAR_TASKS=true

./retry ./gradlew publishToOssrh
./retry ./gradlew releaseNexusRepositories

./.travis/deploy-pages.sh
