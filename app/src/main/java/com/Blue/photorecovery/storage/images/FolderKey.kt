package com.Blue.photorecovery.storage.images

// stable key function — must match how you created ImageFolder keys
import ImageFolder
import ImageItem
import android.net.Uri

private fun normRel(path: String?): String? =
    path?.let { if (it.endsWith("/")) it else "$it/" }

private fun folderKey(
    volume: String,
    bucketId: Long?,
    relativePath: String?,
    fallback: String?
): String = "$volume|${bucketId ?: "R:${relativePath ?: fallback}"}"

/** Build FolderSection list where each folder has up to the FIRST 3 image URIs. */
fun buildSectionsTop3(
    folders: List<ImageFolder>,
    images: List<ImageItem>     // ideally sorted newest-first
): List<FolderSection> {

    // Map folders by a stable key
    val folderMap: Map<String, ImageFolder> =
        folders.associateBy {
            folderKey(
                it.volume,
                it.bucketId,
                normRel(it.relativePath),
                it.displayName
            )
        }

    // Collect up to 3 URIs per folder (in input order of `images`)
    val buckets = LinkedHashMap<ImageFolder, MutableList<Uri>>()

    var remainingToFill = folders.size // how many folders still haven't reached 3
    for (img in images) {
        val key = folderKey(img.volume, img.bucketId, normRel(img.relativePath), img.bucketName)
        val folder = folderMap[key] ?: continue

        val list = buckets.getOrPut(folder) { mutableListOf() }
        if (list.size < 3) {
            list += img.uri
            if (list.size == 3) {
                remainingToFill--
                if (remainingToFill <= 0) break // all folders got 3; stop early
            }
        }
    }

    // Return sections in the same order as `folders`
    return folders.map { f ->
        FolderSection(f, buckets[f]?.toList().orEmpty())
    }
}

