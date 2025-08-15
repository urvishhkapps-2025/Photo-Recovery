package com.Blue.photorecovery.storage.video

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.SystemClock
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

object GetAllVideosFolder {

    private val VIDEO_EXTS = setOf("mp4", "m4v", "3gp", "webm", "mkv", "mov", "avi", "ts", "flv")

    private fun isVideoName(name: String?): Boolean {
        val n = name?.lowercase(Locale.US) ?: return false
        val dot = n.lastIndexOf('.')
        if (dot <= 0 || dot == n.lastIndex) return false
        return n.substring(dot + 1) in VIDEO_EXTS
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getDurationMsSafely(context: Context, uri: Uri): Long? {
        return try {
            MediaMetadataRetriever().use { mmr ->
                mmr.setDataSource(context, uri)
                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()
            }
        } catch (_: Throwable) {
            null
        }
    }

    private fun discoverRoots(): List<File> {
        val out = mutableListOf<File>()
        val primary = Environment.getExternalStorageDirectory()
        if (primary.exists() && primary.canRead()) out += primary
        File("/storage").listFiles()?.forEach { f ->
            if (!f.isDirectory || !f.canRead()) return@forEach
            if (f.name.equals("self", true)) return@forEach
            if (f.name.equals("emulated", true)) {
                val zero = File(f, "0")
                if (zero.exists() && zero.canRead()) out += zero
            } else {
                if (f.name.matches(Regex("""[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}""")) || f.name.startsWith(
                        "sdcard",
                        true
                    )
                )
                    out += f
            }
        }
        return out.distinctBy { it.absolutePath }
    }

    private fun volumeLabel(root: File): String =
        if (root.absolutePath.contains("/emulated/0")) "fs:primary" else "fs:${root.name}"

    private fun relPath(root: File, file: File): String {
        val base = root.absolutePath.trimEnd('/')
        val full = file.parentFile?.absolutePath ?: base
        val rel = if (full.startsWith(base)) full.removePrefix(base).trimStart('/') else full
        return if (rel.isBlank()) "/" else "$rel/"
    }

    /**
     * All-in-one video scan.
     * @param safTrees user-picked roots (DocumentTree URIs) you want to include (e.g., WhatsApp/Media)
     * @param allowAllFiles include a file-system pass (needs MANAGE_EXTERNAL_STORAGE on API 30+)
     * @param includeTrash include Recycle Bin on API 30+ (MediaStore only)
     * @param fetchDuration if true, computes duration for FS/SAF using MediaMetadataRetriever (slower)
     */
    @SuppressLint("InlinedApi")
    suspend fun scanAllVideosAllInOne(
        context: Context,
        safTrees: List<Uri> = emptyList(),
        allowAllFiles: Boolean = false,
        includeTrash: Boolean = false,
        fetchDuration: Boolean = false,
        onProgress: (stage: String, done: Int, total: Int) -> Unit = { _, _, _ -> }
    ): VideoScanResult = withContext(Dispatchers.IO) {

        val t0 = SystemClock.elapsedRealtime()

        val videos = ArrayList<VideoItem>(2048)
        val foldersMap = LinkedHashMap<String, VideoFolder>()
        val seenKeys = HashSet<String>()
        var totalBytes = 0L

        fun addToFolder(
            key: String,
            display: String,
            volume: String,
            bucketId: Long?,
            rel: String?,
            cover: Uri?,
            size: Long
        ) {
            val f = foldersMap.getOrPut(key) {
                VideoFolder(
                    key,
                    display.ifBlank { "Unknown" },
                    volume,
                    bucketId,
                    rel,
                    0,
                    0,
                    null,
                    rel?.trimEnd('/')
                )
            }
            f.count++
            f.totalBytes += size
            if (f.coverUri == null && cover != null) f.coverUri = cover
        }

        fun addVideo(item: VideoItem, sourceKey: String, display: String) {
            val stable = when {
                item.uri.scheme == "content" -> "${item.volume}:${item.id}"
                else -> item.uri.toString()
            }
            if (!seenKeys.add(stable)) return
            videos += item
            totalBytes += item.sizeBytes

            val folderKey =
                "$sourceKey|${item.volume}|${item.bucketId ?: "R:${item.relativePath ?: display}"}"
            addToFolder(
                folderKey,
                display,
                item.volume,
                item.bucketId,
                item.relativePath,
                item.uri,
                item.sizeBytes
            )
        }

        fun displayName(bucket: String?, rel: String?, parentPath: String?): String {
            val name = when {
                !rel.isNullOrBlank() -> rel.trimEnd('/').substringAfterLast('/')
                !parentPath.isNullOrBlank() -> File(parentPath).name
                !bucket.isNullOrBlank() -> bucket
                else -> "Unknown"
            }
            return if (name.equals(".thumbnails", true)) ".Thumbnails" else name
        }

        // -------- 1) MediaStore pass (fast, indexed) --------
        run {
            val volumes = MediaStore.getExternalVolumeNames(context)
            var processed = 0
            var grandTotal = 0
            // Estimate for progress
            volumes.forEach { vol ->
                context.contentResolver.query(
                    MediaStore.Video.Media.getContentUri(vol),
                    arrayOf(MediaStore.Video.Media._ID),
                    if (Build.VERSION.SDK_INT >= 30 && !includeTrash) "${MediaStore.MediaColumns.IS_TRASHED}=0" else null,
                    null, null
                )?.use { grandTotal += it.count }
            }

            for (vol in volumes) {
                val base = MediaStore.Video.Media.getContentUri(vol)
                val projection = buildList {
                    add(MediaStore.Video.Media._ID)
                    add(MediaStore.Video.Media.DISPLAY_NAME)
                    add(MediaStore.Video.Media.SIZE)
                    add(MediaStore.Video.Media.DURATION)
                    add(MediaStore.Video.Media.DATE_TAKEN)
                    add(MediaStore.Video.Media.DATE_ADDED)
                    add(MediaStore.Video.Media.DATE_MODIFIED)
                    add(MediaStore.Video.Media.BUCKET_ID)
                    add(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
                    if (Build.VERSION.SDK_INT >= 29) add(MediaStore.MediaColumns.RELATIVE_PATH)
                    if (Build.VERSION.SDK_INT < 29) add(MediaStore.MediaColumns.DATA)
                    if (Build.VERSION.SDK_INT >= 30) add(MediaStore.MediaColumns.IS_TRASHED)
                }.toTypedArray()

                val selection =
                    if (Build.VERSION.SDK_INT >= 30 && !includeTrash) "${MediaStore.MediaColumns.IS_TRASHED}=0" else null

                context.contentResolver.query(
                    base, projection, selection, null,
                    "${MediaStore.Video.Media.DATE_TAKEN} DESC"
                )?.use { c ->
                    val idX = c.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                    val nameX = c.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                    val sizeX = c.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                    val durX = c.getColumnIndex(MediaStore.Video.Media.DURATION)
                    val takenX = c.getColumnIndex(MediaStore.Video.Media.DATE_TAKEN)
                    val addX = c.getColumnIndex(MediaStore.Video.Media.DATE_ADDED)
                    val modX = c.getColumnIndex(MediaStore.Video.Media.DATE_MODIFIED)
                    val bidX = c.getColumnIndex(MediaStore.Video.Media.BUCKET_ID)
                    val bnmX = c.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
                    val relX =
                        if (Build.VERSION.SDK_INT >= 29) c.getColumnIndex(MediaStore.MediaColumns.RELATIVE_PATH) else -1
                    val dataX =
                        if (Build.VERSION.SDK_INT < 29) c.getColumnIndex(MediaStore.MediaColumns.DATA) else -1
                    val trX =
                        if (Build.VERSION.SDK_INT >= 30) c.getColumnIndex(MediaStore.MediaColumns.IS_TRASHED) else -1

                    while (c.moveToNext()) {
                        val id = c.getLong(idX)
                        val uri = ContentUris.withAppendedId(base, id)
                        val size = if (!c.isNull(sizeX)) c.getLong(sizeX) else 0L
                        val name = if (!c.isNull(nameX)) c.getString(nameX) else null
                        val dur = if (durX >= 0 && !c.isNull(durX)) c.getLong(durX) else null
                        val date = when {
                            takenX >= 0 && !c.isNull(takenX) && c.getLong(takenX) > 0 -> c.getLong(
                                takenX
                            )

                            addX >= 0 && !c.isNull(addX) -> c.getLong(addX) * 1000L
                            modX >= 0 && !c.isNull(modX) -> c.getLong(modX) * 1000L
                            else -> null
                        }
                        val bid = if (bidX >= 0 && !c.isNull(bidX)) c.getLong(bidX) else null
                        val bnm = if (bnmX >= 0 && !c.isNull(bnmX)) c.getString(bnmX) else null
                        val rel = if (relX >= 0 && !c.isNull(relX)) c.getString(relX) else null
                        val dataParent =
                            if (dataX >= 0 && !c.isNull(dataX)) File(c.getString(dataX)).parent else null
                        val trashed =
                            if (trX >= 0 && !c.isNull(trX)) (c.getInt(trX) == 1) else false
                        if (!includeTrash && trashed) {
                            processed++; continue
                        }

                        val disp = displayName(bnm, rel, dataParent)
                        addVideo(
                            VideoItem(id, uri, name, size, dur, date, bid, bnm, rel, trashed, vol),
                            sourceKey = "MS",
                            display = disp
                        )

                        processed++
                        if (processed % 150 == 0 || processed == grandTotal) onProgress(
                            "mediastore",
                            processed,
                            grandTotal
                        )
                    }
                }
            }
        }

        // -------- 2) SAF pass (user-picked trees; finds hidden folders) --------
        if (safTrees.isNotEmpty()) {
            var processed = 0
            fun walk(label: String, dir: DocumentFile, rel: String) {
                dir.listFiles().forEach { f ->
                    if (f.isDirectory) {
                        walk(label, f, if (rel.isBlank()) f.name ?: "" else "$rel/${f.name}")
                    } else if (f.isFile && (f.type?.startsWith("video/") == true || isVideoName(f.name))) {
                        val size = f.length()
                        val date = f.lastModified().takeIf { it > 0 }
                        val dur = if (fetchDuration) getDurationMsSafely(context, f.uri) else null
                        val display = rel.substringAfterLast('/').ifBlank { label }
                        addVideo(
                            VideoItem(
                                id = (f.uri.toString().hashCode().toLong()),
                                uri = f.uri,
                                name = f.name,
                                sizeBytes = size,
                                durationMs = dur,
                                dateTakenMillis = date,
                                bucketId = null,
                                bucketName = display,
                                relativePath = if (rel.isBlank()) "$label/" else "$rel/",
                                isTrashed = false,
                                volume = "saf:$label"
                            ),
                            sourceKey = "SAF",
                            display = display
                        )
                        processed++
                        if (processed % 300 == 0) onProgress("saf", processed, processed)
                    }
                }
            }

            safTrees.forEach { uri ->
                val root = DocumentFile.fromTreeUri(context, uri) ?: return@forEach
                val label = root.name ?: "SAF"
                walk(label, root, root.name ?: label)
            }
        }

        // -------- 3) File-system pass (needs All files access on API 30+) --------
        if (allowAllFiles && (Build.VERSION.SDK_INT < 30 || Environment.isExternalStorageManager())) {
            val roots = discoverRoots()
            var processed = 0
            val visitedDirs = HashSet<String>()

            fun walkFs(root: File) {
                val stack = ArrayDeque<File>()
                stack += root
                while (stack.isNotEmpty()) {
                    val dir = stack.removeLast()
                    val canonical =
                        runCatching { dir.canonicalPath }.getOrNull() ?: dir.absolutePath
                    if (!visitedDirs.add(canonical)) continue

                    dir.listFiles()?.forEach { f ->
                        if (f.isDirectory) {
                            val name = f.name.lowercase(Locale.US)
                            if (name == "proc" || name == "acct" || name == "sys") return@forEach
                            stack += f
                        } else if (f.isFile && isVideoName(f.name)) {
                            val vol = volumeLabel(root)
                            val rel = relPath(root, f)
                            val parent = f.parentFile?.name ?: "Root"
                            val uri = Uri.fromFile(f)
                            val size = runCatching { f.length() }.getOrNull() ?: 0L
                            val date =
                                runCatching { f.lastModified() }.getOrNull()?.takeIf { it > 0 }
                            val dur = if (fetchDuration) getDurationMsSafely(context, uri) else null

                            addVideo(
                                VideoItem(
                                    id = (f.absolutePath.hashCode()
                                        .toLong() shl 32) or size.hashCode().toLong(),
                                    uri = uri,
                                    name = f.name,
                                    sizeBytes = size,
                                    durationMs = dur,
                                    dateTakenMillis = date,
                                    bucketId = null,
                                    bucketName = parent,
                                    relativePath = rel,
                                    isTrashed = false,
                                    volume = vol
                                ),
                                sourceKey = "FS",
                                display = parent
                            )

                            processed++
                            if (processed % 600 == 0) onProgress("filesystem", processed, processed)
                        }
                    }
                }
            }
            roots.forEach { r -> if (r.exists() && r.canRead()) walkFs(r) }
        }

        val folders = foldersMap.values
            .sortedWith(compareByDescending<VideoFolder> { it.count }.thenBy { it.displayName })

        VideoScanResult(
            videos = videos,
            folders = folders,
            totalCount = videos.size,
            totalBytes = totalBytes,
            scanDurationMs = SystemClock.elapsedRealtime() - t0
        )
    }

}