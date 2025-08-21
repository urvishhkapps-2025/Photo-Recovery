import android.net.Uri

data class ImageItem(
    val id: Long,
    val uri: Uri,
    val name: String?,
    val sizeBytes: Long,
    val dateTakenMillis: Long?,
    val bucketId: Long?,
    val bucketName: String?,
    val relativePath: String?,
    val isTrashed: Boolean,
    val volume: String
)