package com.Blue.photorecovery.storage.video

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore

object GetAllVideosFromFolder {

    data class VideoThumb(
        val uri: Uri,
        val name: String?,
        val sizeBytes: Long,
        val durationMs: Long?
    )

    /**
     * Load all videos that live in a given RELATIVE_PATH folder.
     *
     * @param relativePath e.g. "WhatsApp/Media/WhatsApp Video/" or "Movies/Camera/"
     * @param includeSubdirs true to include subfolders (LIKE 'path%'), false to match folder exactly.
     */
    @SuppressLint("InlinedApi")
    suspend fun loadVideosInFolderByRelativePath(
        context: Context,
        relativePath: String,
        includeSubdirs: Boolean = true
    ): List<VideoThumb> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {

        val out = mutableListOf<VideoThumb>()
        val volumes = MediaStore.getExternalVolumeNames(context)

        val (selection, args) = if (Build.VERSION.SDK_INT >= 29) {
            val like = if (includeSubdirs) "$relativePath%" else relativePath
            "${MediaStore.MediaColumns.RELATIVE_PATH} ${if (includeSubdirs) "LIKE" else "="} ?" to arrayOf(like)
        } else {
            // Fallback for API < 29 using the DATA path
            val like = if (includeSubdirs) "%/$relativePath%" else "%/$relativePath"
            "${MediaStore.MediaColumns.DATA} LIKE ?" to arrayOf(like)
        }

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DURATION
        )

        for (vol in volumes) {
            val base = MediaStore.Video.Media.getContentUri(vol)
            context.contentResolver.query(
                base, projection, selection, args,
                "${MediaStore.Video.Media.DATE_TAKEN} DESC"
            )?.use { c ->
                val idxId = c.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val idxName = c.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val idxSize = c.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val idxDur = c.getColumnIndex(MediaStore.Video.Media.DURATION)

                while (c.moveToNext()) {
                    val id = c.getLong(idxId)
                    val uri = ContentUris.withAppendedId(base, id)
                    out += VideoThumb(
                        uri = uri,
                        name = if (!c.isNull(idxName)) c.getString(idxName) else null,
                        sizeBytes = if (!c.isNull(idxSize)) c.getLong(idxSize) else 0L,
                        durationMs = if (idxDur >= 0 && !c.isNull(idxDur)) c.getLong(idxDur) else null
                    )
                }
            }
        }
        out
    }

}