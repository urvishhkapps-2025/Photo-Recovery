package com.Blue.photorecovery.storage.video

import android.net.Uri

data class VideoItem(
    val id: Long,
    val uri: Uri,
    val name: String?,
    val sizeBytes: Long,
    val durationMs: Long?,        
    val dateTakenMillis: Long?,
    val bucketId: Long?,         
    val bucketName: String?,
    val relativePath: String?,    
    val isTrashed: Boolean,       
    val volume: String            
)

data class VideoFolder(
    val key: String,             
    val displayName: String,     
    val volume: String,
    val bucketId: Long?,         
    val relativePath: String?,    
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
