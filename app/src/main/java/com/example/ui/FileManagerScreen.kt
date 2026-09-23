package com.example.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.FileManagerEngine
import com.example.model.FileItem
import com.example.model.SortBy
import com.example.model.ViewMode
import com.example.ui.components.BreadcrumbsBar
import com.example.ui.components.CompressDialog
import com.example.ui.components.CreateItemDialog
import com.example.ui.components.DeleteConfirmDialog
import com.example.ui.components.FileInfoDialog
import com.example.ui.components.FileGridItem
import com.example.ui.components.FileListItem
import com.example.ui.components.FloatingClipboardBar
import com.example.ui.components.FrostedGlassPanel
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassPill
import com.example.ui.components.LiquidGlassSurface
import com.example.ui.components.RenameDialog
import com.example.ui.components.SelectionActionBar
import com.example.ui.components.StorageOverviewCard
import com.example.ui.analyzer.DiskAnalyzerScreen
import com.example.ui.reader.ArchivePreviewScreen
import com.example.ui.reader.HtmlReaderScreen
import com.example.ui.reader.TxtReaderScreen
import com.example.ui.theme.GlassAmber
import com.example.ui.theme.GlassAzure
import com.example.ui.theme.GlassBackgroundDeep
import com.example.ui.theme.GlassBorderLight
import com.example.ui.theme.GlassCyan
import com.example.ui.theme.GlassGreen
import com.example.ui.theme.GlassIndigo
import com.example.ui.theme.GlassPurple
import com.example.ui.theme.LiquidGlassTheme
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import com.example.ui.home.FileHomePageScreen
import com.example.ui.home.FileManagerSidebar
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun FileManagerScreen(
    viewModel: FileManagerViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    // Dialog state holders
    var showCreateDialog by remember { mutableStateOf(false) }
    var itemToRename by remember { mutableStateOf<File?>(null) }
    var itemForInfo by remember { mutableStateOf<FileItem?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var singleItemToDelete by remember { mutableStateOf<File?>(null) }
    var filesToCompress by remember { mutableStateOf<List<File>?>(null) }

    // Search bar state
    var isSearchExpanded by remember { mutableStateOf(false) }
    var moreMenuExpanded by remember { mutableStateOf(false) }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    // Storage permission state check
    var hasFullStorageAccess by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Environment.isExternalStorageManager()
            } else {
                true
            }
        )
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        viewModel.loadCurrentDirectory()
        viewModel.refreshStorageStats()
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            requestPermissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }
    }

    // Intercept back presses
    BackHandler {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else if (uiState.activeScreen is ActiveScreen.DiskAnalyzer) {
            viewModel.closeDiskAnalyzer()
        } else if (uiState.activeScreen !is ActiveScreen.MainBrowser && uiState.activeScreen !is ActiveScreen.Home) {
            viewModel.closeReader()
        } else if (uiState.activeScreen is ActiveScreen.MainBrowser) {
            if (uiState.activeCategory != null) {
                viewModel.selectCategory(null)
            } else if (!viewModel.navigateUp()) {
                viewModel.openHome()
            }
        } else {
            // Close app if on Home
            (context as? android.app.Activity)?.finish()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            FileManagerSidebar(
                activeScreen = uiState.activeScreen,
                storageStats = uiState.storageStats,
                onNavigateHome = { viewModel.openHome() },
                onNavigateMainStorage = { viewModel.openMainStorage() },
                onNavigateDownloads = { viewModel.openDownloads() },
                onNavigateAnalyzer = { viewModel.openDiskAnalyzer() },
                onNavigateCategory = { cat -> viewModel.openCategoryFromHome(cat) },
                onNavigateNewFiles = { viewModel.openNewFiles() },
                onToggleTheme = { viewModel.toggleTheme() },
                onCloseSidebar = {
                    coroutineScope.launch { drawerState.close() }
                }
            )
        }
    ) {
        // Active Reader Screens & Home Screen
        AnimatedContent(
            targetState = uiState.activeScreen,
            transitionSpec = {
                if (targetState is ActiveScreen.Home) {
                    (slideInHorizontally(spring(stiffness = Spring.StiffnessMediumLow)) { -it / 2 } + fadeIn(tween(200)))
                        .togetherWith(slideOutHorizontally(spring(stiffness = Spring.StiffnessMediumLow)) { it } + fadeOut(tween(160)))
                } else if (targetState is ActiveScreen.MainBrowser) {
                    (slideInHorizontally(spring(stiffness = Spring.StiffnessMediumLow)) { -it / 2 } + fadeIn(tween(200)))
                        .togetherWith(slideOutHorizontally(spring(stiffness = Spring.StiffnessMediumLow)) { it } + fadeOut(tween(160)))
                } else {
                    (slideInHorizontally(spring(stiffness = Spring.StiffnessMediumLow)) { it } + fadeIn(tween(200)))
                        .togetherWith(slideOutHorizontally(spring(stiffness = Spring.StiffnessMediumLow)) { -it / 2 } + fadeOut(tween(160)))
                }
            },
            label = "screen_transition"
        ) { activeScreen ->
            when (activeScreen) {
                is ActiveScreen.Home -> {
                    FileHomePageScreen(
                        storageStats = uiState.storageStats,
                        deviceMediaStats = uiState.deviceMediaStats,
                        onOpenSidebar = {
                            coroutineScope.launch { drawerState.open() }
                        },
                        onNavigateToMainStorage = { viewModel.openMainStorage() },
                        onNavigateToDownloads = { viewModel.openDownloads() },
                        onOpenDiskAnalyzer = { viewModel.openDiskAnalyzer() },
                        onOpenCategory = { cat -> viewModel.openCategoryFromHome(cat) },
                        onOpenNewFiles = { viewModel.openNewFiles() },
                        onToggleTheme = { viewModel.toggleTheme() },
                        onRefreshStats = { viewModel.refreshStorageStats() }
                    )
                }
            is ActiveScreen.DiskAnalyzer -> {
                DiskAnalyzerScreen(
                    analysis = uiState.diskAnalysis,
                    isAnalyzing = uiState.isAnalyzingDisk,
                    chartType = uiState.diskChartType,
                    selectedItem = uiState.selectedDiskItem,
                    onChartTypeChange = { viewModel.setDiskChartType(it) },
                    onSelectItem = { viewModel.selectDiskItem(it) },
                    onDrillDown = { viewModel.drillDownDiskAnalyzer(it) },
                    onNavigateBack = { viewModel.closeDiskAnalyzer() },
                    onRefresh = { viewModel.refreshDiskAnalysis() },
                    onOpenInBrowser = { folder ->
                        viewModel.navigateTo(folder)
                        viewModel.closeDiskAnalyzer()
                    },
                    onOpenFile = { file ->
                        viewModel.openFromAnalyzer(file)
                    },
                    onShareFile = { file ->
                        FileManagerEngine.shareFiles(context, listOf(file))
                    },
                    onDeleteFile = { file ->
                        viewModel.deleteFromAnalyzer(file)
                    }
                )
            }
            is ActiveScreen.TxtReader -> {
                TxtReaderScreen(
                    file = activeScreen.file,
                    onBack = { viewModel.closeReader() }
                )
            }
            is ActiveScreen.HtmlReader -> {
                HtmlReaderScreen(
                    file = activeScreen.file,
                    onBack = { viewModel.closeReader() }
                )
            }
            is ActiveScreen.ArchivePreview -> {
                ArchivePreviewScreen(
                    archiveFile = activeScreen.file,
                    onBack = { viewModel.closeReader() },
                    onExtracted = { destinationFolder ->
                        viewModel.closeReader()
                        viewModel.navigateTo(destinationFolder)
                    }
                )
            }
            is ActiveScreen.MainBrowser -> {
                val colors = LiquidGlassTheme.colors
                val isDark = colors.isDark

                LiquidGlassBackground {
                    Scaffold(
                        containerColor = Color.Transparent,
                        floatingActionButton = {
                            val fabInteractionSource = remember { MutableInteractionSource() }
                            val fabPressed by fabInteractionSource.collectIsPressedAsState()
                            val fabScale by animateFloatAsState(
                                targetValue = if (fabPressed) 0.88f else 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                ),
                                label = "fab_press_scale"
                            )

                            AnimatedVisibility(
                                visible = !uiState.isSelectionMode,
                                enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                                exit = scaleOut(spring()) + fadeOut()
                            ) {
                                FloatingActionButton(
                                    onClick = { showCreateDialog = true },
                                    interactionSource = fabInteractionSource,
                                    containerColor = Color.Transparent,
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .graphicsLayer {
                                            scaleX = fabScale
                                            scaleY = fabScale
                                        }
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(colors.primaryAccent, colors.secondaryAccent, GlassIndigo)
                                            )
                                        )
                                        .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                                        .size(56.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "New Item",
                                        tint = if (isDark) Color(0xFF041E2B) else Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .statusBarsPadding()
                                .padding(bottom = innerPadding.calculateBottomPadding())
                        ) {
                            // Top Bar or Selection Bar
                            AnimatedContent(
                                targetState = uiState.isSelectionMode,
                                transitionSpec = {
                                    (slideInVertically { -it / 2 } + fadeIn(tween(200))).togetherWith(
                                        slideOutVertically { -it / 2 } + fadeOut(tween(160))
                                    )
                                },
                                label = "topbar_mode_anim"
                            ) { inSelectionMode ->
                                if (inSelectionMode) {
                                    SelectionActionBar(
                                        selectedCount = uiState.selectedFiles.size,
                                        totalCount = uiState.items.size,
                                        onSelectAll = { viewModel.selectAll() },
                                        onCopy = { viewModel.copySelected() },
                                        onCut = { viewModel.cutSelected() },
                                        onDelete = { showDeleteConfirm = true },
                                        onShare = { FileManagerEngine.shareFiles(context, uiState.selectedFiles.toList()) },
                                        onCompress = { filesToCompress = uiState.selectedFiles.toList() },
                                        onCloseSelection = { viewModel.clearSelection() }
                                    )
                                } else {
                                    MainTopBar(
                                        title = if (uiState.activeCategory != null) uiState.activeCategory!!.label else "File Manager",
                                        isCategoryActive = uiState.activeCategory != null,
                                        isSearchExpanded = isSearchExpanded,
                                        searchQuery = uiState.searchQuery,
                                        viewMode = uiState.viewMode,
                                        moreMenuExpanded = moreMenuExpanded,
                                        sortMenuExpanded = sortMenuExpanded,
                                        showHidden = uiState.showHidden,
                                        isDarkTheme = uiState.isDarkTheme,
                                        onSearchToggle = {
                                             isSearchExpanded = !isSearchExpanded
                                            if (!isSearchExpanded) viewModel.onSearchQueryChanged("")
                                        },
                                        onSearchQueryChanged = { viewModel.onSearchQueryChanged(it) },
                                        onToggleViewMode = { viewModel.toggleViewMode() },
                                        onSortMenuClick = { sortMenuExpanded = true },
                                        onSortMenuDismiss = { sortMenuExpanded = false },
                                        onSortSelected = { viewModel.setSort(it) },
                                        onMoreMenuClick = { moreMenuExpanded = true },
                                        onMoreMenuDismiss = { moreMenuExpanded = false },
                                        onToggleHidden = { viewModel.toggleShowHidden() },
                                        onRefresh = {
                                            viewModel.loadCurrentDirectory()
                                            viewModel.refreshStorageStats()
                                        },
                                        onCreateNew = { showCreateDialog = true },
                                        onBackCategory = { viewModel.selectCategory(null) },
                                        onOpenAnalyzer = { viewModel.openDiskAnalyzer() },
                                        onToggleTheme = { viewModel.toggleTheme() },
                                        onOpenSidebar = { coroutineScope.launch { drawerState.open() } },
                                        onNavigateHome = { viewModel.openHome() }
                                    )
                                }
                            }

                            // Storage Permission Banner (if Android 11+ manage storage is not granted)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !hasFullStorageAccess) {
                                LiquidGlassCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Security, contentDescription = null, tint = GlassAmber, modifier = Modifier.size(24.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Full Storage Access", color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("Grant All Files Access to browse SD card & root storage freely.", color = colors.textSecondary, fontSize = 11.sp)
                                        }
                                        TextButton(onClick = {
                                            try {
                                                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                                                    data = Uri.parse("package:${context.packageName}")
                                                }
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                                                context.startActivity(intent)
                                            }
                                        }) {
                                            Text("Grant", color = colors.primaryAccent, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // Storage overview card (shown in root folder when not searching or in category)
                            AnimatedVisibility(
                                visible = uiState.currentDir.absolutePath == uiState.rootStorageDir.absolutePath &&
                                    uiState.activeCategory == null &&
                                    uiState.searchQuery.isBlank(),
                                enter = fadeIn(tween(220)) + scaleIn(initialScale = 0.95f),
                                exit = fadeOut(tween(180)) + scaleOut(targetScale = 0.95f)
                            ) {
                                StorageOverviewCard(
                                    stats = uiState.storageStats,
                                    onCategoryClick = { viewModel.selectCategory(it) },
                                    onAnalyzeClick = { viewModel.openDiskAnalyzer() }
                                )
                            }

                            // Breadcrumbs navigation (only when browsing directories)
                            if (uiState.activeCategory == null && uiState.searchQuery.isBlank()) {
                                BreadcrumbsBar(
                                    currentDir = uiState.currentDir,
                                    rootStorageDir = uiState.rootStorageDir,
                                    onNavigateTo = { viewModel.navigateTo(it) }
                                )
                            } else if (uiState.activeCategory != null) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Category: ${uiState.activeCategory!!.label} (${uiState.items.size} found)",
                                        color = colors.primaryAccent,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    LiquidGlassPill(
                                        text = "Exit Category",
                                        isSelected = false,
                                        onClick = { viewModel.selectCategory(null) }
                                    )
                                }
                            }

                            // Content: List or Grid
                            val displayItems = if (uiState.searchQuery.isNotBlank()) uiState.searchResults else uiState.items

                            Box(modifier = Modifier.weight(1f)) {
                                if (uiState.isLoading || uiState.isSearching) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(color = colors.primaryAccent)
                                    }
                                } else if (displayItems.isEmpty()) {
                                    val infiniteTransition = rememberInfiniteTransition(label = "empty_bob")
                                    val dy by infiniteTransition.animateFloat(
                                        initialValue = -5f,
                                        targetValue = 5f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(1500, easing = FastOutSlowInEasing),
                                            repeatMode = RepeatMode.Reverse
                                        ),
                                        label = "empty_bob_y"
                                    )
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                Icons.Default.FolderOpen,
                                                contentDescription = null,
                                                tint = colors.textMuted,
                                                modifier = Modifier
                                                    .size(54.dp)
                                                    .graphicsLayer { translationY = dy }
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                text = if (uiState.searchQuery.isNotBlank()) "No files match \"${uiState.searchQuery}\""
                                                       else "This folder is empty",
                                                color = colors.textSecondary,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                } else {
                                     if (uiState.viewMode == ViewMode.LIST) {
                                        LazyColumn(
                                            modifier = Modifier.fillMaxSize(),
                                            contentPadding = PaddingValues(bottom = 80.dp)
                                        ) {
                                            items(
                                                items = displayItems,
                                                key = { it.path },
                                                contentType = { if (it.isDirectory) "dir" else "file" }
                                            ) { item ->
                                                val isSelected = uiState.selectedFiles.contains(item.file)
                                                FileListItem(
                                                    item = item,
                                                    isSelected = isSelected,
                                                    isSelectionMode = uiState.isSelectionMode,
                                                    modifier = Modifier.animateItem(),
                                                    onClick = {
                                                        if (uiState.isSelectionMode) {
                                                            viewModel.toggleSelection(item.file)
                                                        } else {
                                                            viewModel.openItem(item)
                                                        }
                                                    },
                                                    onLongClick = {
                                                        viewModel.startSelection(item.file)
                                                    },
                                                    onOpenWith = {
                                                        FileManagerEngine.openWith(context, item.file)
                                                    },
                                                    onShare = {
                                                        FileManagerEngine.shareFiles(context, listOf(item.file))
                                                    },
                                                    onRename = {
                                                        itemToRename = item.file
                                                    },
                                                    onDelete = {
                                                        singleItemToDelete = item.file
                                                        showDeleteConfirm = true
                                                    },
                                                    onInfo = {
                                                        itemForInfo = item
                                                    },
                                                    onCompress = {
                                                        filesToCompress = listOf(item.file)
                                                    }
                                                )
                                            }
                                        }
                                    } else {
                                        LazyVerticalGrid(
                                            columns = GridCells.Adaptive(minSize = 100.dp),
                                            modifier = Modifier.fillMaxSize(),
                                            contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 6.dp, bottom = 80.dp)
                                        ) {
                                            items(
                                                items = displayItems,
                                                key = { it.path },
                                                contentType = { if (it.isDirectory) "dir" else "file" }
                                            ) { item ->
                                                val isSelected = uiState.selectedFiles.contains(item.file)
                                                FileGridItem(
                                                    item = item,
                                                    isSelected = isSelected,
                                                    isSelectionMode = uiState.isSelectionMode,
                                                    modifier = Modifier.animateItem(),
                                                    onClick = {
                                                        if (uiState.isSelectionMode) {
                                                            viewModel.toggleSelection(item.file)
                                                        } else {
                                                            viewModel.openItem(item)
                                                        }
                                                    },
                                                    onLongClick = {
                                                        viewModel.startSelection(item.file)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                // Floating Clipboard Bar with slide and fade
                                androidx.compose.animation.AnimatedVisibility(
                                    visible = uiState.clipboard != null,
                                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .navigationBarsPadding()
                                        .padding(bottom = 70.dp)
                                ) {
                                    if (uiState.clipboard != null) {
                                        FloatingClipboardBar(
                                            clipboard = uiState.clipboard!!,
                                            onPaste = { viewModel.pasteClipboard() },
                                            onCancel = { viewModel.cancelClipboard() }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    }

    // Dialogs
    if (showCreateDialog) {
        CreateItemDialog(
            onDismiss = { showCreateDialog = false },
            onCreateFolder = { viewModel.createFolder(it) },
            onCreateFile = { viewModel.createFile(it) }
        )
    }

    if (itemToRename != null) {
        RenameDialog(
            currentName = itemToRename!!.name,
            onDismiss = { itemToRename = null },
            onConfirm = { newName ->
                viewModel.rename(itemToRename!!, newName)
                itemToRename = null
            }
        )
    }

    if (showDeleteConfirm) {
        DeleteConfirmDialog(
            count = if (singleItemToDelete != null) 1 else uiState.selectedFiles.size,
            singleFileName = singleItemToDelete?.name,
            onDismiss = {
                showDeleteConfirm = false
                singleItemToDelete = null
            },
            onConfirm = {
                if (singleItemToDelete != null) {
                    viewModel.deleteSelected(listOf(singleItemToDelete!!))
                    singleItemToDelete = null
                } else {
                    viewModel.deleteSelected()
                }
            }
        )
    }

    if (filesToCompress != null) {
        CompressDialog(
            selectedFiles = filesToCompress!!,
            onDismiss = { filesToCompress = null },
            onConfirm = { name, format ->
                viewModel.compressFiles(filesToCompress!!, name, format)
                filesToCompress = null
            }
        )
    }

    if (itemForInfo != null) {
        FileInfoDialog(
            item = itemForInfo!!,
            onDismiss = { itemForInfo = null }
        )
    }
}

@Composable
private fun MainTopBar(
    title: String,
    isCategoryActive: Boolean,
    isSearchExpanded: Boolean,
    searchQuery: String,
    viewMode: ViewMode,
    moreMenuExpanded: Boolean,
    sortMenuExpanded: Boolean,
    showHidden: Boolean,
    isDarkTheme: Boolean,
    onSearchToggle: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onToggleViewMode: () -> Unit,
    onSortMenuClick: () -> Unit,
    onSortMenuDismiss: () -> Unit,
    onSortSelected: (SortBy) -> Unit,
    onMoreMenuClick: () -> Unit,
    onMoreMenuDismiss: () -> Unit,
    onToggleHidden: () -> Unit,
    onRefresh: () -> Unit,
    onCreateNew: () -> Unit,
    onBackCategory: () -> Unit,
    onOpenAnalyzer: () -> Unit,
    onToggleTheme: () -> Unit,
    onOpenSidebar: () -> Unit,
    onNavigateHome: () -> Unit
) {
    val colors = LiquidGlassTheme.colors

    FrostedGlassPanel(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    if (isCategoryActive) {
                        IconButton(onClick = onBackCategory) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = colors.primaryAccent)
                        }
                    } else {
                        IconButton(
                            onClick = onOpenSidebar,
                            modifier = Modifier.testTag("btn_main_sidebar")
                        ) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "Open Sidebar Navigation",
                                tint = colors.textPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = title,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Home Button
                    IconButton(
                        onClick = onNavigateHome,
                        modifier = Modifier.testTag("btn_main_home")
                    ) {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = "Return to Home",
                            tint = colors.primaryAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    // Disk Space Analyzer Button
                    IconButton(
                        onClick = onOpenAnalyzer,
                        modifier = Modifier.testTag("btn_disk_analyzer")
                    ) {
                        Icon(
                            Icons.Default.Analytics,
                            contentDescription = "Disk Space Analyzer",
                            tint = colors.primaryAccent
                        )
                    }

                    // Theme Toggle (Light / Dark mode)
                    IconButton(
                        onClick = onToggleTheme,
                        modifier = Modifier.testTag("btn_theme_toggle")
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDarkTheme) "Switch to Light Theme" else "Switch to Dark Theme",
                            tint = colors.textSecondary
                        )
                    }

                    // Search Button
                    IconButton(onClick = onSearchToggle) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = if (isSearchExpanded) colors.primaryAccent else colors.textSecondary
                        )
                    }

                    // View Mode Toggle (List / Grid)
                    IconButton(onClick = onToggleViewMode) {
                        Icon(
                            imageVector = if (viewMode == ViewMode.LIST) Icons.Default.GridView else Icons.Default.ViewList,
                            contentDescription = "Toggle Grid",
                            tint = colors.textSecondary
                        )
                    }

                    // Sort Menu
                    Box {
                        IconButton(onClick = onSortMenuClick) {
                            Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort", tint = colors.textSecondary)
                        }

                        DropdownMenu(
                            expanded = sortMenuExpanded,
                            onDismissRequest = onSortMenuDismiss,
                            modifier = Modifier
                                .background(colors.panelBackground)
                                .border(1.dp, colors.borderLight, RoundedCornerShape(12.dp))
                        ) {
                            DropdownMenuItem(text = { Text("Sort by Name", color = colors.textPrimary) }, onClick = { onSortSelected(SortBy.NAME); onSortMenuDismiss() })
                            DropdownMenuItem(text = { Text("Sort by Date", color = colors.textPrimary) }, onClick = { onSortSelected(SortBy.DATE); onSortMenuDismiss() })
                            DropdownMenuItem(text = { Text("Sort by Size", color = colors.textPrimary) }, onClick = { onSortSelected(SortBy.SIZE); onSortMenuDismiss() })
                            DropdownMenuItem(text = { Text("Sort by Type", color = colors.textPrimary) }, onClick = { onSortSelected(SortBy.TYPE); onSortMenuDismiss() })
                        }
                    }

                    // More Menu
                    Box {
                        IconButton(onClick = onMoreMenuClick) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More", tint = colors.textSecondary)
                        }

                        DropdownMenu(
                            expanded = moreMenuExpanded,
                            onDismissRequest = onMoreMenuDismiss,
                            modifier = Modifier
                                .background(colors.panelBackground)
                                .border(1.dp, colors.borderLight, RoundedCornerShape(12.dp))
                        ) {
                            DropdownMenuItem(
                                text = { Text("Disk Space Analyzer", color = colors.textPrimary) },
                                leadingIcon = { Icon(Icons.Default.Analytics, contentDescription = null, tint = colors.primaryAccent) },
                                onClick = { onMoreMenuDismiss(); onOpenAnalyzer() }
                            )
                            DropdownMenuItem(
                                text = { Text(if (isDarkTheme) "Light Theme" else "Dark Theme", color = colors.textPrimary) },
                                leadingIcon = { Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, contentDescription = null, tint = colors.secondaryAccent) },
                                onClick = { onMoreMenuDismiss(); onToggleTheme() }
                            )
                            DropdownMenuItem(
                                text = { Text("New Item", color = colors.textPrimary) },
                                leadingIcon = { Icon(Icons.Default.CreateNewFolder, contentDescription = null, tint = colors.primaryAccent) },
                                onClick = { onMoreMenuDismiss(); onCreateNew() }
                            )
                            DropdownMenuItem(
                                text = { Text(if (showHidden) "Hide Hidden Files" else "Show Hidden Files", color = colors.textPrimary) },
                                leadingIcon = { Icon(if (showHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, tint = colors.secondaryAccent) },
                                onClick = { onMoreMenuDismiss(); onToggleHidden() }
                            )
                            DropdownMenuItem(
                                text = { Text("Refresh", color = colors.textPrimary) },
                                leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null, tint = GlassGreen) },
                                onClick = { onMoreMenuDismiss(); onRefresh() }
                            )
                        }
                    }
                }
            }

            // Expanding search bar
            AnimatedVisibility(
                visible = isSearchExpanded,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChanged,
                        placeholder = { Text("Search files, folders, extensions...", color = colors.textMuted, fontSize = 13.sp) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            focusedBorderColor = colors.primaryAccent,
                            unfocusedBorderColor = colors.borderLight,
                            focusedContainerColor = colors.panelBackground,
                            unfocusedContainerColor = colors.panelBackground
                        ),
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChanged("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = colors.textSecondary)
                                }
                            }
                        },
                        textStyle = TextStyle(fontSize = 13.sp)
                    )
                }
            }
        }
    }
}
