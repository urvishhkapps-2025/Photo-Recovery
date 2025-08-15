package com.Blue.photorecovery.storage.video

import android.net.Uri

data class VideoItem(
    val id: Long,
    val uri: Uri,
    val name: String?,
    val sizeBytes: Long,
    val durationMs: Long?,        // null if not fetched
    val dateTakenMillis: Long?,
    val bucketId: Long?,          // null for FS/SAF
    val bucketName: String?,
    val relativePath: String?,    // e.g., "Movies/WhatsApp Video/"
    val isTrashed: Boolean,       // true only when includeTrash && API 30+ (MediaStore)
    val volume: String            // e.g., "external_primary", "XXXX-XXXX", "fs:primary", "saf:WhatsApp"
)

data class VideoFolder(
    val key: String,              // unique (source + volume + bucket/rel)
    val displayName: String,      // shown to user (e.g., "WhatsApp Video")
    val volume: String,
    val bucketId: Long?,          // null for FS/SAF
    val relativePath: String?,    // for API 29+ or FS/SAF path hint
    var count: Int = 0,
    var totalBytes: Long = 0,
    var coverUri: Uri? = null,
    var samplePath: String? = null
)

data class VideoScanResult(
    val videos: List<VideoItem>,
    val folders: List<VideoFolder>,
    val totalCount: Int,
    val totalBytes: Long,
    val scanDurationMs: Long
)
