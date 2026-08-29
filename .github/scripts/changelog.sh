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

readonly CHANGELOGFILE="changelog.md"

if [ -f "$CHANGELOGFILE" ]; then
    mkdir -p mainfiles
    cp "$CHANGELOGFILE" "mainfiles/changelog.md" 2>/dev/null || true
    [ -d "webui/public" ] && cp "$CHANGELOGFILE" "webui/public/changelog.md" 2>/dev/null || true
    echo "✅ Success: $CHANGELOGFILE copied to module assets"
fi