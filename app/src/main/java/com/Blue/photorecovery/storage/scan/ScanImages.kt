package com.Blue.photorecovery.storage.scan

import com.Blue.photorecovery.storage.images.FolderSection

object ScanImages {
    @Volatile
    var sections: List<FolderSection> = emptyList()
}