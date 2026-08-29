#!/system/bin/sh
# Wann Optimizer fork - screen-off app hibernation helper.
#
# Dipanggil daemon  : wann-hibernate.sh apply | release
# Fallback watcher  : wann-hibernate.sh watch   (dijalankan service.sh)
# Manual / debug    : wann-hibernate.sh status | boot-reset | apply --force
#
# Author: @wann

export PATH=/system/bin:/system/xbin:/vendor/bin:/product/bin:$PATH
export ANDROID_DATA=${ANDROID_DATA:-/data}
export ANDROID_ROOT=${ANDROID_ROOT:-/system}

MODULE_CONFIG=/data/adb/.config/wann
ECO_DIR=$MODULE_CONFIG/eco
LIST=$ECO_DIR/hibernate.list
LOG=$ECO_DIR/hibernate.log
STATE=$ECO_DIR/.applied
ENABLED=$ECO_DIR/enabled
DELAY_FILE=$ECO_DIR/delay
MODE_DEFAULT_FILE=$ECO_DIR/mode.default
MODE_LIST=$ECO_DIR/mode.list
SKIP_CHARGING_FILE=$ECO_DIR/skip_charging
SKIP_AUDIO_FILE=$ECO_DIR/skip_audio
WATCH_LOCK=/dev/.wann_hibernate.lock
MAXLOG=131072

mkdir -p "$ECO_DIR" 2>/dev/null
[ -f "$LIST" ] || : >"$LIST"

log() {
	[ -d "$ECO_DIR" ] || mkdir -p "$ECO_DIR" 2>/dev/null
	if [ -f "$LOG" ] && [ "$(stat -c %s "$LOG" 2>/dev/null || echo 0)" -gt "$MAXLOG" ]; then
		: >"$LOG"
	fi
	echo "$(date '+%m-%d %H:%M:%S') $*" >>"$LOG" 2>/dev/null
}

hib_enabled() {
	_v=$(cat "$ENABLED" 2>/dev/null | tr -d ' \r\n')
	[ -z "$_v" ] && _v=1
	[ "$_v" = "1" ]
}

delay_sec() {
	_d=$(cat "$DELAY_FILE" 2>/dev/null | tr -d ' \r\n')
	case "$_d" in '' | *[!0-9]*) _d=300 ;; esac
	[ "$_d" -lt 10 ] && _d=10
	[ "$_d" -gt 7200 ] && _d=7200
	echo "$_d"
}

mode_default() {
	_m=$(cat "$MODE_DEFAULT_FILE" 2>/dev/null | tr -d ' \r\n')
	case "$_m" in
	full | restrict) echo "$_m" ;;
	*) echo full ;;
	esac
}

app_mode() {
	_p=$1
	if [ -f "$MODE_LIST" ]; then
		_m=$(sed -e 's/#.*//' "$MODE_LIST" 2>/dev/null | awk -v p="$_p" '$1==p {print $2; exit}')
		case "$_m" in
		full | restrict)
			echo "$_m"
			return 0
			;;
		esac
	fi
	mode_default
}

skip_charging_on() {
	_v=$(cat "$SKIP_CHARGING_FILE" 2>/dev/null | tr -d ' \r\n')
	[ -z "$_v" ] && _v=1
	[ "$_v" = "1" ]
}

skip_audio_on() {
	_v=$(cat "$SKIP_AUDIO_FILE" 2>/dev/null | tr -d ' \r\n')
	[ -z "$_v" ] && _v=1
	[ "$_v" = "1" ]
}

is_charging() {
	for _st in /sys/class/power_supply/*/status; do
		[ -f "$_st" ] || continue
		case "$(cat "$_st" 2>/dev/null)" in
		Charging | Full) return 0 ;;
		esac
	done
	return 1
}

audio_active() {
	dumpsys audio 2>/dev/null | grep -Eqi 'state:started|state: *started' && return 0
	dumpsys media.audio_flinger 2>/dev/null | grep -Eqi 'standby: *no|Active tracks' && return 0
	return 1
}

pkglist() {
	[ -f "$LIST" ] || return 0
	sed -e 's/#.*//' -e 's/[[:space:]]//g' "$LIST" | grep -v '^$'
}

is_installed() {
	if ! command -v pm >/dev/null 2>&1; then
		return 0
	fi
	if pm path --user 0 "$1" 2>/dev/null | grep -q '^package:'; then
		return 0
	fi
	if pm path "$1" 2>/dev/null | grep -q '^package:'; then
		return 0
	fi
	return 1
}

is_protected() {
	case "$1" in
	android | com.android.systemui | com.android.phone | com.android.settings | *.inputmethod* | *inputmethod* | com.google.android.gms | com.google.android.gsf | com.android.vending)
		return 0
		;;
	esac
	return 1
}

focused_app() {
	sed -n 's/^focused_app \([^ ]*\).*/\1/p' "$MODULE_CONFIG/app_status" 2>/dev/null | head -n1
}

screen_is_on() {
	_s=$(sed -n 's/^screen_awake \([0-9]*\).*/\1/p' "$MODULE_CONFIG/app_status" 2>/dev/null | head -n1)
	if [ -n "$_s" ]; then
		[ "$_s" = "1" ] && return 0
		return 1
	fi
	_w=$(dumpsys power 2>/dev/null | grep -m1 -o 'mWakefulness=[A-Za-z]*' | cut -d= -f2)
	case "$_w" in
	Awake | Dreaming) return 0 ;;
	Asleep | Dozing) return 1 ;;
	esac
	dumpsys deviceidle 2>/dev/null | grep -qm1 'mScreenOn=true' && return 0
	return 1
}

freeze_full() {
	p=$1
	appops set "$p" RUN_ANY_IN_BACKGROUND ignore >/dev/null 2>&1
	for op in RUN_IN_BACKGROUND WAKE_LOCK START_FOREGROUND; do
		appops set "$p" $op ignore >/dev/null 2>&1
	done
	am set-standby-bucket "$p" restricted >/dev/null 2>&1
	cmd deviceidle whitelist -"$p" >/dev/null 2>&1
	am force-stop "$p" >/dev/null 2>&1
}

freeze_restrict() {
	p=$1
	appops set "$p" RUN_ANY_IN_BACKGROUND ignore >/dev/null 2>&1
	appops set "$p" WAKE_LOCK ignore >/dev/null 2>&1
	am set-standby-bucket "$p" restricted >/dev/null 2>&1
	cmd deviceidle whitelist -"$p" >/dev/null 2>&1
}

freeze() {
	case "$(app_mode "$1")" in
	restrict) freeze_restrict "$1" ;;
	*) freeze_full "$1" ;;
	esac
}

thaw() {
	p=$1
	appops set "$p" RUN_ANY_IN_BACKGROUND allow >/dev/null 2>&1
	for op in RUN_IN_BACKGROUND WAKE_LOCK START_FOREGROUND; do
		appops set "$p" $op default >/dev/null 2>&1
	done
	am set-standby-bucket "$p" active >/dev/null 2>&1
	cmd deviceidle whitelist -"$p" >/dev/null 2>&1
}

do_apply() {
	hib_enabled || {
		log "apply dilewati: hibernasi dimatikan di config"
		return 0
	}
	if [ -f "$STATE" ] && [ "$1" != "--force" ]; then
		return 0
	fi
	if [ "$1" != "--force" ]; then
		if skip_charging_on && is_charging; then
			log "apply ditunda: sedang mengisi daya (skip_charging=1)"
			return 0
		fi
		if skip_audio_on && audio_active; then
			log "apply ditunda: ada audio aktif (skip_audio=1)"
			return 0
		fi
	fi
	_fg=$(focused_app)
	n=0
	skipped=0
	for p in $(pkglist); do
		if is_protected "$p"; then
			log "skip (protected): $p"
			skipped=$((skipped + 1))
			continue
		fi
		if [ -n "$_fg" ] && [ "$p" = "$_fg" ]; then
			log "skip (foreground): $p"
			skipped=$((skipped + 1))
			continue
		fi
		if ! is_installed "$p"; then
			log "skip (not installed): $p"
			skipped=$((skipped + 1))
			continue
		fi
		_mode=$(app_mode "$p")
		freeze "$p"
		log "freeze ($_mode): $p"
		n=$((n + 1))
	done
	: >"$STATE"
	log "hibernate applied ke $n paket (skip $skipped)"
	return 0
}

do_release() {
	[ -f "$STATE" ] || [ "$1" = "--force" ] || return 0
	n=0
	for p in $(pkglist); do
		is_protected "$p" && continue
		is_installed "$p" || continue
		thaw "$p"
		n=$((n + 1))
	done
	rm -f "$STATE"
	log "hibernate released untuk $n paket"
	return 0
}

case "$1" in
apply)
	do_apply "$2"
	;;
release)
	do_release "$2"
	;;
boot-reset)
	if [ -f "$STATE" ]; then
		log "boot-reset: state lama ditemukan, melepas hibernasi"
		do_release --force
	fi
	rm -f "$STATE"
	;;
status)
	echo "enabled : $(cat "$ENABLED" 2>/dev/null || echo 1)"
	echo "delay   : $(delay_sec) detik"
	echo "state   : $([ -f "$STATE" ] && echo 'HIBERNATING' || echo 'normal')"
	echo "screen  : $(screen_is_on && echo on || echo off)"
	echo "mode    : $(mode_default) (default)"
	echo "charging: skip=$(skip_charging_on && echo yes || echo no)"
	echo "audio   : skip=$(skip_audio_on && echo yes || echo no)"
	echo "list    :"
	pkglist | sed 's/^/  - /'
	;;
watch)
	if [ -f "$WATCH_LOCK" ]; then
		old=$(cat "$WATCH_LOCK" 2>/dev/null)
		if [ -n "$old" ] && kill -0 "$old" 2>/dev/null; then
			exit 0
		fi
	fi
	echo $$ >"$WATCH_LOCK"
	while [ "$(getprop sys.boot_completed)" != "1" ]; do sleep 3; done
	sleep 30
	log "watcher start (pid $$)"
	off_since=0
	while true; do
		sleep 5
		if ! hib_enabled; then
			[ -f "$STATE" ] && do_release
			off_since=0
			sleep 10
			continue
		fi
		if screen_is_on; then
			off_since=0
			[ -f "$STATE" ] && do_release
		else
			now=$(date +%s)
			[ "$off_since" -eq 0 ] && off_since=$now
			if [ ! -f "$STATE" ] && [ $((now - off_since)) -ge "$(delay_sec)" ]; then
				log "watcher: layar mati $((now - off_since))s, menjalankan hibernasi"
				do_apply
			fi
		fi
	done
	;;
*)
	echo "usage: $0 apply|release|watch|status|boot-reset [--force]" >&2
	exit 1
	;;
esac

exit 0
