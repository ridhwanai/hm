use std::fs;
use std::process::Command;
use std::os::unix::fs::PermissionsExt;
use std::path::Path;
use glob::glob;

pub mod logger;
use crate::utils::logger::{log_info, log_warn, log_error};

pub const MY_PATH: &str = "/system/bin:/system/xbin:/data/adb/ap/bin:/data/adb/ksu/bin:/data/adb/magisk:/debug_ramdisk:/sbin:/sbin/su:/su/bin:/su/xbin:/data/data/com.termux/files/usr/bin";

pub fn resetprop(key: &str, val: &str) {
    let status = if val.is_empty() {
        Command::new("resetprop").arg("--delete").arg(key).status()
    } else {
        Command::new("resetprop").arg(key).arg(val).status()
    };

    if let Err(e) = status {
        log_error(&format!("Failed to resetprop '{}': {}", key, e));
    }
}

pub fn setprop(key: &str, val: &str) {
    if let Err(e) = Command::new("setprop").arg(key).arg(val).status() {
        log_error(&format!("Failed to setprop '{}' to '{}': {}", key, val, e));
    }
}

pub fn chmod(path: &str, mode: u32) {
    match fs::metadata(path) {
        Ok(metadata) => {
            let mut perms = metadata.permissions();
            perms.set_mode(mode);
            if let Err(e) = fs::set_permissions(path, perms) {
                log_error(&format!("Failed to set permissions for {}: {}", path, e));
            }
        },
        Err(e) => log_warn(&format!("Failed to read metadata (chmod) for {}: {}", path, e)),
    }
}

pub fn systemv(command: &str) -> i32 {
    match Command::new("/system/bin/sh")
        .arg("-c")
        .arg(command)
        .env("PATH", MY_PATH)
        .status()
    {
        Ok(status) => status.code().unwrap_or(-1),
        Err(e) => {
            log_error(&format!("systemv failed for '{}': {}", command, e));
            -1
        }
    }
}

pub fn setsgov(gov: &str) {
    match glob("/sys/devices/system/cpu/cpu*/cpufreq/scaling_governor") {
        Ok(paths) => {
            let mut applied = false;
            for path in paths.flatten() {
                if let Some(p_str) = path.to_str() {
                    chmod(p_str, 0o644);
                    if let Err(e) = fs::write(p_str, gov) {
                        log_error(&format!("Failed to write CPU governor to {}: {}", p_str, e));
                    } else {
                        applied = true;
                    }
                    chmod(p_str, 0o444);
                }
            }
            if applied {
                log_info(&format!("Set current CPU Governor to {}", gov));
            } else {
                log_warn(&format!("Failed to set CPU Governor to {}. No paths updated.", gov));
            }
        },
        Err(e) => log_error(&format!("Glob pattern failed for CPU scaling_governor: {}", e)),
    }
}

pub fn sets_io(scheduler: &str) {
    let mut applied = false;
    for block in &["sda", "sdb", "sdc", "mmcblk0", "mmcblk1"] {
        let path = format!("/sys/block/{}/queue/scheduler", block);
        if Path::new(&path).exists() {
            chmod(&path, 0o644);
            if let Err(e) = fs::write(&path, scheduler) {
                log_error(&format!("Failed to write IO scheduler to {}: {}", path, e));
            } else {
                applied = true;
            }
            chmod(&path, 0o444);
        }
    }
    if applied {
        log_info(&format!("Set current IO Scheduler to {}", scheduler));
    } else {
        log_warn(&format!("IO Scheduler path not found or failed for {}", scheduler));
    }
}

pub fn enable_dnd() {
    if systemv("cmd notification set_dnd priority") == 0 {
        log_info("DND enabled");
    } else {
        log_error("Failed to enable DND");
    }
}

pub fn disable_dnd() {
    if systemv("cmd notification set_dnd off") == 0 {
        log_info("DND disabled");
    } else {
        log_error("Failed to disable DND");
    }
}

pub fn restartservice() {
    // [RN9] layanan thermalcore dihapus
    let _ = systemv("pkill -9 -f sys.azenith-service");
    let _ = systemv("pkill -9 -f sys.azenith-appmonitoring");
    
    setprop("persist.sys.azenith.state", "stopped");
    
    if systemv("sh /data/adb/modules/AZenith/service.sh &") != 0 {
        log_error("Failed to restart service script");
    } else {
        log_info("Restarted AZenith services");
    }
}

pub fn setrender(renderer: &str) {
    if renderer == "default" || renderer.is_empty() {
        setprop("debug.hwui.renderer", "");
        setprop("debug.renderengine.backend", "");
        setprop("debug.hwui.render_thread", "");
        setprop("debug.skia.threaded_mode", "");
        resetprop("ro.hwui.use_vulkan", ""); 
        
        log_info("Resetting all renderers to system default");
        return;
    }

    setprop("debug.hwui.renderer", renderer);

    if renderer.contains("threaded") {
        setprop("debug.hwui.render_thread", "true");
        if renderer.contains("skia") {
            setprop("debug.skia.threaded_mode", "true");
        } else {
            setprop("debug.skia.threaded_mode", "false");
        }
    } else {
        setprop("debug.hwui.render_thread", "false");
        setprop("debug.skia.threaded_mode", "false");
    }

    match renderer {
        "skiavk" | "skiavkthreaded" | "vulkan" => {
            setprop("debug.renderengine.backend", "vulkan");
            resetprop("ro.hwui.use_vulkan", "true"); 
        }
        "skiagl" | "skiaglthreaded" | "gles" | "opengl" | "openglthreaded" => {
            setprop("debug.renderengine.backend", "gles");
            resetprop("ro.hwui.use_vulkan", "false");
        }
        "software" => {
            setprop("debug.renderengine.backend", "");
            resetprop("ro.hwui.use_vulkan", "false");
        }
        _ => {
            if renderer.contains("vk") || renderer.contains("vulkan") {
                setprop("debug.renderengine.backend", "vulkan");
                resetprop("ro.hwui.use_vulkan", "true");
            } else if renderer.contains("gl") || renderer.contains("gles") {
                setprop("debug.renderengine.backend", "gles");
                resetprop("ro.hwui.use_vulkan", "false");
            } else {
                setprop("debug.renderengine.backend", "");
            }
        }
    }
    log_info(&format!("Successfully applied renderer: {}", renderer));
}

