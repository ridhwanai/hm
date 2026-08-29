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

q() {
    pkill -9 -f "$1" >/dev/null 2>&1
}

# Reset properties
resetprop | awk -F'[][]' '/persist\.sys\.(wann|azenith)/ {print $2}' | while read -r prop; do
    resetprop -p --delete "$prop" >/dev/null 2>&1
done

# Terminate running background daemons
q sys.azenith-service
q wann-hibernate.sh
q wann-fstrim.sh
q wann-memory.sh
q azenith-hibernate.sh
q azenith-fstrim.sh
q azenith-memory.sh

# Remove Symlinks
for dir in "/data/adb/ap/bin" "/data/adb/ksu/bin"; do
    [ -d "$dir/zx" ] && rm -rf "$dir/zx"
    [ -d "$dir" ] && find "$dir" -name "sys.azenith-*" -exec rm -f {} +
done

# Unfreeze all hibernated apps
HIBLIST=/data/adb/.config/wann/eco/hibernate.list
LEGACY_HIBLIST=/data/adb/.config/AZenith/eco/hibernate.list
for HFILE in "$HIBLIST" "$LEGACY_HIBLIST"; do
    if [ -f "$HFILE" ]; then
        for p in $(sed -e 's/#.*//' -e 's/[[:space:]]//g' "$HFILE" | grep -v '^$'); do
            for op in RUN_ANY_IN_BACKGROUND RUN_IN_BACKGROUND WAKE_LOCK START_FOREGROUND; do
                appops set "$p" $op default >/dev/null 2>&1
            done
            am set-standby-bucket "$p" active >/dev/null 2>&1
        done
    fi
done
rm -f /dev/.wann_hibernate.lock /dev/.azenith_hibernate.lock /dev/.wann_fstrim.lock /dev/.azenith_fstrim.lock

# Remove configuration directories
rm -rf \
    "/data/adb/.config/wann" \
    "/data/adb/.config/AZenith" \
    "/data/AZenith" \
    "/data/data/zx.azenith"

: > "/data/adb/modules/wann/remove" 2>/dev/null
: > "/data/adb/modules/AZenith/remove" 2>/dev/null
