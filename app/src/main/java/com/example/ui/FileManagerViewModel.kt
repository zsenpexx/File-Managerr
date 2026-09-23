package com.example.ui

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engine.ArchiveEngine
import com.example.engine.FileManagerEngine
import com.example.model.ClipboardAction
import com.example.model.ClipboardState
import com.example.model.DiskChartType
import com.example.model.DiskDirectoryAnalysis
import com.example.model.DiskItemAnalysis
import com.example.model.FileCategory
import com.example.model.FileItem
import com.example.model.SortBy
import com.example.model.StorageStats
import com.example.model.ViewMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

sealed class ActiveScreen {
    object Home : ActiveScreen()
    object MainBrowser : ActiveScreen()
    data class TxtReader(val file: File) : ActiveScreen()
    data class HtmlReader(val file: File) : ActiveScreen()
    data class ArchivePreview(val file: File) : ActiveScreen()
    data class DiskAnalyzer(val directory: File) : ActiveScreen()
}

data class FileManagerUiState(
    val currentDir: File,
    val rootStorageDir: File,
    val items: List<FileItem> = emptyList(),
    val storageStats: StorageStats = StorageStats(),
    val selectedFiles: Set<File> = emptySet(),
    val isSelectionMode: Boolean = false,
    val clipboard: ClipboardState? = null,
    val viewMode: ViewMode = ViewMode.LIST,
    val sortBy: SortBy = SortBy.NAME,
    val sortAscending: Boolean = true,
    val showHidden: Boolean = false,
    val activeCategory: FileCategory? = null,
    val searchQuery: String = "",
    val searchResults: List<FileItem> = emptyList(),
    val isSearching: Boolean = false,
    val isLoading: Boolean = false,
    val activeScreen: ActiveScreen = ActiveScreen.Home,
    val statusMessage: String? = null,
    val isDarkTheme: Boolean = true,
    val diskAnalysis: DiskDirectoryAnalysis? = null,
    val isAnalyzingDisk: Boolean = false,
    val diskChartType: DiskChartType = DiskChartType.TREEMAP,
    val selectedDiskItem: DiskItemAnalysis? = null
)

class FileManagerViewModel(application: Application) : AndroidViewModel(application) {

    private val context get() = getApplication<Application>()
    private val defaultStorage = FileManagerEngine.getDefaultStorageDirectory(context)

    private val _uiState = MutableStateFlow(
        FileManagerUiState(
            currentDir = defaultStorage,
            rootStorageDir = defaultStorage
        )
    )
    val uiState: StateFlow<FileManagerUiState> = _uiState.asStateFlow()

    init {
        loadCurrentDirectory()
        refreshStorageStats()
        ensureSampleFilesIfNeeded()
    }

    /**
     * Creates friendly sample files (including a sample .txt, .html, and sample archive)
     * if the directory is empty, giving immediate rich interactivity!
     */
    private fun ensureSampleFilesIfNeeded() {
        viewModelScope.launch {
            try {
                val appDocs = File(defaultStorage, "Documents")
                if (!appDocs.exists()) appDocs.mkdirs()

                val sampleTxt = File(appDocs, "welcome_guide.txt")
                if (!sampleTxt.exists()) {
                    sampleTxt.writeText(
                        """
                        ============================================
                         Welcome to File Managerr! 
                         High-Performance Liquid Glass File Explorer
                        ============================================
                        
                        Features at your fingertips:
                        1. Inbuilt .txt Reader & Editor:
                           - Monospace syntax display with line numbers
                           - Real-time word, line, and byte counters
                           - Instant search within text
                           - Font size scaling & word wrap
                           - In-place saving!
                        
                        2. Inbuilt .html Reader & Live Previewer:
                           - Live rendered WebView preview
                           - Raw HTML source code editor & line numbers
                        
                        3. Archive Powerhouse (.zip & .7z):
                           - Pure-engine extraction & compression
                           - Entry contents viewer without extraction
                        
                        4. Sleek File Management:
                           - Delete, Copy, Cut, Paste, Rename
                           - Open With and Share via FileProvider
                           - Multi-select batch operations
                           - Liquid glassmorphism UI & smooth spring animations
                        
                        Enjoy the smoothest file exploration on Android!
                        """.trimIndent()
                    )
                }

                val sampleHtml = File(appDocs, "liquid_glass_showcase.html")
                if (!sampleHtml.exists()) {
                    sampleHtml.writeText(
                        """
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <meta charset="utf-8">
                            <meta name="viewport" content="width=device-width, initial-scale=1">
                            <title>Liquid Glass Showcase</title>
                            <style>
                                body {
                                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
                                    background: linear-gradient(135deg, #070B14 0%, #0F172A 100%);
                                    color: #F8FAFC;
                                    margin: 0;
                                    padding: 24px;
                                }
                                .glass-card {
                                    background: rgba(255, 255, 255, 0.08);
                                    backdrop-filter: blur(16px);
                                    border: 1px solid rgba(0, 229, 255, 0.3);
                                    border-radius: 20px;
                                    padding: 24px;
                                    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5);
                                    margin-bottom: 20px;
                                }
                                h1 { color: #00E5FF; font-size: 24px; margin-top: 0; }
                                p { color: #94A3B8; font-size: 14px; line-height: 1.6; }
                                .badge {
                                    display: inline-block;
                                    background: rgba(0, 229, 255, 0.15);
                                    color: #00E5FF;
                                    border-radius: 12px;
                                    padding: 4px 12px;
                                    font-size: 12px;
                                    font-weight: bold;
                                }
                            </style>
                        </head>
                        <body>
                            <div class="glass-card">
                                <span class="badge">Interactive HTML Preview</span>
                                <h1>File Managerr Liquid Glass</h1>
                                <p>This HTML document is rendered in real-time inside the built-in HTML reader. Switch to the <b>Code</b> tab to edit its HTML/CSS on the fly!</p>
                            </div>
                        </body>
                        </html>
                        """.trimIndent()
                    )
                }

                // Sample Downloads Folder with realistic large files
                val downloadsDir = File(defaultStorage, "Downloads")
                if (!downloadsDir.exists()) downloadsDir.mkdirs()

                val largeVideo = File(downloadsDir, "4K_drone_nature_footage.mp4")
                if (!largeVideo.exists()) {
                    java.io.RandomAccessFile(largeVideo, "rw").use { it.setLength(24 * 1024 * 1024L) } // 24 MB
                }

                val backupArchive = File(downloadsDir, "system_backup_archive.zip")
                if (!backupArchive.exists()) {
                    java.io.RandomAccessFile(backupArchive, "rw").use { it.setLength(14 * 1024 * 1024L) } // 14 MB
                }

                val presentationPdf = File(downloadsDir, "quarterly_financial_report.pdf")
                if (!presentationPdf.exists()) {
                    java.io.RandomAccessFile(presentationPdf, "rw").use { it.setLength(4 * 1024 * 1024L) } // 4 MB
                }

                // Sample Media Folders
                val picturesDir = File(defaultStorage, "Pictures")
                if (!picturesDir.exists()) picturesDir.mkdirs()
                val photo1 = File(picturesDir, "mountain_sunrise_panorama.jpg")
                if (!photo1.exists()) {
                    java.io.RandomAccessFile(photo1, "rw").use { it.setLength(6 * 1024 * 1024L) } // 6 MB
                }
                val photo2 = File(picturesDir, "city_night_skyline.png")
                if (!photo2.exists()) {
                    java.io.RandomAccessFile(photo2, "rw").use { it.setLength(3 * 1024 * 1024L) } // 3 MB
                }

                val musicDir = File(defaultStorage, "Music")
                if (!musicDir.exists()) musicDir.mkdirs()
                val audioTrack = File(musicDir, "lofi_synthwave_session.mp3")
                if (!audioTrack.exists()) {
                    java.io.RandomAccessFile(audioTrack, "rw").use { it.setLength(8 * 1024 * 1024L) } // 8 MB
                }

                loadCurrentDirectory()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadCurrentDirectory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val state = _uiState.value

            val items = if (state.activeCategory != null) {
                FileManagerEngine.queryCategoryFiles(state.rootStorageDir, state.activeCategory)
            } else {
                FileManagerEngine.listDirectory(
                    directory = state.currentDir,
                    sortBy = state.sortBy,
                    ascending = state.sortAscending,
                    showHidden = state.showHidden
                )
            }

            _uiState.update { it.copy(items = items, isLoading = false) }
        }
    }

    fun refreshStorageStats() {
        viewModelScope.launch {
            val stats = FileManagerEngine.computeStorageStats(context)
            _uiState.update { it.copy(storageStats = stats) }
        }
    }

    fun navigateTo(dir: File) {
        if (!dir.exists() || !dir.isDirectory) return
        _uiState.update {
            it.copy(
                currentDir = dir,
                activeCategory = null,
                searchQuery = "",
                selectedFiles = emptySet(),
                isSelectionMode = false
            )
        }
        loadCurrentDirectory()
    }

    fun navigateUp(): Boolean {
        val state = _uiState.value
        if (state.activeScreen !is ActiveScreen.MainBrowser) {
            closeReader()
            return true
        }

        if (state.activeCategory != null) {
            _uiState.update { it.copy(activeCategory = null) }
            loadCurrentDirectory()
            return true
        }

        if (state.isSelectionMode) {
            clearSelection()
            return true
        }

        val parent = state.currentDir.parentFile
        if (parent != null && parent.exists() && state.currentDir.absolutePath != state.rootStorageDir.absolutePath) {
            navigateTo(parent)
            return true
        }
        return false
    }

    fun selectCategory(category: FileCategory?) {
        _uiState.update { it.copy(activeCategory = category, searchQuery = "") }
        loadCurrentDirectory()
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
        } else {
            viewModelScope.launch {
                _uiState.update { it.copy(isSearching = true) }
                val results = FileManagerEngine.searchFiles(_uiState.value.rootStorageDir, query)
                _uiState.update { it.copy(searchResults = results, isSearching = false) }
            }
        }
    }

    fun toggleViewMode() {
        _uiState.update {
            it.copy(viewMode = if (it.viewMode == ViewMode.LIST) ViewMode.GRID else ViewMode.LIST)
        }
    }

    fun setSort(sortBy: SortBy) {
        val newAsc = if (_uiState.value.sortBy == sortBy) !_uiState.value.sortAscending else true
        _uiState.update { it.copy(sortBy = sortBy, sortAscending = newAsc) }
        loadCurrentDirectory()
    }

    fun toggleShowHidden() {
        _uiState.update { it.copy(showHidden = !it.showHidden) }
        loadCurrentDirectory()
    }

    // Selection Handling
    fun toggleSelection(file: File) {
        val current = _uiState.value.selectedFiles.toMutableSet()
        if (current.contains(file)) {
            current.remove(file)
        } else {
            current.add(file)
        }
        _uiState.update {
            it.copy(
                selectedFiles = current,
                isSelectionMode = current.isNotEmpty()
            )
        }
    }

    fun startSelection(file: File) {
        _uiState.update {
            it.copy(
                selectedFiles = setOf(file),
                isSelectionMode = true
            )
        }
    }

    fun selectAll() {
        val all = _uiState.value.items.map { it.file }.toSet()
        _uiState.update {
            it.copy(
                selectedFiles = all,
                isSelectionMode = true
            )
        }
    }

    fun clearSelection() {
        _uiState.update {
            it.copy(
                selectedFiles = emptySet(),
                isSelectionMode = false
            )
        }
    }

    // Clipboard Handling
    fun copySelected() {
        val files = _uiState.value.selectedFiles.toList()
        if (files.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    clipboard = ClipboardState(ClipboardAction.COPY, files),
                    isSelectionMode = false,
                    selectedFiles = emptySet()
                )
            }
            Toast.makeText(context, "${files.size} items copied to clipboard", Toast.LENGTH_SHORT).show()
        }
    }

    fun cutSelected() {
        val files = _uiState.value.selectedFiles.toList()
        if (files.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    clipboard = ClipboardState(ClipboardAction.CUT, files),
                    isSelectionMode = false,
                    selectedFiles = emptySet()
                )
            }
            Toast.makeText(context, "${files.size} items cut to clipboard", Toast.LENGTH_SHORT).show()
        }
    }

    fun pasteClipboard() {
        val clipboard = _uiState.value.clipboard ?: return
        val target = _uiState.value.currentDir
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val count = if (clipboard.action == ClipboardAction.COPY) {
                FileManagerEngine.copyFiles(clipboard.files, target)
            } else {
                FileManagerEngine.moveFiles(clipboard.files, target)
            }
            _uiState.update { it.copy(clipboard = null, isLoading = false) }
            Toast.makeText(context, "Pasted $count items into ${target.name}", Toast.LENGTH_SHORT).show()
            loadCurrentDirectory()
            refreshStorageStats()
        }
    }

    fun cancelClipboard() {
        _uiState.update { it.copy(clipboard = null) }
    }

    // File Operations
    fun deleteSelected(files: List<File>? = null) {
        val targets = files ?: _uiState.value.selectedFiles.toList()
        if (targets.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val count = FileManagerEngine.deleteFiles(targets)
            clearSelection()
            _uiState.update { it.copy(isLoading = false) }
            Toast.makeText(context, "Deleted $count items", Toast.LENGTH_SHORT).show()
            loadCurrentDirectory()
            refreshStorageStats()
        }
    }

    fun rename(file: File, newName: String) {
        viewModelScope.launch {
            val result = FileManagerEngine.renameFile(file, newName)
            if (result.isSuccess) {
                Toast.makeText(context, "Renamed to $newName", Toast.LENGTH_SHORT).show()
                loadCurrentDirectory()
            } else {
                Toast.makeText(context, "Rename failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun createFolder(name: String) {
        viewModelScope.launch {
            val result = FileManagerEngine.createFolder(_uiState.value.currentDir, name)
            if (result.isSuccess) {
                Toast.makeText(context, "Created folder: $name", Toast.LENGTH_SHORT).show()
                loadCurrentDirectory()
                refreshStorageStats()
            } else {
                Toast.makeText(context, "Failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun createFile(name: String, content: String = "") {
        viewModelScope.launch {
            val result = FileManagerEngine.createFile(_uiState.value.currentDir, name, content)
            if (result.isSuccess) {
                Toast.makeText(context, "Created file: $name", Toast.LENGTH_SHORT).show()
                loadCurrentDirectory()
                refreshStorageStats()
            } else {
                Toast.makeText(context, "Failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun compressFiles(files: List<File>, outputName: String, format: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val outputArchive = File(_uiState.value.currentDir, outputName)
            val result = ArchiveEngine.compressFiles(files, outputArchive)
            _uiState.update { it.copy(isLoading = false) }
            if (result.isSuccess) {
                Toast.makeText(context, "Archive created: $outputName", Toast.LENGTH_SHORT).show()
                clearSelection()
                loadCurrentDirectory()
                refreshStorageStats()
            } else {
                Toast.makeText(context, "Compression failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // File opening & Readers
    fun openItem(item: FileItem) {
        if (item.isDirectory) {
            navigateTo(item.file)
            return
        }

        when (item.category) {
            FileCategory.TEXT, FileCategory.CODE -> {
                _uiState.update { it.copy(activeScreen = ActiveScreen.TxtReader(item.file)) }
            }
            FileCategory.HTML -> {
                _uiState.update { it.copy(activeScreen = ActiveScreen.HtmlReader(item.file)) }
            }
            FileCategory.ARCHIVE -> {
                val ext = item.extension
                if (ext == "zip" || ext == "7z") {
                    _uiState.update { it.copy(activeScreen = ActiveScreen.ArchivePreview(item.file)) }
                } else {
                    FileManagerEngine.openWith(context, item.file)
                }
            }
            else -> {
                FileManagerEngine.openWith(context, item.file)
            }
        }
    }

    fun closeReader() {
        _uiState.update { it.copy(activeScreen = ActiveScreen.MainBrowser) }
        loadCurrentDirectory()
    }

    // Theme toggle
    fun toggleTheme() {
        _uiState.update { it.copy(isDarkTheme = !it.isDarkTheme) }
    }

    // Disk Space Analyzer
    fun openDiskAnalyzer(directory: File = _uiState.value.currentDir) {
        _uiState.update {
            it.copy(
                activeScreen = ActiveScreen.DiskAnalyzer(directory),
                selectedDiskItem = null
            )
        }
        analyzeDirectorySpace(directory)
    }

    fun openHome() {
        _uiState.update { it.copy(activeScreen = ActiveScreen.Home) }
    }

    fun openMainStorage() {
        val root = _uiState.value.rootStorageDir
        _uiState.update {
            it.copy(
                activeScreen = ActiveScreen.MainBrowser,
                activeCategory = null
            )
        }
        navigateTo(root)
    }

    fun openDownloads() {
        val extDownloads = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
        val target = if (extDownloads != null && extDownloads.exists()) {
            extDownloads
        } else {
            File(_uiState.value.rootStorageDir, "Downloads").apply { if (!exists()) mkdirs() }
        }
        _uiState.update {
            it.copy(
                activeScreen = ActiveScreen.MainBrowser,
                activeCategory = null
            )
        }
        navigateTo(target)
    }

    fun openCategoryFromHome(category: FileCategory) {
        _uiState.update {
            it.copy(
                activeScreen = ActiveScreen.MainBrowser
            )
        }
        selectCategory(category)
    }

    fun openNewFiles() {
        _uiState.update {
            it.copy(
                activeScreen = ActiveScreen.MainBrowser,
                activeCategory = null,
                sortBy = SortBy.DATE,
                sortAscending = false
            )
        }
        loadCurrentDirectory()
    }

    fun closeDiskAnalyzer() {
        _uiState.update { it.copy(activeScreen = ActiveScreen.Home) }
    }

    fun drillDownDiskAnalyzer(directory: File) {
        _uiState.update {
            it.copy(
                activeScreen = ActiveScreen.DiskAnalyzer(directory),
                selectedDiskItem = null
            )
        }
        analyzeDirectorySpace(directory)
    }

    fun setDiskChartType(type: DiskChartType) {
        _uiState.update { it.copy(diskChartType = type) }
    }

    fun selectDiskItem(item: DiskItemAnalysis?) {
        _uiState.update { it.copy(selectedDiskItem = item) }
    }

    fun refreshDiskAnalysis() {
        val currentScreen = _uiState.value.activeScreen
        if (currentScreen is ActiveScreen.DiskAnalyzer) {
            analyzeDirectorySpace(currentScreen.directory)
        } else {
            analyzeDirectorySpace(_uiState.value.currentDir)
        }
    }

    private fun analyzeDirectorySpace(directory: File) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzingDisk = true) }
            val analysis = FileManagerEngine.analyzeDirectory(directory)
            _uiState.update {
                it.copy(
                    diskAnalysis = analysis,
                    isAnalyzingDisk = false
                )
            }
        }
    }

    fun openFromAnalyzer(file: File) {
        if (file.isDirectory) {
            navigateTo(file)
            closeDiskAnalyzer()
        } else {
            openItem(FileItem(file))
        }
    }

    fun deleteFromAnalyzer(file: File) {
        viewModelScope.launch {
            val count = FileManagerEngine.deleteFiles(listOf(file))
            if (count > 0) {
                Toast.makeText(context, "Deleted ${file.name}", Toast.LENGTH_SHORT).show()
                refreshDiskAnalysis()
                loadCurrentDirectory()
                refreshStorageStats()
            }
        }
    }
}
