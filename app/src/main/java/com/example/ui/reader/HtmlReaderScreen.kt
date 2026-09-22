package com.example.ui.reader

import android.annotation.SuppressLint
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassPill
import com.example.ui.components.LiquidGlassSurface
import com.example.ui.theme.GlassAzure
import com.example.ui.theme.GlassCyan
import com.example.ui.theme.GlassGreen
import com.example.ui.theme.LiquidGlassTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

enum class HtmlViewMode {
    RENDERED, SOURCE
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun HtmlReaderScreen(
    file: File,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val colors = LiquidGlassTheme.colors
    val isDark = colors.isDark

    var htmlContent by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var viewMode by remember { mutableStateOf(HtmlViewMode.RENDERED) }
    var isSaving by remember { mutableStateOf(false) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    LaunchedEffect(file) {
        withContext(Dispatchers.IO) {
            try {
                htmlContent = file.readText()
            } catch (e: Exception) {
                htmlContent = "<html><body><h1>Error loading HTML</h1><p>${e.message}</p></body></html>"
            } finally {
                isLoading = false
            }
        }
    }

    val lines = remember(htmlContent) { htmlContent.lines() }

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
                                text = file.name,
                                color = colors.textPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                maxLines = 1
                            )
                            Text(
                                text = "HTML Document • ${file.length() / 1024} KB",
                                color = colors.textSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Mode switch pills
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        LiquidGlassPill(
                            text = "Preview",
                            isSelected = viewMode == HtmlViewMode.RENDERED,
                            onClick = { viewMode = HtmlViewMode.RENDERED },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Language,
                                    contentDescription = null,
                                    tint = if (viewMode == HtmlViewMode.RENDERED) colors.primaryAccent else colors.textSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        )

                        LiquidGlassPill(
                            text = "Code",
                            isSelected = viewMode == HtmlViewMode.SOURCE,
                            onClick = { viewMode = HtmlViewMode.SOURCE },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Code,
                                    contentDescription = null,
                                    tint = if (viewMode == HtmlViewMode.SOURCE) colors.primaryAccent else colors.textSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        )

                        if (viewMode == HtmlViewMode.RENDERED) {
                            IconButton(onClick = {
                                webViewInstance?.reload()
                            }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Reload", tint = GlassAzure)
                            }
                        } else {
                            IconButton(onClick = {
                                if (!isSaving) {
                                    isSaving = true
                                    coroutineScope.launch {
                                        withContext(Dispatchers.IO) {
                                            try {
                                                file.writeText(htmlContent)
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }
                                        isSaving = false
                                        Toast.makeText(context, "Saved ${file.name}", Toast.LENGTH_SHORT).show()
                                        webViewInstance?.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                                    }
                                }
                            }) {
                                if (isSaving) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = GlassGreen, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Save, contentDescription = "Save", tint = GlassGreen)
                                }
                            }
                        }
                    }
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.primaryAccent)
                }
            } else {
                when (viewMode) {
                    HtmlViewMode.RENDERED -> {
                        LiquidGlassCard(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            AndroidView(
                                factory = { ctx ->
                                    WebView(ctx).apply {
                                        settings.javaScriptEnabled = true
                                        settings.domStorageEnabled = true
                                        settings.builtInZoomControls = true
                                        settings.displayZoomControls = false
                                        settings.cacheMode = WebSettings.LOAD_NO_CACHE
                                        webViewClient = WebViewClient()
                                        loadDataWithBaseURL("file://${file.parentFile?.absolutePath}/", htmlContent, "text/html", "UTF-8", null)
                                        webViewInstance = this
                                    }
                                },
                                update = { wv ->
                                    webViewInstance = wv
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    HtmlViewMode.SOURCE -> {
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
                                Column(
                                    modifier = Modifier
                                        .verticalScroll(verticalScrollState)
                                        .padding(end = 12.dp)
                                ) {
                                    for (i in 1..lines.size) {
                                        Text(
                                            text = "$i",
                                            color = colors.textMuted.copy(alpha = 0.7f),
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 13.sp,
                                            lineHeight = (13 * 1.4f).sp,
                                            fontWeight = FontWeight.Light
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(2000.dp)
                                        .background(if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f))
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .verticalScroll(verticalScrollState)
                                        .horizontalScroll(horizontalScrollState)
                                ) {
                                    BasicTextField(
                                        value = htmlContent,
                                        onValueChange = { htmlContent = it },
                                        textStyle = TextStyle(
                                            color = colors.textPrimary,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 13.sp,
                                            lineHeight = (13 * 1.4f).sp
                                        ),
                                        cursorBrush = SolidColor(colors.primaryAccent),
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
}
