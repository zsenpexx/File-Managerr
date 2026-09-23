package com.example.engine

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.example.model.CategorySummary
import com.example.model.DeviceMediaStats
import com.example.model.FileCategory
import com.example.model.FileItem
import com.example.model.SortBy
import com.example.model.StorageStats
import com.example.model.DiskDirectoryAnalysis
import com.example.model.DiskItemAnalysis
import com.example.model.DiskCategoryStat
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.GlassCyan
import com.example.ui.theme.GlassAzure
import com.example.ui.theme.GlassIndigo
import com.example.ui.theme.GlassPurple
import com.example.ui.theme.GlassPink
import com.example.ui.theme.GlassGreen
import com.example.ui.theme.GlassAmber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileManagerEngine {

    /**
     * Resolves the primary root directory.
     * Uses Environment.getExternalStorageDirectory() or falls back to context.filesDir.
     */
    fun getDefaultStorageDirectory(context: Context): File {
        val ext = Environment.getExternalStorageDirectory()
        return if (ext != null && ext.exists() && ext.canRead()) {
            ext
        } else {
            context.getExternalFilesDir(null) ?: context.filesDir
        }
    }

    // Fast LRU cache for directory child counts to avoid redundant filesystem calls
    private val directoryCountCache = object : android.util.LruCache<String, Pair<Long, Int>>(500) {}

    // Storage stats cache with short TTL
    private var cachedStorageStats: StorageStats? = null
    private var lastStatsTime: Long = 0L

    fun getDirectoryChildCount(dir: File): Int {
        val lastMod = dir.lastModified()
        val cached = directoryCountCache.get(dir.absolutePath)
        if (cached != null && cached.first == lastMod) {
            return cached.second
        }
        val count = dir.list()?.size ?: 0
        directoryCountCache.put(dir.absolutePath, Pair(lastMod, count))
        return count
    }

    /**
     * Fast directory listing with metadata and sorting.
     */
    suspend fun listDirectory(
        directory: File,
        sortBy: SortBy = SortBy.NAME,
        ascending: Boolean = true,
        showHidden: Boolean = false
    ): List<FileItem> = withContext(Dispatchers.IO) {
        if (!directory.exists() || !directory.isDirectory) {
            return@withContext emptyList()
        }

        val rawFiles = directory.listFiles() ?: return@withContext emptyList()

        val items = ArrayList<FileItem>(rawFiles.size)
        for (file in rawFiles) {
            if (!showHidden && file.name.startsWith(".")) continue
            val itemCount = if (file.isDirectory) getDirectoryChildCount(file) else 0
            items.add(FileItem(file = file, itemCount = itemCount))
        }

        return@withContext sortFileItems(items, sortBy, ascending)
    }

    private fun sortFileItems(
        items: List<FileItem>,
        sortBy: SortBy,
        ascending: Boolean
    ): List<FileItem> {
        val dirs = ArrayList<FileItem>()
        val files = ArrayList<FileItem>()
        for (item in items) {
            if (item.isDirectory) dirs.add(item) else files.add(item)
        }

        val dirSorted = when (sortBy) {
            SortBy.NAME -> if (ascending) dirs.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
                           else dirs.sortedWith(compareByDescending(String.CASE_INSENSITIVE_ORDER) { it.name })
            SortBy.DATE -> if (ascending) dirs.sortedBy { it.lastModified } else dirs.sortedByDescending { it.lastModified }
            SortBy.SIZE -> if (ascending) dirs.sortedBy { it.itemCount } else dirs.sortedByDescending { it.itemCount }
            SortBy.TYPE -> if (ascending) dirs.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
                           else dirs.sortedWith(compareByDescending(String.CASE_INSENSITIVE_ORDER) { it.name })
        }

        val filesSorted = when (sortBy) {
            SortBy.NAME -> if (ascending) files.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
                           else files.sortedWith(compareByDescending(String.CASE_INSENSITIVE_ORDER) { it.name })
            SortBy.DATE -> if (ascending) files.sortedBy { it.lastModified } else files.sortedByDescending { it.lastModified }
            SortBy.SIZE -> if (ascending) files.sortedBy { it.size } else files.sortedByDescending { it.size }
            SortBy.TYPE -> if (ascending) files.sortedWith(compareBy<FileItem, String>(String.CASE_INSENSITIVE_ORDER) { it.extension }.thenBy(String.CASE_INSENSITIVE_ORDER) { it.name })
                           else files.sortedWith(compareByDescending<FileItem, String>(String.CASE_INSENSITIVE_ORDER) { it.extension }.thenByDescending(String.CASE_INSENSITIVE_ORDER) { it.name })
        }

        return dirSorted + filesSorted
    }

    /**
     * Fast recursive indexing for category views (Images, Videos, Audio, Documents, Archives, APKs).
     */
    suspend fun queryCategoryFiles(
        rootDir: File,
        category: FileCategory,
        maxLimit: Int = 300
    ): List<FileItem> = withContext(Dispatchers.IO) {
        val result = mutableListOf<FileItem>()

        fun scan(dir: File) {
            if (result.size >= maxLimit) return
            val children = dir.listFiles() ?: return
            for (file in children) {
                if (file.name.startsWith(".")) continue
                if (file.isDirectory) {
                    // Skip Android/data or excessive system trees for speed
                    if (file.name.equals("Android", ignoreCase = true)) continue
                    scan(file)
                } else {
                    val item = FileItem(file)
                    if (item.category == category) {
                        result.add(item)
                        if (result.size >= maxLimit) return
                    }
                }
            }
        }

        scan(rootDir)
        return@withContext result.sortedByDescending { it.lastModified }
    }

    /**
     * Instant search within directory tree.
     */
    suspend fun searchFiles(
        rootDir: File,
        query: String,
        maxLimit: Int = 200
    ): List<FileItem> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        val result = mutableListOf<FileItem>()
        val lowerQuery = query.lowercase().trim()

        fun scan(dir: File) {
            if (result.size >= maxLimit) return
            val children = dir.listFiles() ?: return
            for (file in children) {
                if (file.name.startsWith(".")) continue
                if (file.name.lowercase().contains(lowerQuery)) {
                    val itemCount = if (file.isDirectory) (file.list()?.size ?: 0) else 0
                    result.add(FileItem(file = file, itemCount = itemCount))
                    if (result.size >= maxLimit) return
                }
                if (file.isDirectory && !file.name.equals("Android", ignoreCase = true)) {
                    scan(file)
                }
            }
        }

        scan(rootDir)
        return@withContext result.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
    }

    /**
     * Computes storage device stats (used, total, available) with TTL cache.
     * Accurately inspects the real device storage file system partitions.
     */
    suspend fun computeStorageStats(context: Context, forceRefresh: Boolean = false): StorageStats = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        if (!forceRefresh && cachedStorageStats != null && (now - lastStatsTime) < 3000L) {
            return@withContext cachedStorageStats!!
        }

        try {
            val candidatePaths = listOfNotNull(
                Environment.getDataDirectory(),
                Environment.getExternalStorageDirectory(),
                context.getExternalFilesDir(null),
                context.filesDir
            )

            var totalBytes = 0L
            var availableBytes = 0L

            for (path in candidatePaths) {
                try {
                    if (path.exists()) {
                        val stat = StatFs(path.path)
                        val blockSize = stat.blockSizeLong
                        val total = stat.blockCountLong * blockSize
                        val avail = stat.availableBlocksLong * blockSize
                        if (total > 0L) {
                            totalBytes = total
                            availableBytes = avail
                            break
                        }
                    }
                } catch (_: Exception) {}
            }

            val usedBytes = (totalBytes - availableBytes).coerceAtLeast(0L)

            val stats = StorageStats(
                totalBytes = totalBytes,
                availableBytes = availableBytes,
                usedBytes = usedBytes
            )
            cachedStorageStats = stats
            lastStatsTime = now
            return@withContext stats
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext cachedStorageStats ?: StorageStats()
        }
    }

    /**
     * Real device queries for home category summaries (Downloads, Images, Audio, Videos, Documents, Apps, New files).
     * Uses MediaStore + Device Filesystem scans to report actual counts and sizes on the user's device!
     */
    suspend fun computeDeviceMediaStats(context: Context, rootDir: File): DeviceMediaStats = withContext(Dispatchers.IO) {
        // 1. Downloads
        val downloadsSummary = run {
            var count = 0
            var size = 0L
            val downloadDirs = listOfNotNull(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                File(rootDir, "Downloads"),
                File(context.filesDir, "Downloads")
            ).distinctBy { it.absolutePath }

            val seenPaths = mutableSetOf<String>()
            for (dir in downloadDirs) {
                if (dir.exists() && dir.isDirectory) {
                    dir.listFiles()?.forEach { file ->
                        if (!file.name.startsWith(".") && seenPaths.add(file.absolutePath)) {
                            count++
                            size += if (file.isDirectory) file.walkTopDown().filter { it.isFile }.sumOf { it.length() } else file.length()
                        }
                    }
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    val projection = arrayOf(MediaStore.Downloads._ID, MediaStore.Downloads.SIZE, MediaStore.Downloads.DATA)
                    context.contentResolver.query(MediaStore.Downloads.EXTERNAL_CONTENT_URI, projection, null, null, null)?.use { cursor ->
                        val sizeIdx = cursor.getColumnIndex(MediaStore.Downloads.SIZE)
                        val dataIdx = cursor.getColumnIndex(MediaStore.Downloads.DATA)
                        while (cursor.moveToNext()) {
                            val path = if (dataIdx >= 0) cursor.getString(dataIdx) else null
                            if (path == null || seenPaths.add(path)) {
                                count++
                                if (sizeIdx >= 0) {
                                    val s = cursor.getLong(sizeIdx)
                                    if (s > 0) size += s
                                }
                            }
                        }
                    }
                } catch (_: Exception) {}
            }
            CategorySummary(sizeBytes = size, count = count)
        }

        // 2. Images
        val imagesSummary = run {
            var count = 0
            var size = 0L
            try {
                val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.SIZE)
                context.contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, null)?.use { cursor ->
                    val sizeIdx = cursor.getColumnIndex(MediaStore.Images.Media.SIZE)
                    while (cursor.moveToNext()) {
                        count++
                        if (sizeIdx >= 0) {
                            val s = cursor.getLong(sizeIdx)
                            if (s > 0) size += s
                        }
                    }
                }
            } catch (_: Exception) {}

            if (count == 0) {
                val imgExtensions = setOf("jpg", "jpeg", "png", "webp", "gif", "bmp", "heic", "svg")
                val imgDirs = listOfNotNull(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                    File(rootDir, "Pictures"),
                    File(rootDir, "DCIM")
                ).distinctBy { it.absolutePath }
                for (dir in imgDirs) {
                    if (dir.exists()) {
                        dir.walkTopDown().maxDepth(4).filter { it.isFile && it.extension.lowercase() in imgExtensions }.forEach {
                            count++
                            size += it.length()
                        }
                    }
                }
            }
            CategorySummary(sizeBytes = size, count = count)
        }

        // 3. Audio
        val audioSummary = run {
            var count = 0
            var size = 0L
            try {
                val projection = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.SIZE)
                context.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null, null)?.use { cursor ->
                    val sizeIdx = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)
                    while (cursor.moveToNext()) {
                        count++
                        if (sizeIdx >= 0) {
                            val s = cursor.getLong(sizeIdx)
                            if (s > 0) size += s
                        }
                    }
                }
            } catch (_: Exception) {}

            if (count == 0) {
                val audioExtensions = setOf("mp3", "m4a", "wav", "aac", "flac", "ogg", "opus", "mid", "wma")
                val audioDirs = listOfNotNull(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS),
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES),
                    File(rootDir, "Music"),
                    File(rootDir, "Audio")
                ).distinctBy { it.absolutePath }
                for (dir in audioDirs) {
                    if (dir.exists()) {
                        dir.walkTopDown().maxDepth(4).filter { it.isFile && it.extension.lowercase() in audioExtensions }.forEach {
                            count++
                            size += it.length()
                        }
                    }
                }
            }
            CategorySummary(sizeBytes = size, count = count)
        }

        // 4. Videos
        val videosSummary = run {
            var count = 0
            var size = 0L
            try {
                val projection = arrayOf(MediaStore.Video.Media._ID, MediaStore.Video.Media.SIZE)
                context.contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null, null)?.use { cursor ->
                    val sizeIdx = cursor.getColumnIndex(MediaStore.Video.Media.SIZE)
                    while (cursor.moveToNext()) {
                        count++
                        if (sizeIdx >= 0) {
                            val s = cursor.getLong(sizeIdx)
                            if (s > 0) size += s
                        }
                    }
                }
            } catch (_: Exception) {}

            if (count == 0) {
                val videoExtensions = setOf("mp4", "mkv", "webm", "avi", "mov", "3gp", "flv", "wmv", "ts")
                val videoDirs = listOfNotNull(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                    File(rootDir, "Movies"),
                    File(rootDir, "Videos")
                ).distinctBy { it.absolutePath }
                for (dir in videoDirs) {
                    if (dir.exists()) {
                        dir.walkTopDown().maxDepth(4).filter { it.isFile && it.extension.lowercase() in videoExtensions }.forEach {
                            count++
                            size += it.length()
                        }
                    }
                }
            }
            CategorySummary(sizeBytes = size, count = count)
        }

        // 5. Documents
        val docsSummary = run {
            var count = 0
            var size = 0L
            val docExtensions = setOf(
                "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
                "txt", "rtf", "csv", "epub", "md", "html", "htm", "json", "xml"
            )
            val docDirs = listOfNotNull(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                File(rootDir, "Documents"),
                File(rootDir, "Download"),
                File(rootDir, "Downloads")
            ).distinctBy { it.absolutePath }

            val seenPaths = mutableSetOf<String>()
            for (dir in docDirs) {
                if (dir.exists()) {
                    dir.walkTopDown().maxDepth(4).filter { it.isFile && it.extension.lowercase() in docExtensions }.forEach {
                        if (seenPaths.add(it.absolutePath)) {
                            count++
                            size += it.length()
                        }
                    }
                }
            }
            rootDir.listFiles()?.filter { it.isFile && it.extension.lowercase() in docExtensions }?.forEach {
                if (seenPaths.add(it.absolutePath)) {
                    count++
                    size += it.length()
                }
            }
            CategorySummary(sizeBytes = size, count = count)
        }

        // 6. Apps (Installed User Applications on device)
        val appsSummary = run {
            var count = 0
            var size = 0L
            try {
                val pm = context.packageManager
                val apps = pm.getInstalledApplications(0)
                for (app in apps) {
                    if ((app.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0 ||
                        (app.flags and android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                        count++
                        val file = File(app.sourceDir)
                        if (file.exists()) {
                            size += file.length()
                        }
                    }
                }
            } catch (_: Exception) {}

            val apkDirs = listOfNotNull(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                File(rootDir, "Downloads"),
                rootDir
            )
            for (dir in apkDirs) {
                if (dir.exists()) {
                    dir.walkTopDown().maxDepth(3).filter { it.isFile && it.extension.equals("apk", ignoreCase = true) }.forEach {
                        count++
                        size += it.length()
                    }
                }
            }
            CategorySummary(sizeBytes = size, count = count)
        }

        // 7. New Files (Created/Modified within past 7 days)
        val newFilesSummary = run {
            val sevenDaysAgoSec = (System.currentTimeMillis() - 7L * 86400 * 1000L) / 1000L
            var count = 0
            var size = 0L
            try {
                val uri = MediaStore.Files.getContentUri("external")
                val projection = arrayOf(MediaStore.MediaColumns._ID, MediaStore.MediaColumns.SIZE, MediaStore.MediaColumns.DATE_MODIFIED)
                val selection = "${MediaStore.MediaColumns.DATE_MODIFIED} >= ?"
                val selectionArgs = arrayOf(sevenDaysAgoSec.toString())
                context.contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
                    val sizeIdx = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE)
                    while (cursor.moveToNext()) {
                        count++
                        if (sizeIdx >= 0) {
                            val s = cursor.getLong(sizeIdx)
                            if (s > 0) size += s
                        }
                    }
                }
            } catch (_: Exception) {}

            if (count == 0) {
                val sevenDaysAgoMs = System.currentTimeMillis() - 7L * 86400 * 1000L
                rootDir.walkTopDown().maxDepth(4).filter { it.isFile && it.lastModified() >= sevenDaysAgoMs && !it.name.startsWith(".") }.forEach {
                    count++
                    size += it.length()
                }
            }
            CategorySummary(sizeBytes = size, count = count)
        }

        DeviceMediaStats(
            downloads = downloadsSummary,
            images = imagesSummary,
            audio = audioSummary,
            videos = videosSummary,
            documents = docsSummary,
            apps = appsSummary,
            newFiles = newFilesSummary
        )
    }

    /**
     * Deletes files or folders recursively.
     */
    suspend fun deleteFiles(files: List<File>): Int = withContext(Dispatchers.IO) {
        var count = 0
        for (file in files) {
            if (file.isDirectory) {
                if (file.deleteRecursively()) count++
            } else {
                if (file.delete()) count++
            }
        }
        return@withContext count
    }

    /**
     * Copies files/directories to targetDir.
     */
    suspend fun copyFiles(sources: List<File>, targetDir: File): Int = withContext(Dispatchers.IO) {
        var count = 0
        targetDir.mkdirs()
        for (source in sources) {
            val target = resolveUniqueDestination(targetDir, source.name)
            if (source.isDirectory) {
                source.copyRecursively(target, overwrite = true)
                count++
            } else {
                source.copyTo(target, overwrite = true)
                count++
            }
        }
        return@withContext count
    }

    /**
     * Moves files/directories to targetDir.
     */
    suspend fun moveFiles(sources: List<File>, targetDir: File): Int = withContext(Dispatchers.IO) {
        var count = 0
        targetDir.mkdirs()
        for (source in sources) {
            val target = resolveUniqueDestination(targetDir, source.name)
            val success = source.renameTo(target)
            if (success) {
                count++
            } else {
                // Fallback copy + delete
                if (source.isDirectory) {
                    source.copyRecursively(target, overwrite = true)
                    source.deleteRecursively()
                    count++
                } else {
                    source.copyTo(target, overwrite = true)
                    source.delete()
                    count++
                }
            }
        }
        return@withContext count
    }

    private fun resolveUniqueDestination(dir: File, baseName: String): File {
        var candidate = File(dir, baseName)
        if (!candidate.exists()) return candidate

        val nameWithoutExt = candidate.nameWithoutExtension
        val ext = if (candidate.extension.isNotEmpty()) ".${candidate.extension}" else ""
        var index = 1
        while (candidate.exists()) {
            candidate = File(dir, "$nameWithoutExt ($index)$ext")
            index++
        }
        return candidate
    }

    suspend fun renameFile(file: File, newName: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            val target = File(file.parentFile, newName.trim())
            if (target.exists()) {
                return@withContext Result.failure(Exception("File with this name already exists"))
            }
            if (file.renameTo(target)) {
                Result.success(target)
            } else {
                Result.failure(Exception("Failed to rename file"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createFolder(parentDir: File, name: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            val target = File(parentDir, name.trim())
            if (target.exists()) {
                return@withContext Result.failure(Exception("Folder already exists"))
            }
            if (target.mkdirs()) {
                Result.success(target)
            } else {
                Result.failure(Exception("Failed to create folder"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createFile(parentDir: File, name: String, content: String = ""): Result<File> = withContext(Dispatchers.IO) {
        try {
            val target = File(parentDir, name.trim())
            if (target.exists()) {
                return@withContext Result.failure(Exception("File already exists"))
            }
            target.writeText(content)
            Result.success(target)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Calculates MD5 hash of a file for verification / info dialog.
     */
    suspend fun calculateMD5(file: File): String = withContext(Dispatchers.IO) {
        try {
            if (!file.isFile || !file.canRead()) return@withContext "N/A"
            val digest = MessageDigest.getInstance("MD5")
            FileInputStream(file).use { fis ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (fis.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
            }
            val md5Bytes = digest.digest()
            val sb = StringBuilder()
            for (b in md5Bytes) {
                sb.append(String.format("%02x", b))
            }
            sb.toString()
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    /**
     * "Open With" using system intent chooser and FileProvider.
     */
    fun openWith(context: Context, file: File) {
        try {
            val uri = getFileUri(context, file)
            val mimeType = getMimeType(file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Open with").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * "Share" one or multiple files.
     */
    fun shareFiles(context: Context, files: List<File>) {
        try {
            if (files.isEmpty()) return
            if (files.size == 1) {
                val file = files[0]
                val uri = getFileUri(context, file)
                val mimeType = getMimeType(file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_STREAM, uri)
                    type = mimeType
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(Intent.createChooser(intent, "Share ${file.name}").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            } else {
                val uris = ArrayList(files.map { getFileUri(context, it) })
                val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                    type = "*/*"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(Intent.createChooser(intent, "Share ${files.size} files").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getFileUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }

    fun getMimeType(file: File): String {
        val ext = file.extension.lowercase()
        val fromMap = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
        if (!fromMap.isNullOrEmpty()) return fromMap

        return when (ext) {
            "txt", "log", "cfg", "ini", "md" -> "text/plain"
            "html", "htm" -> "text/html"
            "zip" -> "application/zip"
            "7z" -> "application/x-7z-compressed"
            "rar" -> "application/x-rar-compressed"
            "tar" -> "application/x-tar"
            "gz" -> "application/gzip"
            "json" -> "application/json"
            "pdf" -> "application/pdf"
            "apk" -> "application/vnd.android.package-archive"
            else -> "*/*"
        }
    }

    fun formatSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt().coerceIn(0, units.size - 1)
        val value = bytes / Math.pow(1024.0, digitGroups.toDouble())
        return String.format(Locale.US, "%.1f %s", value, units[digitGroups])
    }

    fun formatDate(timestamp: Long): String {
        if (timestamp <= 0L) return "Unknown"
        val sdf = SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    // Fast cache for folder recursive sizes
    private val folderSizeCache = object : android.util.LruCache<String, Pair<Long, Pair<Long, Int>>>(300) {}

    fun computeFolderSizeAndCount(folder: File): Pair<Long, Int> {
        val lastMod = folder.lastModified()
        val cached = folderSizeCache.get(folder.absolutePath)
        if (cached != null && cached.first == lastMod) {
            return cached.second
        }

        var size = 0L
        var count = 0
        val stack = java.util.ArrayDeque<File>()
        stack.push(folder)
        var maxVisited = 0

        while (stack.isNotEmpty() && maxVisited < 4000) {
            val curr = stack.pop()
            val list = curr.listFiles() ?: continue
            for (f in list) {
                if (f.name.startsWith(".")) continue
                if (f.isDirectory) {
                    if (!f.name.equals("Android", ignoreCase = true)) {
                        stack.push(f)
                        maxVisited++
                    }
                } else {
                    size += f.length()
                    count++
                }
            }
        }

        val result = Pair(size, count)
        folderSizeCache.put(folder.absolutePath, Pair(lastMod, result))
        return result
    }

    suspend fun analyzeDirectory(directory: File): DiskDirectoryAnalysis = withContext(Dispatchers.IO) {
        val dirName = if (directory.name.isEmpty()) "Internal Storage" else directory.name
        if (!directory.exists() || !directory.isDirectory) {
            return@withContext DiskDirectoryAnalysis(
                directory = directory,
                directoryName = dirName,
                totalSize = 0L,
                formattedTotalSize = "0 B",
                totalItems = 0,
                totalFiles = 0,
                totalFolders = 0,
                items = emptyList(),
                categoryStats = emptyList(),
                largestItem = null
            )
        }

        val children = directory.listFiles() ?: emptyArray()
        var totalDirSize = 0L
        var totalFiles = 0
        var totalFolders = 0

        val palette = listOf(
            GlassCyan,
            GlassAzure,
            GlassIndigo,
            GlassPurple,
            GlassPink,
            GlassGreen,
            GlassAmber,
            Color(0xFF06B6D4),
            Color(0xFF3B82F6),
            Color(0xFF8B5CF6),
            Color(0xFFEC4899),
            Color(0xFFF97316),
            Color(0xFF10B981)
        )

        val rawList = mutableListOf<Triple<File, Long, Int>>()
        for (child in children) {
            if (child.name.startsWith(".")) continue
            if (child.isDirectory) {
                totalFolders++
                val (fSize, count) = computeFolderSizeAndCount(child)
                rawList.add(Triple(child, fSize, count))
                totalDirSize += fSize
            } else {
                totalFiles++
                val fSize = child.length()
                rawList.add(Triple(child, fSize, 0))
                totalDirSize += fSize
            }
        }

        val sorted = rawList.sortedByDescending { it.second }
        val categoryTotals = mutableMapOf<FileCategory, Long>()
        val categoryCounts = mutableMapOf<FileCategory, Int>()

        val items = sorted.mapIndexed { index, (file, size, childCount) ->
            val cat = FileItem.determineCategory(file)
            categoryTotals[cat] = (categoryTotals[cat] ?: 0L) + size
            categoryCounts[cat] = (categoryCounts[cat] ?: 0) + 1

            val percentage = if (totalDirSize > 0) {
                ((size.toDouble() / totalDirSize.toDouble()) * 100.0).toFloat().coerceIn(0f, 100f)
            } else 0f

            val itemColor = palette[index % palette.size]

            DiskItemAnalysis(
                file = file,
                name = file.name,
                isDirectory = file.isDirectory,
                size = size,
                formattedSize = formatSize(size),
                percentage = percentage,
                category = cat,
                childCount = childCount,
                color = itemColor
            )
        }

        val categoryColorMap = mapOf(
            FileCategory.FOLDER to GlassCyan,
            FileCategory.IMAGE to GlassPurple,
            FileCategory.VIDEO to GlassPink,
            FileCategory.AUDIO to GlassAmber,
            FileCategory.DOCUMENT to GlassAzure,
            FileCategory.ARCHIVE to GlassGreen,
            FileCategory.CODE to Color(0xFF38BDF8),
            FileCategory.TEXT to Color(0xFF818CF8),
            FileCategory.HTML to Color(0xFFF97316),
            FileCategory.APK to Color(0xFF10B981),
            FileCategory.OTHER to Color(0xFF94A3B8)
        )

        val categoryStats = categoryTotals.entries
            .sortedByDescending { it.value }
            .map { (cat, bytes) ->
                val pct = if (totalDirSize > 0) ((bytes.toDouble() / totalDirSize.toDouble()) * 100.0).toFloat() else 0f
                DiskCategoryStat(
                    category = cat,
                    totalBytes = bytes,
                    formattedBytes = formatSize(bytes),
                    percentage = pct,
                    count = categoryCounts[cat] ?: 0,
                    color = categoryColorMap[cat] ?: GlassCyan
                )
            }

        return@withContext DiskDirectoryAnalysis(
            directory = directory,
            directoryName = dirName,
            totalSize = totalDirSize,
            formattedTotalSize = formatSize(totalDirSize),
            totalItems = items.size,
            totalFiles = totalFiles,
            totalFolders = totalFolders,
            items = items,
            categoryStats = categoryStats,
            largestItem = items.firstOrNull()
        )
    }
}
