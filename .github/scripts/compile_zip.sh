#!/bin/env bash
# shellcheck disable=SC2035

if [ -z "$GITHUB_WORKSPACE" ]; then
	echo "This script should only run on GitHub action!" >&2
	exit 1
fi

# Make sure we're on right directory
cd "$GITHUB_WORKSPACE" || {
	echo "Unable to cd to GITHUB_WORKSPACE" >&2
	exit 1
}

# Put critical files and folders here
need_integrity=(
	"mainfiles/system/bin"
	"mainfiles/libs"
	"mainfiles/META-INF"
	"mainfiles/service.sh"
	"mainfiles/preferenced-tweaks.sh"
	"mainfiles/post-fs-data.sh"
	"mainfiles/action.sh"
	"mainfiles/uninstall.sh"
	"mainfiles/module.prop"
	"mainfiles/module.banner.png"
	"mainfiles/wannApplist.json"
	"mainfiles/wann-hibernate.sh"
	"mainfiles/wann-memory.sh"
	"mainfiles/wann-fstrim.sh"
	"mainfiles/webroot"
)

# Version info
version="$(cat version)"
version_type="$(cat version_type | tr -d '\n\r ')"
version_code="$(git rev-list HEAD --count)"
release_code="$(git rev-list HEAD --count)-$(git rev-parse --short HEAD)-$version_type"
sed -i "s/version=.*/version=$version ($release_code)/" mainfiles/module.prop
sed -i "s/versionCode=.*/versionCode=$version_code/" mainfiles/module.prop

# Set Profile Folder untuk Rust berdasarkan version_type
RUST_PROFILE="release"
if [ "$version_type" == "experimental" ]; then
    RUST_PROFILE="debug"
fi
echo "Using Rust build profile: $RUST_PROFILE"

mkdir -p mainfiles/libs/arm64-v8a
mkdir -p mainfiles/system/bin

[ -d "libs" ] && cp -r libs/* mainfiles/libs/ 2>/dev/null
[ -d "archdaemon/libs" ] && cp -r archdaemon/libs/* mainfiles/libs/ 2>/dev/null

# Ambil binari Rust berdasarkan RUST_PROFILE (debug / release)
cp binprofiles/target/aarch64-linux-android/$RUST_PROFILE/azenith-profilesettings mainfiles/libs/arm64-v8a/sys.azenith-profilesettings 2>/dev/null || true
cp binutils/target/aarch64-linux-android/$RUST_PROFILE/azenith-utilityconf mainfiles/libs/arm64-v8a/sys.azenith-utilityconf 2>/dev/null || true

# Copy WebUI assets from webui/dist if present
if [ -d "webui/dist" ]; then
    mkdir -p mainfiles/webroot
    cp -r webui/dist/* mainfiles/webroot/
    echo "WebUI dist copied to mainfiles/webroot successfully."
fi

# Other Files
cp wannApplist.json mainfiles/ 2>/dev/null || cp azenithApplist.json mainfiles/wannApplist.json 2>/dev/null
cp LICENSE mainfiles/ 2>/dev/null
cp NOTICE.md mainfiles/ 2>/dev/null

# Parse version info to module prop
zipName="Wann-WebUI-$version-$release_code.zip"
echo "zipName=$zipName" >>"$GITHUB_OUTPUT"
artifactName="${zipName%.zip}"
echo "artifactName=$artifactName" >>"$GITHUB_OUTPUT"

# Generate sha256sum for integrity checkup
for file in "${need_integrity[@]}"; do
	bash .github/scripts/generatesha256.sh "$file"
done

# Zip the file
cd ./mainfiles || {
	echo "Unable to cd to ./mainfiles" >&2
	exit 1
}

zip -r9 ../"$zipName" * -x *placeholder* *.map .shellcheckrc
zip -z ../"$zipName" <<EOF
$version-$release_code
Wann Optimizer (MD3 WebUI)
Build Date $(date +"%a %b %d %H:%M:%S %Z %Y")
EOF
