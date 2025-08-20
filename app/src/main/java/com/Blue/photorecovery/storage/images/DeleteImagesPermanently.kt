package com.Blue.photorecovery.storage.images

import ImageItem
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

fun deleteImagesPermanently(context: Context, images: List<ImageItem>, callback: (Boolean) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        var allDeleted = true
        images.forEach { image ->
            val deleted = when {
                image.uri.scheme == "file" -> {
                    val file = File(image.uri.path ?: "")
                    file.exists() && file.delete()
                }
                image.uri.scheme == "content" -> {
                    try {
                        context.contentResolver.delete(image.uri, null, null) > 0
                    } catch (e: Exception) {
                        false
                    }
                }
                else -> false
            }
            if (!deleted) allDeleted = false
        }
        
        withContext(Dispatchers.Main) {
            callback(allDeleted)
        }
    }
}
