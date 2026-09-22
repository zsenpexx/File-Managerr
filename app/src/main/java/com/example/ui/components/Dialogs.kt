package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.FileManagerEngine
import com.example.model.FileItem
import com.example.ui.theme.GlassAzure
import com.example.ui.theme.GlassCyan
import com.example.ui.theme.GlassIndigo
import com.example.ui.theme.GlassRed
import com.example.ui.theme.LiquidGlassTheme
import java.io.File

@Composable
fun CreateItemDialog(
    onDismiss: () -> Unit,
    onCreateFolder: (String) -> Unit,
    onCreateFile: (String) -> Unit
) {
    val colors = LiquidGlassTheme.colors
    val isDark = colors.isDark

    var isFolder by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.border(
            1.dp,
            Brush.linearGradient(
                if (isDark) listOf(
                    Color.White.copy(alpha = 0.35f),
                    colors.primaryAccent.copy(alpha = 0.20f),
                    Color.White.copy(alpha = 0.08f)
                ) else listOf(
                    Color.White,
                    colors.primaryAccent.copy(alpha = 0.25f),
                    Color(0x20000000)
                )
            ),
            RoundedCornerShape(24.dp)
        ),
        containerColor = if (isDark) Color(0xF40D1527) else Color(0xF8FFFFFF),
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = if (isFolder) "New Folder" else "New File",
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LiquidGlassPill(
                        text = "Folder",
                        isSelected = isFolder,
                        onClick = {
                            isFolder = true
                            if (name.isEmpty() || name == "new_file.txt" || name == "index.html") name = "New Folder"
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Folder, contentDescription = null, tint = if (isFolder) colors.primaryAccent else colors.textSecondary, modifier = Modifier.size(16.dp))
                        }
                    )
                    LiquidGlassPill(
                        text = "File (.txt/.html)",
                        isSelected = !isFolder,
                        onClick = {
                            isFolder = false
                            if (name.isEmpty() || name == "New Folder") name = "note.txt"
                        },
                        leadingIcon = {
                            Icon(Icons.Default.InsertDriveFile, contentDescription = null, tint = if (!isFolder) colors.primaryAccent else colors.textSecondary, modifier = Modifier.size(16.dp))
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text(if (isFolder) "Folder name" else "e.g. document.txt or page.html", color = colors.textMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                        focusedBorderColor = colors.primaryAccent,
                        unfocusedBorderColor = colors.borderLight,
                        focusedContainerColor = if (isDark) Color(0x220F172A) else Color(0x0A000000),
                        unfocusedContainerColor = if (isDark) Color(0x110F172A) else Color(0x05000000)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        if (isFolder) onCreateFolder(name.trim()) else onCreateFile(name.trim())
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.primaryAccent)
            ) {
                Text("Create", color = if (isDark) Color(0xFF041E2B) else Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colors.textSecondary)
            }
        }
    )
}

@Composable
fun RenameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val colors = LiquidGlassTheme.colors
    val isDark = colors.isDark

    var newName by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.border(
            1.dp,
            Brush.linearGradient(
                if (isDark) listOf(
                    Color.White.copy(alpha = 0.35f),
                    colors.secondaryAccent.copy(alpha = 0.20f),
                    Color.White.copy(alpha = 0.08f)
                ) else listOf(
                    Color.White,
                    colors.secondaryAccent.copy(alpha = 0.25f),
                    Color(0x20000000)
                )
            ),
            RoundedCornerShape(24.dp)
        ),
        containerColor = if (isDark) Color(0xF40D1527) else Color(0xF8FFFFFF),
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = colors.secondaryAccent, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Rename", color = colors.textPrimary, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    focusedBorderColor = colors.primaryAccent,
                    unfocusedBorderColor = colors.borderLight,
                    focusedContainerColor = if (isDark) Color(0x220F172A) else Color(0x0A000000),
                    unfocusedContainerColor = if (isDark) Color(0x110F172A) else Color(0x05000000)
                ),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newName.isNotBlank() && newName != currentName) {
                        onConfirm(newName.trim())
                    }
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.secondaryAccent)
            ) {
                Text("Rename", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colors.textSecondary)
            }
        }
    )
}

@Composable
fun DeleteConfirmDialog(
    count: Int,
    singleFileName: String?,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val colors = LiquidGlassTheme.colors
    val isDark = colors.isDark

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.border(
            1.dp,
            Brush.linearGradient(
                if (isDark) listOf(
                    Color.White.copy(alpha = 0.35f),
                    GlassRed.copy(alpha = 0.20f),
                    Color.White.copy(alpha = 0.08f)
                ) else listOf(
                    Color.White,
                    GlassRed.copy(alpha = 0.25f),
                    Color(0x20000000)
                )
            ),
            RoundedCornerShape(24.dp)
        ),
        containerColor = if (isDark) Color(0xF40D1527) else Color(0xF8FFFFFF),
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = GlassRed, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete Confirmation", color = colors.textPrimary, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Text(
                text = if (count == 1 && singleFileName != null) {
                    "Are you sure you want to permanently delete \"$singleFileName\"?"
                } else {
                    "Are you sure you want to permanently delete $count selected items?"
                },
                color = colors.textSecondary,
                fontSize = 14.sp
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = GlassRed)
            ) {
                Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colors.textSecondary)
            }
        }
    )
}

@Composable
fun CompressDialog(
    selectedFiles: List<File>,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    val colors = LiquidGlassTheme.colors
    val isDark = colors.isDark

    var format by remember { mutableStateOf("zip") }
    var archiveName by remember {
        mutableStateOf(
            if (selectedFiles.size == 1) selectedFiles[0].nameWithoutExtension else "archive"
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.border(
            1.dp,
            Brush.linearGradient(
                if (isDark) listOf(
                    Color.White.copy(alpha = 0.35f),
                    GlassIndigo.copy(alpha = 0.20f),
                    Color.White.copy(alpha = 0.08f)
                ) else listOf(
                    Color.White,
                    GlassIndigo.copy(alpha = 0.25f),
                    Color(0x20000000)
                )
            ),
            RoundedCornerShape(24.dp)
        ),
        containerColor = if (isDark) Color(0xF40D1527) else Color(0xF8FFFFFF),
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Archive, contentDescription = null, tint = GlassIndigo, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Compress Files", color = colors.textPrimary, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text(
                    text = "Select compression format:",
                    color = colors.textSecondary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LiquidGlassPill(
                        text = ".ZIP (Fast & Standard)",
                        isSelected = format == "zip",
                        onClick = { format = "zip" }
                    )
                    LiquidGlassPill(
                        text = ".7Z (High Ratio)",
                        isSelected = format == "7z",
                        onClick = { format = "7z" }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Archive Name:",
                    color = colors.textSecondary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = archiveName,
                    onValueChange = { archiveName = it },
                    suffix = { Text(".$format", color = colors.primaryAccent) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                        focusedBorderColor = colors.primaryAccent,
                        unfocusedBorderColor = colors.borderLight,
                        focusedContainerColor = if (isDark) Color(0x220F172A) else Color(0x0A000000),
                        unfocusedContainerColor = if (isDark) Color(0x110F172A) else Color(0x05000000)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Compressing ${selectedFiles.size} items",
                    color = colors.textMuted,
                    fontSize = 12.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (archiveName.isNotBlank()) {
                        val fullName = "$archiveName.$format"
                        onConfirm(fullName, format)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GlassIndigo)
            ) {
                Text("Compress", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colors.textSecondary)
            }
        }
    )
}

@Composable
fun FileInfoDialog(
    item: FileItem,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val colors = LiquidGlassTheme.colors
    val isDark = colors.isDark

    var md5Hash by remember { mutableStateOf("Calculating...") }

    LaunchedEffect(item) {
        if (!item.isDirectory) {
            md5Hash = FileManagerEngine.calculateMD5(item.file)
        } else {
            md5Hash = "N/A (Directory)"
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.border(
            1.dp,
            Brush.linearGradient(
                if (isDark) listOf(
                    Color.White.copy(alpha = 0.35f),
                    colors.primaryAccent.copy(alpha = 0.20f),
                    Color.White.copy(alpha = 0.08f)
                ) else listOf(
                    Color.White,
                    colors.primaryAccent.copy(alpha = 0.25f),
                    Color(0x20000000)
                )
            ),
            RoundedCornerShape(24.dp)
        ),
        containerColor = if (isDark) Color(0xF40D1527) else Color(0xF8FFFFFF),
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = colors.primaryAccent, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("File Details", color = colors.textPrimary, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoRow(label = "Name", value = item.name)
                InfoRow(label = "Path", value = item.path)
                InfoRow(
                    label = "Size",
                    value = if (item.isDirectory) "${item.itemCount} items" else "${FileManagerEngine.formatSize(item.size)} (${item.size} bytes)"
                )
                InfoRow(label = "Type", value = item.category.label + if (item.extension.isNotEmpty()) " (.${item.extension})" else "")
                InfoRow(label = "Last Modified", value = FileManagerEngine.formatDate(item.lastModified))
                InfoRow(
                    label = "Permissions",
                    value = buildString {
                        append(if (item.file.canRead()) "R " else "- ")
                        append(if (item.file.canWrite()) "W " else "- ")
                        append(if (item.file.canExecute()) "X" else "-")
                    }
                )

                if (!item.isDirectory) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("MD5 Hash:", color = colors.textSecondary, fontSize = 11.sp)
                            Text(md5Hash, color = colors.primaryAccent, fontSize = 11.sp, maxLines = 1)
                        }
                        IconButton(
                            onClick = {
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cm.setPrimaryClip(ClipData.newPlainText("MD5", md5Hash))
                                Toast.makeText(context, "Copied MD5 hash", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy MD5", tint = colors.secondaryAccent, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = colors.primaryAccent)
            ) {
                Text("Close", color = if (isDark) Color(0xFF041E2B) else Color.White, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    val colors = LiquidGlassTheme.colors
    Column {
        Text(text = label, color = colors.textMuted, fontSize = 11.sp)
        Text(text = value, color = colors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}
