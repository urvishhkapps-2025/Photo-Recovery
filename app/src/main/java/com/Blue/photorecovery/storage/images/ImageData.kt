import android.net.Uri

data class ImageItem(
    val id: Long,
    val uri: Uri,
    val name: String?,
    val sizeBytes: Long,
    val dateTakenMillis: Long?,
    val bucketId: Long?,          // null for FS scan
    val bucketName: String?,
    val relativePath: String?,    // path relative to volume root
    val isTrashed: Boolean,       // false for FS scan
    val volume: String            // e.g., "fs:primary" or "fs:XXXX-XXXX"
)

data class ImageFolder(
    val key: String,
    val displayName: String,
    val volume: String,
    val bucketId: Long?,          // null for FS scan
    val relativePath: String?,
    var count: Int = 0,
    var totalBytes: Long = 0,
    var coverUri: Uri? = null,
    var samplePath: String? = null
)

data class ScanResult(
    val images: List<ImageItem>,
    val folders: List<ImageFolder>,
    val totalCount: Int,
    val totalBytes: Long,
    val scanDurationMs: Long
)