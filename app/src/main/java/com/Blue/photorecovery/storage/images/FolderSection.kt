package com.Blue.photorecovery.storage.images

import ImageFolder
import android.net.Uri

data class FolderSection(
    val folder: ImageFolder,
    val thumbs: List<Uri> // up to 3
)