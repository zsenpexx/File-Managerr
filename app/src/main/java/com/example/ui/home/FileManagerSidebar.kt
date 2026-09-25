package com.example.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FileCategory
import com.example.model.StorageStats
import com.example.engine.FileManagerEngine
import com.example.ui.ActiveScreen
import com.example.ui.theme.GlassAmber
import com.example.ui.theme.GlassAzure
import com.example.ui.theme.GlassCyan
import com.example.ui.theme.GlassIndigo
import com.example.ui.theme.LiquidGlassTheme

/**
 * Apple Liquid Glass Sidebar Navigation Drawer.
 * Sliding navigation panel giving instant access to Home, Storage, Analyzer, Categories, and Theme.
 */
@Composable
fun FileManagerSidebar(
    activeScreen: ActiveScreen,
    storageStats: StorageStats,
    onNavigateHome: () -> Unit,
    onNavigateMainStorage: () -> Unit,
    onNavigateDownloads: () -> Unit,
    onNavigateAnalyzer: () -> Unit,
    onNavigateCategory: (FileCategory) -> Unit,
    onNavigateNewFiles: () -> Unit,
    onToggleTheme: () -> Unit,
    onCloseSidebar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LiquidGlassTheme.colors
    val isDark = colors.isDark

    ModalDrawerSheet(
        modifier = modifier
            .width(310.dp)
            .fillMaxHeight(),
        drawerContainerColor = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .clip(RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp))
                .background(
                    if (isDark) {
                        Brush.verticalGradient(
                            listOf(
                                Color(0xF50B132B),
                                Color(0xF50F172A),
                                Color(0xF5070E1E)
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            listOf(
                                Color(0xF5FFFFFF),
                                Color(0xF2F8FAFC),
                                Color(0xF5FFFFFF)
                            )
                        )
                    }
                )
                .border(
                    BorderStroke(
                        1.2.dp,
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = if (isDark) 0.60f else 0.95f),
                                if (isDark) GlassCyan.copy(alpha = 0.25f) else Color(0xFF007AFF).copy(alpha = 0.30f),
                                Color.White.copy(alpha = if (isDark) 0.08f else 0.30f)
                            )
                        )
                    ),
                    RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
                )
                .drawBehind {
                    // Right edge subtle specular highlight
                    drawLine(
                        color = Color.White.copy(alpha = if (isDark) 0.30f else 0.60f),
                        start = Offset(size.width - 1f, 0f),
                        end = Offset(size.width - 1f, size.height),
                        strokeWidth = 1.5f
                    )
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp)
            ) {
                // Header: App Logo & Storage Progress
                SidebarHeader(
                    storageStats = storageStats,
                    isDark = isDark,
                    onStorageClick = {
                        onCloseSidebar()
                        onNavigateAnalyzer()
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    color = if (isDark) Color.White.copy(alpha = 0.10f) else Color(0xFFE2E8F0)
                )

                // Navigation Items: Primary Destinations
                SidebarNavItem(
                    title = "Home",
                    icon = Icons.Default.Home,
                    isSelected = activeScreen is ActiveScreen.Home,
                    accentColor = colors.primaryAccent,
                    onClick = {
                        onCloseSidebar()
                        onNavigateHome()
                    }
                )

                SidebarNavItem(
                    title = "Main Storage",
                    icon = Icons.Default.Storage,
                    isSelected = activeScreen is ActiveScreen.MainBrowser,
                    accentColor = Color(0xFF0284C7),
                    onClick = {
                        onCloseSidebar()
                        onNavigateMainStorage()
                    }
                )

                SidebarNavItem(
                    title = "Downloads",
                    icon = Icons.Default.Download,
                    isSelected = false,
                    accentColor = Color(0xFFF59E0B),
                    onClick = {
                        onCloseSidebar()
                        onNavigateDownloads()
                    }
                )

                SidebarNavItem(
                    title = "Storage Analyzer",
                    icon = Icons.Default.PieChart,
                    isSelected = activeScreen is ActiveScreen.DiskAnalyzer,
                    accentColor = Color(0xFFA855F7),
                    onClick = {
                        onCloseSidebar()
                        onNavigateAnalyzer()
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    color = if (isDark) Color.White.copy(alpha = 0.10f) else Color(0xFFE2E8F0)
                )

                // Category Section
                Text(
                    text = "CATEGORIES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textMuted,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                )

                SidebarNavItem(
                    title = "Images",
                    icon = Icons.Default.Image,
                    isSelected = false,
                    accentColor = Color(0xFFA855F7),
                    onClick = {
                        onCloseSidebar()
                        onNavigateCategory(FileCategory.IMAGE)
                    }
                )

                SidebarNavItem(
                    title = "Audio",
                    icon = Icons.Default.MusicNote,
                    isSelected = false,
                    accentColor = Color(0xFF0D9488),
                    onClick = {
                        onCloseSidebar()
                        onNavigateCategory(FileCategory.AUDIO)
                    }
                )

                SidebarNavItem(
                    title = "Videos",
                    icon = Icons.Default.Videocam,
                    isSelected = false,
                    accentColor = Color(0xFFEF4444),
                    onClick = {
                        onCloseSidebar()
                        onNavigateCategory(FileCategory.VIDEO)
                    }
                )

                SidebarNavItem(
                    title = "Documents",
                    icon = Icons.Default.Description,
                    isSelected = false,
                    accentColor = Color(0xFF2563EB),
                    onClick = {
                        onCloseSidebar()
                        onNavigateCategory(FileCategory.DOCUMENT)
                    }
                )

                SidebarNavItem(
                    title = "Apps",
                    icon = Icons.Default.Android,
                    isSelected = false,
                    accentColor = Color(0xFF16A34A),
                    onClick = {
                        onCloseSidebar()
                        onNavigateCategory(FileCategory.APK)
                    }
                )

                SidebarNavItem(
                    title = "New Files",
                    icon = Icons.Default.AccessTime,
                    isSelected = false,
                    accentColor = Color(0xFF0284C7),
                    onClick = {
                        onCloseSidebar()
                        onNavigateNewFiles()
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    color = if (isDark) Color.White.copy(alpha = 0.10f) else Color(0xFFE2E8F0)
                )

                // Theme Toggle Item
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.surfaceElevated.copy(alpha = 0.6f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = null,
                            tint = if (isDark) GlassAmber else GlassIndigo,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (isDark) "Light Mode" else "Dark Mode",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textPrimary
                        )
                    }
                    Switch(
                        checked = !isDark,
                        onCheckedChange = { onToggleTheme() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF007AFF),
                            uncheckedThumbColor = Color(0xFF94A3B8),
                            uncheckedTrackColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.testTag("sidebar_theme_switch")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Footer Info
                Text(
                    text = "File Managerr v1.0.1 • Liquid Glass",
                    fontSize = 11.sp,
                    color = colors.textMuted,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }
    }
}

/**
 * Sidebar Header with App Logo and Storage Meter.
 */
@Composable
private fun SidebarHeader(
    storageStats: StorageStats,
    isDark: Boolean,
    onStorageClick: () -> Unit
) {
    val colors = LiquidGlassTheme.colors

    val usedGB = if (storageStats.totalBytes > 0) {
        FileManagerEngine.formatSize(storageStats.usedBytes)
    } else "0 B"

    val totalGB = if (storageStats.totalBytes > 0) {
        FileManagerEngine.formatSize(storageStats.totalBytes)
    } else "0 B"

    val usedPct = (storageStats.usedPercent * 100).toInt()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // App Identity
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF0284C7), Color(0xFF0EA5E9), Color(0xFF06B6D4))
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "File Managerr",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = " +",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.primaryAccent
                    )
                }
                Text(
                    text = "High-Performance Explorer",
                    fontSize = 11.5.sp,
                    color = colors.textMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Storage Usage Gauge Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(colors.surfaceElevated)
                .clickable(onClick = onStorageClick)
                .padding(12.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Internal Storage",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "$usedPct%",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primaryAccent
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (isDark) Color(0x30FFFFFF) else Color(0x20000000))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(storageStats.usedPercent.coerceIn(0.05f, 1f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(colors.primaryAccent, colors.secondaryAccent)
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "$usedGB / $totalGB used",
                    fontSize = 11.sp,
                    color = colors.textMuted
                )
            }
        }
    }
}

/**
 * Individual Sidebar Navigation Item with Active Liquid Glass Pill.
 */
@Composable
private fun SidebarNavItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val colors = LiquidGlassTheme.colors
    val isDark = colors.isDark

    val bgColor by animateColorAsState(
        targetValue = if (isSelected) {
            accentColor.copy(alpha = if (isDark) 0.22f else 0.15f)
        } else Color.Transparent,
        label = "nav_item_bg"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) {
            if (isDark) Color.White else accentColor
        } else colors.textPrimary,
        label = "nav_item_text"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = accentColor.copy(alpha = 0.2f)),
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (isSelected) accentColor else colors.textSecondary,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
    }
}
