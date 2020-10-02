#!/usr/bin/env bash
set -x -e +o pipefail

export DISABLE_COMPILATION=true



./.travis/deploy-pages.sh
