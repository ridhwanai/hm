use crate::utils::*; use std::fs; use std::path::Path; use std::process::Command;
use crate::chipsets::mediatek::*;

pub fn performance_profile() {
    let mut performance_gov = getprop("persist.sys.azenith.custom_performance_cpu_gov");
    if performance_gov.is_empty() {
        performance_gov = "powersave".to_string();
    }
    setgov(&performance_gov);
    log_info(&format!("Applying governor to : {}", performance_gov));
    
    let lite_mode = get_litemode();

    // I/O Scheduler Tweaks
    let custom_perf_io = getprop("persist.sys.azenith.custom_performance_IO");
    if !custom_perf_io.is_empty() {
        sets_io(&custom_perf_io);
        log_info(&format!("Applying I/O scheduler to : {}", custom_perf_io));
    } else {
        let mut default_io = getprop("persist.sys.azenith.custom_default_balanced_IO");
        if default_io.is_empty() {
            default_io = getprop("persist.sys.azenith.default_balanced_IO");
        }
        if default_io.is_empty() {
            default_io = "none".to_string();
        }
        sets_io(&default_io);
        log_info(&format!("Applying I/O scheduler to : {}", default_io));
    }

    if Path::new("/proc/ppm").exists() {
        setgamefreqppm();
    } else {
        setgamefreq();
    }

    if !lite_mode {
        log_info("Set CPU freq to max available Frequencies");
    } else {
        log_info("Set CPU freq to normal Frequencies");
    }

    let pl_base = "/sys/devices/system/cpu/perf";
    if Path::new(pl_base).exists() {
        write_lock("1", &format!("{}/gpu_pmu_enable", pl_base));
        write_lock("1", &format!("{}/fuel_gauge_enable", pl_base));
        write_lock("1", &format!("{}/enable", pl_base));
        write_lock("1", &format!("{}/charger_enable", pl_base));
    }

    write_lock("80", "/proc/sys/vm/vfs_cache_pressure");
    // [RN9] drop_caches (pembersih memori) dihapus
    write_lock("N", "/sys/module/workqueue/parameters/power_efficient");
    write_lock("0", "/sys/devices/system/cpu/eas/enable");

    // [RN9] Sched Tuner (schedtune) dihapus

    let bs_path = "/sys/module/battery_saver/parameters/enabled";
    if Path::new(bs_path).exists() {
        let content = fs::read_to_string(bs_path).unwrap_or_default();
        if content.chars().any(|c: char| c.is_ascii_digit()) {
            write_lock("0", bs_path);
        } else {
            write_lock("N", bs_path);
        }
    }

    write_lock("0", "/proc/sys/kernel/split_lock_mitigate");

    // [RN9] Sched Tuner (sched_features) dihapus
    
    // I/O Tweaks
    std::thread::spawn(|| {
        if let Ok(paths) = glob::glob("/sys/block/*") {
            for path in paths.flatten() {
                if let Some(file_name) = path.file_name().and_then(|n| n.to_str()) {
                    if file_name == "mmcblk0" || file_name == "mmcblk1" || file_name.starts_with("sd") {
                        if let Some(p_str) = path.to_str() {
                            // RN9: eMMC lambat -> read_ahead 128 (bukan 32) mempercepat
                            // pemuatan aset/app; nr_requests 64 selaras profil Balanced.
                            write_lock("128", &format!("{}/queue/read_ahead_kb", p_str));
                            write_lock("64", &format!("{}/queue/nr_requests", p_str));
                        }
                    }
                }
            }
        }
    });

    if !lite_mode {
        match getprop("persist.sys.azenith.soctype").as_str() {
            "1" => mediatek_performance(),
            _ => {}
        }
    }

    log_verbose("Performance Profile Applied Successfully!");
}

pub fn balanced_profile() {
    let mut default_gov = getprop("persist.sys.azenith.custom_default_cpu_gov");
    if default_gov.is_empty() {
        default_gov = getprop("persist.sys.azenith.default_cpu_gov");
    }
    if default_gov.is_empty() {
        default_gov = "schedutil".to_string();
    }
    setgov(&default_gov);
    log_info(&format!("Applying governor to : {}", default_gov));

    // I/O Scheduler Tweaks
    let mut default_io = getprop("persist.sys.azenith.custom_default_balanced_IO");
    if default_io.is_empty() {
        default_io = getprop("persist.sys.azenith.default_balanced_IO");
    }
    if default_io.is_empty() {
        default_io = "none".to_string();
    }
    sets_io(&default_io);
    log_info(&format!("Applying I/O scheduler to : {}", default_io));

    if Path::new("/proc/ppm").exists() {
        setfreqppm();
    } else {
        setfreq();
    }

    log_info("Set CPU freq to normal Frequencies");

    let pl_base = "/sys/devices/system/cpu/perf";
    if Path::new(pl_base).exists() {
        write_lock("0", &format!("{}/gpu_pmu_enable", pl_base));
        write_lock("0", &format!("{}/fuel_gauge_enable", pl_base));
        write_lock("0", &format!("{}/enable", pl_base));
        write_lock("1", &format!("{}/charger_enable", pl_base));
    }

    write_lock("120", "/proc/sys/vm/vfs_cache_pressure");
    write_lock("Y", "/sys/module/workqueue/parameters/power_efficient");
    write_lock("1", "/sys/devices/system/cpu/eas/enable");

    // [RN9] Sched Tuner (schedtune) dihapus

    let bs_path = "/sys/module/battery_saver/parameters/enabled";
    if Path::new(bs_path).exists() {
        let content = fs::read_to_string(bs_path).unwrap_or_default();
        if content.chars().any(|c: char| c.is_ascii_digit()) {
            write_lock("0", bs_path);
        } else {
            write_lock("N", bs_path);
        }
    }

    write_lock("1", "/proc/sys/kernel/split_lock_mitigate");

    // [RN9] Sched Tuner (sched_features) dihapus
    
    // I/O Tweaks
    std::thread::spawn(|| {
        if let Ok(paths) = glob::glob("/sys/block/*") {
            for path in paths.flatten() {
                if let Some(file_name) = path.file_name().and_then(|n| n.to_str()) {
                    if file_name == "mmcblk0" || file_name == "mmcblk1" || file_name.starts_with("sd") {
                        if let Some(p_str) = path.to_str() {
                            write_lock("128", &format!("{}/queue/read_ahead_kb", p_str));
                            write_lock("64", &format!("{}/queue/nr_requests", p_str));
                        }
                    }
                }
            }
        }
    });

    match getprop("persist.sys.azenith.soctype").as_str() {
        "1" => mediatek_balance(),
        _ => {}
    }

    log_verbose("Balanced Profile applied successfully!");
}

pub fn eco_mode() {
    let mut powersave_gov = getprop("persist.sys.azenith.custom_powersave_cpu_gov");
    if powersave_gov.is_empty() {
        powersave_gov = "powersave".to_string();
    }
    setgov(&powersave_gov);
    log_info(&format!("Applying governor to : {}", powersave_gov));

    // I/O Scheduler Tweaks
    let mut powersave_io = getprop("persist.sys.azenith.custom_powersave_IO");
    if powersave_io.is_empty() {
        powersave_io = "none".to_string();
    }
    sets_io(&powersave_io);
    log_info(&format!("Applying I/O scheduler to : {}", powersave_io));

    if Path::new("/proc/ppm").exists() {
        setfreqppm();
    } else {
        setfreq();
    }
    log_info("Set CPU freq to low Frequencies");

    let pl_base = "/sys/devices/system/cpu/perf";
    if Path::new(pl_base).exists() {
        write_lock("0", &format!("{}/gpu_pmu_enable", pl_base));
        write_lock("0", &format!("{}/fuel_gauge_enable", pl_base));
        write_lock("0", &format!("{}/enable", pl_base));
        write_lock("1", &format!("{}/charger_enable", pl_base));
    }

    write_lock("120", "/proc/sys/vm/vfs_cache_pressure");
    write_lock("Y", "/sys/module/workqueue/parameters/power_efficient");
    write_lock("1", "/sys/devices/system/cpu/eas/enable");

    // [RN9] Sched Tuner (schedtune) dihapus

    let bs_path = "/sys/module/battery_saver/parameters/enabled";
    if Path::new(bs_path).exists() {
        let content = fs::read_to_string(bs_path).unwrap_or_default();
        if content.chars().any(|c: char| c.is_ascii_digit()) {
            write_lock("1", bs_path);
        } else {
            write_lock("Y", bs_path);
        }
    }

    write_lock("1", "/proc/sys/kernel/split_lock_mitigate");

    // [RN9] Sched Tuner (sched_features) dihapus
    
    // Enable battery saver module
    let bs_path = "/sys/module/battery_saver/parameters/enabled";
    if Path::new(bs_path).exists() {
        let content = fs::read_to_string(bs_path).unwrap_or_default();
        if content.chars().any(|c| c.is_ascii_digit()) {
            write_lock("1", bs_path);
        } else {
            write_lock("Y", bs_path);
        }
    }

    match getprop("persist.sys.azenith.soctype").as_str() {
        "1" => mediatek_powersave(),
        _ => {}
    }

    log_verbose("ECO Mode applied successfully!");
}

pub fn initialize() {
    // 1. Initial kernel panics & sync
    for param in &["panic", "panic_on_warn", "panic_on_oops", "softlockup_panic"] {
        write_lock("0", &format!("/proc/sys/kernel/{}", param));
    }
    let _ = Command::new("sync").status();

    // 2. Initialize CPU & I/O
    init_cpu_governor();
    init_io_scheduler();

    // 3. Display / SurfaceFlinger config
    
    // 4. Thermal governor
    if let Ok(paths) = glob::glob("/sys/class/thermal/thermal_zone*") {
        for path in paths.flatten() {
            if let Some(p_str) = path.to_str() {
                write_lock("step_wise", &format!("{}/policy", p_str));
            }
        }
    }
    
    // 5. I/O Tweaks
    if let Ok(paths) = glob::glob("/sys/block/*") {
        for path in paths.flatten() {
            if let Some(p_str) = path.to_str() {
                write_lock("0", &format!("{}/queue/iostats", p_str));
                write_lock("0", &format!("{}/queue/add_random", p_str));
            }
        }
    }

    // 6. Networking tweaks
    let tcp_avail = fs::read_to_string("/proc/sys/net/ipv4/tcp_available_congestion_control").unwrap_or_default();
    let algos = ["bbr3", "bbr2", "bbrplus", "bbr", "westwood", "cubic"];
    for algo in algos.iter() {
        if tcp_avail.contains(algo) {
            write_lock(algo, "/proc/sys/net/ipv4/tcp_congestion_control");
            break;
        }
    }

    write_lock("1", "/proc/sys/net/ipv4/tcp_low_latency");
    write_lock("1", "/proc/sys/net/ipv4/tcp_ecn");
    write_lock("3", "/proc/sys/net/ipv4/tcp_fastopen");
    write_lock("1", "/proc/sys/net/ipv4/tcp_sack");
    write_lock("0", "/proc/sys/net/ipv4/tcp_timestamps");

    // 7. General Kernel & Scheduler Tweaks
    write_lock("3", "/proc/sys/kernel/perf_cpu_time_max_percent");
    write_lock("0", "/proc/sys/kernel/sched_schedstats");
    write_lock("0", "/proc/sys/kernel/task_cpustats_enable");
    write_lock("0", "/proc/sys/kernel/sched_autogroup_enabled");
    write_lock("1", "/proc/sys/kernel/sched_child_runs_first");
    write_lock("32", "/proc/sys/kernel/sched_nr_migrate");
    write_lock("50000", "/proc/sys/kernel/sched_migration_cost_ns");
    write_lock("1000000", "/proc/sys/kernel/sched_min_granularity_ns");
    write_lock("1500000", "/proc/sys/kernel/sched_wakeup_granularity_ns");

    // 8. VM Tweaks
    write_lock("0", "/proc/sys/vm/page-cluster");
    write_lock("15", "/proc/sys/vm/stat_interval");
    write_lock("0", "/proc/sys/vm/compaction_proactiveness");

    // 9. Vendor Bloats & Module Tweaks
    write_lock("0", "/sys/module/mmc_core/parameters/use_spi_crc");
    write_lock("0", "/sys/module/opchain/parameters/chain_on");
    write_lock("0", "/sys/module/cpufreq_bouncing/parameters/enable");
    write_lock("0", "/proc/task_info/task_sched_info/task_sched_info_enable");
    write_lock("0", "/proc/oplus_scheduler/sched_assist/sched_assist_enabled");

    // 10. Libraries Max Perf Reporting
    let libs = "libunity.so, libil2cpp.so, libmain.so, libUE4.so, libgodot_android.so, libgdx.so, libgdx-box2d.so, libminecraftpe.so, libLive2DCubismCore.so, libyuzu-android.so, libryujinx.so, libcitra-android.so, libhdr_pro_engine.so, libandroidx.graphics.path.so, libeffect.so";
    write_lock(libs, "/proc/sys/kernel/sched_lib_name");
    write_lock("255", "/proc/sys/kernel/sched_lib_mask_force");

    // [RN9] Trim partisi (FSTrim) dihapus: operasi fstrim sinkron ini mengunci I/O
    // beberapa detik saat boot dan menjadi penyebab utama UI freeze/ANR di awal boot.
    systemv("sh /data/adb/modules/AZenith/preferenced-tweaks.sh");
    
    // 11. Final Sync & Logs
    let _ = Command::new("sync").status();
    log_verbose("Initializing Complete");
    log_info("Initializing Complete");
}
