/*
 * Copyright (C) 2026-2027 Zexshia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package zx.azenith.ui.viewmodel


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.io.SuFile
import com.topjohnwu.superuser.io.SuFileOutputStream
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import zx.azenith.R
import zx.azenith.ui.util.BackupManager
import zx.azenith.ui.util.PropertyUtils


class TweakViewModel : ViewModel() {
    data class ValidationResult(
        val isValid: Boolean, 
        val message: String, 
        val hasTweaks: Boolean, 
        val hasApplist: Boolean,
        val socType: String?,
        val data: Map<String, String>?
    )
    
    var isUiLoaded by mutableStateOf(false)
        private set


    var availableGovernors by mutableStateOf<List<String>?>(null)
    var defaultGovIndex by mutableStateOf<Int?>(null)
    var powersaveGovIndex by mutableStateOf<Int?>(null)
    var performanceGovIndex by mutableStateOf<Int?>(null)


    var availableIOSchedulers by mutableStateOf<List<String>?>(null)
    var balancedIOIndex by mutableStateOf<Int?>(null)
    var performanceIOIndex by mutableStateOf<Int?>(null)
    var powersaveIOIndex by mutableStateOf<Int?>(null)
    

    


    var dndState by mutableStateOf<Boolean?>(null)


    var currentRenderer by mutableStateOf<String?>(null)
    
    var isRendererLoading by mutableStateOf(false)
        private set
    
    private val configKeysToBackup = listOf(
        "persist.sys.azenith.soctype",
        "persist.sys.azenithconf.dnd",
        "persist.sys.azenithconf.logd",
        "persist.sys.azenith.custom_default_cpu_gov",
        "persist.sys.azenith.custom_powersave_cpu_gov",
        "persist.sys.azenith.custom_performance_cpu_gov",
        "persist.sys.azenith.custom_default_balanced_IO",
        "persist.sys.azenith.custom_performance_IO",
        "persist.sys.azenith.custom_powersave_IO"
    )
    
    
    private val APPLIST_BACKUP_KEY = "__AZENITH_APPLIST_DATA__"
    private val APPLIST_PATH = "/data/adb/.config/AZenith/gamelist/azenithApplist.json"
    
    suspend fun createConfigFileBackup(
        context: Context, 
        uri: Uri, 
        backupTweaks: Boolean, 
        backupApplist: Boolean
    ): Boolean {
        return withContext(Dispatchers.IO) {
            val propsMap = mutableMapOf<String, String>()
            

            propsMap["persist.sys.azenith.soctype"] = PropertyUtils.get("persist.sys.azenith.soctype")

            if (backupTweaks) {
                configKeysToBackup.forEach { key ->
                    if (key != "persist.sys.azenith.soctype") {
                        propsMap[key] = PropertyUtils.get(key)
                    }
                }
            }

            if (backupApplist) {

                val applistContent = Shell.cmd("cat $APPLIST_PATH").exec().out.joinToString("\n")
                if (applistContent.isNotBlank()) {
                    propsMap[APPLIST_BACKUP_KEY] = applistContent
                }
            }

            val isSuccess = BackupManager.createBackup(context, uri, propsMap)
            
            delay(1500) 
            
            isSuccess 
        }
    }
    
    suspend fun validateAndRestoreFile(context: Context, uri: Uri): ValidationResult {
        return withContext(Dispatchers.IO) {
            val backupData = BackupManager.readBackup(context, uri)
            
            if (backupData == null) {
                return@withContext ValidationResult(false, context.getString(R.string.err_invalid_backup), false, false, null, null)
            }
            val backupSocType = backupData["persist.sys.azenith.soctype"] 
                ?: backupData["persist.sys.azenithdebug.soctype"]
                
            val hasApplist = backupData.containsKey(APPLIST_BACKUP_KEY)
            
            val hasTweaks = backupData.keys.any { 
                it.startsWith("persist.sys.azenith") && 
                it != "persist.sys.azenith.soctype" &&
                it != "persist.sys.azenithdebug.soctype" 
            }
    
            ValidationResult(true, "", hasTweaks, hasApplist, backupSocType, backupData)
        }
    }

    suspend fun applyRestoreData(
        context: Context, 
        backupData: Map<String, String>, 
        restoreTweaks: Boolean, 
        restoreApplist: Boolean
    ) {
        withContext(Dispatchers.IO) {
            if (restoreTweaks) {
                backupData.forEach { (key, value) ->
                    if (key != "persist.sys.azenith.soctype" && 
                        key != "persist.sys.azenithdebug.soctype" && 
                        key != APPLIST_BACKUP_KEY && 
                        value.isNotEmpty()) {
                        
                        PropertyUtils.set(key, value)
                    }
                }
            }

            if (restoreApplist && backupData.containsKey(APPLIST_BACKUP_KEY)) {
                val applistContent = backupData[APPLIST_BACKUP_KEY]!!
                val file = SuFile(APPLIST_PATH)
                val parent = file.parentFile
                if (parent != null && !parent.exists()) {
                    parent.mkdirs()
                }
                SuFileOutputStream.open(file).use { outputStream ->
                    outputStream.write(applistContent.toByteArray())
                }
            }
            
            Shell.cmd("touch /data/adb/modules/AZenith/reboot").exec()
            if (restoreTweaks) {
                loadAllConfiguration(context)
            }
            delay(1200) 
        }
    }
    
    fun loadAllConfiguration(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            
            launch {
                
                dndState = PropertyUtils.get("persist.sys.azenithconf.dnd") == "1"

                val rawRenderer = PropertyUtils.get("debug.hwui.renderer")
                currentRenderer = if (rawRenderer.isEmpty() || rawRenderer.equals("default", ignoreCase = true)) {
                    "Default"
                } else {
                    rawRenderer
                }
            }

            val govJob = async { loadGovernorsInternal() }
            val ioJob = async { loadIOSchedulersInternal() }

            govJob.await()
            ioJob.await()

            withContext(Dispatchers.Main) {
                isUiLoaded = true
            }
        }
    }

    private fun loadGovernorsInternal() {
        val result = Shell.cmd("cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors").exec()
        if (result.isSuccess) {
            val govs = result.out.firstOrNull()?.trim()?.split("\\s+".toRegex()) ?: emptyList()
            val currentDefault = PropertyUtils.get("persist.sys.azenith.custom_default_cpu_gov").ifEmpty {
                PropertyUtils.get("persist.sys.azenith.default_cpu_gov")
            }
            val currentPowersave = PropertyUtils.get("persist.sys.azenith.custom_powersave_cpu_gov")
            val currentPerformance = PropertyUtils.get("persist.sys.azenith.custom_performance_cpu_gov")

            availableGovernors = govs
            defaultGovIndex = govs.indexOf(currentDefault).coerceAtLeast(0)
            powersaveGovIndex = govs.indexOf(currentPowersave).coerceAtLeast(0)
            performanceGovIndex = govs.indexOf(currentPerformance).coerceAtLeast(0)
        } else {
            availableGovernors = emptyList()
        }
    }

    private fun loadIOSchedulersInternal() {
        val candidates = listOf("mmcblk0", "mmcblk1", "sda", "sdb", "sdc")
        var validBlock = ""
        for (block in candidates) {
            if (Shell.cmd("test -e /sys/block/$block/queue/scheduler").exec().isSuccess) {
                validBlock = block
                break
            }
        }
        if (validBlock.isNotEmpty()) {
            val result = Shell.cmd("cat /sys/block/$validBlock/queue/scheduler").exec()
            if (result.isSuccess) {
                val rawOut = result.out.firstOrNull() ?: ""
                val schedulers = rawOut.replace("[", "").replace("]", "").trim().split("\\s+".toRegex())

                val currentBal = PropertyUtils.get("persist.sys.azenith.custom_default_balanced_IO").ifEmpty {
                    PropertyUtils.get("persist.sys.azenith.default_balanced_IO")
                }
                val currentPerf = PropertyUtils.get("persist.sys.azenith.custom_performance_IO")
                val currentEco = PropertyUtils.get("persist.sys.azenith.custom_powersave_IO")

                availableIOSchedulers = schedulers
                balancedIOIndex = schedulers.indexOf(currentBal).coerceAtLeast(0)
                performanceIOIndex = schedulers.indexOf(currentPerf).coerceAtLeast(0)
                powersaveIOIndex = schedulers.indexOf(currentEco).coerceAtLeast(0)
            } else {
                availableIOSchedulers = emptyList()
            }
        } else {
            availableIOSchedulers = emptyList()
        }
    }


    


    fun updateDefaultGovernor(index: Int) {
        defaultGovIndex = index
        val selectedGov = availableGovernors?.getOrNull(index) ?: return
        viewModelScope.launch(Dispatchers.IO) {
            PropertyUtils.set("persist.sys.azenith.custom_default_cpu_gov", selectedGov)
            val currentProfile = Shell.cmd("cat /data/adb/.config/AZenith/API/current_profile").exec().out.firstOrNull()?.trim()
            if (currentProfile == "2") {
                Shell.cmd("/data/adb/modules/AZenith/system/bin/sys.azenith-utilityconf setsgov $selectedGov").exec()
            }
        }
    }

    fun updatePowersaveGovernor(index: Int) {
        powersaveGovIndex = index
        val selectedGov = availableGovernors?.getOrNull(index) ?: return
        viewModelScope.launch(Dispatchers.IO) {
            PropertyUtils.set("persist.sys.azenith.custom_powersave_cpu_gov", selectedGov)
            val currentProfile = Shell.cmd("cat /data/adb/.config/AZenith/API/current_profile").exec().out.firstOrNull()?.trim()
            if (currentProfile == "3") {
                Shell.cmd("/data/adb/modules/AZenith/system/bin/sys.azenith-utilityconf setsgov $selectedGov").exec()
            }
        }
    }
    
    fun updatePerformanceGovernor(index: Int) {
        performanceGovIndex = index
        val selectedGov = availableGovernors?.getOrNull(index) ?: return
        viewModelScope.launch(Dispatchers.IO) {
            PropertyUtils.set("persist.sys.azenith.custom_performance_cpu_gov", selectedGov)
            val currentProfile = Shell.cmd("cat /data/adb/.config/AZenith/API/current_profile").exec().out.firstOrNull()?.trim()
            if (currentProfile == "3") {
                Shell.cmd("/data/adb/modules/AZenith/system/bin/sys.azenith-utilityconf setsgov $selectedGov").exec()
            }
        }
    }


    fun updateBalancedIO(index: Int) {
        balancedIOIndex = index
        val selectedIO = availableIOSchedulers?.getOrNull(index) ?: return
        viewModelScope.launch(Dispatchers.IO) {
            PropertyUtils.set("persist.sys.azenith.custom_default_balanced_IO", selectedIO)
            val currentProfile = Shell.cmd("cat /data/adb/.config/AZenith/API/current_profile").exec().out.firstOrNull()?.trim()
            if (currentProfile == "2") {
                Shell.cmd("/data/adb/modules/AZenith/system/bin/sys.azenith-utilityconf setsIO $selectedIO").exec()
            }
        }
    }

    fun updatePerformanceIO(index: Int) {
        performanceIOIndex = index
        val selectedIO = availableIOSchedulers?.getOrNull(index) ?: return
        viewModelScope.launch(Dispatchers.IO) {
            PropertyUtils.set("persist.sys.azenith.custom_performance_IO", selectedIO)
            val currentProfile = Shell.cmd("cat /data/adb/.config/AZenith/API/current_profile").exec().out.firstOrNull()?.trim()
            if (currentProfile == "1") {
                Shell.cmd("/data/adb/modules/AZenith/system/bin/sys.azenith-utilityconf setsIO $selectedIO").exec()
            }
        }
    }

    fun updatePowersaveIO(index: Int) {
        powersaveIOIndex = index
        val selectedIO = availableIOSchedulers?.getOrNull(index) ?: return
        viewModelScope.launch(Dispatchers.IO) {
            PropertyUtils.set("persist.sys.azenith.custom_powersave_IO", selectedIO)
            val currentProfile = Shell.cmd("cat /data/adb/.config/AZenith/API/current_profile").exec().out.firstOrNull()?.trim()
            if (currentProfile == "3") {
                Shell.cmd("/data/adb/modules/AZenith/system/bin/sys.azenith-utilityconf setsIO $selectedIO").exec()
            }
        }
    }
    




    fun updateDndMode(checked: Boolean) {
        dndState = checked
        viewModelScope.launch(Dispatchers.IO) {
            PropertyUtils.set("persist.sys.azenithconf.dnd", if (checked) "1" else "0")
        }
    }



    fun executeSetRenderer(reason: String, context: Context) {
        isRendererLoading = true
        Shell.cmd("/data/adb/modules/AZenith/system/bin/sys.azenith-utilityconf setrender $reason").submit {
            viewModelScope.launch {
                delay(1000)
                loadAllConfiguration(context)
                isRendererLoading = false
            }
        }
    }
    

}
