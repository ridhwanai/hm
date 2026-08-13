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
 * RN9 Edition: screen-off hibernation manager.
 */

package zx.azenith.ui.mainscreens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import zx.azenith.ui.component.AppIconImage
import zx.azenith.ui.viewmodel.ApplistViewmodel

private const val ECO_DIR = "/data/adb/.config/AZenith/eco"
private const val ECO_LIST = "$ECO_DIR/hibernate.list"
private const val LEGACY_ECO_DIR = "/data/adb/.config/AZenith/hibernate"
private const val ECO_ENABLED = "$ECO_DIR/enabled"
private const val ECO_DELAY = "$ECO_DIR/delay"
private const val HIB_SCRIPT = "/data/adb/modules/AZenith/azenith-hibernate.sh"

/** Packages that must never be frozen, mirrors azenith-hibernate.sh */
private val PROTECTED = setOf(
    "android",
    "com.android.systemui",
    "com.android.phone",
    "com.google.android.gms",
    "zx.azenith"
)

private fun isProtected(pkg: String): Boolean =
    PROTECTED.contains(pkg) || pkg.contains("inputmethod")

private fun shellRead(path: String): String =
    Shell.cmd("cat '$path' 2>/dev/null").exec().out.joinToString("\n")

private fun shellWrite(path: String, content: String) {
    val escaped = content.replace("'", "'\\''")
    Shell.cmd(
        "mkdir -p '$ECO_DIR'",
        "printf '%s' '$escaped' > '$path'",
        "chmod 0644 '$path'"
    ).exec()
}

/** Reads hibernate.list, ignoring comments and blank lines. */
private fun readSelected(): Set<String> =
    shellRead(ECO_LIST)
        .lines()
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("#") }
        .toSet()

/** Rewrites hibernate.list, preserving the header comments. */
private fun writeSelected(pkgs: Set<String>) {
    val header = buildString {
        appendLine("# ==========================================================")
        appendLine("#  AZenith-RN9 :: Daftar Aplikasi untuk Hibernasi Layar Mati")
        appendLine("# ==========================================================")
        appendLine("#")
        appendLine("#  Dikelola lewat menu Hibernasi di aplikasi.")
        appendLine("#  Boleh juga diedit manual, satu nama paket per baris.")
        appendLine("#")
    }
    shellWrite(ECO_LIST, header + pkgs.sorted().joinToString("\n") + "\n")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HibernationScreen(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewModel: ApplistViewmodel = viewModel()

    var selected by remember { mutableStateOf<Set<String>>(emptySet()) }
    var enabled by remember { mutableStateOf(true) }
    var delayText by remember { mutableStateOf("300") }
    var query by remember { mutableStateOf("") }
    var loaded by remember { mutableStateOf(false) }
    var hibernateBusy by remember { mutableStateOf(false) }
    var hibernateStatus by remember { mutableStateOf("") }

    // Initial load of config + app list
    LaunchedEffect(Unit) {
        viewModel.loadApps(context)
        withContext(Dispatchers.IO) {
            Shell.cmd(
                "mkdir -p '$ECO_DIR'",
                "if [ ! -f '$ECO_LIST' ] && [ -f '$LEGACY_ECO_DIR/hibernate.list' ]; then cp '$LEGACY_ECO_DIR/hibernate.list' '$ECO_LIST'; fi",
                "if [ ! -f '$ECO_ENABLED' ] && [ -f '$LEGACY_ECO_DIR/enabled' ]; then cp '$LEGACY_ECO_DIR/enabled' '$ECO_ENABLED'; fi",
                "if [ ! -f '$ECO_DELAY' ] && [ -f '$LEGACY_ECO_DIR/delay' ]; then cp '$LEGACY_ECO_DIR/delay' '$ECO_DELAY'; fi"
            ).exec()
            val sel = readSelected()
            val en = shellRead(ECO_ENABLED).trim()
            val dl = shellRead(ECO_DELAY).trim()
            withContext(Dispatchers.Main) {
                selected = sel
                enabled = en != "0"
                if (dl.toIntOrNull() != null) delayText = dl
                loaded = true
            }
        }
    }

    val persist: (Set<String>) -> Unit = { newSet ->
        selected = newSet
        scope.launch(Dispatchers.IO) { writeSelected(newSet) }
    }

    var savingPackage by remember { mutableStateOf<String?>(null) }

    val toggleApp: (String, Boolean) -> Unit = { pkg, on ->
        val newSet = if (on) selected + pkg else selected - pkg
        savingPackage = pkg
        selected = newSet
        scope.launch {
            withContext(Dispatchers.IO) { writeSelected(newSet) }
            savingPackage = null
        }
    }

    // Only user-selectable apps: installed, not protected
    val visibleApps = remember(viewModel.filteredApps, query, selected) {
        viewModel.filteredApps.filter { app ->
            val q = query.trim().lowercase()
            val matches = q.isEmpty() ||
                app.label.lowercase().contains(q) ||
                app.packageName.lowercase().contains(q)
            val systemOk = !app.isSystem
            matches && systemOk && !isProtected(app.packageName)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hibernasi & Baterai") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ---------- Section: master switch + delay ----------
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Hibernasi saat layar mati", fontWeight = FontWeight.Medium)
                                Text(
                                    "Bekukan aplikasi terpilih setelah layar mati",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = enabled,
                                onCheckedChange = { v ->
                                    enabled = v
                                    scope.launch(Dispatchers.IO) {
                                        shellWrite(ECO_ENABLED, if (v) "1" else "0")
                                    }
                                }
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Spacer(Modifier.height(12.dp))

                        Text(
                            "Uji langsung tanpa menunggu layar mati:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                enabled = !hibernateBusy,
                                onClick = {
                                    scope.launch {
                                        hibernateBusy = true
                                        val out = withContext(Dispatchers.IO) {
                                            Shell.cmd(
                                                "sh $HIB_SCRIPT apply --force",
                                                "sh $HIB_SCRIPT status"
                                            ).exec().out.joinToString("\n")
                                        }
                                        hibernateStatus = out
                                        hibernateBusy = false
                                    }
                                }
                            ) { Text("Hibernasi sekarang") }
                            OutlinedButton(
                                enabled = !hibernateBusy,
                                onClick = {
                                    scope.launch {
                                        hibernateBusy = true
                                        val out = withContext(Dispatchers.IO) {
                                            Shell.cmd(
                                                "sh $HIB_SCRIPT release --force",
                                                "sh $HIB_SCRIPT status"
                                            ).exec().out.joinToString("\n")
                                        }
                                        hibernateStatus = out
                                        hibernateBusy = false
                                    }
                                }
                            ) { Text("Lepas") }
                        }
                        if (hibernateBusy) {
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }
                        if (hibernateStatus.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                hibernateStatus,
                                fontFamily = FontFamily.Monospace,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = delayText,
                            onValueChange = { v ->
                                if (v.length <= 5 && v.all { it.isDigit() }) {
                                    delayText = v
                                    v.toIntOrNull()?.let { n ->
                                        if (n in 10..7200) {
                                            scope.launch(Dispatchers.IO) {
                                                shellWrite(ECO_DELAY, n.toString())
                                            }
                                        }
                                    }
                                }
                            },
                            label = { Text("Jeda sebelum hibernasi (detik)") },
                            supportingText = { Text("Antara 10 dan 7200. Default 300.") },
                            singleLine = true,
                            enabled = enabled,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // ---------- Section: selection controls ----------
            item {
                Column {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text("Cari aplikasi") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "${selected.size} dipilih",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = {
                            persist(selected + visibleApps.map { it.packageName }.toSet())
                        }) { Text("Pilih semua") }
                        TextButton(onClick = {
                            persist(selected - visibleApps.map { it.packageName }.toSet())
                        }) { Text("Kosongkan") }
                    }
                }
            }

            if (!loaded || viewModel.isRefreshing) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                }
            }

            // ---------- Section: app list ----------
            items(visibleApps, key = { it.packageName }) { app ->
                val checked = selected.contains(app.packageName)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(enabled = savingPackage == null) {
                            toggleApp(app.packageName, !checked)
                        }
                        .padding(vertical = 8.dp, horizontal = 4.dp)
                ) {
                    AppIconImage(app = app, size = 44.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            app.label,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            app.packageName,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = checked,
                        enabled = savingPackage == null,
                        onCheckedChange = { v -> toggleApp(app.packageName, v) }
                    )
                }
            }

            item { Spacer(Modifier.height(96.dp)) }
        }
    }
}
