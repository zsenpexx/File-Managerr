package com.example.engine

import com.example.model.ArchiveEntryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object ArchiveEngine {

    /**
     * Inspects entries inside a .zip or .7z archive file without extracting.
     */
    suspend fun listArchiveEntries(archiveFile: File): List<ArchiveEntryItem> = withContext(Dispatchers.IO) {
        val ext = archiveFile.extension.lowercase()
        return@withContext when (ext) {
            "zip" -> listZipEntries(archiveFile)
            "7z" -> listSevenZEntries(archiveFile)
            else -> emptyList()
        }
    }

    private fun listZipEntries(zipFile: File): List<ArchiveEntryItem> {
        val list = mutableListOf<ArchiveEntryItem>()
        try {
            ZipFile(zipFile).use { zip ->
                val entries = zip.entries()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    list.add(
                        ArchiveEntryItem(
                            name = entry.name,
                            isDirectory = entry.isDirectory,
                            size = if (entry.size >= 0) entry.size else 0L,
                            compressedSize = if (entry.compressedSize >= 0) entry.compressedSize else 0L,
                            lastModified = entry.time
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun listSevenZEntries(sevenZFile: File): List<ArchiveEntryItem> {
        val list = mutableListOf<ArchiveEntryItem>()
        try {
            SevenZFile.builder().setFile(sevenZFile).get().use { sz ->
                var entry = sz.nextEntry
                while (entry != null) {
                    list.add(
                        ArchiveEntryItem(
                            name = entry.name,
                            isDirectory = entry.isDirectory,
                            size = entry.size,
                            compressedSize = 0L,
                            lastModified = entry.lastModifiedDate?.time ?: 0L
                        )
                    )
                    entry = sz.nextEntry
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    /**
     * Extracts a .zip or .7z archive to a target directory.
     * Invokes [onProgress] with (current, total, currentFileName).
     */
    suspend fun extractArchive(
        archiveFile: File,
        targetDir: File,
        onProgress: (Int, Int, String) -> Unit = { _, _, _ -> }
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }
            val ext = archiveFile.extension.lowercase()
            val extractedCount = when (ext) {
                "zip" -> extractZip(archiveFile, targetDir, onProgress)
                "7z" -> extractSevenZ(archiveFile, targetDir, onProgress)
                else -> throw IllegalArgumentException("Unsupported archive format: .$ext")
            }
            Result.success(extractedCount)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private fun extractZip(
        archiveFile: File,
        targetDir: File,
        onProgress: (Int, Int, String) -> Unit
    ): Int {
        var count = 0
        val entries = listZipEntries(archiveFile)
        val total = entries.size.coerceAtLeast(1)

        ZipInputStream(BufferedInputStream(FileInputStream(archiveFile))).use { zis ->
            var entry: ZipEntry? = zis.nextEntry
            while (entry != null) {
                val file = File(targetDir, entry.name)
                // Guard against Zip Slip vulnerability
                if (!file.canonicalPath.startsWith(targetDir.canonicalPath)) {
                    throw SecurityException("Zip entry is outside of the target dir: ${entry.name}")
                }

                if (entry.isDirectory) {
                    file.mkdirs()
                } else {
                    file.parentFile?.mkdirs()
                    BufferedOutputStream(FileOutputStream(file)).use { bos ->
                        val buffer = ByteArray(8192)
                        var len: Int
                        while (zis.read(buffer).also { len = it } > 0) {
                            bos.write(buffer, 0, len)
                        }
                    }
                    count++
                }
                zis.closeEntry()
                onProgress(count, total, entry.name)
                entry = zis.nextEntry
            }
        }
        return count
    }

    private fun extractSevenZ(
        archiveFile: File,
        targetDir: File,
        onProgress: (Int, Int, String) -> Unit
    ): Int {
        var count = 0
        val entries = listSevenZEntries(archiveFile)
        val total = entries.size.coerceAtLeast(1)

        SevenZFile.builder().setFile(archiveFile).get().use { sz ->
            var entry: SevenZArchiveEntry? = sz.nextEntry
            while (entry != null) {
                val file = File(targetDir, entry.name)
                // Guard against Path traversal
                if (!file.canonicalPath.startsWith(targetDir.canonicalPath)) {
                    throw SecurityException("7z entry is outside of target dir: ${entry.name}")
                }

                if (entry.isDirectory) {
                    file.mkdirs()
                } else {
                    file.parentFile?.mkdirs()
                    BufferedOutputStream(FileOutputStream(file)).use { bos ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        while (sz.read(buffer).also { bytesRead = it } > 0) {
                            bos.write(buffer, 0, bytesRead)
                        }
                    }
                    count++
                }
                onProgress(count, total, entry.name)
                entry = sz.nextEntry
            }
        }
        return count
    }

    /**
     * Compresses a list of files/folders to a destination .zip or .7z file.
     */
    suspend fun compressFiles(
        sources: List<File>,
        outputArchive: File,
        onProgress: (Int, Int, String) -> Unit = { _, _, _ -> }
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            outputArchive.parentFile?.mkdirs()
            val ext = outputArchive.extension.lowercase()
            when (ext) {
                "zip" -> compressToZip(sources, outputArchive, onProgress)
                "7z" -> compressToSevenZ(sources, outputArchive, onProgress)
                else -> throw IllegalArgumentException("Unsupported compression format: .$ext")
            }
            Result.success(outputArchive)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private fun compressToZip(
        sources: List<File>,
        outputFile: File,
        onProgress: (Int, Int, String) -> Unit
    ) {
        val allFiles = mutableListOf<Pair<File, String>>()
        for (source in sources) {
            collectFiles(source, source.parentFile?.absolutePath ?: "", allFiles)
        }
        val total = allFiles.size.coerceAtLeast(1)
        var current = 0

        ZipOutputStream(BufferedOutputStream(FileOutputStream(outputFile))).use { zos ->
            for ((file, relPath) in allFiles) {
                val entryPath = relPath.replace(File.separatorChar, '/').let {
                    if (file.isDirectory && !it.endsWith('/')) "$it/" else it
                }
                val zipEntry = ZipEntry(entryPath).apply {
                    time = file.lastModified()
                }
                zos.putNextEntry(zipEntry)
                if (file.isFile) {
                    BufferedInputStream(FileInputStream(file)).use { bis ->
                        val buffer = ByteArray(8192)
                        var len: Int
                        while (bis.read(buffer).also { len = it } > 0) {
                            zos.write(buffer, 0, len)
                        }
                    }
                }
                zos.closeEntry()
                current++
                onProgress(current, total, file.name)
            }
        }
    }

    private fun compressToSevenZ(
        sources: List<File>,
        outputFile: File,
        onProgress: (Int, Int, String) -> Unit
    ) {
        val allFiles = mutableListOf<Pair<File, String>>()
        for (source in sources) {
            collectFiles(source, source.parentFile?.absolutePath ?: "", allFiles)
        }
        val total = allFiles.size.coerceAtLeast(1)
        var current = 0

        SevenZOutputFile(outputFile).use { szOut ->
            for ((file, relPath) in allFiles) {
                val entryPath = relPath.replace(File.separatorChar, '/').let {
                    if (file.isDirectory && !it.endsWith('/')) "$it/" else it
                }
                val entry = szOut.createArchiveEntry(file, entryPath)
                szOut.putArchiveEntry(entry)
                if (file.isFile) {
                    BufferedInputStream(FileInputStream(file)).use { bis ->
                        val buffer = ByteArray(8192)
                        var len: Int
                        while (bis.read(buffer).also { len = it } > 0) {
                            szOut.write(buffer, 0, len)
                        }
                    }
                }
                szOut.closeArchiveEntry()
                current++
                onProgress(current, total, file.name)
            }
        }
    }

    private fun collectFiles(
        file: File,
        basePath: String,
        outList: MutableList<Pair<File, String>>
    ) {
        val relPath = if (basePath.isEmpty()) file.name else file.absolutePath.removePrefix(basePath).trimStart(File.separatorChar)
        outList.add(Pair(file, relPath))
        if (file.isDirectory) {
            val children = file.listFiles() ?: emptyArray()
            for (child in children) {
                collectFiles(child, basePath, outList)
            }
        }
    }
}
