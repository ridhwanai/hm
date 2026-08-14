/*
 * AZenith RN9 Edition - Pengaturan Lanjutan (Tweak > Lanjutan).
 *
 * Menu ini menyambungkan backend yang sudah ada tapi belum punya UI:
 *   - azenith-memory.sh   : ZRAM + swappiness (config dir: mem)
 *   - azenith-hibernate.sh: mode hibernasi + kondisi tunda (config dir: eco)
 *   - azenith-fstrim.sh   : fstrim terjadwal (config dir: maint)
 *
 * CATATAN: jangan pakai pola bintang-garis-miring di dalam komentar blok
 * Kotlin. Kotlin mengizinkan komentar blok bersarang, jadi teks seperti
 * "mem" diikuti garis-miring-bintang akan membuka komentar baru dan bikin
 * error "Unclosed comment".
 *
 * Licensed under the Apache License, Version 2.0.
 */

package zx.azenith.ui.subscreens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.navigation.NavController
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val CFG = "/data/adb/.config/AZenith"
private const val MEM_DIR = "$CFG/mem"
private const val ECO_DIR = "$CFG/eco"
private const val MAINT_DIR = "$CFG/maint"
private const val MEM_SCRIPT = "/data/adb/modules/AZenith/azenith-memory.sh"
private const val FSTRIM_SCRIPT = "/data/adb/modules/AZenith/azenith-fstrim.sh"

private val ZRAM_ALGOS = listOf("lz4", "lzo", "lzo-rle", "zstd", "deflate")

private fun readCfg(path: String): String =
    Shell.cmd("cat '$path' 2>/dev/null").exec().out.joinToString("\n").trim()

private fun writeCfg(dir: String, path: String, value: String) {
    val escaped = value.replace("'", "'\\''")
    Shell.cmd(
        "mkdir -p '$dir'",
        "printf '%s\\n' '$escaped' > '$path'",
        "chmod 0644 '$path'"
    ).exec()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedTweakScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var loaded by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("") }

    // ---- ZRAM / memori ----
    var memEnabled by remember { mutableStateOf(true) }
    var zramMb by remember { mutableStateOf("") }
    var swappiness by remember { mutableStateOf("140") }
    var algo by remember { mutableStateOf("lz4") }
    var algoExpanded by remember { mutableStateOf(false) }

    // ---- Hibernasi lanjutan ----
    var modeDefault by remember { mutableStateOf("full") }
    var skipCharging by remember { mutableStateOf(true) }
    var skipAudio by remember { mutableStateOf(true) }

    // ---- Perawatan (fstrim) ----
    var fstrimEnabled by remember { mutableStateOf(true) }
    var fstrimInterval by remember { mutableStateOf("86400") }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val memEn = readCfg("$MEM_DIR/enabled")
            val zram = readCfg("$MEM_DIR/zram_mb")
            val swap = readCfg("$MEM_DIR/swappiness")
            val alg = readCfg("$MEM_DIR/algo")
            val mode = readCfg("$ECO_DIR/mode.default")
            val skipChg = readCfg("$ECO_DIR/skip_charging")
            val skipAud = readCfg("$ECO_DIR/skip_audio")
            val trimEn = readCfg("$MAINT_DIR/fstrim_enabled")
            val trimIv = readCfg("$MAINT_DIR/fstrim_interval")
            val memStatus = Shell.cmd("sh $MEM_SCRIPT status").exec().out.joinToString("\n")
            withContext(Dispatchers.Main) {
                memEnabled = memEn != "0"
                zramMb = zram.filter { it.isDigit() }
                if (swap.toIntOrNull() != null) swappiness = swap
                if (alg.isNotEmpty()) algo = alg
                if (mode == "restrict" || mode == "full") modeDefault = mode
                skipCharging = skipChg != "0"
                skipAudio = skipAud != "0"
                fstrimEnabled = trimEn != "0"
                if (trimIv.toIntOrNull() != null) fstrimInterval = trimIv
                statusText = memStatus
                loaded = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan Lanjutan") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            if (!loaded) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }

            // ======================= ZRAM =======================
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionHeader("Memori & ZRAM", "Perbesar swap terkompresi supaya aplikasi tidak sering di-kill (cocok untuk RAM kecil).")

                    RowSwitch(
                        title = "Aktifkan tuning memori",
                        subtitle = "Terapkan ZRAM + swappiness saat boot",
                        checked = memEnabled
                    ) { v ->
                        memEnabled = v
                        scope.launch(Dispatchers.IO) {
                            writeCfg(MEM_DIR, "$MEM_DIR/enabled", if (v) "1" else "0")
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = zramMb,
                        onValueChange = { v ->
                            if (v.length <= 5 && v.all { it.isDigit() }) {
                                zramMb = v
                                scope.launch(Dispatchers.IO) {
                                    writeCfg(MEM_DIR, "$MEM_DIR/zram_mb", v)
                                }
                            }
                        },
                        label = { Text("Ukuran ZRAM (MB)") },
                        supportingText = { Text("Kosong = otomatis 50% RAM. Dibatasi 512-3072 MB.") },
                        singleLine = true,
                        enabled = memEnabled,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = swappiness,
                        onValueChange = { v ->
                            if (v.length <= 3 && v.all { it.isDigit() }) {
                                swappiness = v
                                v.toIntOrNull()?.let { n ->
                                    if (n in 0..200) {
                                        scope.launch(Dispatchers.IO) {
                                            writeCfg(MEM_DIR, "$MEM_DIR/swappiness", n.toString())
                                        }
                                    }
                                }
                            }
                        },
                        label = { Text("Swappiness") },
                        supportingText = { Text("0-200. Default 140 (agresif memakai ZRAM).") },
                        singleLine = true,
                        enabled = memEnabled,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = algoExpanded,
                        onExpandedChange = { if (memEnabled) algoExpanded = !algoExpanded }
                    ) {
                        OutlinedTextField(
                            value = algo,
                            onValueChange = {},
                            readOnly = true,
                            enabled = memEnabled,
                            label = { Text("Algoritma kompresi") },
                            supportingText = { Text("lz4 = paling ringan untuk Helio G85.") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = algoExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = algoExpanded,
                            onDismissRequest = { algoExpanded = false }
                        ) {
                            ZRAM_ALGOS.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item) },
                                    onClick = {
                                        algo = item
                                        algoExpanded = false
                                        scope.launch(Dispatchers.IO) {
                                            writeCfg(MEM_DIR, "$MEM_DIR/algo", item)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            enabled = !busy && memEnabled,
                            onClick = {
                                scope.launch {
                                    busy = true
                                    val out = withContext(Dispatchers.IO) {
                                        Shell.cmd(
                                            "sh $MEM_SCRIPT apply-now",
                                            "sh $MEM_SCRIPT status"
                                        ).exec().out.joinToString("\n")
                                    }
                                    statusText = out
                                    busy = false
                                }
                            }
                        ) { Text("Terapkan sekarang") }
                        OutlinedButton(
                            enabled = !busy,
                            onClick = {
                                scope.launch {
                                    busy = true
                                    val out = withContext(Dispatchers.IO) {
                                        Shell.cmd("sh $MEM_SCRIPT status").exec().out.joinToString("\n")
                                    }
                                    statusText = out
                                    busy = false
                                }
                            }
                        ) { Text("Segarkan status") }
                    }

                    if (busy) {
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                    if (statusText.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            statusText,
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            // ================= Hibernasi lanjutan =================
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionHeader(
                        "Hibernasi Lanjutan",
                        "Atur seberapa keras aplikasi dibekukan saat layar mati. Daftar aplikasinya tetap di menu Hibernasi."
                    )

                    Text("Mode default", fontWeight = FontWeight.Medium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = modeDefault == "full",
                            onClick = {
                                modeDefault = "full"
                                scope.launch(Dispatchers.IO) {
                                    writeCfg(ECO_DIR, "$ECO_DIR/mode.default", "full")
                                }
                            }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Penuh (force-stop)")
                            Text(
                                "Paling hemat baterai, tapi notifikasi aplikasi mati selama hibernasi.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = modeDefault == "restrict",
                            onClick = {
                                modeDefault = "restrict"
                                scope.launch(Dispatchers.IO) {
                                    writeCfg(ECO_DIR, "$ECO_DIR/mode.default", "restrict")
                                }
                            }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Batasi saja (tanpa force-stop)")
                            Text(
                                "Aplikasi tetap hidup, notifikasi chat masih bisa masuk.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    RowSwitch(
                        title = "Jangan hibernasi saat mengisi daya",
                        subtitle = "Tunda hibernasi selama charger tersambung",
                        checked = skipCharging
                    ) { v ->
                        skipCharging = v
                        scope.launch(Dispatchers.IO) {
                            writeCfg(ECO_DIR, "$ECO_DIR/skip_charging", if (v) "1" else "0")
                        }
                    }

                    RowSwitch(
                        title = "Jangan hibernasi saat ada audio",
                        subtitle = "Aman untuk musik / podcast layar mati",
                        checked = skipAudio
                    ) { v ->
                        skipAudio = v
                        scope.launch(Dispatchers.IO) {
                            writeCfg(ECO_DIR, "$ECO_DIR/skip_audio", if (v) "1" else "0")
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = { navController.navigate("hibernate") }) {
                        Text("Buka daftar aplikasi hibernasi")
                    }
                }
            }

            // ==================== Perawatan ====================
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionHeader("Perawatan Penyimpanan", "fstrim menjaga eMMC tetap responsif. Dijalankan async supaya tidak bikin ANR.")

                    RowSwitch(
                        title = "fstrim terjadwal",
                        subtitle = "Trim otomatis setelah boot lalu berkala",
                        checked = fstrimEnabled
                    ) { v ->
                        fstrimEnabled = v
                        scope.launch(Dispatchers.IO) {
                            writeCfg(MAINT_DIR, "$MAINT_DIR/fstrim_enabled", if (v) "1" else "0")
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = fstrimInterval,
                        onValueChange = { v ->
                            if (v.length <= 6 && v.all { it.isDigit() }) {
                                fstrimInterval = v
                                v.toIntOrNull()?.let { n ->
                                    if (n >= 3600) {
                                        scope.launch(Dispatchers.IO) {
                                            writeCfg(MAINT_DIR, "$MAINT_DIR/fstrim_interval", n.toString())
                                        }
                                    }
                                }
                            }
                        },
                        label = { Text("Selang fstrim (detik)") },
                        supportingText = { Text("Minimal 3600. Default 86400 (sehari sekali).") },
                        singleLine = true,
                        enabled = fstrimEnabled,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

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
                    ) { Text("Jalankan fstrim sekarang") }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(
            subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun RowSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
