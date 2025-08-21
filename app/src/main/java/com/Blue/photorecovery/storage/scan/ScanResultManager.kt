package com.Blue.photorecovery.storage.scan

import com.Blue.photorecovery.storage.images.FolderItem

object ScanResultManager {
    var scanResult: ScanResult? = null

    fun getFolders(): List<FolderItem> {
        return scanResult?.scannedFolders ?: emptyList()
    }
    fun removeFolder(folderPath: String) {
        scanResult = scanResult?.copy(
            scannedFolders = scanResult?.scannedFolders?.filter { it.path != folderPath }
                ?: emptyList(),
            totalImages = (scanResult?.totalImages ?: 0) - (getFolder(folderPath)?.imageCount ?: 0)
        )
    }

    fun getFolder(folderPath: String): FolderItem? {
        return scanResult?.scannedFolders?.firstOrNull { it.path == folderPath }
    }

    fun clearResults() {
        scanResult = null
    }
}