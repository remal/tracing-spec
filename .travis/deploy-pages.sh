#!/usr/bin/env bash
set -e +o pipefail

if [ -z "$TRAVIS_TAG" ] && [ "$TRAVIS_BRANCH" != "test-pages" ]; then
    echo "Skip updating GitHub pages"
    exit
fi

echo "Updating GitHub pages..."

PROJECT_SLUG=tracing-spec

PAGES_ROOT_DIR=./.gh-pages
PROJECT_PAGES_DIR="${PAGES_ROOT_DIR:?}/${PROJECT_SLUG:?}"

echo "Cloning 'https://github.com/remal/remal.github.io.git'..."
git clone "https://${GITHUB_TOKEN}@github.com/remal/remal.github.io.git" "$PAGES_ROOT_DIR"

#git -C "$PAGES_ROOT_DIR" config --global user.email "travis@travis-ci.org"
#git -C "$PAGES_ROOT_DIR" config --global user.name "Travis CI"
ls

echo "Updating content"
rm -rf "${PROJECT_PAGES_DIR:?}"
mkdir -p "$PROJECT_PAGES_DIR"
cp -r "./docs/*" "$PROJECT_PAGES_DIR"

git -C "$PAGES_ROOT_DIR" add --all

if git -C "$PAGES_ROOT_DIR" diff-index --quiet HEAD; then
    echo "Pushing changes"
    git -C "$PAGES_ROOT_DIR" commit --all -m "Update pages for $PROJECT_SLUG"
    git -C "$PAGES_ROOT_DIR" push
else
    echo "Nothing changed"
fi
