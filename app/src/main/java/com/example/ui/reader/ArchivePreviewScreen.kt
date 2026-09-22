package com.example.ui.reader

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.ArchiveEngine
import com.example.engine.FileManagerEngine
import com.example.model.ArchiveEntryItem
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassSurface
import com.example.ui.components.LiquidProgressBar
import com.example.ui.theme.GlassAzure
import com.example.ui.theme.GlassCyan
import com.example.ui.theme.GlassGreen
import com.example.ui.theme.GlassIndigo
import com.example.ui.theme.GlassPurple
import com.example.ui.theme.LiquidGlassTheme
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun ArchivePreviewScreen(
    archiveFile: File,
    onBack: () -> Unit,
    onExtracted: (File) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val colors = LiquidGlassTheme.colors
    val isDark = colors.isDark

    var entries by remember { mutableStateOf<List<ArchiveEntryItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isExtracting by remember { mutableStateOf(false) }
    var extractProgress by remember { mutableFloatStateOf(0f) }
    var extractStatusText by remember { mutableStateOf("") }
    var extractFinished by remember { mutableStateOf(false) }

    LaunchedEffect(archiveFile) {
        isLoading = true
        entries = ArchiveEngine.listArchiveEntries(archiveFile)
        isLoading = false
    }

    val totalUncompressed = remember(entries) { entries.sumOf { it.size } }

    LiquidGlassBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Liquid Glass Top Bar
            LiquidGlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = colors.primaryAccent
                            )
                        }
                        Column(modifier = Modifier.padding(start = 4.dp)) {
                            Text(
                                text = archiveFile.name,
                                color = colors.textPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                maxLines = 1
                            )
                            Text(
                                text = "${archiveFile.extension.uppercase()} Archive • ${entries.size} entries • ${FileManagerEngine.formatSize(archiveFile.length())}",
                                color = colors.textSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Extract Button
                    Button(
                        onClick = {
                            if (!isExtracting) {
                                isExtracting = true
                                extractFinished = false
                                coroutineScope.launch {
                                    val destinationFolder = File(archiveFile.parentFile, archiveFile.nameWithoutExtension)
                                    val result = ArchiveEngine.extractArchive(
                                        archiveFile = archiveFile,
                                        targetDir = destinationFolder
                                    ) { current, total, name ->
                                        extractProgress = current.toFloat() / total.toFloat()
                                        extractStatusText = name
                                    }

                                    isExtracting = false
                                    if (result.isSuccess) {
                                        extractFinished = true
                                        Toast.makeText(context, "Extracted to ${destinationFolder.name}", Toast.LENGTH_SHORT).show()
                                        onExtracted(destinationFolder)
                                    } else {
                                        Toast.makeText(context, "Extraction failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.horizontalGradient(listOf(GlassCyan, GlassAzure)))
                    ) {
                        Icon(Icons.Default.Unarchive, contentDescription = null, tint = Color(0xFF041E2B), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Extract All", color = Color(0xFF041E2B), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            // Extraction progress overlay card
            if (isExtracting || extractFinished) {
                LiquidGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (extractFinished) "Extraction Complete!" else "Extracting...",
                                color = if (extractFinished) GlassGreen else GlassCyan,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            if (extractFinished) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GlassGreen, modifier = Modifier.size(18.dp))
                            } else {
                                Text(
                                    text = "${(extractProgress * 100).toInt()}%",
                                    color = colors.textPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LiquidProgressBar(progress = if (extractFinished) 1f else extractProgress)
                        if (extractStatusText.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = extractStatusText,
                                color = colors.textMuted,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Archive Content List
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.primaryAccent)
                }
            } else {
                LiquidGlassCard(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    if (entries.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Archive, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Archive is empty or encrypted", color = colors.textSecondary, fontSize = 14.sp)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Archive contents (${entries.size})", color = colors.textSecondary, fontSize = 12.sp)
                                    Text("Unpacked: ${FileManagerEngine.formatSize(totalUncompressed)}", color = colors.primaryAccent, fontSize = 12.sp)
                                }
                            }

                            items(entries) { entry ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (entry.isDirectory) GlassPurple.copy(alpha = 0.2f)
                                                else GlassAzure.copy(alpha = 0.2f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (entry.isDirectory) Icons.Default.Folder else Icons.Default.InsertDriveFile,
                                            contentDescription = null,
                                            tint = if (entry.isDirectory) GlassPurple else GlassAzure,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = entry.name,
                                            color = colors.textPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (entry.isDirectory) "Directory"
                                                   else "${FileManagerEngine.formatSize(entry.size)} (Compressed: ${FileManagerEngine.formatSize(entry.compressedSize)})",
                                            color = colors.textMuted,
                                            fontSize = 11.sp
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
