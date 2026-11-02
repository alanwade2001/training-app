#!/usr/bin/env bash

set -euo pipefail

if [[ $# -ne 2 ]]; then
    echo "Usage: ./release.sh <version> <commit-message>"
    exit 1
fi

VERSION="$1"
MESSAGE="$2"

if [[ ! "$VERSION" =~ ^v[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    echo "Invalid version: $VERSION"
    echo "Expected format: v4.0.3"
    exit 1
fi

if git rev-parse "$VERSION" >/dev/null 2>&1; then
    echo "Tag $VERSION already exists."
    exit 1
fi

if ! git diff --quiet || ! git diff --cached --quiet; then
    echo "Working tree contains uncommitted changes."
fi

echo "Running tests..."
./gradlew clean test

echo "Committing release..."
git add .
git commit -m "$MESSAGE"

echo "Creating tag $VERSION..."
git tag "$VERSION"

echo
echo "Release $VERSION created successfully."
echo
echo "Push with:"
echo "  git push"
echo "  git push origin $VERSION"