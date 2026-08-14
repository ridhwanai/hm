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

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package zx.azenith.ui.mainscreens


import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.topjohnwu.superuser.Shell
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import zx.azenith.R
import zx.azenith.ui.component.*
import zx.azenith.ui.util.PropertyUtils
import zx.azenith.ui.util.*
import zx.azenith.ui.viewmodel.TweakViewModel


@Composable
fun TweakScreen(
    navController: NavController,
    viewModel: TweakViewModel = viewModel()
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val colorScheme = MaterialTheme.colorScheme
    var showBackupRestoreSheet by remember { mutableStateOf(false) }
    var showRendererDialog by remember { mutableStateOf(false) }
    var pendingRestoreData by remember { mutableStateOf<Map<String, String>?>(null) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    
    var showBackupOptionsDialog by remember { mutableStateOf(false) }
    var optBackupTweaks by remember { mutableStateOf(true) }
    var optBackupApplist by remember { mutableStateOf(true) }

    var pendingRestoreResult by remember { mutableStateOf<TweakViewModel.ValidationResult?>(null) }
    var optRestoreTweaks by remember { mutableStateOf(true) }
    var optRestoreApplist by remember { mutableStateOf(true) }
    
    val loadingDialog = rememberLoadingDialog()
    val confirmDialog = rememberConfirmDialog(onConfirm = {}, onDismiss = {})
    
    LoadingDialogHost(handle = loadingDialog)
    ConfirmDialogHost(handle = confirmDialog)

    val createDocLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        uri?.let {
            scope.launch {
                val success = loadingDialog.withLoading {
                    viewModel.createConfigFileBackup(context, it, optBackupTweaks, optBackupApplist)
                }
                if (success) {
                    snackbarHostState.showSnackbar(context.getString(R.string.dialog_backup_success))
                } else {
                    snackbarHostState.showSnackbar(context.getString(R.string.dialog_backup_fail))
                }
            }
        }
    }
    
    val openDocLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            showBackupRestoreSheet = false
            scope.launch {
                loadingDialog.withLoading {
                    val result = viewModel.validateAndRestoreFile(context, it)
                    if (result.isValid && result.data != null) {
                        pendingRestoreResult = result
                        optRestoreTweaks = result.hasTweaks
                        optRestoreApplist = result.hasApplist
                        showRestoreDialog = true 
                    } else {
                        confirmDialog.showConfirm(context.getString(R.string.dialog_restore_fail_title), result.message, context.getString(android.R.string.ok), null)
                    }
                }
            }
        }
    }


    LaunchedEffect(Unit) {
        viewModel.loadAllConfiguration(context)
    }

    MaterialExpressiveTheme {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                TweakScreenTopAppBar(
                    scrollBehavior = scrollBehavior,
                    onMoreClick = { showBackupRestoreSheet = true }
                )
            },
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.padding(
                        bottom = 100.dp
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.surface
        ) { innerPadding ->
            
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 110.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                )
            ) {
            
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    ExpressiveList(
                        content = listOf( 
                            {
                                ExpressiveInfoCard(
                                    supportingContent = { Text(text = stringResource(R.string.str_these_settings_apply_to_all_en)) },
                                    leadingContent = { LeadingIcon(icon = Icons.Filled.Info) },
                                    containerColor = colorScheme.surfaceContainerLow,
                                    onClick = {}
                                )
                            }
                        )
                    )
                }

                // [RN9] Perf Lite Mode (batasi frekuensi) dihapus

                item { TweaksSectionTitle(stringResource(R.string.section_additionalsettings)) }
                item {
                    if (viewModel.dndState != null) {
                        ExpressiveList(
                            content = listOf(
                                {
                                    ExpressiveSwitchItem(
                                        icon = Icons.Rounded.DoNotDisturbOn,
                                        title = stringResource(R.string.dnd_mode_gaming),
                                        summary = stringResource(R.string.dnd_mode_gaming_desc),
                                        checked = viewModel.dndState!!,
                                        onCheckedChange = { viewModel.updateDndMode(it) }
                                    )
                                }
                            )
                        )
                    } else {
                        SectionLoadingIndicator()
                    }
                }

                item { TweaksSectionTitle(stringResource(R.string.section_renderer)) }
                item {
                    if (viewModel.currentRenderer != null) {
                        ExpressiveList(
                            content = listOf(
                                {
                                    ExpressiveListItem(
                                        leadingContent = { LeadingIcon(icon = Icons.Rounded.SettingsSuggest) },
                                        onClick = { showRendererDialog = true },
                                        headlineContent = { Text(stringResource(R.string.renderengine)) },
                                        supportingContent = {
                                            Text(
                                                if (viewModel.isRendererLoading)
                                                    stringResource(R.string.renderer_applying)
                                                else
                                                    viewModel.currentRenderer!!
                                            )
                                        },
                                        trailingContent = {
                                            if (viewModel.isRendererLoading) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(20.dp),
                                                    strokeWidth = 2.dp
                                                )
                                            } else {
                                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
                                            }
                                        }
                                    )
                                }
                            )
                        )
                    } else {
                        SectionLoadingIndicator()
                    }
                }

                item { TweaksSectionTitle(stringResource(R.string.section_CPUSettings)) }
                item {
                    if (viewModel.defaultGovIndex != null && 
                        viewModel.powersaveGovIndex != null && 
                        viewModel.performanceGovIndex != null) {
                        ExpressiveList(
                            content = listOf(
                                {
                                    ExpressiveDropdownItem(
                                        icon = Icons.Outlined.Water,
                                        title = stringResource(R.string.default_cpu_gov),
                                        summary = stringResource(R.string.default_cpu_gov_desc),
                                        items = viewModel.availableGovernors ?: emptyList(),
                                        selectedIndex = viewModel.defaultGovIndex!!,
                                        onItemSelected = { viewModel.updateDefaultGovernor(it) }
                                    )
                                },
                                {
                                    ExpressiveDropdownItem(
                                        icon = Icons.Outlined.OfflineBolt,
                                        title = stringResource(R.string.performance_cpu_gov),
                                        summary = stringResource(R.string.performance_cpu_gov_desc),
                                        items = viewModel.availableGovernors ?: emptyList(),
                                        selectedIndex = viewModel.performanceGovIndex!!,
                                        onItemSelected = { viewModel.updatePerformanceGovernor(it) }
                                    )
                                },
                                {
                                    ExpressiveDropdownItem(
                                        icon = Icons.Outlined.EnergySavingsLeaf,
                                        title = stringResource(R.string.powersave_cpu_gov),
                                        summary = stringResource(R.string.powersave_cpu_gov_desc),
                                        items = viewModel.availableGovernors ?: emptyList(),
                                        selectedIndex = viewModel.powersaveGovIndex!!,
                                        onItemSelected = { viewModel.updatePowersaveGovernor(it) }
                                    )
                                }
                            )
                        )
                    } else {
                        SectionLoadingIndicator()
                    }
                }

                item { TweaksSectionTitle(stringResource(R.string.io_settings)) }
                item {
                    if (viewModel.availableIOSchedulers == null) {
                        SectionLoadingIndicator()
                    } else if (viewModel.availableIOSchedulers!!.isNotEmpty()) {
                        if (viewModel.balancedIOIndex != null && 
                            viewModel.performanceIOIndex != null && 
                            viewModel.powersaveIOIndex != null) {
                            ExpressiveList(
                                content = listOf(
                                    {
                                        ExpressiveDropdownItem(
                                            icon = Icons.Outlined.Water,
                                            title = stringResource(R.string.balanced_io_scheduler),
                                            summary = stringResource(R.string.balanced_io_scheduler_desc),
                                            items = viewModel.availableIOSchedulers ?: emptyList(),
                                            selectedIndex = viewModel.balancedIOIndex!!,
                                            onItemSelected = { viewModel.updateBalancedIO(it) }
                                        )
                                    },
                                    {
                                        ExpressiveDropdownItem(
                                            icon = Icons.Outlined.OfflineBolt,
                                            title = stringResource(R.string.performance_io_scheduler),
                                            summary = stringResource(R.string.performance_io_scheduler_desc),
                                            items = viewModel.availableIOSchedulers ?: emptyList(),
                                            selectedIndex = viewModel.performanceIOIndex!!,
                                            onItemSelected = { viewModel.updatePerformanceIO(it) }
                                        )
                                    },
                                    {
                                        ExpressiveDropdownItem(
                                            icon = Icons.Outlined.EnergySavingsLeaf,
                                            title = stringResource(R.string.powersave_io_scheduler),
                                            summary = stringResource(R.string.powersave_io_scheduler_desc),
                                            items = viewModel.availableIOSchedulers ?: emptyList(),
                                            selectedIndex = viewModel.powersaveIOIndex!!,
                                            onItemSelected = { viewModel.updatePowersaveIO(it) }
                                        )
                                    }
                                )
                            )
                        } else {
                            SectionLoadingIndicator()
                        }
                    } else {

                    }
                }
                

                
                item { TweaksSectionTitle("Pengaturan Lanjutan") }
                item {
                    ExpressiveList(
                        content = listOf(
                            {
                                ExpressiveListItem(
                                    leadingContent = { LeadingIcon(icon = Icons.Outlined.Memory) },
                                    onClick = { navController.navigate("advanced_tweaks") },
                                    headlineContent = { Text("ZRAM, Hibernasi Lanjutan & fstrim") },
                                    supportingContent = { Text("Ukuran ZRAM, swappiness, mode hibernasi, dan trim penyimpanan") },
                                    trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null) }
                                )
                            }
                        )
                    )
                }

                item { TweaksSectionTitle(stringResource(R.string.section_addons)) }
                item {
                    ExpressiveList(
                        content = listOf(
                            {
                                ExpressiveListItem(
                                    leadingContent = { LeadingIcon(icon = Icons.Filled.AddToPhotos) },
                                    onClick = { navController.navigate("preferenced") },
                                    headlineContent = { Text(stringResource(R.string.prefs)) },
                                    supportingContent = { Text(stringResource(R.string.prefsdesc)) },
                                    trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null) }
                                )
                            }
                        )
                    )
                }
            }
        }
        
        RootAppDialog {
            BackupRestoreBottomSheet(
                show = showBackupRestoreSheet,
                onDismiss = { showBackupRestoreSheet = false },
                onBackup = { 
                    showBackupRestoreSheet = false
                    showBackupOptionsDialog = true
                },
                onRestore = { 
                    openDocLauncher.launch(arrayOf("application/octet-stream", "*/*")) 
                }
            )
        }


        RootAppDialog {
            CustomContentDialog(
                visible = showBackupOptionsDialog,
                title = context.getString(R.string.dialog_backup_options_title),
                confirmText = context.getString(R.string.dialog_backup_options_confirm),
                confirmEnabled = optBackupTweaks || optBackupApplist,
                onDismiss = { showBackupOptionsDialog = false },
                onConfirm = {
                    showBackupOptionsDialog = false
                    val sdf = java.text.SimpleDateFormat("ddMMyyyy_HHmmss", java.util.Locale.getDefault())
                    val timestamp = sdf.format(java.util.Date())
                    val dynamicFileName = "AZenithConfig_Backup_$timestamp.zx"
                    createDocLauncher.launch(dynamicFileName) 
                }
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.str_select_the_configurations_you),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { optBackupTweaks = !optBackupTweaks }) {
                        Checkbox(checked = optBackupTweaks, onCheckedChange = { optBackupTweaks = it })
                        Text(stringResource(R.string.str_tweak_configuration_settings), color = MaterialTheme.colorScheme.onSurface)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { optBackupApplist = !optBackupApplist }) {
                        Checkbox(checked = optBackupApplist, onCheckedChange = { optBackupApplist = it })
                        Text(stringResource(R.string.str_per_app_applist_settings), color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }


        RootAppDialog {
            CustomContentDialog(
                visible = showRestoreDialog,
                title = context.getString(R.string.str_restore_configuration),
                confirmText = context.getString(R.string.dialog_restore_confirm),
                confirmEnabled = pendingRestoreResult?.let { result ->
                    val currentSocType = PropertyUtils.get("persist.sys.azenith.soctype")
                    val isSocMismatch = result.socType != currentSocType
                    (optRestoreTweaks && !isSocMismatch) || optRestoreApplist
                } ?: false,
                onDismiss = { showRestoreDialog = false },
                onConfirm = {
                    showRestoreDialog = false
                    

                    pendingRestoreResult?.let { result ->
                        val dataToRestore = result.data
                        val currentSocType = PropertyUtils.get("persist.sys.azenith.soctype")
                        val isSocMismatch = result.socType != currentSocType
                        
                        if (dataToRestore != null) {
                            scope.launch {
                                loadingDialog.withLoading {
                                    viewModel.applyRestoreData(context, dataToRestore, optRestoreTweaks && !isSocMismatch, optRestoreApplist)
                                    navController.navigate("home") {
                                        popUpTo(navController.graph.startDestinationId)
                                        launchSingleTop = true
                                    }
                                }
                            }
                        }
                    }
                }
            ) {

                pendingRestoreResult?.let { result ->
                    val socName = zx.azenith.ui.util.BackupManager.getSocName(result.socType)
                    val currentSocType = PropertyUtils.get("persist.sys.azenith.soctype")
                    val isSocMismatch = result.socType != currentSocType
        
                    Column {
                        Text(
                            text = stringResource(R.string.str_backup_content_detected_select),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        
                        if (isSocMismatch && result.hasTweaks) {
                            Text(
                                stringResource(R.string.str_warning_backup_is_for_socname, socName),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(Modifier.height(8.dp))
                        }
        
                        if (result.hasTweaks) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { if (!isSocMismatch) optRestoreTweaks = !optRestoreTweaks }) {
                                Checkbox(
                                    checked = optRestoreTweaks && !isSocMismatch, 
                                    onCheckedChange = { if (!isSocMismatch) optRestoreTweaks = it },
                                    enabled = !isSocMismatch
                                )
                                Text(stringResource(R.string.str_tweak_configuration_settings), color = if (isSocMismatch) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                        if (result.hasApplist) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { optRestoreApplist = !optRestoreApplist }) {
                                Checkbox(checked = optRestoreApplist, onCheckedChange = { optRestoreApplist = it })
                                Text(stringResource(R.string.str_per_app_applist_settings), color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        }
        
        RootAppDialog {
            RendererDialog(
                show = showRendererDialog,
                onDismiss = { showRendererDialog = false },
                onRenderer = { reason -> viewModel.executeSetRenderer(reason, context) }
            )
        }
    }
}

@Composable
fun SectionLoadingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(32.dp),
            strokeWidth = 3.dp
        )
    }
}

@Composable
fun TweaksSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(
            start = 12.dp,
            end = 12.dp,
            top = 16.dp,
            bottom = 8.dp
        )
    )
}

@Composable
fun ExpressiveTile(
    modifier: Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    highlight: Boolean,
    showArrow: Boolean = false,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    val cardBgColor = colorScheme.surfaceColorAtElevation(1.dp)

    val iconBoxBgColor by animateColorAsState(
        targetValue = if (highlight) colorScheme.primaryContainer else colorScheme.surfaceVariant.copy(alpha = 0.5f),
        animationSpec = tween(400), 
        label = "iconBoxBgColorAnim"
    )

    val iconColor by animateColorAsState(
        targetValue = if (highlight) colorScheme.onPrimaryContainer else colorScheme.onSurfaceVariant,
        animationSpec = tween(400),
        label = "iconColorAnim"
    )

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(26.dp))
            .clickable { onClick() }
            .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)),
        color = cardBgColor,
        shape = RoundedCornerShape(26.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp) 
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(82.dp)  
                    .clip(RoundedCornerShape(18.dp)) 
                    .background(iconBoxBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon, 
                    contentDescription = null, 
                    tint = iconColor,
                    modifier = Modifier.size(36.dp) 
                )

                if (showArrow) {
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight, 
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(20.dp), 
                        tint = iconColor.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            
            Column(
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(
                    text = label, 
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AnimatedContent(
                        targetState = value,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(300, delayMillis = 100)) +
                             scaleIn(initialScale = 0.95f, animationSpec = tween(300, delayMillis = 100)))
                                .togetherWith(
                                    fadeOut(animationSpec = tween(200)) +
                                    scaleOut(targetScale = 1.05f, animationSpec = tween(200))
                                )
                        },
                        label = "ValueTextAnimation"
                    ) { targetValue ->
                        Text(
                            text = targetValue, 
                            style = MaterialTheme.typography.bodyMedium, 
                            color = colorScheme.onSurfaceVariant, 
                            maxLines = 2, 
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = androidx.compose.ui.unit.TextUnit.Unspecified
                        )
                    }
                    

                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = colorScheme.primary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}


@Composable
fun TweakScreenTopAppBar(scrollBehavior: TopAppBarScrollBehavior, onMoreClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme

    val smoothGradient = Brush.verticalGradient(
        0.0f to colorScheme.surface,
        0.4f to colorScheme.surface.copy(alpha = 0.9f),
        0.5f to colorScheme.surface.copy(alpha = 0.8f),
        0.6f to colorScheme.surface.copy(alpha = 0.7f),
        0.7f to colorScheme.surface.copy(alpha = 0.5f),
        0.8f to colorScheme.surface.copy(alpha = 0.4f),
        0.9f to colorScheme.surface.copy(alpha = 0.3f),
        1.0f to Color.Transparent 
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(smoothGradient)
            .statusBarsPadding()
    ) {
        LargeFlexibleTopAppBar(
            navigationIcon = {
                Box(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(colorScheme.surfaceVariant)
                ) {
                    Image(
                        painter = painterResource(R.drawable.avatar),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            },
            title = {
                Text(
                    text = stringResource(R.string.nav_tweaks),
                    fontWeight = FontWeight.Bold
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent
            ),
            actions = {
                IconButton(onClick = onMoreClick) {
                    Icon(
                        imageVector = Icons.Outlined.Cloud,
                        contentDescription = stringResource(R.string.cd_menu)
                    )
                }
            },
            scrollBehavior = scrollBehavior,
            windowInsets = WindowInsets(0, 0, 0, 0)
        )
    }
}
