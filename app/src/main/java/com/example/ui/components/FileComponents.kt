@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Html
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.engine.FileManagerEngine
import com.example.model.ClipboardAction
import com.example.model.ClipboardState
import com.example.model.FileCategory
import com.example.model.FileItem
import com.example.model.StorageStats
import com.example.ui.theme.GlassAmber
import com.example.ui.theme.GlassAzure
import com.example.ui.theme.GlassBorderHighlight
import com.example.ui.theme.GlassBorderLight
import com.example.ui.theme.GlassCyan
import com.example.ui.theme.GlassFrostInnerGlow
import com.example.ui.theme.GlassFrostPanelBg
import com.example.ui.theme.GlassGreen
import com.example.ui.theme.GlassIndigo
import com.example.ui.theme.GlassPink
import com.example.ui.theme.GlassPurple
import com.example.ui.theme.GlassRed
import com.example.ui.theme.GlassSpecularCyan
import com.example.ui.theme.GlassSpecularEdge
import com.example.ui.theme.LiquidGlassTheme
import java.io.File

@Composable
fun StorageOverviewCard(
    stats: StorageStats,
    onCategoryClick: (FileCategory) -> Unit,
    onAnalyzeClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = LiquidGlassTheme.colors

    FrostedGlassPanel(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        colors.primaryAccent.copy(alpha = 0.35f),
                                        colors.secondaryAccent.copy(alpha = 0.20f)
                                    )
                                )
                            )
                            .border(1.dp, colors.primaryAccent.copy(alpha = 0.45f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Storage,
                            contentDescription = null,
                            tint = colors.primaryAccent,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Internal Storage",
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp
                        )
                        Text(
                            "${FileManagerEngine.formatSize(stats.usedBytes)} used of ${FileManagerEngine.formatSize(stats.totalBytes)}",
                            color = colors.textSecondary,
                            fontSize = 11.5.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onAnalyzeClick != null) {
                        LiquidGlassPill(
                            text = "Analyze",
                            isSelected = false,
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Analytics,
                                    contentDescription = null,
                                    tint = colors.primaryAccent,
                                    modifier = Modifier.size(13.dp)
                                )
                            },
                            onClick = onAnalyzeClick
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Text(
                        "${(stats.usedPercent * 100).toInt()}%",
                        color = colors.primaryAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            LiquidProgressBar(progress = stats.usedPercent, height = 9.dp)

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Category Shortcuts with Frosted Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CategoryChip(name = "Images", icon = Icons.Default.Image, color = GlassPink) { onCategoryClick(FileCategory.IMAGE) }
                CategoryChip(name = "Videos", icon = Icons.Default.Movie, color = GlassPurple) { onCategoryClick(FileCategory.VIDEO) }
                CategoryChip(name = "Audio", icon = Icons.Default.Audiotrack, color = GlassAmber) { onCategoryClick(FileCategory.AUDIO) }
                CategoryChip(name = "Docs", icon = Icons.Default.Description, color = GlassAzure) { onCategoryClick(FileCategory.DOCUMENT) }
                CategoryChip(name = "Archives", icon = Icons.Default.Archive, color = GlassIndigo) { onCategoryClick(FileCategory.ARCHIVE) }
                CategoryChip(name = "APKs", icon = Icons.Default.Android, color = GlassGreen) { onCategoryClick(FileCategory.APK) }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    name: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "chip_press"
    )

    val colors = LiquidGlassTheme.colors
    val isDark = colors.isDark

    val chipBorder = remember(color, isDark) {
        Brush.linearGradient(
            listOf(
                color.copy(alpha = if (isDark) 0.70f else 0.85f),
                color.copy(alpha = if (isDark) 0.30f else 0.40f),
                if (isDark) Color.White.copy(alpha = 0.15f) else Color.White
            )
        )
    }

    val chipBg = remember(color, isDark) {
        if (isDark) {
            Brush.verticalGradient(
                listOf(
                    color.copy(alpha = 0.22f),
                    color.copy(alpha = 0.08f),
                    Color(0xFF0F172A).copy(alpha = 0.40f)
                )
            )
        } else {
            Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.92f),
                    color.copy(alpha = 0.14f),
                    Color.White.copy(alpha = 0.80f)
                )
            )
        }
    }

    Row(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(14.dp))
            .background(chipBg)
            .border(BorderStroke(1.dp, chipBorder), RoundedCornerShape(14.dp))
            .combinedClickable(
                interactionSource = interactionSource,
                indication = ripple(color = color.copy(alpha = 0.3f)),
                onClick = onClick
            )
            .padding(horizontal = 11.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(15.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(name, color = colors.textPrimary, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun BreadcrumbsBar(
    currentDir: File,
    rootStorageDir: File,
    onNavigateTo: (File) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LiquidGlassTheme.colors
    val isDark = colors.isDark
    val scrollState = rememberScrollState()

    val segments = remember(currentDir, rootStorageDir) {
        val list = mutableListOf<File>()
        var curr: File? = currentDir
        while (curr != null && curr.exists()) {
            list.add(0, curr)
            if (curr.absolutePath == rootStorageDir.absolutePath || curr.parentFile == null) break
            curr = curr.parentFile
        }
        list
    }

    LaunchedEffect(segments.size, currentDir) {
        scrollState.animateScrollTo(
            value = scrollState.maxValue,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Home root pill
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(if (isDark) Color.White.copy(alpha = 0.09f) else Color.White.copy(alpha = 0.85f))
                .border(
                    BorderStroke(
                        1.dp,
                        if (isDark) {
                            Brush.linearGradient(
                                listOf(Color.White.copy(alpha = 0.35f), colors.primaryAccent.copy(alpha = 0.25f))
                            )
                        } else {
                            Brush.linearGradient(
                                listOf(Color.White, Color(0x33000000))
                            )
                        }
                    ),
                    CircleShape
                )
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = colors.primaryAccent.copy(alpha = 0.35f)),
                    onClick = { onNavigateTo(rootStorageDir) }
                )
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Home, contentDescription = "Home", tint = colors.primaryAccent, modifier = Modifier.size(16.dp))
        }

        segments.forEachIndexed { index, dir ->
            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = colors.textMuted,
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(9.dp)
            )

            val isLast = index == segments.size - 1
            val displayName = if (dir.absolutePath == rootStorageDir.absolutePath) "Storage" else dir.name

            val bgBrush = if (isLast) {
                Brush.horizontalGradient(
                    listOf(
                        colors.primaryAccent.copy(alpha = if (isDark) 0.32f else 0.88f),
                        colors.secondaryAccent.copy(alpha = if (isDark) 0.22f else 0.82f)
                    )
                )
            } else {
                Brush.horizontalGradient(
                    listOf(
                        if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.85f),
                        if (isDark) Color.White.copy(alpha = 0.03f) else Color.White.copy(alpha = 0.70f)
                    )
                )
            }

            val borderBrush = if (isLast) {
                Brush.horizontalGradient(listOf(colors.primaryAccent, colors.secondaryAccent))
            } else {
                Brush.horizontalGradient(
                    listOf(
                        if (isDark) Color.White.copy(alpha = 0.25f) else Color.White,
                        if (isDark) Color.White.copy(alpha = 0.08f) else Color(0x25000000)
                    )
                )
            }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(bgBrush)
                    .border(BorderStroke(1.dp, borderBrush), CircleShape)
                    .combinedClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = colors.primaryAccent.copy(alpha = 0.3f)),
                        onClick = { onNavigateTo(dir) }
                    )
                    .padding(horizontal = 11.dp, vertical = 6.dp)
            ) {
                Text(
                    text = displayName,
                    color = if (isLast) {
                        if (isDark) colors.primaryAccent else Color.White
                    } else {
                        colors.textPrimary // Pure black in light mode!
                    },
                    fontSize = 12.sp,
                    fontWeight = if (isLast) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun FileListItem(
    item: FileItem,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onOpenWith: () -> Unit,
    onShare: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onInfo: () -> Unit,
    onCompress: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val colors = LiquidGlassTheme.colors
    val isDark = colors.isDark

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.965f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "item_press"
    )

    val bgBrush = if (isSelected) {
        Brush.linearGradient(
            listOf(
                colors.primaryAccent.copy(alpha = if (isDark) 0.28f else 0.22f),
                colors.secondaryAccent.copy(alpha = if (isDark) 0.16f else 0.12f),
                if (isDark) Color(0xFF0F172A).copy(alpha = 0.55f) else Color.White.copy(alpha = 0.88f)
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                if (isDark) Color.White.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.92f),
                if (isDark) Color.White.copy(alpha = 0.04f) else Color.White.copy(alpha = 0.78f),
                if (isDark) Color(0xFF0A1224).copy(alpha = 0.50f) else Color.White.copy(alpha = 0.84f)
            )
        )
    }

    val borderBrush = if (isSelected) {
        Brush.linearGradient(listOf(colors.primaryAccent, colors.secondaryAccent))
    } else {
        Brush.linearGradient(
            listOf(
                if (isDark) GlassSpecularEdge.copy(alpha = 0.35f) else Color.White,
                if (isDark) Color.White.copy(alpha = 0.12f) else Color(0x30000000),
                if (isDark) Color.White.copy(alpha = 0.04f) else Color(0x10000000)
            )
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .clip(RoundedCornerShape(18.dp))
            .background(bgBrush)
            .drawBehind {
                // Top frosted highlight hairline for liquid glass reflection
                drawLine(
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            if (isSelected) colors.primaryAccent.copy(alpha = 0.6f)
                            else (if (isDark) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.85f)),
                            Color.Transparent
                        )
                    ),
                    start = Offset(size.width * 0.05f, 1f),
                    end = Offset(size.width * 0.95f, 1f),
                    strokeWidth = 1f
                )
            }
            .border(BorderStroke(1.dp, borderBrush), RoundedCornerShape(18.dp))
            .combinedClickable(
                interactionSource = interactionSource,
                indication = ripple(color = colors.primaryAccent.copy(alpha = 0.25f)),
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Selection Checkbox with smooth spring scale pop
        AnimatedVisibility(
            visible = isSelectionMode,
            enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)) + fadeIn(),
            exit = scaleOut(spring()) + fadeOut()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) colors.primaryAccent else Color.Transparent)
                        .border(1.5.dp, if (isSelected) colors.primaryAccent else colors.textMuted, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = if (isDark) Color(0xFF041E2B) else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
            }
        }

        // File/Folder Avatar or Image Thumbnail with Downsampling
        FileIconBadge(item = item, size = 40)

        Spacer(modifier = Modifier.width(12.dp))

        // Name and Subtitle
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                color = colors.textPrimary,
                fontWeight = FontWeight.Medium,
                fontSize = 13.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (item.isDirectory) "${item.itemCount} items • ${FileManagerEngine.formatDate(item.lastModified)}"
                       else "${FileManagerEngine.formatSize(item.size)} • ${FileManagerEngine.formatDate(item.lastModified)}",
                color = colors.textSecondary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Action menu button
        if (!isSelectionMode) {
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier
                        .background(colors.panelBackground)
                        .border(1.dp, colors.borderLight, RoundedCornerShape(14.dp))
                ) {
                    if (!item.isDirectory) {
                        DropdownMenuItem(
                            text = { Text("Open With", color = colors.textPrimary) },
                            leadingIcon = { Icon(Icons.Default.OpenInNew, contentDescription = null, tint = colors.primaryAccent) },
                            onClick = { menuExpanded = false; onOpenWith() }
                        )
                        DropdownMenuItem(
                            text = { Text("Share", color = colors.textPrimary) },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = GlassAzure) },
                            onClick = { menuExpanded = false; onShare() }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Compress", color = colors.textPrimary) },
                        leadingIcon = { Icon(Icons.Default.Archive, contentDescription = null, tint = GlassIndigo) },
                        onClick = { menuExpanded = false; onCompress() }
                    )
                    DropdownMenuItem(
                        text = { Text("Rename", color = colors.textPrimary) },
                        leadingIcon = { Icon(Icons.Default.MoreVert, contentDescription = null, tint = GlassAmber) },
                        onClick = { menuExpanded = false; onRename() }
                    )
                    DropdownMenuItem(
                        text = { Text("Details", color = colors.textPrimary) },
                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = colors.primaryAccent) },
                        onClick = { menuExpanded = false; onInfo() }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = GlassRed) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = GlassRed) },
                        onClick = { menuExpanded = false; onDelete() }
                    )
                }
            }
        }
    }
}

@Composable
fun FileGridItem(
    item: FileItem,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LiquidGlassTheme.colors
    val isDark = colors.isDark

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "grid_press"
    )

    val bgBrush = if (isSelected) {
        Brush.linearGradient(
            listOf(
                colors.primaryAccent.copy(alpha = if (isDark) 0.28f else 0.22f),
                colors.secondaryAccent.copy(alpha = if (isDark) 0.16f else 0.12f),
                if (isDark) Color(0xFF0F172A).copy(alpha = 0.50f) else Color.White.copy(alpha = 0.88f)
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                if (isDark) Color.White.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.92f),
                if (isDark) Color.White.copy(alpha = 0.04f) else Color.White.copy(alpha = 0.78f),
                if (isDark) Color(0xFF0A1224).copy(alpha = 0.45f) else Color.White.copy(alpha = 0.84f)
            )
        )
    }

    val borderBrush = if (isSelected) {
        Brush.linearGradient(listOf(colors.primaryAccent, colors.secondaryAccent))
    } else {
        Brush.linearGradient(
            listOf(
                if (isDark) GlassSpecularEdge.copy(alpha = 0.35f) else Color.White,
                if (isDark) Color.White.copy(alpha = 0.10f) else Color(0x30000000),
                if (isDark) Color.White.copy(alpha = 0.04f) else Color(0x10000000)
            )
        )
    }

    Column(
        modifier = modifier
            .padding(4.dp)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .clip(RoundedCornerShape(18.dp))
            .background(bgBrush)
            .border(BorderStroke(1.dp, borderBrush), RoundedCornerShape(18.dp))
            .combinedClickable(
                interactionSource = interactionSource,
                indication = ripple(color = colors.primaryAccent.copy(alpha = 0.25f)),
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            FileIconBadge(item = item, size = 46)

            androidx.compose.animation.AnimatedVisibility(
                visible = isSelectionMode && isSelected,
                enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)) + fadeIn(),
                exit = scaleOut(spring()) + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(colors.primaryAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFF041E2B) else Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = item.name,
            color = colors.textPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = if (item.isDirectory) "${item.itemCount} items" else FileManagerEngine.formatSize(item.size),
            color = colors.textMuted,
            fontSize = 10.5.sp
        )
    }
}

/**
 * High-performance file icon badge with Coil downsampled caching for photos.
 */
@Composable
private fun FileIconBadge(item: FileItem, size: Int = 40) {
    val context = LocalContext.current
    val colors = LiquidGlassTheme.colors
    val isDark = colors.isDark

    val (icon, color) = when (item.category) {
        FileCategory.FOLDER -> Pair(Icons.Default.Folder, GlassAzure)
        FileCategory.IMAGE -> Pair(Icons.Default.Image, GlassPink)
        FileCategory.VIDEO -> Pair(Icons.Default.Movie, GlassPurple)
        FileCategory.AUDIO -> Pair(Icons.Default.Audiotrack, GlassAmber)
        FileCategory.DOCUMENT -> Pair(Icons.Default.Description, GlassAzure)
        FileCategory.ARCHIVE -> Pair(Icons.Default.Archive, GlassIndigo)
        FileCategory.CODE -> Pair(Icons.Default.Code, GlassGreen)
        FileCategory.TEXT -> Pair(Icons.AutoMirrored.Filled.InsertDriveFile, GlassCyan)
        FileCategory.HTML -> Pair(Icons.Default.Html, GlassAzure)
        FileCategory.APK -> Pair(Icons.Default.Android, GlassGreen)
        FileCategory.OTHER -> Pair(Icons.AutoMirrored.Filled.InsertDriveFile, colors.textSecondary)
    }

    if (item.category == FileCategory.IMAGE && item.file.length() < 25 * 1024 * 1024) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(item.file)
                .size(140, 140) // Downsample to thumbnail size to preserve RAM and 60fps scrolling
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .crossfade(true)
                .build(),
            contentDescription = item.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(size.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(
                    BorderStroke(0.8.dp, if (isDark) Color.White.copy(alpha = 0.25f) else Color.White),
                    RoundedCornerShape(12.dp)
                )
        )
    } else {
        val badgeBg = if (isDark) {
            Brush.verticalGradient(
                listOf(
                    color.copy(alpha = 0.25f),
                    color.copy(alpha = 0.08f),
                    Color(0xFF0F172A).copy(alpha = 0.40f)
                )
            )
        } else {
            Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.94f),
                    color.copy(alpha = 0.16f),
                    Color.White.copy(alpha = 0.82f)
                )
            )
        }

        Box(
            modifier = Modifier
                .size(size.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(badgeBg)
                .border(
                    BorderStroke(
                        1.dp,
                        Brush.linearGradient(
                            listOf(
                                color.copy(alpha = if (isDark) 0.60f else 0.80f),
                                color.copy(alpha = if (isDark) 0.25f else 0.40f),
                                if (isDark) Color.White.copy(alpha = 0.15f) else Color.White
                            )
                        )
                    ),
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size((size * 0.55f).dp)
            )
        }
    }
}

@Composable
fun FloatingClipboardBar(
    clipboard: ClipboardState,
    onPaste: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LiquidGlassTheme.colors
    val isDark = colors.isDark

    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        FrostedGlassPanel(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (clipboard.action == ClipboardAction.COPY) Icons.Default.ContentCopy else Icons.Default.ContentCut,
                        contentDescription = null,
                        tint = colors.primaryAccent,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "${clipboard.files.size} items ${if (clipboard.action == ClipboardAction.COPY) "copied" else "cut"}",
                        color = colors.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onPaste,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primaryAccent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.ContentPaste,
                            contentDescription = null,
                            tint = if (isDark) Color(0xFF041E2B) else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Paste Here",
                            color = if (isDark) Color(0xFF041E2B) else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp
                        )
                    }

                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Clear, contentDescription = "Cancel", tint = colors.textSecondary)
                    }
                }
            }
        }
    }
}

@Composable
fun SelectionActionBar(
    selectedCount: Int,
    totalCount: Int,
    onSelectAll: () -> Unit,
    onCopy: () -> Unit,
    onCut: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onCompress: () -> Unit,
    onCloseSelection: () -> Unit
) {
    val colors = LiquidGlassTheme.colors

    FrostedGlassPanel(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onCloseSelection) {
                    Icon(Icons.Default.Clear, contentDescription = "Close", tint = colors.textPrimary)
                }
                AnimatedContent(
                    targetState = selectedCount,
                    transitionSpec = {
                        (slideInVertically { -it / 2 } + fadeIn()).togetherWith(slideOutVertically { it / 2 } + fadeOut())
                    },
                    label = "selected_count_anim"
                ) { count ->
                    Text(
                        text = "$count selected",
                        color = colors.primaryAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                IconButton(onClick = onSelectAll) {
                    Icon(Icons.Default.SelectAll, contentDescription = "Select All", tint = colors.textPrimary)
                }
                IconButton(onClick = onCopy) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = GlassAzure)
                }
                IconButton(onClick = onCut) {
                    Icon(Icons.Default.ContentCut, contentDescription = "Cut", tint = GlassAmber)
                }
                IconButton(onClick = onShare) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = GlassGreen)
                }
                IconButton(onClick = onCompress) {
                    Icon(Icons.Default.Archive, contentDescription = "Compress", tint = GlassIndigo)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = GlassRed)
                }
            }
        }
    }
}
