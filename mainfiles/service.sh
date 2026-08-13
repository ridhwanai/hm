#!/system/bin/sh

#
# Copyright (C) 2026-2027 Zexshia
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
readonly MODULE_CONFIG="/data/adb/.config/AZenith"
readonly BIN_SVC="$MODDIR/system/bin/sys.azenith-service"
readonly APK_COMP="$MODDIR/AZenith.apk"

# Wait boot to complete
until [ "$(getprop sys.boot_completed)" = "1" ]; do 
    sleep 1
done

# Reset anti bootloop
echo "BOOTCOUNT=0" > "$MODDIR/count.sh"

# Clear Old Logs
"$BIN_SVC" --clearlogs

# Remove reboot flag
if [ -f "$MODDIR/reboot" ]; then
    rm -f "$MODDIR/reboot"
fi

# Refresh AZenith daemon state
STATE=$(getprop persist.sys.azenith.state)
{ [ -z "$STATE" ] || { [ "$STATE" = "running" ] && [ -z "$(/system/bin/toybox pidof sys.azenith-service)" ]; }; } && {
    setprop persist.sys.azenith.state stopped
    setprop persist.sys.azenith.service ""
}

# Exec Java Companion Daemon
nohup app_process -Djava.class.path="$APK_COMP" / \
    --nice-name=sys.azenith-appmonitoring zx.azenith.AppMonitor \
    "$MODULE_CONFIG/app_status" \
    "$MODULE_CONFIG/background_apps" \
    "$MODULE_CONFIG/java.lock" >"$MODULE_CONFIG/sysmon.log" 2>&1 &
    
# RN9 fork: hibernasi layar-mati
#  - boot-reset  : buang state basi + lepas app yang masih beku dari sesi lalu
#  - watch       : fallback watcher, hibernasi tetap jalan walaupun binary
#                  daemon yang terpasang belum membawa kode screen-off ECO
if [ -f "$MODDIR/azenith-hibernate.sh" ]; then
    chmod 0755 "$MODDIR/azenith-hibernate.sh"
    mkdir -p "$MODULE_CONFIG/eco"
    [ -f "$MODULE_CONFIG/eco/enabled" ] || echo 1 > "$MODULE_CONFIG/eco/enabled"
    [ -f "$MODULE_CONFIG/eco/delay" ] || echo 300 > "$MODULE_CONFIG/eco/delay"
    [ -f "$MODULE_CONFIG/eco/hibernate.list" ] || : > "$MODULE_CONFIG/eco/hibernate.list"
    # Fitur RN9: mode per-app + kondisi (charging/audio)
    [ -f "$MODULE_CONFIG/eco/mode.default" ] || echo full > "$MODULE_CONFIG/eco/mode.default"
    [ -f "$MODULE_CONFIG/eco/mode.list" ] || : > "$MODULE_CONFIG/eco/mode.list"
    [ -f "$MODULE_CONFIG/eco/skip_charging" ] || echo 1 > "$MODULE_CONFIG/eco/skip_charging"
    [ -f "$MODULE_CONFIG/eco/skip_audio" ] || echo 1 > "$MODULE_CONFIG/eco/skip_audio"
    sh "$MODDIR/azenith-hibernate.sh" boot-reset >/dev/null 2>&1
    nohup sh "$MODDIR/azenith-hibernate.sh" watch >/dev/null 2>&1 &
fi

# RN9 fork: tuning memori (ZRAM + swappiness) async setelah boot
if [ -f "$MODDIR/azenith-memory.sh" ]; then
    chmod 0755 "$MODDIR/azenith-memory.sh"
    mkdir -p "$MODULE_CONFIG/mem"
    [ -f "$MODULE_CONFIG/mem/enabled" ] || echo 1 > "$MODULE_CONFIG/mem/enabled"
    [ -f "$MODULE_CONFIG/mem/swappiness" ] || echo 140 > "$MODULE_CONFIG/mem/swappiness"
    [ -f "$MODULE_CONFIG/mem/algo" ] || echo lz4 > "$MODULE_CONFIG/mem/algo"
    nohup sh "$MODDIR/azenith-memory.sh" apply >/dev/null 2>&1 &
fi

# RN9 fork: fstrim terjadwal async (beberapa menit setelah boot, lalu berkala)
if [ -f "$MODDIR/azenith-fstrim.sh" ]; then
    chmod 0755 "$MODDIR/azenith-fstrim.sh"
    mkdir -p "$MODULE_CONFIG/maint"
    [ -f "$MODULE_CONFIG/maint/fstrim_enabled" ] || echo 1 > "$MODULE_CONFIG/maint/fstrim_enabled"
    [ -f "$MODULE_CONFIG/maint/fstrim_delay" ] || echo 300 > "$MODULE_CONFIG/maint/fstrim_delay"
    [ -f "$MODULE_CONFIG/maint/fstrim_interval" ] || echo 86400 > "$MODULE_CONFIG/maint/fstrim_interval"
    nohup sh "$MODDIR/azenith-fstrim.sh" watch >/dev/null 2>&1 &
fi

# RN9 fork: terapkan renderer tersimpan saat boot (debug.hwui.* reset tiap reboot).
# Default modul = skiaglthreaded (SkiaGL Multi-threaded).
RENDERER=$(getprop persist.sys.azenithconf.renderer)
case "$RENDERER" in
    "" | Default | default) : ;;
    *)
        [ -x "$MODDIR/system/bin/sys.azenith-utilityconf" ] && \
            nohup "$MODDIR/system/bin/sys.azenith-utilityconf" setrender "$RENDERER" >/dev/null 2>&1 &
        ;;
esac

# Run AZenith service
sleep 1 && exec "$BIN_SVC" --run
