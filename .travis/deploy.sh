#!/usr/bin/env bash
set -e +o pipefail

echo "Deploying..."

./.travis/deploy-pages.sh
