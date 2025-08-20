package com.Blue.photorecovery.storage.scan

import ImageItem
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File

/** Use your own field if it exists; else resolve via Uri */
fun ImageItem.sizeBytesOrNull(ctx: Context): Long? {
    // If your ImageItem already has a size field, use it:
    // return this.sizeBytes  // ← uncomment if present

    val u = this.uri ?: return null
    return uriSizeBytes(ctx, u)
}

fun uriSizeBytes(ctx: Context, uri: Uri): Long? {
    return when (uri.scheme) {
        "file" -> File(uri.path ?: return null).takeIf { it.exists() }?.length()
        "content" -> queryContentLength(ctx, uri)
        else -> null
    }
}

private fun queryContentLength(ctx: Context, uri: Uri): Long? {
    var cursor: Cursor? = null
    return try {
        cursor = ctx.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)
        if (cursor != null && cursor.moveToFirst()) {
            val idx = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (idx >= 0 && !cursor.isNull(idx)) cursor.getLong(idx) else null
        } else null
    } catch (_: Exception) { null } finally { cursor?.close() }
}
