package com.example.model

import androidx.compose.ui.graphics.Color
import java.io.File

enum class DiskChartType(val label: String) {
    CIRCULAR("Circular Donut"),
    TREEMAP("Treemap Tiles")
}

data class DiskItemAnalysis(
    val file: File,
    val name: String,
    val isDirectory: Boolean,
    val size: Long,
    val formattedSize: String,
    val percentage: Float, // 0.0 to 100.0
    val category: FileCategory,
    val childCount: Int,
    val color: Color
)

data class DiskCategoryStat(
    val category: FileCategory,
    val totalBytes: Long,
    val formattedBytes: String,
    val percentage: Float,
    val count: Int,
    val color: Color
)

data class DiskDirectoryAnalysis(
    val directory: File,
    val directoryName: String,
    val totalSize: Long,
    val formattedTotalSize: String,
    val totalItems: Int,
    val totalFiles: Int,
    val totalFolders: Int,
    val items: List<DiskItemAnalysis>,
    val categoryStats: List<DiskCategoryStat>,
    val largestItem: DiskItemAnalysis?
)
