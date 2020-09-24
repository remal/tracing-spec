#!/usr/bin/env bash
set -x -e +o pipefail

echo "Deploying..."

./.travis/deploy-pages.sh
