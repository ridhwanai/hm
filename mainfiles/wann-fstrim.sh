#!/system/bin/sh
# Wann Optimizer fork - fstrim terjadwal async setelah boot.
#
# Dipanggil service.sh saat boot: wann-fstrim.sh watch &
# Manual / debug                : wann-fstrim.sh once | status
#
# Config (/data/adb/.config/wann/maint/):
#   fstrim_enabled   1/0     (default 1)
#   fstrim_delay     detik   tunda setelah boot (default 300 = 5 menit)
#   fstrim_interval  detik   jeda antar trim (default 86400 = 24 jam)
#
# Author: @wann

export PATH=/system/bin:/system/xbin:/vendor/bin:$PATH

MODULE_CONFIG=/data/adb/.config/wann
MNT_DIR=$MODULE_CONFIG/maint
LOG=$MNT_DIR/fstrim.log
MAXLOG=65536
LOCK=/dev/.wann_fstrim.lock

ENABLED_FILE=$MNT_DIR/fstrim_enabled
DELAY_FILE=$MNT_DIR/fstrim_delay
INTERVAL_FILE=$MNT_DIR/fstrim_interval

mkdir -p "$MNT_DIR" 2>/dev/null

log() {
	[ -d "$MNT_DIR" ] || mkdir -p "$MNT_DIR" 2>/dev/null
	if [ -f "$LOG" ] && [ "$(stat -c %s "$LOG" 2>/dev/null || echo 0)" -gt "$MAXLOG" ]; then
		: >"$LOG"
	fi
	echo "$(date '+%m-%d %H:%M:%S') $*" >>"$LOG" 2>/dev/null
}

enabled() {
	_v=$(cat "$ENABLED_FILE" 2>/dev/null | tr -d ' \r\n')
	[ -z "$_v" ] && _v=1
	[ "$_v" = "1" ]
}

get_delay() {
	_v=$(cat "$DELAY_FILE" 2>/dev/null | tr -d ' \r\n')
	case "$_v" in '' | *[!0-9]*) _v=300 ;; esac
	[ "$_v" -lt 30 ] && _v=30
	echo "$_v"
}

get_interval() {
	_v=$(cat "$INTERVAL_FILE" 2>/dev/null | tr -d ' \r\n')
	case "$_v" in '' | *[!0-9]*) _v=86400 ;; esac
	[ "$_v" -lt 3600 ] && _v=3600
	echo "$_v"
}

trim_one() {
	[ -d "$1" ] || return 0
	_out=$(fstrim -v "$1" 2>&1)
	if [ $? -eq 0 ]; then
		log "trim ok: $1 ${_out#* }"
	else
		log "trim skip: $1 ($_out)"
	fi
}

do_trim() {
	command -v fstrim >/dev/null 2>&1 || {
		log "fstrim tidak tersedia, batal"
		return 1
	}
	log "mulai fstrim terjadwal"
	for mp in /data /cache /system /vendor /product /odm /metadata /system_ext; do
		trim_one "$mp"
	done
	log "fstrim selesai"
}

case "$1" in
once)
	enabled || exit 0
	do_trim
	;;
watch)
	enabled || {
		log "watch dilewati: fstrim dimatikan"
		exit 0
	}
	if [ -f "$LOCK" ]; then
		old=$(cat "$LOCK" 2>/dev/null)
		if [ -n "$old" ] && kill -0 "$old" 2>/dev/null; then
			exit 0
		fi
	fi
	echo $$ >"$LOCK"
	while [ "$(getprop sys.boot_completed)" != "1" ]; do sleep 3; done
	sleep "$(get_delay)"
	while true; do
		enabled && do_trim
		sleep "$(get_interval)"
	done
	;;
status)
	echo "enabled  : $(cat "$ENABLED_FILE" 2>/dev/null || echo 1)"
	echo "delay    : $(get_delay) detik setelah boot"
	echo "interval : $(get_interval) detik"
	;;
*)
	echo "usage: $0 watch|once|status" >&2
	exit 1
	;;
esac

exit 0
