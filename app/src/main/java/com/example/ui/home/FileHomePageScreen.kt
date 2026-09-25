package com.example.ui.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.FileCategory
import com.example.model.StorageStats
import com.example.model.DeviceMediaStats
import com.example.engine.FileManagerEngine
import com.example.ui.components.FrostedGlassPanel
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.theme.GlassAmber
import com.example.ui.theme.GlassAzure
import com.example.ui.theme.GlassCyan
import com.example.ui.theme.GlassGreen
import com.example.ui.theme.GlassIndigo
import com.example.ui.theme.GlassPink
import com.example.ui.theme.GlassPurple
import com.example.ui.theme.GlassRed
import com.example.ui.theme.LiquidGlassTheme

/**
 * Visual Data Representation of an iconic Home Tile.
 */
data class HomeTileItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val gradientColors: List<Color>,
    val iconTint: Color = Color.White,
    val showCustomIcon: Boolean = false,
    val onClick: () -> Unit
)

/**
 * File Manager + Iconic Liquid Glass Homepage.
 * Closely matching the user's reference screenshot with:
 * - Top Bar: Hamburger menu, Title with +, Crown VIP badge, and Overflow menu
 * - 3x4 Liquid Glass Grid of items (Main storage, Downloads, Storage Analysis, etc.)
 * - Bottom Ad banner place (ColorNote Notepad install banner)
 */
@Composable
fun FileHomePageScreen(
    storageStats: StorageStats,
    deviceMediaStats: DeviceMediaStats = DeviceMediaStats(),
    onOpenSidebar: () -> Unit,
    onNavigateToMainStorage: () -> Unit,
    onNavigateToDownloads: () -> Unit,
    onOpenDiskAnalyzer: () -> Unit,
    onOpenCategory: (FileCategory) -> Unit,
    onOpenNewFiles: () -> Unit,
    onToggleTheme: () -> Unit,
    onRefreshStats: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LiquidGlassTheme.colors
    val isDark = colors.isDark
    val context = LocalContext.current

    var showMoreMenu by remember { mutableStateOf(false) }
    var showCloudDialog by remember { mutableStateOf(false) }
    var showRemoteDialog by remember { mutableStateOf(false) }
    var showAccessFromPcDialog by remember { mutableStateOf(false) }
    var showAdDismissed by remember { mutableStateOf(false) }
    var showVipDialog by remember { mutableStateOf(false) }

    // Format Real Device Storage Stats
    val usedFormatted = if (storageStats.totalBytes > 0) {
        FileManagerEngine.formatSize(storageStats.usedBytes)
    } else "0 B"

    val totalFormatted = if (storageStats.totalBytes > 0) {
        FileManagerEngine.formatSize(storageStats.totalBytes)
    } else "0 B"

    val storageSubtitle = if (storageStats.totalBytes > 0) {
        "$usedFormatted / $totalFormatted"
    } else "Internal Storage"

    val usedPercentInt = (storageStats.usedPercent * 100).toInt()

    // Build the 12 Tiles with real device values
    val tiles = listOf(
        // 1. Main Storage
        HomeTileItem(
            id = "main_storage",
            title = "Main storage",
            subtitle = storageSubtitle,
            icon = Icons.Default.Storage,
            gradientColors = listOf(Color(0xFFE2E8F0), Color(0xFF94A3B8)),
            iconTint = Color(0xFF334155),
            showCustomIcon = true,
            onClick = onNavigateToMainStorage
        ),
        // 2. Downloads
        HomeTileItem(
            id = "downloads",
            title = "Downloads",
            subtitle = deviceMediaStats.downloads.displaySubtitle,
            icon = Icons.Default.Download,
            gradientColors = listOf(Color(0xFFFBBF24), Color(0xFFF59E0B)),
            onClick = onNavigateToDownloads
        ),
        // 3. Storage Analysis
        HomeTileItem(
            id = "storage_analyzer",
            title = "Storage Anal...",
            subtitle = "$usedPercentInt% used",
            icon = Icons.Default.PieChart,
            gradientColors = listOf(Color(0xFF64748B), Color(0xFF334155)),
            showCustomIcon = true,
            onClick = onOpenDiskAnalyzer
        ),
        // 4. Images
        HomeTileItem(
            id = "images",
            title = "Images",
            subtitle = deviceMediaStats.images.displaySubtitle,
            icon = Icons.Default.Image,
            gradientColors = listOf(Color(0xFFC084FC), Color(0xFFA855F7)),
            onClick = { onOpenCategory(FileCategory.IMAGE) }
        ),
        // 5. Audio
        HomeTileItem(
            id = "audio",
            title = "Audio",
            subtitle = deviceMediaStats.audio.displaySubtitle,
            icon = Icons.Default.MusicNote,
            gradientColors = listOf(Color(0xFF2DD4BF), Color(0xFF0D9488)),
            onClick = { onOpenCategory(FileCategory.AUDIO) }
        ),
        // 6. Videos
        HomeTileItem(
            id = "videos",
            title = "Videos",
            subtitle = deviceMediaStats.videos.displaySubtitle,
            icon = Icons.Default.Videocam,
            gradientColors = listOf(Color(0xFFF87171), Color(0xFFEF4444)),
            onClick = { onOpenCategory(FileCategory.VIDEO) }
        ),
        // 7. Documents
        HomeTileItem(
            id = "documents",
            title = "Documents",
            subtitle = deviceMediaStats.documents.displaySubtitle,
            icon = Icons.Default.Description,
            gradientColors = listOf(Color(0xFF60A5FA), Color(0xFF2563EB)),
            onClick = { onOpenCategory(FileCategory.DOCUMENT) }
        ),
        // 8. Apps
        HomeTileItem(
            id = "apps",
            title = "Apps",
            subtitle = deviceMediaStats.apps.displaySubtitle,
            icon = Icons.Default.Android,
            gradientColors = listOf(Color(0xFF4ADE80), Color(0xFF16A34A)),
            onClick = { onOpenCategory(FileCategory.APK) }
        ),
        // 9. New Files
        HomeTileItem(
            id = "new_files",
            title = "New files",
            subtitle = deviceMediaStats.newFiles.displaySubtitle,
            icon = Icons.Default.AccessTime,
            gradientColors = listOf(Color(0xFF38BDF8), Color(0xFF0284C7)),
            onClick = onOpenNewFiles
        ),
        // 10. Cloud
        HomeTileItem(
            id = "cloud",
            title = "Cloud",
            subtitle = "(0)",
            icon = Icons.Default.Cloud,
            gradientColors = listOf(Color(0xFF60A5FA), Color(0xFF3B82F6)),
            onClick = { showCloudDialog = true }
        ),
        // 11. Remote
        HomeTileItem(
            id = "remote",
            title = "Remote",
            subtitle = "(0)",
            icon = Icons.Default.Tv,
            gradientColors = listOf(Color(0xFF818CF8), Color(0xFF4F46E5)),
            onClick = { showRemoteDialog = true }
        ),
        // 12. Access from...
        HomeTileItem(
            id = "access_from_pc",
            title = "Access from...",
            subtitle = "(FTP/Web)",
            icon = Icons.Default.Devices,
            gradientColors = listOf(Color(0xFF34D399), Color(0xFF059669)),
            onClick = { showAccessFromPcDialog = true }
        )
    )

    LiquidGlassBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Bar
            HomeTopBar(
                onOpenSidebar = onOpenSidebar,
                onVipClick = { showVipDialog = true },
                onToggleTheme = onToggleTheme,
                onRefreshStats = onRefreshStats,
                showMoreMenu = showMoreMenu,
                onDismissMoreMenu = { showMoreMenu = false },
                onOpenMoreMenu = { showMoreMenu = true }
            )

            // Main 3x4 Grid of Tiles
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(tiles.size) { index ->
                    HomeGridTile(tile = tiles[index])
                }
            }

            // Bottom Ad Banner Place (ColorNote Notepad style)
            AnimatedVisibility(
                visible = !showAdDismissed,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                BottomAdBannerPlace(
                    onInstallClick = {
                        Toast.makeText(context, "Opening ColorNote Notepad...", Toast.LENGTH_SHORT).show()
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.socialnmobile.colornote"))
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.socialnmobile.colornote"))
                            webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(webIntent)
                        }
                    },
                    onDismiss = { showAdDismissed = true }
                )
            }
        }
    }

    // Modals
    if (showCloudDialog) {
        CloudServicesDialog(onDismiss = { showCloudDialog = false })
    }

    if (showRemoteDialog) {
        RemoteConnectionsDialog(onDismiss = { showRemoteDialog = false })
    }

    if (showAccessFromPcDialog) {
        AccessFromPcDialog(onDismiss = { showAccessFromPcDialog = false })
    }

    if (showVipDialog) {
        VipFeaturesDialog(onDismiss = { showVipDialog = false })
    }
}

/**
 * Top Bar with Hamburger, "File Manager +", VIP Crown, and Overflow Menu.
 */
@Composable
private fun HomeTopBar(
    onOpenSidebar: () -> Unit,
    onVipClick: () -> Unit,
    onToggleTheme: () -> Unit,
    onRefreshStats: () -> Unit,
    showMoreMenu: Boolean,
    onDismissMoreMenu: () -> Unit,
    onOpenMoreMenu: () -> Unit
) {
    val colors = LiquidGlassTheme.colors
    val isDark = colors.isDark

    FrostedGlassPanel(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(22.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Hamburger Menu
            IconButton(
                onClick = onOpenSidebar,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("home_hamburger_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open Sidebar Navigation",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }

            // Title: "File Manager +"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "File Managerr",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    text = " +",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.primaryAccent
                )
            }

            // Right Actions: VIP Crown + Overflow Menu
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Crown VIP Icon
                IconButton(
                    onClick = onVipClick,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("home_crown_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = "VIP Premium Features",
                        tint = Color(0xFFF59E0B), // Vibrant Gold
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Three-dot Overflow Menu
                Box {
                    IconButton(
                        onClick = onOpenMoreMenu,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("home_overflow_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = onDismissMoreMenu,
                        modifier = Modifier
                            .background(if (isDark) Color(0xF20F172A) else Color(0xF2FFFFFF))
                            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (isDark) "Switch to Light Theme" else "Switch to Dark Theme",
                                    color = colors.textPrimary
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = null,
                                    tint = if (isDark) GlassAmber else GlassIndigo
                                )
                            },
                            onClick = {
                                onDismissMoreMenu()
                                onToggleTheme()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Refresh Storage Stats", color = colors.textPrimary) },
                            leadingIcon = {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = colors.primaryAccent)
                            },
                            onClick = {
                                onDismissMoreMenu()
                                onRefreshStats()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Settings", color = colors.textPrimary) },
                            leadingIcon = {
                                Icon(Icons.Default.Settings, contentDescription = null, tint = colors.textSecondary)
                            },
                            onClick = {
                                onDismissMoreMenu()
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual Squircle Home Tile with Apple Liquid Glass styling.
 */
@Composable
private fun HomeGridTile(
    tile: HomeTileItem
) {
    val colors = LiquidGlassTheme.colors
    val isDark = colors.isDark

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "tile_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = colors.primaryAccent.copy(alpha = 0.25f), bounded = false),
                onClick = tile.onClick
            )
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Rounded Squircle Glass Icon Container
        Box(
            modifier = Modifier
                .size(72.dp)
                .shadow(
                    elevation = if (isDark) 10.dp else 6.dp,
                    shape = RoundedCornerShape(22.dp),
                    ambientColor = Color.Black.copy(alpha = 0.35f),
                    spotColor = tile.gradientColors.first().copy(alpha = 0.5f)
                )
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.linearGradient(
                        colors = if (isDark) {
                            listOf(
                                tile.gradientColors.first().copy(alpha = 0.35f),
                                tile.gradientColors.last().copy(alpha = 0.20f),
                                Color(0x18FFFFFF)
                            )
                        } else {
                            listOf(
                                tile.gradientColors.first().copy(alpha = 0.22f),
                                tile.gradientColors.last().copy(alpha = 0.12f),
                                Color.White.copy(alpha = 0.85f)
                            )
                        }
                    )
                )
                .border(
                    BorderStroke(
                        1.2.dp,
                        Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = if (isDark) 0.70f else 0.95f),
                                tile.gradientColors.first().copy(alpha = 0.50f),
                                Color.White.copy(alpha = if (isDark) 0.15f else 0.40f)
                            )
                        )
                    ),
                    RoundedCornerShape(22.dp)
                )
                .drawBehind {
                    // Top hairline specular reflection
                    drawLine(
                        color = Color.White.copy(alpha = if (isDark) 0.75f else 0.90f),
                        start = Offset(size.width * 0.15f, 1f),
                        end = Offset(size.width * 0.85f, 1f),
                        strokeWidth = 2f
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (tile.id == "main_storage") {
                // Main Storage Custom HDD with Green LED
                HardwareStorageIcon(isDark = isDark)
            } else if (tile.id == "storage_analyzer") {
                // Segmented Storage Chart Icon
                StorageDonutChartIcon()
            } else if (tile.id == "downloads") {
                // Folder with download circle arrow
                DownloadsFolderIcon()
            } else {
                // Standard Category Squircle
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(tile.gradientColors)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = tile.icon,
                        contentDescription = tile.title,
                        tint = tile.iconTint,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Title Text
        Text(
            text = tile.title,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 2.dp)
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Subtitle Text
        Text(
            text = tile.subtitle,
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            color = colors.textMuted,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
    }
}

/**
 * Custom Hardware HDD Icon with Green Active LED dot.
 */
@Composable
private fun HardwareStorageIcon(isDark: Boolean) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFCBD5E1), Color(0xFF94A3B8))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Drive lines
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(Color(0xFF64748B).copy(alpha = 0.4f))
            )
            // LED Light
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF22C55E)) // Bright Green LED
                        .border(1.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                )
            }
        }
    }
}

/**
 * Custom Downloads Folder with Download Badge.
 */
@Composable
private fun DownloadsFolderIcon() {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFFBBF24), Color(0xFFF59E0B), Color(0xFFD97706))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Inner white card with downward arrow
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = null,
                tint = Color(0xFF0284C7),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Segmented Donut Storage Analysis Graphic Icon.
 */
@Composable
private fun StorageDonutChartIcon() {
    Canvas(modifier = Modifier.size(46.dp)) {
        val strokeW = 10.dp.toPx()
        val radius = (size.minDimension - strokeW) / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        // Segment 1: Slate
        drawArc(
            color = Color(0xFF475569),
            startAngle = 0f,
            sweepAngle = 210f,
            useCenter = false,
            style = Stroke(strokeW),
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2f, radius * 2f)
        )
        // Segment 2: Light Gray / Cyan
        drawArc(
            color = Color(0xFF94A3B8),
            startAngle = 210f,
            sweepAngle = 150f,
            useCenter = false,
            style = Stroke(strokeW),
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2f, radius * 2f)
        )
    }
}

/**
 * Bottom Ad Place closely matching the user's reference screenshot:
 * - Left: Yellow sticky note icon with handwritten "NOTE"
 * - Title: "ColorNote Notepad Notes To..."
 * - Subtitle: "ColorNote® is a simple and awesome notepad app."
 * - Right: "INSTALL" blue button
 * - Tiny "Ad" indicator in the bottom corner
 */
@Composable
private fun BottomAdBannerPlace(
    onInstallClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LiquidGlassTheme.colors
    val isDark = colors.isDark

    Surface(
        color = if (isDark) Color(0xFF0F172A).copy(alpha = 0.95f) else Color.White.copy(alpha = 0.98f),
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .border(
                BorderStroke(
                    1.dp,
                    if (isDark) Color.White.copy(alpha = 0.12f) else Color(0xFFE2E8F0)
                )
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Yellow Sticky Note Icon
                StickyNoteAdIcon()

                Spacer(modifier = Modifier.width(12.dp))

                // Ad Text Description
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "ColorNote Notepad Notes To...",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "ColorNote® is a simple and awesome notepad app.",
                        fontSize = 12.sp,
                        color = colors.textMuted,
                        maxLines = 2,
                        lineHeight = 15.sp,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // "INSTALL" Blue Button
                Button(
                    onClick = onInstallClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3B82F6), // Vibrant Play Store Blue
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("ad_install_button")
                ) {
                    Text(
                        text = "INSTALL",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Tiny "Ad" Badge in bottom-right corner
            Text(
                text = "Ad",
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF007AFF),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 2.dp, bottom = 0.dp)
            )
        }
    }
}

/**
 * Yellow Post-It Note Graphic Icon with "NOTE".
 */
@Composable
private fun StickyNoteAdIcon() {
    Box(
        modifier = Modifier
            .size(46.dp)
            .shadow(2.dp, RoundedCornerShape(6.dp))
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFFEF08A)) // Warm Post-It Yellow
            .border(0.8.dp, Color(0xFFFDE047), RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Folded corner on top/bottom
            val path = Path().apply {
                moveTo(size.width * 0.75f, size.height)
                lineTo(size.width, size.height * 0.75f)
                lineTo(size.width * 0.75f, size.height * 0.75f)
                close()
            }
            drawPath(path, color = Color(0xFFEAB308).copy(alpha = 0.6f))
        }

        Text(
            text = "NOTE",
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF713F12), // Deep brown/amber ink
            fontFamily = FontFamily.SansSerif,
            letterSpacing = (-0.5).sp
        )
    }
}

// --------------------------------------------------------------------------
// MODAL DIALOGS (Cloud, Remote, Access from PC, VIP)
// --------------------------------------------------------------------------

@Composable
private fun CloudServicesDialog(onDismiss: () -> Unit) {
    val colors = LiquidGlassTheme.colors
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colors.panelBackground),
            border = BorderStroke(1.dp, colors.borderLight),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0x2038BDF8)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Cloud,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Cloud Storage",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Seamlessly access files stored in your cloud drives directly within the app.",
                    fontSize = 13.sp,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Cloud Services List
                CloudServiceRow("Google Drive", "Connected • 12.4 GB / 15 GB", Color(0xFF34A853)) {
                    Toast.makeText(context, "Browsing Google Drive files", Toast.LENGTH_SHORT).show()
                    onDismiss()
                }
                CloudServiceRow("Microsoft OneDrive", "Connected • 4.1 GB / 5 GB", Color(0xFF0078D4)) {
                    Toast.makeText(context, "Browsing OneDrive files", Toast.LENGTH_SHORT).show()
                    onDismiss()
                }
                CloudServiceRow("Dropbox", "Connected • 1.2 GB / 2 GB", Color(0xFF0061FF)) {
                    Toast.makeText(context, "Browsing Dropbox files", Toast.LENGTH_SHORT).show()
                    onDismiss()
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primaryAccent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun CloudServiceRow(name: String, status: String, accent: Color, onClick: () -> Unit) {
    val colors = LiquidGlassTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceElevated)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(accent)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                Text(text = status, fontSize = 11.sp, color = colors.textMuted)
            }
        }
        Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = colors.textMuted,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun RemoteConnectionsDialog(onDismiss: () -> Unit) {
    val colors = LiquidGlassTheme.colors
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colors.panelBackground),
            border = BorderStroke(1.dp, colors.borderLight),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0x20818CF8)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Tv,
                        contentDescription = null,
                        tint = Color(0xFF818CF8),
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Remote Network Storage",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Connect to Windows SMB network shares, FTP, SFTP, and WebDAV servers on your local network.",
                    fontSize = 13.sp,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                CloudServiceRow("Home NAS (SMB)", "Online • 192.168.1.120", Color(0xFF10B981)) {
                    Toast.makeText(context, "Connected to Home NAS", Toast.LENGTH_SHORT).show()
                    onDismiss()
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primaryAccent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun AccessFromPcDialog(onDismiss: () -> Unit) {
    val colors = LiquidGlassTheme.colors
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var isServerRunning by remember { mutableStateOf(true) }
    val serverUrl = "ftp://192.168.1.105:2121"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colors.panelBackground),
            border = BorderStroke(1.dp, colors.borderLight),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0x2034D399)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Devices,
                        contentDescription = null,
                        tint = Color(0xFF34D399),
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Access from PC",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Transfer files wirelessly between your computer and phone over the same Wi-Fi network without cables.",
                    fontSize = 13.sp,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Server Status Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.surfaceElevated)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isServerRunning) "Status: Running" else "Status: Stopped",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isServerRunning) Color(0xFF10B981) else colors.textMuted
                    )
                    Switch(
                        checked = isServerRunning,
                        onCheckedChange = { isServerRunning = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF10B981)
                        )
                    )
                }

                if (isServerRunning) {
                    Spacer(modifier = Modifier.height(12.dp))

                    // Address Box with Copy Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0284C7).copy(alpha = 0.12f))
                            .border(1.dp, Color(0xFF0284C7).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = serverUrl,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0284C7)
                        )
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(serverUrl))
                                Toast.makeText(context, "Address copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "Copy URL",
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Enter this URL into Windows Explorer or browser on your PC.",
                        fontSize = 11.5.sp,
                        color = colors.textMuted,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primaryAccent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun VipFeaturesDialog(onDismiss: () -> Unit) {
    val colors = LiquidGlassTheme.colors

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colors.panelBackground),
            border = BorderStroke(1.dp, colors.borderLight),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF59E0B).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "File Managerr + VIP",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Enjoy an ad-free experience, unlimited cloud connections, advanced squarified storage analyzer, and high-speed PC wireless file transfers.",
                    fontSize = 13.sp,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Upgrade / Activate VIP", color = Color(0xFF1E1B4B), fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(6.dp))

                TextButton(onClick = onDismiss) {
                    Text("Maybe Later", color = colors.textMuted)
                }
            }
        }
    }
}
