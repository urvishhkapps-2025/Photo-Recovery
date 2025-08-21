package com.Blue.photorecovery.storage.images

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

// EventBus.kt
object EventBus {
    private val _folderUpdateEvent = MutableSharedFlow<String>()
    val folderUpdateEvent = _folderUpdateEvent.asSharedFlow()

    suspend fun notifyFolderUpdated(folderPath: String) {
        _folderUpdateEvent.emit(folderPath)
    }
}