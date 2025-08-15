package com.Blue.photorecovery.storage.images

import ImageFolder
import ImageItem
import ScanResult
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.SystemClock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import kotlin.math.absoluteValue

object GetAllImagesFolder{

     val IMG_EXTS = setOf(
        "jpg", "jpeg", "png", "gif", "webp", "heic", "heif", "bmp", "tif", "tiff", "dng", "raw"
    )

     fun isImageFile(name: String): Boolean {
        val n = name.lowercase(Locale.US)
        val dot = n.lastIndexOf('.')
        if (dot <= 0 || dot == n.lastIndex) return false
        val ext = n.substring(dot + 1)
        return ext in IMG_EXTS
    }

     fun discoverRoots(): List<File> {
        val roots = mutableListOf<File>()
        val primary = Environment.getExternalStorageDirectory()
        if (primary.exists() && primary.canRead()) roots += primary
        File("/storage").listFiles()?.forEach { f ->
            if (!f.isDirectory || !f.canRead()) return@forEach
            if (f.name.equals("self", true)) return@forEach
            if (f.name.equals("emulated", true)) {
                val zero = File(f, "0")
                if (zero.exists() && zero.canRead()) roots += zero
            } else {
                // typical SD card mount: 1234-5678
                if (f.name.matches(Regex("""[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}""")) || f.name.startsWith(
                        "sdcard",
                        true
                    )
                )
                    roots += f
            }
        }
        return roots.distinctBy { it.absolutePath }
    }

    private fun volumeLabel(root: File): String =
        if (root.absolutePath.contains("/emulated/0")) "fs:primary"
        else "fs:${root.name}"

    private fun relPath(root: File, file: File): String {
        val base = root.absolutePath.trimEnd('/')
        val full = file.parentFile?.absolutePath ?: base
        val rel = if (full.startsWith(base)) full.removePrefix(base).trimStart('/') else full
        return if (rel.isBlank()) "/" else "$rel/"
    }

    /** Deep file-system scan. Requires All files access on API 30+. */
    suspend fun scanAllImagesFromFileSystem(context: Context): ScanResult =
        withContext(Dispatchers.IO) {
            if (Build.VERSION.SDK_INT >= 30 && !Environment.isExternalStorageManager())
                return@withContext ScanResult(emptyList(), emptyList(), 0, 0, 0)

            val t0 = SystemClock.elapsedRealtime()

            val images = ArrayList<ImageItem>(4096)
            val foldersMap = LinkedHashMap<String, ImageFolder>()
            val visitedDirs = HashSet<String>()
            val roots = discoverRoots()

            fun addToFolder(
                key: String,
                display: String,
                vol: String,
                rel: String,
                cover: Uri?,
                size: Long
            ) {
                val f = foldersMap.getOrPut(key) {
                    ImageFolder(key, display.ifBlank { "Unknown" }, vol, null, rel, 0, 0, null, rel)
                }
                f.count++
                f.totalBytes += size
                if (f.coverUri == null && cover != null) f.coverUri = cover
            }

            fun walk(root: File) {
                val stack = ArrayDeque<File>()
                stack += root

                while (stack.isNotEmpty()) {
                    val dir = stack.removeLast()

                    val canonical =
                        runCatching { dir.canonicalPath }.getOrNull() ?: dir.absolutePath
                    if (!visitedDirs.add(canonical)) continue

                    val children = runCatching { dir.listFiles() }.getOrNull() ?: continue
                    for (f in children) {
                        if (f.isDirectory) {
                            // skip known OS dirs
                            val name = f.name.lowercase(Locale.US)
                            if (name == "proc" || name == "acct" || name == "sys") continue
                            stack += f
                        } else if (f.isFile && isImageFile(f.name)) {
                            val vol = volumeLabel(root)
                            val rel = relPath(root, f)
                            val parentName = f.parentFile?.name ?: "Root"
                            val uri = Uri.fromFile(f)
                            val size = runCatching { f.length() }.getOrNull() ?: 0L
                            val date =
                                runCatching { f.lastModified() }.getOrNull()?.takeIf { it > 0 }

                            val id = (f.absolutePath.hashCode().toLong() shl 32) or size.hashCode()
                                .toLong().absoluteValue
                            images += ImageItem(
                                id = id,
                                uri = uri,
                                name = f.name,
                                sizeBytes = size,
                                dateTakenMillis = date,
                                bucketId = null,
                                bucketName = parentName,
                                relativePath = rel,
                                isTrashed = false,
                                volume = vol
                            )

                            val folderKey = "$vol|$rel"
                            addToFolder(folderKey, parentName, vol, rel, uri, size)
                        }
                    }
                }
            }

            roots.forEach { root ->
                if (root.exists() && root.canRead()) walk(root)
            }

            val folders = foldersMap.values
                .sortedWith(compareByDescending<ImageFolder> { it.count }.thenBy { it.displayName })

            ScanResult(
                images = images,
                folders = folders,
                totalCount = images.size,
                totalBytes = images.sumOf { it.sizeBytes },
                scanDurationMs = SystemClock.elapsedRealtime() - t0
            )
        }

    fun humanBytes(b: Long): String {
        if (b < 1024) return "$b B"
        val kb = b / 1024.0
        if (kb < 1024) return String.format(Locale.US, "%.0f KB", kb)
        val mb = kb / 1024.0
        if (mb < 1024) return String.format(Locale.US, "%.1f MB", mb)
        val gb = mb / 1024.0
        return String.format(Locale.US, "%.2f GB", gb)
    }

}