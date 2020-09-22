#!/usr/bin/env bash
set -e +o pipefail

PROJECT_SLUG=tracing-spec

PAGES_ROOT_DIR=./.gh-pages
PROJECT_PAGES_DIR="${PAGES_ROOT_DIR:?}/${PROJECT_SLUG:?}"

echo "Cloning 'https://github.com/remal/remal.github.io.git' to '$PAGES_ROOT_DIR'..."
git clone "https://${GITHUB_TOKEN}@github.com/remal/remal.github.io.git" "$PAGES_ROOT_DIR"

echo "Updating content"
rm -rf "${PROJECT_PAGES_DIR:?}/*"
cp -r "./docs/*" "$PROJECT_PAGES_DIR"

git add --all 
