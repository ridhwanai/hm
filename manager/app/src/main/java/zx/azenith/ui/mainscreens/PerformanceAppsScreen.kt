/*
 * Copyright (C) 2026-2027 Zexshia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package zx.azenith.ui.mainscreens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import zx.azenith.ui.component.AppIconImage
import zx.azenith.ui.viewmodel.ApplistViewmodel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceAppsScreen(navController: NavHostController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val viewModel: ApplistViewmodel = viewModel()
    var query by remember { mutableStateOf("") }
    var savingPackage by remember { mutableStateOf<String?>(null) }
    var saveError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadApps(context, forceRefresh = true)
        viewModel.refreshAppConfigStatus()
    }

    val visibleApps = remember(viewModel.filteredApps, query) {
        val q = query.trim().lowercase()
        viewModel.filteredApps.filter { app ->
            q.isEmpty() ||
                app.label.lowercase().contains(q) ||
                app.packageName.lowercase().contains(q)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Aplikasi Performance") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadApps(context, true) }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Muat ulang")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Performance Apps", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Aplikasi yang dipilih akan dikenali AZenith saat menjadi aplikasi foreground dan menjalankan Performance Profile. Pengaturan global tetap diwariskan sebagai default.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            label = { Text("Cari aplikasi") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                val enabledCount = viewModel.enabledCount
                Text(
                    "$enabledCount aplikasi menggunakan Performance Profile",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )
            }

            if (viewModel.isRefreshing && visibleApps.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }

            items(visibleApps, key = { it.packageName }) { app ->
                val checked = app.isEnabledInConfig
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(enabled = savingPackage == null) {
                            savingPackage = app.packageName
                            saveError = null
                            viewModel.setPerformanceEnabled(app.packageName, !checked) { success ->
                                if (!success) saveError = "Gagal menyimpan ${app.packageName}"
                                savingPackage = null
                            }
                        }
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppIconImage(app = app, size = 44.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                app.label,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            if (app.isRecommended) {
                                Spacer(Modifier.width(8.dp))
                                AssistChip(
                                    onClick = {},
                                    label = { Text("Game") },
                                    enabled = false,
                                    modifier = Modifier.height(28.dp)
                                )
                            }
                        }
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
                        onCheckedChange = { value ->
                            savingPackage = app.packageName
                            saveError = null
                            viewModel.setPerformanceEnabled(app.packageName, value) { success ->
                                if (!success) saveError = "Gagal menyimpan ${app.packageName}"
                                savingPackage = null
                            }
                        }
                    )
                }
            }

            saveError?.let { message ->
                item {
                    Text(message, color = MaterialTheme.colorScheme.error)
                }
            }

            item { Spacer(Modifier.height(96.dp)) }
        }
    }
}
