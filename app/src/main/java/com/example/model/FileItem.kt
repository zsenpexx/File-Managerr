package com.example.model

import java.io.File

enum class FileCategory(val label: String) {
    FOLDER("Folder"),
    IMAGE("Image"),
    VIDEO("Video"),
    AUDIO("Audio"),
    DOCUMENT("Document"),
    ARCHIVE("Archive"),
    CODE("Code"),
    TEXT("Text"),
    HTML("HTML"),
    APK("APK"),
    OTHER("Other")
}

data class FileItem(
    val file: File,
    val name: String = file.name,
    val path: String = file.absolutePath,
    val isDirectory: Boolean = file.isDirectory,
    val size: Long = if (file.isFile) file.length() else 0L,
    val lastModified: Long = file.lastModified(),
    val extension: String = file.extension.lowercase(),
    val category: FileCategory = determineCategory(file),
    val itemCount: Int = 0 // if directory, number of children
) {
    companion object {
        fun determineCategory(file: File): FileCategory {
            if (file.isDirectory) return FileCategory.FOLDER
            val ext = file.extension.lowercase()
            return when (ext) {
                "txt", "log", "md", "cfg", "ini", "csv", "tsv" -> FileCategory.TEXT
                "html", "htm", "xhtml" -> FileCategory.HTML
                "zip", "7z", "rar", "tar", "gz", "bz2", "xz" -> FileCategory.ARCHIVE
                "jpg", "jpeg", "png", "webp", "gif", "bmp", "svg" -> FileCategory.IMAGE
                "mp4", "mkv", "avi", "mov", "webm", "flv", "3gp" -> FileCategory.VIDEO
                "mp3", "wav", "ogg", "m4a", "flac", "aac", "wma" -> FileCategory.AUDIO
                "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx" -> FileCategory.DOCUMENT
                "json", "xml", "kt", "java", "py", "js", "ts", "css", "c", "cpp", "h", "sh", "yaml", "yml" -> FileCategory.CODE
                "apk", "xapk", "apks" -> FileCategory.APK
                else -> FileCategory.OTHER
            }
        }
    }
}

enum class SortBy {
    NAME, DATE, SIZE, TYPE
}

data class StorageStats(
    val totalBytes: Long = 0L,
    val availableBytes: Long = 0L,
    val usedBytes: Long = 0L,
    val imageBytes: Long = 0L,
    val videoBytes: Long = 0L,
    val audioBytes: Long = 0L,
    val docBytes: Long = 0L,
    val archiveBytes: Long = 0L,
    val apkBytes: Long = 0L,
    val otherBytes: Long = 0L
) {
    val usedPercent: Float
        get() = if (totalBytes > 0) (usedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f) else 0f
}

enum class ClipboardAction {
    COPY, CUT
}

data class ClipboardState(
    val action: ClipboardAction,
    val files: List<File>
)

data class ArchiveEntryItem(
    val name: String,
    val isDirectory: Boolean,
    val size: Long,
    val compressedSize: Long = 0L,
    val lastModified: Long = 0L
)

enum class ViewMode {
    LIST, GRID
}
