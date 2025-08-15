package com.Blue.photorecovery.storage.images

import ImageFolder
import ImageItem
import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.documentfile.provider.DocumentFile
import com.Blue.photorecovery.storage.images.GetAllImagesFolder.isImageFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object GetAllImagesFromFolder {

    @SuppressLint("InlinedApi")
    suspend fun loadImagesInFolder(
        context: Context,
        folder: ImageFolder,
        safTrees: List<Uri> = emptyList(),
        includeSubdirs: Boolean = true
    ): List<ImageItem> = withContext(Dispatchers.IO) {

        when {
            folder.volume.startsWith("fs:") -> {
                // FILE SYSTEM mode
                val root: File = if (folder.volume == "fs:primary") {
                    Environment.getExternalStorageDirectory()
                } else {
                    val label = folder.volume.removePrefix("fs:")
                    File("/storage/$label")
                }
                val base = File(root, (folder.relativePath ?: "").trimStart('/'))
                if (!base.exists() || !base.canRead()) return@withContext emptyList<ImageItem>()

                val out = mutableListOf<ImageItem>()
                fun walk(dir: File) {
                    dir.listFiles()?.forEach { f ->
                        if (f.isDirectory && includeSubdirs) {
                            walk(f)
                        } else if (f.isFile && isImageFile(f.name)) {
                            val size = runCatching { f.length() }.getOrNull() ?: 0L
                            val date =
                                runCatching { f.lastModified() }.getOrNull()?.takeIf { it > 0 }
                            val rel = f.parentFile?.absolutePath?.removePrefix(root.absolutePath)
                                ?.trimStart('/') ?: "/"
                            out += ImageItem(
                                id = (f.absolutePath.hashCode().toLong() shl 32) or size.hashCode()
                                    .toLong(),
                                uri = Uri.fromFile(f),
                                name = f.name,
                                sizeBytes = size,
                                dateTakenMillis = date,
                                bucketId = null,
                                bucketName = f.parentFile?.name,
                                relativePath = if (rel.isBlank()) "/" else "$rel/",
                                isTrashed = false,
                                volume = folder.volume
                            )
                        }
                    }
                }
                if (includeSubdirs) walk(base) else {
                    base.listFiles()?.forEach { f ->
                        if (f.isFile && isImageFile(f.name)) {
                            val size = f.length()
                            val date = f.lastModified().takeIf { it > 0 }
                            val rel =
                                base.absolutePath.removePrefix(root.absolutePath).trimStart('/')
                            out += ImageItem(
                                id = (f.absolutePath.hashCode().toLong() shl 32) or size.hashCode()
                                    .toLong(),
                                uri = Uri.fromFile(f),
                                name = f.name,
                                sizeBytes = size,
                                dateTakenMillis = date,
                                bucketId = null,
                                bucketName = base.name,
                                relativePath = if (rel.isBlank()) "/" else "$rel/",
                                isTrashed = false,
                                volume = folder.volume
                            )
                        }
                    }
                }
                out
            }

            folder.volume.startsWith("saf:") -> {
                // SAF mode (needs a matching tree root in safTrees)
                val label = folder.volume.removePrefix("saf:")
                val rootTree = safTrees
                    .mapNotNull { DocumentFile.fromTreeUri(context, it) }
                    .firstOrNull { it.name == label } ?: return@withContext emptyList<ImageItem>()

                // Navigate to folder.relativePath inside the tree
                val rel = (folder.relativePath ?: label).trimEnd('/')
                val parts = rel.split('/').filter { it.isNotBlank() }.toMutableList()
                // First element is often the root label itself
                if (parts.isNotEmpty() && parts.first() == (rootTree.name ?: "")) parts.removeAt(0)
                var dir: DocumentFile? = rootTree
                for (p in parts) {
                    dir = dir?.findFile(p)
                    if (dir == null || !dir.isDirectory) break
                }
                dir ?: return@withContext emptyList<ImageItem>()

                val out = mutableListOf<ImageItem>()
                fun walk(d: DocumentFile) {
                    d.listFiles().forEach { f ->
                        if (f.isDirectory && includeSubdirs) {
                            walk(f)
                        } else if (f.isFile && isImageFile(f.name ?: "")) {
                            val size = f.length()
                            val date = f.lastModified().takeIf { it > 0 }
                            out += ImageItem(
                                id = (f.uri.toString().hashCode().toLong()),
                                uri = f.uri,
                                name = f.name,
                                sizeBytes = size,
                                dateTakenMillis = date,
                                bucketId = null,
                                bucketName = d.name,
                                relativePath = (folder.relativePath ?: ""),
                                isTrashed = false,
                                volume = folder.volume
                            )
                        }
                    }
                }
                if (includeSubdirs) walk(dir) else {
                    dir.listFiles().forEach { f ->
                        if (f.isFile && isImageFile(f.name ?: "")) {
                            val size = f.length()
                            val date = f.lastModified().takeIf { it > 0 }
                            out += ImageItem(
                                id = (f.uri.toString().hashCode().toLong()),
                                uri = f.uri,
                                name = f.name,
                                sizeBytes = size,
                                dateTakenMillis = date,
                                bucketId = null,
                                bucketName = dir.name,
                                relativePath = (folder.relativePath ?: ""),
                                isTrashed = false,
                                volume = folder.volume
                            )
                        }
                    }
                }
                out
            }

            else -> {
                // MediaStore mode
                val cr = context.contentResolver
                val base = MediaStore.Images.Media.getContentUri(folder.volume)
                val projection = arrayOf(
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.SIZE,
                    MediaStore.Images.Media.DATE_TAKEN,
                    MediaStore.Images.Media.DATE_ADDED,
                    MediaStore.Images.Media.DATE_MODIFIED,
                    MediaStore.Images.Media.BUCKET_ID,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.MediaColumns.RELATIVE_PATH
                )

                val (sel, args) = when {
                    folder.bucketId != null ->
                        "${MediaStore.Images.Media.BUCKET_ID}=?" to arrayOf(folder.bucketId.toString())

                    Build.VERSION.SDK_INT >= 29 && !folder.relativePath.isNullOrBlank() -> {
                        val like =
                            if (includeSubdirs) "${folder.relativePath}%" else folder.relativePath!!
                        "${MediaStore.MediaColumns.RELATIVE_PATH} ${if (includeSubdirs) "LIKE" else "="} ?" to arrayOf(
                            like
                        )
                    }

                    else -> null to null
                }

                val out = mutableListOf<ImageItem>()
                cr.query(base, projection, sel, args, "${MediaStore.Images.Media.DATE_TAKEN} DESC")
                    ?.use { c ->
                        val idxId = c.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                        val idxName = c.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                        val idxSize = c.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
                        val idxTaken = c.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN)
                        val idxAdded = c.getColumnIndex(MediaStore.Images.Media.DATE_ADDED)
                        val idxMod = c.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED)
                        val idxBid = c.getColumnIndex(MediaStore.Images.Media.BUCKET_ID)
                        val idxBnm = c.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                        val idxRel = c.getColumnIndex(MediaStore.MediaColumns.RELATIVE_PATH)

                        while (c.moveToNext()) {
                            val id = c.getLong(idxId)
                            val uri = ContentUris.withAppendedId(base, id)
                            val size = c.getLong(idxSize)
                            val name = c.getString(idxName)
                            val date = when {
                                idxTaken >= 0 && !c.isNull(idxTaken) && c.getLong(idxTaken) > 0 -> c.getLong(
                                    idxTaken
                                )

                                !c.isNull(idxAdded) -> c.getLong(idxAdded) * 1000L
                                !c.isNull(idxMod) -> c.getLong(idxMod) * 1000L
                                else -> null
                            }
                            out += ImageItem(
                                id = id,
                                uri = uri,
                                name = name,
                                sizeBytes = size,
                                dateTakenMillis = date,
                                bucketId = if (!c.isNull(idxBid)) c.getLong(idxBid) else null,
                                bucketName = if (!c.isNull(idxBnm)) c.getString(idxBnm) else null,
                                relativePath = if (!c.isNull(idxRel)) c.getString(idxRel) else null,
                                isTrashed = false,
                                volume = folder.volume
                            )
                        }
                    }
                out
            }
        }
    }


}