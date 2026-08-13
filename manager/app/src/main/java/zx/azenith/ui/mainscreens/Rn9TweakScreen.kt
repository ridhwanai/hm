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
 *
 * RN9 Edition: on/off + tuning untuk fitur tambahan (hibernasi lanjutan,
 * ZRAM + swappiness, fstrim terjadwal). Semua disimpan sebagai file config
 * yang dibaca skrip modul, jadi tidak butuh reboot untuk sebagian besar nilai.
 */

package zx.azenith.ui.mainscreens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val CONFIG_ROOT = "/data/adb/.config/AZenith"
private const val ECO_DIR = "$CONFIG_ROOT/eco"
private const val MEM_DIR = "$CONFIG_ROOT/mem"
private const val MAINT_DIR = "$CONFIG_ROOT/maint"

private const val MODE_DEFAULT = "$ECO_DIR/mode.default"
private const val SKIP_CHARGING = "$ECO_DIR/skip_charging"
private const val SKIP_AUDIO = "$ECO_DIR/skip_audio"

private const val MEM_ENABLED = "$MEM_DIR/enabled"
private const val MEM_ZRAM = "$MEM_DIR/zram_mb"
private const val MEM_SWAP = "$MEM_DIR/swappiness"
private const val MEM_ALGO = "$MEM_DIR/algo"

private const val MAINT_ENABLED = "$MAINT_DIR/fstrim_enabled"
private const val MAINT_DELAY = "$MAINT_DIR/fstrim_delay"
private const val MAINT_INTERVAL = "$MAINT_DIR/fstrim_interval"

private const val MEM_SCRIPT = "/data/adb/modules/AZenith/azenith-memory.sh"
private const val FSTRIM_SCRIPT = "/data/adb/modules/AZenith/azenith-fstrim.sh"

private fun shellRead(path: String): String =
    Shell.cmd("cat '$path' 2>/dev/null").exec().out.joinToString("\n").trim()

private fun shellWrite(path: String, content: String) {
    val dir = path.substringBeforeLast('/')
    val escaped = content.replace("'", "'\\''")
    Shell.cmd(
        "mkdir -p '$dir'",
        "printf '%s' '$escaped' > '$path'",
        "chmod 0644 '$path'"
    ).exec()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Rn9TweakScreen(navController: NavHostController) {
    val scope = rememberCoroutineScope()

    var loaded by remember { mutableStateOf(false) }

    // Hibernasi lanjutan
    var modeRestrict by remember { mutableStateOf(false) }
    var skipCharging by remember { mutableStateOf(true) }
    var skipAudio by remember { mutableStateOf(true) }

    // ZRAM + swappiness
    var memEnabled by remember { mutableStateOf(true) }
    var zramText by remember { mutableStateOf("") }
    var swapText by remember { mutableStateOf("140") }
    var algoText by remember { mutableStateOf("lz4") }

    // fstrim
    var fstrimEnabled by remember { mutableStateOf(true) }
    var delayText by remember { mutableStateOf("300") }
    var intervalText by remember { mutableStateOf("86400") }

    var busy by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            Shell.cmd(
                "mkdir -p '$ECO_DIR' '$MEM_DIR' '$MAINT_DIR'"
            ).exec()
            val mode = shellRead(MODE_DEFAULT)
            val sc = shellRead(SKIP_CHARGING)
            val sa = shellRead(SKIP_AUDIO)
            val me = shellRead(MEM_ENABLED)
            val zr = shellRead(MEM_ZRAM)
            val sw = shellRead(MEM_SWAP)
            val al = shellRead(MEM_ALGO)
            val fe = shellRead(MAINT_ENABLED)
            val dl = shellRead(MAINT_DELAY)
            val iv = shellRead(MAINT_INTERVAL)
            withContext(Dispatchers.Main) {
                modeRestrict = mode == "restrict"
                skipCharging = sc != "0"
                skipAudio = sa != "0"
                memEnabled = me != "0"
                if (zr.toIntOrNull() != null) zramText = zr
                if (sw.toIntOrNull() != null) swapText = sw
                if (al.isNotEmpty()) algoText = al
                fstrimEnabled = fe != "0"
                if (dl.toIntOrNull() != null) delayText = dl
                if (iv.toIntOrNull() != null) intervalText = iv
                loaded = true
            }
        }
    }

    fun save(path: String, value: String) {
        scope.launch(Dispatchers.IO) { shellWrite(path, value) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Optimasi RN9") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        if (!loaded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ---------- Hibernasi lanjutan ----------
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Hibernasi lanjutan", fontWeight = FontWeight.Bold)
                        Text(
                            "Berlaku untuk aplikasi yang dipilih di menu Hibernasi.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))

                        SwitchRow(
                            title = "Mode default: Restrict (jangan matikan app)",
                            subtitle = "Aktif = Dibatasi tanpa force-stop (notifikasi tetap masuk). " +
                                "Nonaktif = Full (force-stop, paling hemat).",
                            checked = modeRestrict
                        ) { v ->
                            modeRestrict = v
                            save(MODE_DEFAULT, if (v) "restrict" else "full")
                        }
                        Spacer(Modifier.height(8.dp))
                        SwitchRow(
                            title = "Jangan hibernasi saat mengisi daya",
                            subtitle = "Tunda hibernasi selama charger tersambung.",
                            checked = skipCharging
                        ) { v ->
                            skipCharging = v
                            save(SKIP_CHARGING, if (v) "1" else "0")
                        }
                        Spacer(Modifier.height(8.dp))
                        SwitchRow(
                            title = "Jangan hibernasi saat audio aktif",
                            subtitle = "Tunda hibernasi saat ada pemutaran audio (mis. musik layar mati).",
                            checked = skipAudio
                        ) { v ->
                            skipAudio = v
                            save(SKIP_AUDIO, if (v) "1" else "0")
                        }
                    }
                }
            }

            // ---------- ZRAM + swappiness ----------
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Memori: ZRAM + swappiness", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        SwitchRow(
                            title = "Aktifkan tuning memori",
                            subtitle = "Resize ZRAM dan set swappiness saat boot.",
                            checked = memEnabled
                        ) { v ->
                            memEnabled = v
                            save(MEM_ENABLED, if (v) "1" else "0")
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = zramText,
                            onValueChange = { v ->
                                if (v.length <= 5 && v.all { it.isDigit() }) {
                                    zramText = v
                                    v.toIntOrNull()?.let { save(MEM_ZRAM, it.toString()) }
                                }
                            },
                            label = { Text("Ukuran ZRAM (MB)") },
                            supportingText = { Text("Kosong = otomatis 50% RAM. Batas 512-3072.") },
                            singleLine = true,
                            enabled = memEnabled,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = swapText,
                            onValueChange = { v ->
                                if (v.length <= 3 && v.all { it.isDigit() }) {
                                    swapText = v
                                    v.toIntOrNull()?.let { n ->
                                        if (n in 0..200) save(MEM_SWAP, n.toString())
                                    }
                                }
                            },
                            label = { Text("Swappiness (0-200)") },
                            supportingText = { Text("RAM kecil cocok nilai agresif. Default 140.") },
                            singleLine = true,
                            enabled = memEnabled,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = algoText,
                            onValueChange = { v ->
                                algoText = v.trim()
                                if (algoText.isNotEmpty()) save(MEM_ALGO, algoText)
                            },
                            label = { Text("Algoritma kompresi") },
                            supportingText = { Text("mis. lz4, lzo, zstd. Default lz4.") },
                            singleLine = true,
                            enabled = memEnabled,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            enabled = !busy,
                            onClick = {
                                scope.launch {
                                    busy = true
                                    val out = withContext(Dispatchers.IO) {
                                        Shell.cmd(
                                            "sh $MEM_SCRIPT apply",
                                            "sh $MEM_SCRIPT status"
                                        ).exec().out.joinToString("\n")
                                    }
                                    statusText = out
                                    busy = false
                                }
                            }
                        ) { Text("Terapkan sekarang") }
                    }
                }
            }

            // ---------- fstrim ----------
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("fstrim terjadwal", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        SwitchRow(
                            title = "Aktifkan fstrim terjadwal",
                            subtitle = "Trim otomatis beberapa menit setelah boot, lalu berkala.",
                            checked = fstrimEnabled
                        ) { v ->
                            fstrimEnabled = v
                            save(MAINT_ENABLED, if (v) "1" else "0")
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = delayText,
                            onValueChange = { v ->
                                if (v.length <= 5 && v.all { it.isDigit() }) {
                                    delayText = v
                                    v.toIntOrNull()?.let { n ->
                                        if (n >= 30) save(MAINT_DELAY, n.toString())
                                    }
                                }
                            },
                            label = { Text("Tunda setelah boot (detik)") },
                            supportingText = { Text("Minimal 30. Default 300 (5 menit).") },
                            singleLine = true,
                            enabled = fstrimEnabled,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = intervalText,
                            onValueChange = { v ->
                                if (v.length <= 7 && v.all { it.isDigit() }) {
                                    intervalText = v
                                    v.toIntOrNull()?.let { n ->
                                        if (n >= 3600) save(MAINT_INTERVAL, n.toString())
                                    }
                                }
                            },
                            label = { Text("Jeda antar trim (detik)") },
                            supportingText = { Text("Minimal 3600. Default 86400 (24 jam).") },
                            singleLine = true,
                            enabled = fstrimEnabled,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            enabled = !busy,
                            onClick = {
                                scope.launch {
                                    busy = true
                                    val out = withContext(Dispatchers.IO) {
                                        Shell.cmd(
                                            "sh $FSTRIM_SCRIPT once",
                                            "sh $FSTRIM_SCRIPT status"
                                        ).exec().out.joinToString("\n")
                                    }
                                    statusText = out
                                    busy = false
                                }
                            }
                        ) { Text("Trim sekarang") }
                    }
                }
            }

            if (busy) {
                item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
            }
            if (statusText.isNotBlank()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            statusText,
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
