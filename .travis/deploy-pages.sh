#!/usr/bin/env bash
set -e +o pipefail

PROJECT_SLUG=tracing-spec
DOCS_DIR=docs

if [ -z "$TRAVIS_TAG" ] && [ "$TRAVIS_BRANCH" != "master" ]; then
    echo "Skip updating GitHub pages"
    exit
fi

set -x

PAGES_ROOT_DIR=.gh-pages
rm -rf "${PAGES_ROOT_DIR:?}"
PROJECT_PAGES_DIR="${PAGES_ROOT_DIR:?}/${PROJECT_SLUG:?}"

echo "Cloning 'https://github.com/remal/remal.github.io.git'..."
git clone "https://${GITHUB_TOKEN}@github.com/remal/remal.github.io.git" "$PAGES_ROOT_DIR"

#git -C "$PAGES_ROOT_DIR" config --global user.email "travis@travis-ci.org"
#git -C "$PAGES_ROOT_DIR" config --global user.name "Travis CI"

echo "Updating content"
rm -rf "${PROJECT_PAGES_DIR:?}"
mkdir -p "$PROJECT_PAGES_DIR"
cp -r "$DOCS_DIR"/* "$PROJECT_PAGES_DIR"

git -C "$PAGES_ROOT_DIR" add --all

ret=0
git -C "$PAGES_ROOT_DIR" diff-index HEAD || ret=$?
if [ $ret -eq 0 ]; then
    echo "Pushing changes"
    git -C "$PAGES_ROOT_DIR" commit --all -m "Update pages for $PROJECT_SLUG"
    git -C "$PAGES_ROOT_DIR" push
else
    echo "Nothing changed"
fi
