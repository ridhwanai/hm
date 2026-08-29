#!/system/bin/sh

#
# Copyright (C) 2026-2027 wann
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

readonly MODDIR="${0%/*}"
readonly MODULE_CONFIG="/data/adb/.config/wann"
readonly LEGACY_CONFIG="/data/adb/.config/AZenith"
readonly BIN_SVC="$MODDIR/system/bin/sys.azenith-service"

# Wait boot to complete
until [ "$(getprop sys.boot_completed)" = "1" ]; do 
    sleep 1
done

# Ensure config directories and compatibility symlink
mkdir -p "$MODULE_CONFIG"
mkdir -p "$MODULE_CONFIG/debug"
mkdir -p "$MODULE_CONFIG/API"
mkdir -p "$MODULE_CONFIG/gamelist"
mkdir -p "$MODULE_CONFIG/mem"
mkdir -p "$MODULE_CONFIG/eco"
mkdir -p "$MODULE_CONFIG/maint"

# Legacy compatibility symlink so older paths still work
[ -d "$LEGACY_CONFIG" ] || ln -sf "$MODULE_CONFIG" "$LEGACY_CONFIG"

# Reset anti bootloop
echo "BOOTCOUNT=0" > "$MODDIR/count.sh"

# Clear Old Logs
[ -x "$BIN_SVC" ] && "$BIN_SVC" --clearlogs

# Remove reboot flag
if [ -f "$MODDIR/reboot" ]; then
    rm -f "$MODDIR/reboot"
fi

# Refresh Wann daemon state
STATE=$(getprop persist.sys.wann.state)
[ -z "$STATE" ] && STATE=$(getprop persist.sys.azenith.state)
{ [ -z "$STATE" ] || { [ "$STATE" = "running" ] && [ -z "$(/system/bin/toybox pidof sys.azenith-service)" ]; }; } && {
    setprop persist.sys.wann.state stopped
    setprop persist.sys.azenith.state stopped
}

# Wann: hibernasi layar-mati
if [ -f "$MODDIR/wann-hibernate.sh" ]; then
    chmod 0755 "$MODDIR/wann-hibernate.sh"
    [ -f "$MODULE_CONFIG/eco/enabled" ] || echo 1 > "$MODULE_CONFIG/eco/enabled"
    [ -f "$MODULE_CONFIG/eco/delay" ] || echo 300 > "$MODULE_CONFIG/eco/delay"
    [ -f "$MODULE_CONFIG/eco/hibernate.list" ] || : > "$MODULE_CONFIG/eco/hibernate.list"
    [ -f "$MODULE_CONFIG/eco/mode.default" ] || echo full > "$MODULE_CONFIG/eco/mode.default"
    [ -f "$MODULE_CONFIG/eco/skip_charging" ] || echo 1 > "$MODULE_CONFIG/eco/skip_charging"
    [ -f "$MODULE_CONFIG/eco/skip_audio" ] || echo 1 > "$MODULE_CONFIG/eco/skip_audio"
    sh "$MODDIR/wann-hibernate.sh" boot-reset >/dev/null 2>&1
    nohup sh "$MODDIR/wann-hibernate.sh" watch >/dev/null 2>&1 &
fi

# Wann: tuning memori (ZRAM + swappiness) async setelah boot
if [ -f "$MODDIR/wann-memory.sh" ]; then
    chmod 0755 "$MODDIR/wann-memory.sh"
    [ -f "$MODULE_CONFIG/mem/enabled" ] || echo 1 > "$MODULE_CONFIG/mem/enabled"
    [ -f "$MODULE_CONFIG/mem/swappiness" ] || echo 140 > "$MODULE_CONFIG/mem/swappiness"
    [ -f "$MODULE_CONFIG/mem/algo" ] || echo lz4 > "$MODULE_CONFIG/mem/algo"
    nohup sh "$MODDIR/wann-memory.sh" apply >/dev/null 2>&1 &
fi

# Wann: fstrim terjadwal async setelah boot
if [ -f "$MODDIR/wann-fstrim.sh" ]; then
    chmod 0755 "$MODDIR/wann-fstrim.sh"
    [ -f "$MODULE_CONFIG/maint/fstrim_enabled" ] || echo 1 > "$MODULE_CONFIG/maint/fstrim_enabled"
    [ -f "$MODULE_CONFIG/maint/fstrim_delay" ] || echo 300 > "$MODULE_CONFIG/maint/fstrim_delay"
    [ -f "$MODULE_CONFIG/maint/fstrim_interval" ] || echo 86400 > "$MODULE_CONFIG/maint/fstrim_interval"
    nohup sh "$MODDIR/wann-fstrim.sh" watch >/dev/null 2>&1 &
fi

# Wann: pastikan dynamic profile (auto mode) default ON
if [ ! -s "$MODULE_CONFIG/API/current_modes" ]; then
    AISTATE=$(getprop persist.sys.wannconf.AIenabled)
    [ -z "$AISTATE" ] && AISTATE=$(getprop persist.sys.azenithconf.AIenabled)
    case "$AISTATE" in
        0) 
            echo 0 > "$MODULE_CONFIG/API/current_modes" 
            ;;
        *)
            echo 1 > "$MODULE_CONFIG/API/current_modes"
            setprop persist.sys.wannconf.AIenabled 1
            setprop persist.sys.azenithconf.AIenabled 1
            ;;
    esac
fi

# Wann: terapkan renderer tersimpan saat boot
RENDERER=$(getprop persist.sys.wannconf.renderer)
[ -z "$RENDERER" ] && RENDERER=$(getprop persist.sys.azenithconf.renderer)
case "$RENDERER" in
    "" | Default | default) : ;;
    *)
        [ -x "$MODDIR/system/bin/sys.azenith-utilityconf" ] && \
            nohup "$MODDIR/system/bin/sys.azenith-utilityconf" setrender "$RENDERER" >/dev/null 2>&1 &
        ;;
esac

# Run Wann daemon service
sleep 1 && exec "$BIN_SVC" --run
