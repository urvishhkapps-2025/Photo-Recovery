package com.Blue.photorecovery.storage.scan

import android.net.Uri
import com.Blue.photorecovery.storage.images.FolderItem

data class ScanResult(
    val totalImages: Int = 0,
    val totalSizeBytes: Long = 0,
    val totalFolders: Int = 0,
    val scannedFolders: List<FolderItem> = emptyList(),
    val firstFolderImages: List<Uri> = emptyList() // Add this
) {
    fun getFormattedSize(): String {
        val mb = totalSizeBytes / (1024.0 * 1024)
        return String.format("%.1f MB", mb)
    }
}