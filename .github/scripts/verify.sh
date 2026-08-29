#!/usr/bin/env bash

set -euo pipefail

if [ -z "${GITHUB_WORKSPACE:-}" ]; then
	echo "❌ Error: This script should only run on GitHub Actions!" >&2
	exit 1
fi

cd "$GITHUB_WORKSPACE" || {
	echo "❌ Error: Unable to cd to GITHUB_WORKSPACE" >&2
	exit 1
}

readonly HEADER_FILE="archdaemon/jni/include/AZenith.h"
readonly MODULE_PROP="mainfiles/module.prop"

[ -f "version" ] || { echo "❌ Error: 'version' file not found!"; exit 1; }
[ -f "version_type" ] || { echo "❌ Error: 'version_type' file not found!"; exit 1; }

readonly VERSION=$(cat version)
readonly VERSION_TYPE=$(cat version_type | tr -d '\n\r ')
readonly VERSION_CODE=$(git rev-list HEAD --count)
readonly SHORT_HASH=$(git rev-parse --short HEAD)
readonly RELEASE_CODE="${VERSION_CODE}-${SHORT_HASH}-${VERSION_TYPE}"
readonly FULL_VERSION="${VERSION} (${RELEASE_CODE})"

echo "Starting version injection..."
echo "Target Version: $FULL_VERSION"
echo "Version Code  : $VERSION_CODE"

if [ -f "$HEADER_FILE" ]; then
	sed -i "s|#define MODULE_VERSION \".*\"|#define MODULE_VERSION \"$FULL_VERSION\"|" "$HEADER_FILE"
fi

if [ -f "$MODULE_PROP" ]; then
	sed -i "s/version=.*/version=$FULL_VERSION/" "$MODULE_PROP"
	sed -i "s/versionCode=.*/versionCode=$VERSION_CODE/" "$MODULE_PROP"
fi

echo "✅ Injection complete! Verifying changes:"
echo "---------------------------------------------------"
[ -f "$MODULE_PROP" ] && grep -H "version" "$MODULE_PROP"
[ -f "$HEADER_FILE" ] && grep -H "MODULE_VERSION" "$HEADER_FILE"
echo "---------------------------------------------------"
