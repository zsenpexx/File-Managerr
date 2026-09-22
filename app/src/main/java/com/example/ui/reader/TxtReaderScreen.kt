package com.example.ui.reader

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WrapText
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassSurface
import com.example.ui.theme.GlassAzure
import com.example.ui.theme.GlassBackgroundDeep
import com.example.ui.theme.GlassCyan
import com.example.ui.theme.GlassGreen
import com.example.ui.theme.LiquidGlassTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun TxtReaderScreen(
    file: File,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val colors = LiquidGlassTheme.colors
    val isDark = colors.isDark

    var textContent by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isEditing by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var fontSize by remember { mutableFloatStateOf(14f) }
    var wordWrap by remember { mutableStateOf(true) }

    // Load file content
    LaunchedEffect(file) {
        withContext(Dispatchers.IO) {
            try {
                textContent = if (file.length() > 5 * 1024 * 1024) {
                    file.bufferedReader().useLines { lines ->
                        lines.take(2000).joinToString("\n") + "\n\n--- [File truncated: showing first 2,000 lines] ---"
                    }
                } else {
                    file.readText()
                }
            } catch (e: Exception) {
                textContent = "Error reading file: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    val lines = remember(textContent) { textContent.lines() }
    val lineCount = lines.size
    val charCount = textContent.length
    val wordCount = remember(textContent) {
        if (textContent.isBlank()) 0 else textContent.split("\\s+".toRegex()).size
    }

    val searchMatchesCount by remember {
        derivedStateOf {
            if (searchQuery.isBlank()) 0
            else {
                val pattern = Regex(Regex.escape(searchQuery), RegexOption.IGNORE_CASE)
                pattern.findAll(textContent).count()
            }
        }
    }

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
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                                    text = file.name,
                                    color = colors.textPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    maxLines = 1
                                )
                                Text(
                                    text = "$lineCount lines • $wordCount words • ${file.length() / 1024} KB",
                                    color = colors.textSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Search toggle
                            IconButton(onClick = { showSearch = !showSearch }) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Search in file",
                                    tint = if (showSearch) colors.primaryAccent else colors.textSecondary
                                )
                            }

                            // Word Wrap toggle
                            IconButton(onClick = { wordWrap = !wordWrap }) {
                                Icon(
                                    Icons.Default.WrapText,
                                    contentDescription = "Word Wrap",
                                    tint = if (wordWrap) colors.primaryAccent else colors.textMuted
                                )
                            }

                            // Font size cycle
                            IconButton(onClick = {
                                fontSize = if (fontSize >= 20f) 12f else fontSize + 2f
                            }) {
                                Icon(
                                    Icons.Default.FormatSize,
                                    contentDescription = "Font size",
                                    tint = colors.textSecondary
                                )
                            }

                            // Edit / Save toggle
                            if (isEditing) {
                                IconButton(
                                    onClick = {
                                        if (!isSaving) {
                                            isSaving = true
                                            coroutineScope.launch {
                                                withContext(Dispatchers.IO) {
                                                    try {
                                                        file.writeText(textContent)
                                                    } catch (e: Exception) {
                                                        e.printStackTrace()
                                                    }
                                                }
                                                isSaving = false
                                                isEditing = false
                                                Toast.makeText(context, "Saved changes to ${file.name}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                ) {
                                    if (isSaving) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = GlassGreen,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.Save,
                                            contentDescription = "Save file",
                                            tint = GlassGreen
                                        )
                                    }
                                }
                            } else {
                                IconButton(onClick = { isEditing = true }) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Edit file",
                                        tint = colors.secondaryAccent
                                    )
                                }
                            }
                        }
                    }

                    // Search row animation
                    AnimatedVisibility(
                        visible = showSearch,
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
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Find in text...", color = colors.textMuted, fontSize = 13.sp) },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = colors.textPrimary,
                                    unfocusedTextColor = colors.textPrimary,
                                    focusedBorderColor = colors.primaryAccent,
                                    unfocusedBorderColor = colors.borderLight,
                                    focusedContainerColor = colors.panelBackground,
                                    unfocusedContainerColor = colors.panelBackground
                                ),
                                textStyle = TextStyle(fontSize = 13.sp)
                            )
                            if (searchQuery.isNotEmpty()) {
                                Text(
                                    text = "$searchMatchesCount matches",
                                    color = if (searchMatchesCount > 0) colors.primaryAccent else GlassAzure,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = colors.textSecondary)
                                }
                            }
                        }
                    }
                }
            }

            // Reader Body
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colors.primaryAccent)
                }
            } else {
                LiquidGlassCard(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    val verticalScrollState = rememberScrollState()
                    val horizontalScrollState = rememberScrollState()

                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        // Line numbers column
                        Column(
                            modifier = Modifier
                                .verticalScroll(verticalScrollState)
                                .padding(end = 12.dp)
                        ) {
                            for (i in 1..lineCount) {
                                Text(
                                    text = "$i",
                                    color = colors.textMuted.copy(alpha = 0.7f),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = fontSize.sp,
                                    lineHeight = (fontSize * 1.4f).sp,
                                    fontWeight = FontWeight.Light
                                )
                            }
                        }

                        // Divider line
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(2000.dp)
                                .background(if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f))
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        // Text Content Area
                        val wrapModifier = if (wordWrap) Modifier else Modifier.horizontalScroll(horizontalScrollState)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(verticalScrollState)
                                .then(wrapModifier)
                        ) {
                            if (isEditing) {
                                BasicTextField(
                                    value = textContent,
                                    onValueChange = { textContent = it },
                                    textStyle = TextStyle(
                                        color = colors.textPrimary,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = fontSize.sp,
                                        lineHeight = (fontSize * 1.4f).sp
                                    ),
                                    cursorBrush = SolidColor(colors.primaryAccent),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                Text(
                                    text = textContent,
                                    color = colors.textPrimary,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = fontSize.sp,
                                    lineHeight = (fontSize * 1.4f).sp,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
