#
# Copyright (C) 2026-2027 Zexshia & wann
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

SKIPUNZIP=1

# Paths
MODULE_CONFIG="/data/adb/.config/AZenith"
device_codename=$(getprop ro.product.board)
chip=$(getprop ro.hardware)
HM_DIR="/data/adb/hybrid-mount"
HM_CONFIG="$HM_DIR/config.toml"
API_LEVEL=$(getprop ro.build.version.sdk)

abort_api() {
  echo ""
  echo "! Installation Aborted"
  echo "! Unsupported Android Version Detected"
  echo "! AZenith requires Android 10 (API 29) or newer."
  abort "! Your device is currently running API $API_LEVEL."
}

abort_corrupted() {
  clear
  echo ""
  echo "! Installation Aborted"
  echo "! The AZenith package appears to be corrupted or incomplete."
  echo "! Required installation files were not found."
  echo ""
  abort "! Please re-download the module and try again."
}

abort_arch() {
  clear
  echo "! Installation Aborted"
  echo "! Unsupported CPU Architecture Detected"
  echo "! Your device architecture is not compatible with this build of AZenith."
  echo "! Supported architectures:"
  echo "  • arm64-v8a"
  abort "  • armeabi-v7a"
}

installation_complete() {
  echo "- AZenith WebUI Edition successfully installed"
  echo "- Open WebUI via KernelSU / APatch / MMRL / WebUI X"
  echo "- Thank you for using AZenith Remake!"
  echo "- Please reboot your device."
}

# Display banner
echo ""
echo "       AZenith WebUI Remake       "
echo ""
echo "- Installing AZenith..."

# API Level Check (Require API 29+)
[ "$API_LEVEL" -lt 29 ] && abort_api

# Extract Module Directories
mkdir -p "$MODULE_CONFIG"
mkdir -p "$MODULE_CONFIG/debug"
mkdir -p "$MODULE_CONFIG/API"
mkdir -p "$MODULE_CONFIG/preload"
mkdir -p "$MODULE_CONFIG/bypasschgconfig"
mkdir -p "$MODULE_CONFIG/gamelist"
mkdir -p "$MODULE_CONFIG/mem"
mkdir -p "$MODULE_CONFIG/eco"
mkdir -p "$MODULE_CONFIG/maint"
mkdir -p "$MODPATH/system/bin"
mkdir -p "$MODPATH/webroot"
echo "- Initialized module config directories"

# Flashable integrity checkup
echo "- Extracting verify.sh..."
unzip -o "$ZIPFILE" 'verify.sh' -d "$TMPDIR" >&2
[ ! -f "$TMPDIR/verify.sh" ] && abort_corrupted
source "$TMPDIR/verify.sh"

# Target architecture detection
case $ARCH in
"arm64") ARCH_TMP="arm64-v8a" ;;
"arm") ARCH_TMP="armeabi-v7a" ;;
*) abort_arch ;;
esac

echo "- Extracting binaries for $ARCH_TMP..."
extract "$ZIPFILE" "libs/$ARCH_TMP/sys.azenith-service" "$TMPDIR"
extract "$ZIPFILE" "libs/$ARCH_TMP/sys.azenith-profilesettings" "$TMPDIR"
extract "$ZIPFILE" "libs/$ARCH_TMP/sys.azenith-utilityconf" "$TMPDIR"
cp "$TMPDIR/libs/$ARCH_TMP/"* "$MODPATH/system/bin/"
rm -rf "$TMPDIR/libs"
echo "- All binaries installed successfully"

# Extract Module standard files
echo "- Extracting service.sh..."
extract "$ZIPFILE" service.sh "$MODPATH"
echo "- Extracting helper scripts..."
extract "$ZIPFILE" azenith-hibernate.sh "$MODPATH"
[ -f "$MODPATH/azenith-hibernate.sh" ] || abort_corrupted
extract "$ZIPFILE" azenith-memory.sh "$MODPATH"
[ -f "$MODPATH/azenith-memory.sh" ] || abort_corrupted
extract "$ZIPFILE" azenith-fstrim.sh "$MODPATH"
[ -f "$MODPATH/azenith-fstrim.sh" ] || abort_corrupted
echo "- Helper scripts installed"

echo "- Extracting post-fs-data.sh..."
extract "$ZIPFILE" post-fs-data.sh "$MODPATH"
echo "- Extracting action.sh..."
extract "$ZIPFILE" action.sh "$MODPATH"
echo "- Extracting preferenced-tweaks.sh..."
extract "$ZIPFILE" preferenced-tweaks.sh "$MODPATH"
echo "- Extracting module.prop..."
extract "$ZIPFILE" module.prop "$MODPATH"
cp "$MODPATH/module.prop" "$MODPATH/module.prop.orig"
echo "- Extracting uninstall.sh..."
extract "$ZIPFILE" uninstall.sh "$MODPATH"

if [ ! -f "$MODULE_CONFIG/gamelist/azenithApplist.json" ]; then
    echo "- Extracting default gamelist..."
    extract "$ZIPFILE" azenithApplist.json "$MODULE_CONFIG/gamelist"
fi

echo "- Extracting module banner..."
extract "$ZIPFILE" module.banner.avif "$MODPATH"

# Extract WebUI files (webroot)
echo "- Extracting WebUI interface..."
unzip -o "$ZIPFILE" 'webroot/*' -d "$MODPATH" >&2
if [ -d "$MODPATH/webroot" ]; then
    echo "- WebUI assets installed successfully"
    set_perm_recursive "$MODPATH/webroot" 0 0 0755 0644
fi

# Skip mountify
touch "$MODPATH/skip_mountify"

# Skip hybrid mount
if [ -f "$HM_CONFIG" ]; then
    echo "- Hybrid Mount detected, configuring rules..."
    HM_BIN=""
    if [ -x "/data/adb/modules/hybrid_mount/hybrid-mount" ]; then
        HM_BIN="/data/adb/modules/hybrid_mount/hybrid-mount"
    elif command -v hybrid-mount >/dev/null 2>&1; then
        HM_BIN="hybrid-mount"
    fi

    if [ -n "$HM_BIN" ]; then
        $HM_BIN api config-patch --patch '{"rules":{"AZenith":{"default_mode":"ignore"}}}' --apply-runtime >/dev/null 2>&1
    fi

    if ! grep -q "\[rules\.AZenith\]" "$HM_CONFIG"; then
        echo "" >> "$HM_CONFIG"
        echo "[rules.AZenith]" >> "$HM_CONFIG"
        echo 'default_mode = "ignore"' >> "$HM_CONFIG"
    fi
fi

# Symlinks for APatch / KernelSU
if [ "$KSU" = "true" ] || [ "$APATCH" = "true" ]; then
	touch "$MODPATH/skip_mount"
	manager_paths="/data/adb/ap/bin /data/adb/ksu/bin"
	BIN_PATH="/data/adb/modules/AZenith/system/bin"
	for dir in $manager_paths; do
		[ -d "$dir" ] && {
			ln -sf "$BIN_PATH/sys.azenith-service" "$dir/sys.azenith-service"
			ln -sf "$BIN_PATH/sys.azenith-service" "$dir/zx"
			ln -sf "$BIN_PATH/sys.azenith-profilesettings" "$dir/sys.azenith-profilesettings"
			ln -sf "$BIN_PATH/sys.azenith-utilityconf" "$dir/sys.azenith-utilityconf"
		}
	done
fi

# Apply Tweaks Based on Chipset
echo "- Checking device SoC..."
chipset=$(grep -i 'hardware' /proc/cpuinfo | uniq | cut -d ':' -f2 | sed 's/^[ \t]*//')
[ -z "$chipset" ] && chipset="$(getprop ro.board.platform) $(getprop ro.hardware)"
case "$(echo "$chipset" | tr '[:upper:]' '[:lower:]')" in
*mt* | *MT*)
	soc="MediaTek"
	setprop persist.sys.azenith.soctype 1
	;;
*sm* | *qcom* | *SM* | *QCOM* | *Qualcomm* | *sdm* | *snapdragon*)
	soc="Snapdragon"
	setprop persist.sys.azenith.soctype 2
	;;
*exynos* | *Exynos* | *EXYNOS* | *universal* | *samsung* | *erd* | *s5e*)
	soc="Exynos"
	setprop persist.sys.azenith.soctype 3
	;;
*Unisoc* | *unisoc* | *ums*)
	soc="Unisoc"
	setprop persist.sys.azenith.soctype 4
	;;
*gs* | *Tensor* | *tensor*)
	soc="Tensor"
	setprop persist.sys.azenith.soctype 5
	;;
*)
	soc="Unknown"
	setprop persist.sys.azenith.soctype 0
	;;
esac

# Set default renderer
if [ -z "$(getprop persist.sys.azenithconf.renderer)" ]; then
	setprop persist.sys.azenithconf.renderer "skiaglthreaded"
fi

# Daemon Configurations
if [ -z "$(getprop persist.sys.azenithconf.showtoast)" ]; then
	setprop persist.sys.azenithconf.showtoast 1
fi

if [ -z "$(getprop persist.sys.azenithconf.AIenabled)" ]; then
    setprop persist.sys.azenithconf.AIenabled 1
    echo 1 > "$MODULE_CONFIG/API/current_modes"
fi

setprop persist.sys.azenith.debugmode "false"

# Set config properties
props="
persist.sys.azenithconf.logd
persist.sys.azenithconf.cpulimit
persist.sys.azenithconf.dnd
"
for prop in $props; do
	curval=$(getprop "$prop")
	if [ -z "$curval" ]; then
		setprop "$prop" 0
	fi
done

# Permissions
set_perm_recursive "$MODPATH/system/bin" 0 0 0755 0755
set_perm "$MODPATH/azenith-hibernate.sh" 0 0 0755
set_perm "$MODPATH/azenith-memory.sh" 0 0 0755
set_perm "$MODPATH/azenith-fstrim.sh" 0 0 0755
set_perm "$MODPATH/action.sh" 0 0 0755
set_perm "$MODPATH/service.sh" 0 0 0755
set_perm "$MODPATH/post-fs-data.sh" 0 0 0755
set_perm "$MODPATH/uninstall.sh" 0 0 0755

# RN9 Defaults
ECO_DIR="/data/adb/.config/AZenith/eco"
mkdir -p "$ECO_DIR"
[ -f "$ECO_DIR/enabled" ] || echo 1 > "$ECO_DIR/enabled"
[ -f "$ECO_DIR/delay" ] || echo 300 > "$ECO_DIR/delay"
[ -f "$ECO_DIR/mode.default" ] || echo full > "$ECO_DIR/mode.default"
[ -f "$ECO_DIR/skip_charging" ] || echo 1 > "$ECO_DIR/skip_charging"
[ -f "$ECO_DIR/skip_audio" ] || echo 1 > "$ECO_DIR/skip_audio"

if [ ! -f "$ECO_DIR/hibernate.list" ]; then
	cat >"$ECO_DIR/hibernate.list" <<'HIBEOF'
# Paket aplikasi yang dibekukan saat layar mati (1 per baris)
#com.facebook.katana
#com.instagram.android
#com.shopee.id
#com.zhiliaoapp.musically
HIBEOF
fi

# Memory defaults
MEM_DIR="/data/adb/.config/AZenith/mem"
mkdir -p "$MEM_DIR"
[ -f "$MEM_DIR/enabled" ] || echo 1 > "$MEM_DIR/enabled"
[ -f "$MEM_DIR/zram_mb" ] || echo 2048 > "$MEM_DIR/zram_mb"
[ -f "$MEM_DIR/swappiness" ] || echo 140 > "$MEM_DIR/swappiness"
[ -f "$MEM_DIR/algo" ] || echo lz4 > "$MEM_DIR/algo"

# FSTRIM defaults
MAINT_DIR="/data/adb/.config/AZenith/maint"
mkdir -p "$MAINT_DIR"
[ -f "$MAINT_DIR/fstrim_enabled" ] || echo 1 > "$MAINT_DIR/fstrim_enabled"
[ -f "$MAINT_DIR/fstrim_interval" ] || echo 86400 > "$MAINT_DIR/fstrim_interval"

installation_complete
