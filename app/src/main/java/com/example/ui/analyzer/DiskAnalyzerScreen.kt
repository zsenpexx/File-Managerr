package com.example.ui.analyzer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.FileManagerEngine
import com.example.model.DiskCategoryStat
import com.example.model.DiskChartType
import com.example.model.DiskDirectoryAnalysis
import com.example.model.DiskItemAnalysis
import com.example.model.FileCategory
import com.example.ui.components.FrostedGlassPanel
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassIconButton
import com.example.ui.components.LiquidGlassPill
import com.example.ui.components.LiquidProgressBar
import com.example.ui.theme.GlassAmber
import com.example.ui.theme.GlassAzure
import com.example.ui.theme.GlassCyan
import com.example.ui.theme.GlassGreen
import com.example.ui.theme.GlassIndigo
import com.example.ui.theme.GlassPink
import com.example.ui.theme.GlassPurple
import com.example.ui.theme.GlassRed
import com.example.ui.theme.LiquidGlassTheme
import java.io.File
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class TreemapFilterType(val label: String) {
    ALL("All Space"),
    LARGE_ONLY("🔥 Large (>1MB)"),
    FOLDERS_ONLY("📁 Folders"),
    FILES_ONLY("📄 Files")
}

@Composable
fun DiskAnalyzerScreen(
    analysis: DiskDirectoryAnalysis?,
    isAnalyzing: Boolean,
    chartType: DiskChartType,
    selectedItem: DiskItemAnalysis?,
    onChartTypeChange: (DiskChartType) -> Unit,
    onSelectItem: (DiskItemAnalysis?) -> Unit,
    onDrillDown: (File) -> Unit,
    onNavigateBack: () -> Unit,
    onRefresh: () -> Unit,
    onOpenInBrowser: (File) -> Unit,
    onOpenFile: (File) -> Unit,
    onShareFile: (File) -> Unit,
    onDeleteFile: (File) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LiquidGlassTheme.colors
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<FileCategory?>(null) }
    var treemapFilter by remember { mutableStateOf(TreemapFilterType.ALL) }
    var fileToDelete by remember { mutableStateOf<File?>(null) }

    LiquidGlassBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Bar
            AnalyzerTopBar(
                currentDirectory = analysis?.directory,
                chartType = chartType,
                isAnalyzing = isAnalyzing,
                onNavigateBack = onNavigateBack,
                onChartTypeChange = onChartTypeChange,
                onRefresh = onRefresh,
                onOpenInBrowser = {
                    analysis?.directory?.let { onOpenInBrowser(it) }
                },
                onBreadcrumbClick = onDrillDown
            )

            if (isAnalyzing && analysis == null) {
                // Loading State
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = colors.primaryAccent,
                            modifier = Modifier.size(52.dp),
                            strokeWidth = 3.5.dp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Analyzing Directory Space...",
                            color = colors.textPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Recursively calculating file and folder sizes",
                            color = colors.textSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            } else if (analysis != null) {
                // Main Content with Filter logic
                val filteredItems = remember(analysis.items, searchQuery, selectedCategoryFilter, treemapFilter) {
                    analysis.items.filter { item ->
                        val matchesQuery = searchQuery.isBlank() || item.name.contains(searchQuery, ignoreCase = true)
                        val matchesCategory = selectedCategoryFilter == null || item.category == selectedCategoryFilter
                        val matchesType = when (treemapFilter) {
                            TreemapFilterType.ALL -> true
                            TreemapFilterType.LARGE_ONLY -> item.size >= 1024 * 1024L || item.percentage >= 10f
                            TreemapFilterType.FOLDERS_ONLY -> item.isDirectory
                            TreemapFilterType.FILES_ONLY -> !item.isDirectory
                        }
                        matchesQuery && matchesCategory && matchesType
                    }
                }

                // Top space consumers (Top 3-5 largest items)
                val topSpaceHogs = remember(analysis.items) {
                    analysis.items.filter { it.size > 0 }.sortedByDescending { it.size }.take(5)
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = if (selectedItem != null) 150.dp else 50.dp)
                    ) {
                        // Hero Summary Card
                        item {
                            AnalyzerHeroCard(
                                analysis = analysis,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }

                        // Top Space Consumers Section (Highlighting Heavy Hogs)
                        if (topSpaceHogs.isNotEmpty()) {
                            item {
                                TopSpaceHogsSection(
                                    topItems = topSpaceHogs,
                                    selectedItem = selectedItem,
                                    onSelectItem = onSelectItem,
                                    onDrillDown = onDrillDown,
                                    onOpenFile = onOpenFile,
                                    onDeleteFile = { fileToDelete = it },
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Visualization Section (Treemap or Donut)
                        item {
                            LiquidGlassCard(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Visualizer Header
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (chartType == DiskChartType.CIRCULAR) Icons.Default.DonutLarge else Icons.Default.GridView,
                                                contentDescription = null,
                                                tint = colors.primaryAccent,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = if (chartType == DiskChartType.CIRCULAR) "Donut Distribution" else "Treemap Space Map",
                                                    color = colors.textPrimary,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp
                                                )
                                                Text(
                                                    text = if (chartType == DiskChartType.CIRCULAR)
                                                        "Relative distribution of folder contents"
                                                    else
                                                        "Squarified tile area proportional to byte size",
                                                    color = colors.textSecondary,
                                                    fontSize = 11.5.sp
                                                )
                                            }
                                        }

                                        Text(
                                            text = "${filteredItems.size} items",
                                            color = colors.primaryAccent,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Treemap Filter Selector Pills (All, Large, Folders, Files)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        TreemapFilterType.entries.forEach { filterOption ->
                                            val isSelected = treemapFilter == filterOption
                                            Row(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(
                                                        if (isSelected) colors.primaryAccent.copy(alpha = if (colors.isDark) 0.28f else 0.18f)
                                                        else (if (colors.isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.04f))
                                                    )
                                                    .border(
                                                        width = if (isSelected) 1.5.dp else 0.8.dp,
                                                        color = if (isSelected) colors.primaryAccent else (if (colors.isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)),
                                                        shape = RoundedCornerShape(10.dp)
                                                    )
                                                    .clickable { treemapFilter = filterOption }
                                                    .padding(vertical = 7.dp),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = filterOption.label,
                                                    color = if (isSelected) colors.primaryAccent else colors.textSecondary,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    if (chartType == DiskChartType.CIRCULAR) {
                                        CircularDonutChart(
                                            analysis = analysis,
                                            selectedItem = selectedItem,
                                            onSelectItem = onSelectItem,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(280.dp)
                                        )
                                    } else {
                                        TreemapChart(
                                            analysis = analysis,
                                            displayedItems = filteredItems,
                                            selectedItem = selectedItem,
                                            onSelectItem = onSelectItem,
                                            onDrillDown = onDrillDown,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(340.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Treemap Legend / Color Key
                                    TreemapLegendRow()
                                }
                            }
                        }

                        // Category Breakdown Chips
                        item {
                            CategoryBreakdownRow(
                                categoryStats = analysis.categoryStats,
                                selectedCategory = selectedCategoryFilter,
                                onSelectCategory = { cat ->
                                    selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat
                                },
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }

                        // Ranked Items Header with Search Box
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Ranked by Size",
                                        color = colors.textPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )

                                    Text(
                                        text = "${filteredItems.size} of ${analysis.items.size} shown",
                                        color = colors.textSecondary,
                                        fontSize = 12.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Inline Search Input to quickly filter ranked list & treemap
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (colors.isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.70f))
                                        .border(1.dp, if (colors.isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 12.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search items",
                                        tint = colors.textMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    TextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it },
                                        placeholder = {
                                            Text("Filter files or folders...", color = colors.textMuted, fontSize = 12.5.sp)
                                        },
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            disabledContainerColor = Color.Transparent,
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent
                                        ),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    if (searchQuery.isNotEmpty()) {
                                        LiquidGlassIconButton(
                                            icon = Icons.Default.Clear,
                                            contentDescription = "Clear search",
                                            onClick = { searchQuery = "" },
                                            tint = colors.textMuted,
                                            size = 28.dp
                                        )
                                    }
                                }
                            }
                        }

                        // Ranked Items List
                        itemsIndexed(
                            items = filteredItems,
                            key = { _, item -> item.file.absolutePath }
                        ) { index, item ->
                            DiskItemRow(
                                rank = index + 1,
                                item = item,
                                isSelected = selectedItem?.file?.absolutePath == item.file.absolutePath,
                                onClick = {
                                    if (selectedItem?.file?.absolutePath == item.file.absolutePath) {
                                        onSelectItem(null)
                                    } else {
                                        onSelectItem(item)
                                    }
                                },
                                onDrillDown = { onDrillDown(item.file) },
                                onOpen = { onOpenFile(item.file) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Floating Selected Item Action Sheet
                    androidx.compose.animation.AnimatedVisibility(
                        visible = selectedItem != null,
                        enter = slideInVertically { it } + fadeIn(),
                        exit = slideOutVertically { it } + fadeOut(),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    ) {
                        if (selectedItem != null) {
                            SelectedItemActionCard(
                                item = selectedItem,
                                onClose = { onSelectItem(null) },
                                onDrillDown = { onDrillDown(selectedItem.file) },
                                onOpen = { onOpenFile(selectedItem.file) },
                                onShare = { onShareFile(selectedItem.file) },
                                onDelete = { fileToDelete = selectedItem.file }
                            )
                        }
                    }
                }
            }
        }

        // Delete Confirmation Dialog
        if (fileToDelete != null) {
            val file = fileToDelete!!
            val isFolder = file.isDirectory
            AlertDialog(
                onDismissRequest = { fileToDelete = null },
                modifier = Modifier.clip(RoundedCornerShape(20.dp)),
                containerColor = if (colors.isDark) Color(0xFF0F172A) else Color(0xFFFFFFFF),
                icon = {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = GlassRed, modifier = Modifier.size(28.dp))
                },
                title = {
                    Text(
                        text = "Delete ${if (isFolder) "Folder" else "File"}?",
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Are you sure you want to permanently delete '${file.name}'?",
                            color = colors.textSecondary,
                            fontSize = 13.5.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "This will immediately reclaim space on your device.",
                            color = colors.primaryAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onDeleteFile(file)
                            if (selectedItem?.file?.absolutePath == file.absolutePath) {
                                onSelectItem(null)
                            }
                            fileToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GlassRed)
                    ) {
                        Text("Delete Permanently", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { fileToDelete = null }) {
                        Text("Cancel", color = colors.textSecondary)
                    }
                }
            )
        }
    }
}

/**
 * Top bar with breadcrumb trail, switch pills for Donut vs Treemap, and actions.
 */
@Composable
private fun AnalyzerTopBar(
    currentDirectory: File?,
    chartType: DiskChartType,
    isAnalyzing: Boolean,
    onNavigateBack: () -> Unit,
    onChartTypeChange: (DiskChartType) -> Unit,
    onRefresh: () -> Unit,
    onOpenInBrowser: () -> Unit,
    onBreadcrumbClick: (File) -> Unit
) {
    val colors = LiquidGlassTheme.colors

    FrostedGlassPanel(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LiquidGlassIconButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        onClick = onNavigateBack,
                        size = 38.dp
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "Disk Space Analyzer",
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = currentDirectory?.name?.ifEmpty { "Root" } ?: "Storage",
                            color = colors.primaryAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Open in Browser action
                    LiquidGlassIconButton(
                        icon = Icons.Default.OpenInNew,
                        contentDescription = "Browse Folder",
                        onClick = onOpenInBrowser,
                        size = 38.dp,
                        modifier = Modifier.testTag("analyzer_open_browser")
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Rescan / Refresh
                    val refreshRotation = remember { Animatable(0f) }
                    LaunchedEffect(isAnalyzing) {
                        if (isAnalyzing) {
                            refreshRotation.animateTo(
                                targetValue = 360f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(900),
                                    repeatMode = RepeatMode.Restart
                                )
                            )
                        } else {
                            refreshRotation.snapTo(0f)
                        }
                    }

                    LiquidGlassIconButton(
                        icon = Icons.Default.Refresh,
                        contentDescription = "Refresh Analysis",
                        onClick = onRefresh,
                        size = 38.dp,
                        modifier = Modifier
                            .rotate(refreshRotation.value)
                            .testTag("analyzer_refresh")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Chart View Selector Switch (Circular Donut vs Treemap)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interactive Chart Type Pills
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (colors.isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.85f))
                        .border(
                            1.dp,
                            if (colors.isDark) Color.White.copy(alpha = 0.18f) else Color(0x33CBD5E1),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(3.dp)
                ) {
                    ChartSwitchPill(
                        label = "Circular",
                        icon = Icons.Default.DonutLarge,
                        isSelected = chartType == DiskChartType.CIRCULAR,
                        onClick = { onChartTypeChange(DiskChartType.CIRCULAR) }
                    )

                    ChartSwitchPill(
                        label = "Treemap",
                        icon = Icons.Default.GridView,
                        isSelected = chartType == DiskChartType.TREEMAP,
                        onClick = { onChartTypeChange(DiskChartType.TREEMAP) }
                    )
                }

                // Breadcrumb trail (allows jumping up to parent directory)
                if (currentDirectory != null && currentDirectory.parentFile != null) {
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(colors.primaryAccent.copy(alpha = 0.15f))
                            .border(1.dp, colors.primaryAccent.copy(alpha = 0.35f), CircleShape)
                            .clickable {
                                currentDirectory.parentFile?.let { onBreadcrumbClick(it) }
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = colors.primaryAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Up to ..",
                            color = colors.primaryAccent,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartSwitchPill(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = LiquidGlassTheme.colors
    val bgBrush = if (isSelected) {
        Brush.horizontalGradient(
            listOf(
                colors.primaryAccent.copy(alpha = if (colors.isDark) 0.45f else 0.30f),
                colors.secondaryAccent.copy(alpha = if (colors.isDark) 0.35f else 0.20f)
            )
        )
    } else {
        Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(9.dp))
            .background(bgBrush)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) colors.primaryAccent else colors.textSecondary,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            color = if (isSelected) colors.primaryAccent else colors.textSecondary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

/**
 * Hero statistics card showing total directory size, counts, and largest item callout.
 */
@Composable
private fun AnalyzerHeroCard(
    analysis: DiskDirectoryAnalysis,
    modifier: Modifier = Modifier
) {
    val colors = LiquidGlassTheme.colors

    LiquidGlassCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Total Directory Footprint",
                color = colors.textSecondary,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = analysis.formattedTotalSize,
                    color = colors.textPrimary,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    CountBadge(
                        label = "${analysis.totalFolders} Folders",
                        icon = Icons.Default.Folder,
                        color = colors.primaryAccent
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    CountBadge(
                        label = "${analysis.totalFiles} Files",
                        icon = Icons.Default.InsertDriveFile,
                        color = GlassPurple
                    )
                }
            }

            if (analysis.largestItem != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (colors.isDark) Color.White.copy(alpha = 0.06f) else Color(0x150284C7))
                        .border(
                            1.dp,
                            if (colors.isDark) Color.White.copy(alpha = 0.12f) else Color(0x300284C7),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Largest:",
                            color = colors.textMuted,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = analysis.largestItem.name,
                            color = colors.textPrimary,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = "${analysis.largestItem.formattedSize} (${String.format("%.1f", analysis.largestItem.percentage)}%)",
                        color = colors.primaryAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun CountBadge(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    val colors = LiquidGlassTheme.colors
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = if (colors.isDark) 0.16f else 0.12f))
            .border(0.8.dp, color.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Circular / Radial Donut Chart with animated sweep angle, interactive slice selection,
 * specular borders, and dynamic center statistics.
 */
@Composable
private fun CircularDonutChart(
    analysis: DiskDirectoryAnalysis,
    selectedItem: DiskItemAnalysis?,
    onSelectItem: (DiskItemAnalysis?) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LiquidGlassTheme.colors
    val sweepAnim = remember { Animatable(0f) }

    LaunchedEffect(analysis.directory.absolutePath) {
        sweepAnim.snapTo(0f)
        sweepAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing)
        )
    }

    // Top items + consolidated "Other"
    val chartItems = remember(analysis.items) {
        if (analysis.items.size <= 8) {
            analysis.items
        } else {
            val top = analysis.items.take(7)
            val rest = analysis.items.drop(7)
            val restBytes = rest.sumOf { it.size }
            val restPct = if (analysis.totalSize > 0) (restBytes.toDouble() / analysis.totalSize.toDouble() * 100).toFloat() else 0f
            top + DiskItemAnalysis(
                file = analysis.directory,
                name = "Other (${rest.size} items)",
                isDirectory = false,
                size = restBytes,
                formattedSize = analysis.formattedTotalSize,
                percentage = restPct,
                category = FileCategory.OTHER,
                childCount = 0,
                color = Color(0xFF64748B)
            )
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(chartItems) {
                    detectTapGestures { tapOffset ->
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f
                        val radius = (minOf(size.width, size.height) / 2f) * 0.88f
                        val innerRadius = radius * 0.60f

                        val dx = tapOffset.x - centerX
                        val dy = tapOffset.y - centerY
                        val dist = sqrt(dx * dx + dy * dy)

                        if (dist in innerRadius..radius) {
                            var angle = (atan2(dy, dx) * 180f / PI.toFloat())
                            if (angle < 0) angle += 360f

                            var currentAngle = 0f
                            for (item in chartItems) {
                                val itemSweep = (item.percentage / 100f) * 360f
                                if (angle in currentAngle..(currentAngle + itemSweep)) {
                                    if (selectedItem?.file?.absolutePath == item.file.absolutePath) {
                                        onSelectItem(null)
                                    } else {
                                        onSelectItem(item)
                                    }
                                    return@detectTapGestures
                                }
                                currentAngle += itemSweep
                            }
                        } else {
                            onSelectItem(null)
                        }
                    }
                }
        ) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val radius = (minOf(size.width, size.height) / 2f) * 0.88f
            val strokeWidth = radius * 0.38f
            val arcRadius = radius - strokeWidth / 2f
            val animatedFactor = sweepAnim.value

            var startAngle = -90f

            if (analysis.totalSize == 0L || chartItems.isEmpty()) {
                // Empty ring placeholder
                drawCircle(
                    color = Color.White.copy(alpha = 0.08f),
                    radius = arcRadius,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = strokeWidth)
                )
                return@Canvas
            }

            for (item in chartItems) {
                val rawSweep = (item.percentage / 100f) * 360f
                val sweep = (rawSweep * animatedFactor).coerceAtLeast(0.5f)
                val isSelected = selectedItem?.name == item.name

                val sliceStroke = if (isSelected) strokeWidth * 1.15f else strokeWidth
                val sliceAlpha = if (selectedItem == null || isSelected) 1f else 0.35f
                val sliceColor = item.color.copy(alpha = sliceAlpha)

                val spacing = if (chartItems.size > 1) 2.2f else 0f
                val effectiveSweep = (sweep - spacing).coerceAtLeast(0.1f)

                // Main arc slice
                drawArc(
                    color = sliceColor,
                    startAngle = startAngle + spacing / 2f,
                    sweepAngle = effectiveSweep,
                    useCenter = false,
                    topLeft = Offset(centerX - arcRadius, centerY - arcRadius),
                    size = Size(arcRadius * 2, arcRadius * 2),
                    style = Stroke(width = sliceStroke, cap = StrokeCap.Round)
                )

                // Specular glint on outer edge
                drawArc(
                    color = Color.White.copy(alpha = if (isSelected) 0.5f else 0.2f),
                    startAngle = startAngle + spacing / 2f,
                    sweepAngle = effectiveSweep,
                    useCenter = false,
                    topLeft = Offset(centerX - arcRadius - sliceStroke / 2.2f, centerY - arcRadius - sliceStroke / 2.2f),
                    size = Size((arcRadius + sliceStroke / 2.2f) * 2, (arcRadius + sliceStroke / 2.2f) * 2),
                    style = Stroke(width = 1.2f)
                )

                startAngle += rawSweep * animatedFactor
            }
        }

        // Center Content Badge
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 48.dp)
        ) {
            if (selectedItem != null) {
                Text(
                    text = String.format("%.1f%%", selectedItem.percentage),
                    color = selectedItem.color,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = selectedItem.formattedSize,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = selectedItem.name,
                    color = colors.textSecondary,
                    fontSize = 11.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = "Total Space",
                    color = colors.textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = analysis.formattedTotalSize,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${analysis.totalItems} items",
                    color = colors.primaryAccent,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Top Space Consumers (Heavy Hogs) Section.
 * Visually showcases the top 3-5 largest files and folders taking up the most space,
 * with rank badges, category icons, bold size displays, and quick action buttons.
 */
@Composable
private fun TopSpaceHogsSection(
    topItems: List<DiskItemAnalysis>,
    selectedItem: DiskItemAnalysis?,
    onSelectItem: (DiskItemAnalysis?) -> Unit,
    onDrillDown: (File) -> Unit,
    onOpenFile: (File) -> Unit,
    onDeleteFile: (File) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LiquidGlassTheme.colors

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = GlassAmber,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Top Space Consumers",
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Text(
                text = "Largest ${topItems.size} items",
                color = colors.textMuted,
                fontSize = 11.5.sp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(topItems) { index, item ->
                val isSelected = selectedItem?.file?.absolutePath == item.file.absolutePath
                val rankLabel = when (index) {
                    0 -> "#1 👑"
                    1 -> "#2 🔥"
                    2 -> "#3 ⚡"
                    else -> "#${index + 1}"
                }

                LiquidGlassCard(
                    modifier = Modifier
                        .width(220.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    onClick = {
                        if (isSelected) onSelectItem(null) else onSelectItem(item)
                    },
                    borderWidth = if (isSelected) 1.8.dp else 1.dp
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Top header: Rank and Category
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(item.color.copy(alpha = 0.20f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = rankLabel,
                                    color = item.color,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            Text(
                                text = String.format("%.1f%%", item.percentage),
                                color = item.color,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Item Icon and Name
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(item.color.copy(alpha = if (colors.isDark) 0.22f else 0.15f))
                                    .border(1.dp, item.color.copy(alpha = 0.45f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (item.isDirectory) Icons.Default.Folder else Icons.Default.InsertDriveFile,
                                    contentDescription = null,
                                    tint = item.color,
                                    modifier = Modifier.size(17.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    color = colors.textPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (item.isDirectory) "${item.childCount} items inside" else item.category.name,
                                    color = colors.textMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Size Display
                        Text(
                            text = item.formattedSize,
                            color = colors.textPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (item.isDirectory) {
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(colors.primaryAccent.copy(alpha = 0.18f))
                                        .border(0.8.dp, colors.primaryAccent.copy(alpha = 0.40f), RoundedCornerShape(8.dp))
                                        .clickable { onDrillDown(item.file) }
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Explore ↗",
                                        color = colors.primaryAccent,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(colors.primaryAccent.copy(alpha = 0.18f))
                                        .border(0.8.dp, colors.primaryAccent.copy(alpha = 0.40f), RoundedCornerShape(8.dp))
                                        .clickable { onOpenFile(item.file) }
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Open",
                                        color = colors.primaryAccent,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GlassRed.copy(alpha = 0.15f))
                                    .border(0.8.dp, GlassRed.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                    .clickable { onDeleteFile(item.file) }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = GlassRed,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Treemap Color Legend Key at the bottom of the Treemap visualizer card.
 */
@Composable
private fun TreemapLegendRow() {
    val colors = LiquidGlassTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (colors.isDark) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.03f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendDot(label = "Folder", color = GlassAzure)
        LegendDot(label = "Video", color = GlassPurple)
        LegendDot(label = "Image", color = GlassPink)
        LegendDot(label = "Audio", color = GlassAmber)
        LegendDot(label = "Doc", color = GlassCyan)
        LegendDot(label = "Zip", color = GlassIndigo)
    }
}

@Composable
private fun LegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, color = LiquidGlassTheme.colors.textMuted, fontSize = 10.sp)
    }
}

/**
 * Treemap Tile Data
 */
private data class TreemapTile(
    val item: DiskItemAnalysis,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
)

private data class RectF(val x: Float, val y: Float, val width: Float, val height: Float)

/**
 * Treemap visual storage analyzer chart using the Squarified algorithm.
 * Proportionally partitions available screen space with aspect ratios close to 1:1.
 * Highlights large files and folders with flame badges and rich liquid glass styling.
 */
@Composable
private fun TreemapChart(
    analysis: DiskDirectoryAnalysis,
    displayedItems: List<DiskItemAnalysis>,
    selectedItem: DiskItemAnalysis?,
    onSelectItem: (DiskItemAnalysis?) -> Unit,
    onDrillDown: (File) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LiquidGlassTheme.colors

    BoxWithConstraints(modifier = modifier) {
        val w = constraints.maxWidth.toFloat()
        val h = constraints.maxHeight.toFloat()

        val itemsToMap = if (displayedItems.isNotEmpty()) displayedItems else analysis.items

        if (itemsToMap.isEmpty() || analysis.totalSize == 0L || w <= 0f || h <= 0f) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No matching items to visualize", color = colors.textSecondary, fontSize = 13.sp)
            }
            return@BoxWithConstraints
        }

        val totalFilteredBytes = remember(itemsToMap) {
            itemsToMap.sumOf { it.size }.coerceAtLeast(1L)
        }

        val tiles = remember(itemsToMap, totalFilteredBytes, w, h) {
            computeSquarifiedTreemap(itemsToMap, totalFilteredBytes, w, h)
        }

        val density = LocalDensity.current

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(14.dp))
        ) {
            for (tile in tiles) {
                val isSelected = selectedItem?.file?.absolutePath == tile.item.file.absolutePath
                val tileWidthDp = with(density) { tile.width.toDp() }
                val tileHeightDp = with(density) { tile.height.toDp() }
                val tileXDp = with(density) { tile.x.toDp() }
                val tileYDp = with(density) { tile.y.toDp() }

                val isLargeItem = tile.item.size >= 1024 * 1024L || tile.item.percentage >= 12f

                Box(
                    modifier = Modifier
                        .offset(x = tileXDp, y = tileYDp)
                        .size(width = tileWidthDp, height = tileHeightDp)
                        .padding(2.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    tile.item.color.copy(alpha = if (isSelected) 0.65f else (if (colors.isDark) 0.32f else 0.22f)),
                                    tile.item.color.copy(alpha = if (isSelected) 0.40f else (if (colors.isDark) 0.16f else 0.10f))
                                )
                            )
                        )
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) colors.primaryAccent else (if (isLargeItem) tile.item.color.copy(alpha = 0.70f) else tile.item.color.copy(alpha = 0.35f)),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .pointerInput(tile.item.file.absolutePath) {
                            detectTapGestures(
                                onDoubleTap = {
                                    if (tile.item.isDirectory) {
                                        onDrillDown(tile.item.file)
                                    }
                                },
                                onTap = {
                                    if (isSelected) {
                                        onSelectItem(null)
                                    } else {
                                        onSelectItem(tile.item)
                                    }
                                }
                            )
                        }
                        .padding(horizontal = 7.dp, vertical = 6.dp)
                ) {
                    // Specular top highlight line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .align(Alignment.TopCenter)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = if (isSelected) 0.45f else 0.20f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Dynamic Content based on Tile Dimensions
                    if (tile.width >= 100f && tile.height >= 70f) {
                        // Large Tile: Full information with badges
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (tile.item.isDirectory) Icons.Default.Folder else Icons.Default.InsertDriveFile,
                                        contentDescription = null,
                                        tint = tile.item.color,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    if (tile.item.isDirectory) {
                                        Text(
                                            text = "FOLDER",
                                            color = tile.item.color,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }

                                if (isLargeItem) {
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(GlassAmber.copy(alpha = 0.25f))
                                            .padding(horizontal = 4.dp, vertical = 1.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "🔥 Large",
                                            color = GlassAmber,
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Text(
                                text = tile.item.name,
                                color = colors.textPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text(
                                        text = tile.item.formattedSize,
                                        color = colors.textPrimary,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.5.sp
                                    )
                                    Text(
                                        text = String.format("%.1f%%", tile.item.percentage),
                                        color = tile.item.color,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }

                                if (tile.item.isDirectory) {
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(colors.primaryAccent.copy(alpha = 0.22f))
                                            .clickable { onDrillDown(tile.item.file) }
                                            .padding(horizontal = 6.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Explore ↗",
                                            color = colors.primaryAccent,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    } else if (tile.width >= 60f && tile.height >= 40f) {
                        // Medium Tile
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = tile.item.name,
                                    color = colors.textPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                if (tile.item.isDirectory) {
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = tile.item.color,
                                        modifier = Modifier.size(11.dp)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Text(
                                    text = tile.item.formattedSize,
                                    color = colors.textSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = String.format("%.0f%%", tile.item.percentage),
                                    color = tile.item.color,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else if (tile.width >= 35f && tile.height >= 25f) {
                        // Small Tile
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = tile.item.name,
                                color = colors.textPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 9.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = tile.item.formattedSize,
                                color = tile.item.color,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Squarified Treemap layout algorithm (Bruls, Huizing, van Wijk).
 * Recursively creates rows along the shorter dimension of the remaining bounding box,
 * optimizing the aspect ratio (max(w/h, h/w)) to be close to 1.0 (squares).
 */
private fun computeSquarifiedTreemap(
    items: List<DiskItemAnalysis>,
    totalSize: Long,
    width: Float,
    height: Float
): List<TreemapTile> {
    if (items.isEmpty() || totalSize <= 0L || width <= 10f || height <= 10f) return emptyList()

    // Aggregate tiny tail files into an "Other" block if there are more than 16 items
    val maxDisplayCount = 16
    val displayedItems = if (items.size > maxDisplayCount) {
        val top = items.take(maxDisplayCount - 1)
        val rest = items.drop(maxDisplayCount - 1)
        val restBytes = rest.sumOf { it.size }
        val restPct = if (totalSize > 0) (restBytes.toDouble() / totalSize.toDouble() * 100).toFloat() else 0f
        top + DiskItemAnalysis(
            file = items.first().file,
            name = "Other (${rest.size} items)",
            isDirectory = false,
            size = restBytes,
            formattedSize = FileManagerEngine.formatSize(restBytes),
            percentage = restPct,
            category = FileCategory.OTHER,
            childCount = rest.size,
            color = Color(0xFF64748B)
        )
    } else {
        items
    }.filter { it.size > 0 }.sortedByDescending { it.size }

    if (displayedItems.isEmpty()) return emptyList()

    val totalArea = width * height
    val sumSize = displayedItems.sumOf { it.size }.toDouble().coerceAtLeast(1.0)

    val normalizedItems = displayedItems.map { item ->
        val area = ((item.size.toDouble() / sumSize) * totalArea).toFloat().coerceAtLeast(1f)
        Pair(item, area)
    }

    val result = mutableListOf<TreemapTile>()

    fun worst(row: List<Pair<DiskItemAnalysis, Float>>, sideLength: Float): Float {
        if (row.isEmpty() || sideLength <= 0f) return Float.MAX_VALUE
        val rowSum = row.sumOf { it.second.toDouble() }.toFloat().coerceAtLeast(1f)
        val sideSq = sideLength * sideLength
        val rowSumSq = rowSum * rowSum
        var maxRatio = 0f
        for (item in row) {
            val a = item.second.coerceAtLeast(1f)
            val r1 = (sideSq * a) / rowSumSq
            val r2 = rowSumSq / (sideSq * a)
            val ratio = maxOf(r1, r2)
            if (ratio > maxRatio) maxRatio = ratio
        }
        return maxRatio
    }

    fun layoutRow(row: List<Pair<DiskItemAnalysis, Float>>, rect: RectF): RectF {
        val rowSum = row.sumOf { it.second.toDouble() }.toFloat().coerceAtLeast(1f)
        val isHorizontal = rect.width <= rect.height
        val side = if (isHorizontal) rect.width else rect.height
        val thickness = (rowSum / side).coerceAtLeast(1f)

        if (isHorizontal) {
            var currX = rect.x
            for (elem in row) {
                val elemWidth = (elem.second / thickness).coerceAtLeast(1f)
                result.add(TreemapTile(elem.first, currX, rect.y, elemWidth, thickness))
                currX += elemWidth
            }
            return RectF(rect.x, rect.y + thickness, rect.width, (rect.height - thickness).coerceAtLeast(0f))
        } else {
            var currY = rect.y
            for (elem in row) {
                val elemHeight = (elem.second / thickness).coerceAtLeast(1f)
                result.add(TreemapTile(elem.first, rect.x, currY, thickness, elemHeight))
                currY += elemHeight
            }
            return RectF(rect.x + thickness, rect.y, (rect.width - thickness).coerceAtLeast(0f), rect.height)
        }
    }

    var remainingRect = RectF(0f, 0f, width, height)
    var itemsRemaining = normalizedItems

    while (itemsRemaining.isNotEmpty() && remainingRect.width > 2f && remainingRect.height > 2f) {
        val sideLength = minOf(remainingRect.width, remainingRect.height)
        val candidateRow = mutableListOf<Pair<DiskItemAnalysis, Float>>()
        candidateRow.add(itemsRemaining.first())
        var idx = 1

        while (idx < itemsRemaining.size) {
            val nextItem = itemsRemaining[idx]
            val testRow = candidateRow + nextItem
            if (worst(candidateRow, sideLength) >= worst(testRow, sideLength)) {
                candidateRow.add(nextItem)
                idx++
            } else {
                break
            }
        }

        remainingRect = layoutRow(candidateRow, remainingRect)
        itemsRemaining = itemsRemaining.drop(candidateRow.size)
    }

    if (itemsRemaining.isNotEmpty() && remainingRect.width > 2f && remainingRect.height > 2f) {
        layoutRow(itemsRemaining, remainingRect)
    }

    return result
}

/**
 * Category breakdown pills row (Folder, Images, Video, Documents, etc.)
 */
@Composable
private fun CategoryBreakdownRow(
    categoryStats: List<DiskCategoryStat>,
    selectedCategory: FileCategory?,
    onSelectCategory: (FileCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (stat in categoryStats) {
            val isSelected = selectedCategory == stat.category
            LiquidGlassPill(
                text = "${stat.category.label} (${String.format("%.0f", stat.percentage)}%)",
                isSelected = isSelected,
                onClick = { onSelectCategory(stat.category) },
                leadingIcon = {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(stat.color)
                    )
                }
            )
        }
    }
}

/**
 * Ranked individual file or folder row with category badge, progress bar, and actions.
 */
@Composable
private fun DiskItemRow(
    rank: Int,
    item: DiskItemAnalysis,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDrillDown: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LiquidGlassTheme.colors

    LiquidGlassCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        borderWidth = if (isSelected) 1.8.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank pill
            Text(
                text = "#$rank",
                color = colors.textMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(26.dp)
            )

            // Category Icon with colored glow
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(item.color.copy(alpha = if (colors.isDark) 0.18f else 0.12f))
                    .border(1.dp, item.color.copy(alpha = 0.40f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (item.isDirectory) Icons.Default.Folder else Icons.Default.InsertDriveFile,
                    contentDescription = null,
                    tint = item.color,
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details and progress bar
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.name,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = item.formattedSize,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LiquidProgressBar(
                        progress = item.percentage / 100f,
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp),
                        barColor = Brush.horizontalGradient(
                            listOf(item.color, item.color.copy(alpha = 0.7f))
                        )
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = String.format("%.1f%%", item.percentage),
                        color = item.color,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp
                    )
                }
            }

            // Quick Drill in or Open Action
            if (item.isDirectory) {
                Spacer(modifier = Modifier.width(8.dp))
                LiquidGlassIconButton(
                    icon = Icons.Default.ChevronRight,
                    contentDescription = "Scan Folder",
                    onClick = onDrillDown,
                    tint = colors.primaryAccent,
                    size = 32.dp
                )
            }
        }
    }
}

/**
 * Floating action card when an item is tapped in the chart or list.
 */
@Composable
private fun SelectedItemActionCard(
    item: DiskItemAnalysis,
    onClose: () -> Unit,
    onDrillDown: () -> Unit,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = LiquidGlassTheme.colors

    FrostedGlassPanel(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.5.dp, colors.primaryAccent.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(item.color.copy(alpha = 0.20f))
                            .border(1.dp, item.color.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (item.isDirectory) Icons.Default.Folder else Icons.Default.InsertDriveFile,
                            contentDescription = null,
                            tint = item.color,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = item.name,
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${item.formattedSize} · ${String.format("%.1f", item.percentage)}% of directory",
                            color = colors.textSecondary,
                            fontSize = 11.5.sp
                        )
                    }
                }

                LiquidGlassIconButton(
                    icon = Icons.Default.Close,
                    contentDescription = "Close",
                    onClick = onClose,
                    tint = colors.textMuted,
                    size = 32.dp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (item.isDirectory) {
                    AnalyzerActionButton(
                        label = "Scan Inside",
                        icon = Icons.Default.Analytics,
                        color = colors.primaryAccent,
                        onClick = onDrillDown,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    AnalyzerActionButton(
                        label = "Open File",
                        icon = Icons.Default.OpenInNew,
                        color = colors.primaryAccent,
                        onClick = onOpen,
                        modifier = Modifier.weight(1f)
                    )
                    AnalyzerActionButton(
                        label = "Share",
                        icon = Icons.Default.Share,
                        color = GlassPurple,
                        onClick = onShare,
                        modifier = Modifier.weight(1f)
                    )
                }

                AnalyzerActionButton(
                    label = "Delete",
                    icon = Icons.Default.Delete,
                    color = GlassRed,
                    onClick = onDelete,
                    modifier = Modifier.weight(0.9f)
                )
            }
        }
    }
}

@Composable
private fun AnalyzerActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LiquidGlassTheme.colors
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = if (colors.isDark) 0.18f else 0.12f))
            .border(1.dp, color.copy(alpha = 0.40f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp, horizontal = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}
