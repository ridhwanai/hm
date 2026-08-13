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

get_state() {
    local val
    val=$(getprop "$1")
    echo "${val:-0}"
}

LOGD_STATE=$(get_state persist.sys.azenithconf.logd)

readonly LIST_LOGGER="logd statsd tcpdump cnss_diag subsystem_ramdump charge_logger wlan_logging"

verbose_log() {
    [ "$DEBUGMODE" = "true" ] && $BIN_SVC --verboselog "AZenith_Prefs" "0" "$1"
}

write_log() {
    $BIN_SVC --log "AZenith_Prefs" "1" "$1"
}

write_val() {
    local value="$1" path="$2" lock="${3:-true}"
    
    [ -e "$path" ] || { verbose_log "File /${path#/} not found, skipping..."; return 1; }

    chmod 644 "$path" 2>/dev/null
    if echo "$value" >"$path" 2>/dev/null; then
        verbose_log "Set /${path#/} to $value"
        [ "$lock" = "true" ] && chmod 444 "$path" 2>/dev/null
    else
        verbose_log "Cannot write to /${path#/} (permission denied)"
        [ "$lock" = "true" ] && chmod 444 "$path" 2>/dev/null
        return 1
    fi
}

prefsettings() {
    if [ "$LOGD_STATE" -eq 1 ]; then
        write_log "Disabling Logger Services"
        for logger in $LIST_LOGGER; do stop "$logger" 2>/dev/null; done
    else
        write_log "Enabling Logger Services"
        for logger in $LIST_LOGGER; do start "$logger" 2>/dev/null; done
    fi
}

prefsettings
