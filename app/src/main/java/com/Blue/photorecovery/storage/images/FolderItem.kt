package com.Blue.photorecovery.storage.images

import android.net.Uri

// FolderItem.kt
data class FolderItem(
    val path: String,
    val name: String,
    val imageCount: Int = 0,
    val coverUris: List<Uri> = emptyList(),
    val lastModified: Long = 0
) {
    fun stableId(): String = path
}