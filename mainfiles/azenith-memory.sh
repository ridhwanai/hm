#!/system/bin/sh
# AZenith RN9 fork - tuning memori (ZRAM + swappiness) untuk RAM kecil.
#
# Dipanggil service.sh saat boot: azenith-memory.sh apply
# Manual / debug                : azenith-memory.sh apply | status
#
# Config (/data/adb/.config/AZenith/mem/):
#   enabled     1/0   (default 1)
#   zram_mb     angka MB target ZRAM (default: 50% RAM, min 512, maks 3072)
#   swappiness  0-200 (default 140; RAM kecil suka nilai agresif)
#   algo        algoritma kompresi (default lz4)
#
# Based on AZenith by Zexshia, Apache License 2.0.

export PATH=/system/bin:/system/xbin:/vendor/bin:$PATH

MODULE_CONFIG=/data/adb/.config/AZenith
MEM_DIR=$MODULE_CONFIG/mem
LOG=$MEM_DIR/mem.log
MAXLOG=65536

ENABLED_FILE=$MEM_DIR/enabled
ZRAM_FILE=$MEM_DIR/zram_mb
SWAP_FILE=$MEM_DIR/swappiness
ALGO_FILE=$MEM_DIR/algo

mkdir -p "$MEM_DIR" 2>/dev/null

log() {
	[ -d "$MEM_DIR" ] || mkdir -p "$MEM_DIR" 2>/dev/null
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

total_ram_mb() {
	_kb=$(grep -m1 MemTotal /proc/meminfo 2>/dev/null | tr -dc '0-9')
	[ -z "$_kb" ] && _kb=0
	echo $((_kb / 1024))
}

target_zram_mb() {
	_v=$(cat "$ZRAM_FILE" 2>/dev/null | tr -d ' \r\n')
	case "$_v" in '' | *[!0-9]*)
		_ram=$(total_ram_mb)
		_v=$((_ram / 2))
		;;
	esac
	[ "$_v" -lt 512 ] && _v=512
	[ "$_v" -gt 3072 ] && _v=3072
	echo "$_v"
}

target_swappiness() {
	_v=$(cat "$SWAP_FILE" 2>/dev/null | tr -d ' \r\n')
	case "$_v" in '' | *[!0-9]*) _v=140 ;; esac
	[ "$_v" -gt 200 ] && _v=200
	echo "$_v"
}

target_algo() {
	_v=$(cat "$ALGO_FILE" 2>/dev/null | tr -d ' \r\n')
	[ -z "$_v" ] && _v=lz4
	echo "$_v"
}

apply_swappiness() {
	_s=$(target_swappiness)
	if [ -w /proc/sys/vm/swappiness ]; then
		echo "$_s" >/proc/sys/vm/swappiness 2>/dev/null && log "swappiness -> $_s"
	fi
}

apply_zram() {
	_dev=/dev/block/zram0
	_sys=/sys/block/zram0
	[ -d "$_sys" ] || {
		log "zram0 tidak ada, lewati resize"
		return 0
	}

	_target_mb=$(target_zram_mb)
	_target_bytes=$((_target_mb * 1024 * 1024))
	_cur=$(cat "$_sys/disksize" 2>/dev/null | tr -dc '0-9')
	[ -z "$_cur" ] && _cur=0

	# Sudah cukup besar? cukup pastikan swap aktif, jangan ganggu.
	if [ "$_cur" -ge "$_target_bytes" ] && [ "$_cur" -gt 0 ]; then
		swapon "$_dev" 2>/dev/null
		log "zram sudah $((_cur / 1048576))MB (>= target ${_target_mb}MB), dibiarkan"
		return 0
	fi

	# Resize butuh swap dimatikan + reset dulu.
	swapoff "$_dev" 2>/dev/null
	if [ -w "$_sys/reset" ]; then
		echo 1 >"$_sys/reset" 2>/dev/null
	fi

	_algo=$(target_algo)
	if [ -w "$_sys/comp_algorithm" ]; then
		echo "$_algo" >"$_sys/comp_algorithm" 2>/dev/null
	fi

	if [ -w "$_sys/disksize" ]; then
		echo "$_target_bytes" >"$_sys/disksize" 2>/dev/null
	else
		log "disksize tidak bisa ditulis, batal"
		swapon "$_dev" 2>/dev/null
		return 1
	fi

	mkswap "$_dev" >/dev/null 2>&1
	if swapon "$_dev" 2>/dev/null; then
		log "zram di-set ke ${_target_mb}MB (algo=$_algo)"
	else
		log "swapon gagal setelah resize (${_target_mb}MB)"
	fi
}

case "$1" in
apply)
	enabled || {
		log "apply dilewati: mem tuning dimatikan"
		exit 0
	}
	# Tunggu boot benar-benar selesai supaya zram vendor sudah siap.
	while [ "$(getprop sys.boot_completed)" != "1" ]; do sleep 2; done
	sleep 20
	apply_zram
	apply_swappiness
	;;
status)
	echo "enabled    : $(cat "$ENABLED_FILE" 2>/dev/null || echo 1)"
	echo "ram        : $(total_ram_mb) MB"
	echo "zram target: $(target_zram_mb) MB"
	echo "swappiness : $(target_swappiness) (aktif: $(cat /proc/sys/vm/swappiness 2>/dev/null))"
	echo "algo       : $(target_algo)"
	if [ -d /sys/block/zram0 ]; then
		_c=$(cat /sys/block/zram0/disksize 2>/dev/null | tr -dc '0-9')
		[ -n "$_c" ] && echo "zram now   : $((_c / 1048576)) MB"
	fi
	;;
*)
	echo "usage: $0 apply|status" >&2
	exit 1
	;;
esac

exit 0
