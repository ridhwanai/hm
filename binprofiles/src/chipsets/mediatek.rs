use crate::utils::*; use std::fs; use std::path::Path;

pub fn mediatek_balance() {
    if Path::new("/proc/ppm/policy_status").exists() {
        let content = fs::read_to_string("/proc/ppm/policy_status").unwrap_or_default();
        for line in content.lines() {
            let is_target_1 = line.contains("FORCE_LIMIT") || line.contains("PWR_THRO") || line.contains("THERMAL") || line.contains("USER_LIMIT");
            let is_target_2 = line.contains("SYS_BOOST");
            if is_target_1 || is_target_2 {
                if let Some(idx_str) = line.split('[').nth(1).and_then(|s| s.split(']').next()) {
                    if is_target_1 {
                        write_lock(&format!("{} 1", idx_str), "/proc/ppm/policy_status");
                    }
                    if is_target_2 {
                        write_lock(&format!("{} 0", idx_str), "/proc/ppm/policy_status");
                    }
                }
            }
        }
    }
    
    ppm_fix_freq("-1"); 
    
    // [RN9] fpsgo force_onoff & GED KPI dihapus
    
    write_lock("0", "/sys/devices/platform/boot_dramboost/dramboost/dramboost");

    write_lock("0", "/proc/cpufreq/cpufreq_cci_mode");
    write_lock("1", "/proc/cpufreq/cpufreq_power_mode");

    if Path::new("/proc/gpufreq").exists() {
        write_lock("0", "/proc/gpufreq/gpufreq_opp_freq");
    } else if Path::new("/proc/gpufreqv2").exists() {
        write_lock("-1", "/proc/gpufreqv2/fix_target_opp_index");
    }

    write_lock("1", "/sys/devices/system/cpu/eas/enable");

    // [RN9] gpufreq_power_limited (override termal/batt) dihapus
    

    write_lock("0", "/proc/perfmgr/syslimiter/syslimiter_force_disable");
    // [RN9] batoc override & eara_thermal dihapus
    write_lock("stop 0", "/proc/pbm/pbm_stop");

    write_lock("-1", "/sys/kernel/helio-dvfsrc/dvfsrc_force_vcore_dvfs_opp");
    write_lock("userspace", "/sys/class/devfreq/mtk-dvfsrc-devfreq/governor");
    
        // 1. Eksekusi statis untuk path /sys/kernel dan /sys/class (karena ini symlink global, tidak berubah)
    write_lock("-1", "/sys/kernel/helio-dvfsrc/dvfsrc_force_vcore_dvfs_opp");
    write_lock("userspace", "/sys/class/devfreq/mtk-dvfsrc-devfreq/governor");

    if let Ok(paths) = glob::glob("/sys/devices/platform/*.dvfsrc") {
        for path in paths.flatten() {
            if let Some(p_str) = path.to_str() {
                write_lock("-1", &format!("{}/helio-dvfsrc/dvfsrc_req_ddr_opp", p_str));
            }
        }
    }

    if let Ok(paths) = glob::glob("/sys/devices/platform/soc/*.dvfsrc") {
        for path in paths.flatten() {
            if let Some(p_str) = path.to_str() {
                write_lock("userspace", &format!("{}/mtk-dvfsrc-devfreq/devfreq/mtk-dvfsrc-devfreq/governor", p_str));
            }
        }
    }
    

    // [RN9] GPU Mali power_policy (scheduling) dihapus
}

pub fn mediatek_performance() {
    if Path::new("/proc/ppm/policy_status").exists() {
        let content = fs::read_to_string("/proc/ppm/policy_status").unwrap_or_default();
        for line in content.lines() {
            let is_target_1 = line.contains("FORCE_LIMIT") || line.contains("PWR_THRO") || line.contains("THERMAL") || line.contains("USER_LIMIT");
            let is_target_2 = line.contains("SYS_BOOST");
            if is_target_1 || is_target_2 {
                if let Some(idx_str) = line.split('[').nth(1).and_then(|s| s.split(']').next()) {
                    if is_target_1 {
                        write_lock(&format!("{} 0", idx_str), "/proc/ppm/policy_status");
                    }
                    if is_target_2 {
                        write_lock(&format!("{} 1", idx_str), "/proc/ppm/policy_status");
                    }
                }
            }
        }
    }

    
    ppm_fix_freq("0"); 

    // [RN9] fpsgo force_onoff & GED KPI dihapus
    
    write_lock("1", "/sys/devices/platform/boot_dramboost/dramboost/dramboost");

    write_lock("1", "/proc/cpufreq/cpufreq_cci_mode");
    write_lock("3", "/proc/cpufreq/cpufreq_power_mode");

    if Path::new("/proc/gpufreq").exists() {
        if let Some(freq) = get_mtk_gpu_max_freq() {
            write_lock(&freq.to_string(), "/proc/gpufreq/gpufreq_opp_freq");
        }
    } else if Path::new("/proc/gpufreqv2").exists() {
        write_lock("0", "/proc/gpufreqv2/fix_target_opp_index");
    }

    write_lock("0", "/sys/devices/system/cpu/eas/enable");

    // [RN9] gpufreq_power_limited (override termal/batt) dihapus

    write_lock("0", "/proc/perfmgr/syslimiter/syslimiter_force_disable");
    // [RN9] batoc override & eara_thermal dihapus

    write_lock("0", "/sys/kernel/helio-dvfsrc/dvfsrc_force_vcore_dvfs_opp");
    write_lock("performance", "/sys/class/devfreq/mtk-dvfsrc-devfreq/governor");
    
    if let Ok(paths) = glob::glob("/sys/devices/platform/*.dvfsrc") {
        for path in paths.flatten() {
            if let Some(p_str) = path.to_str() {
                write_lock("0", &format!("{}/helio-dvfsrc/dvfsrc_req_ddr_opp", p_str));
            }
        }
    }

    if let Ok(paths) = glob::glob("/sys/devices/platform/soc/*.dvfsrc") {
        for path in paths.flatten() {
            if let Some(p_str) = path.to_str() {
                write_lock("performance", &format!("{}/mtk-dvfsrc-devfreq/devfreq/mtk-dvfsrc-devfreq/governor", p_str));
            }
        }
    }

    // [RN9] GPU Mali power_policy (scheduling) dihapus
}

pub fn mediatek_powersave() {
    if Path::new("/proc/ppm/policy_status").exists() {
        let content = fs::read_to_string("/proc/ppm/policy_status").unwrap_or_default();
        for line in content.lines() {
            let is_target_1 = line.contains("FORCE_LIMIT") || line.contains("PWR_THRO") || line.contains("THERMAL") || line.contains("USER_LIMIT");
            let is_target_2 = line.contains("SYS_BOOST");
            if is_target_1 || is_target_2 {
                if let Some(idx_str) = line.split('[').nth(1).and_then(|s| s.split(']').next()) {
                    if is_target_1 {
                        write_lock(&format!("{} 1", idx_str), "/proc/ppm/policy_status");
                    }
                    if is_target_2 {
                        write_lock(&format!("{} 0", idx_str), "/proc/ppm/policy_status");
                    }
                }
            }
        }
    }
    
    ppm_fix_freq("-1"); 
    write_lock("0", "/sys/devices/platform/boot_dramboost/dramboost/dramboost");
    // [RN9] fpsgo force_onoff, GED KPI & eara_thermal dihapus

    write_lock("-1", "/sys/kernel/helio-dvfsrc/dvfsrc_force_vcore_dvfs_opp");
    write_lock("powersave", "/sys/class/devfreq/mtk-dvfsrc-devfreq/governor");
    
    if let Ok(paths) = glob::glob("/sys/devices/platform/*.dvfsrc") {
        for path in paths.flatten() {
            if let Some(p_str) = path.to_str() {
                write_lock("-1", &format!("{}/helio-dvfsrc/dvfsrc_req_ddr_opp", p_str));
            }
        }
    }

    if let Ok(paths) = glob::glob("/sys/devices/platform/soc/*.dvfsrc") {
        for path in paths.flatten() {
            if let Some(p_str) = path.to_str() {
                write_lock("powersave", &format!("{}/mtk-dvfsrc-devfreq/devfreq/mtk-dvfsrc-devfreq/governor", p_str));
            }
        }
    }

    // [RN9] gpufreq_power_limited (override termal/batt) dihapus

    write_lock("0", "/proc/perfmgr/syslimiter/syslimiter_force_disable");
    // [RN9] batoc override dihapus
    write_lock("stop 0", "/proc/pbm/pbm_stop");

    // [RN9] GPU Mali power_policy (scheduling) dihapus
}
