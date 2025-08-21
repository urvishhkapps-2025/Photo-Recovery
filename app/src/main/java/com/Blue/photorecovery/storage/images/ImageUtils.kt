package com.Blue.photorecovery.storage.images

import java.io.File

object ImageUtils {

    //tmp

    val IMG_EXTS = setOf(
        "jpg", "jpeg", "png", "gif", "webp", "heic", "heif", "bmp", "tif", "tiff", "dng", "raw"
    )

    fun isImageFile(file: File): Boolean {
        val extension = file.extension.lowercase()
        return IMG_EXTS.contains(extension)
    }

    fun getImageFolders(root: File): List<File> {
        val folders = mutableSetOf<File>()

        fun scanDirectory(dir: File) {
            dir.listFiles()?.forEach { file ->
                if (file.isDirectory) {
                    // Check if directory contains images
                    val hasImages = file.listFiles()?.any { it.isFile && isImageFile(it) } == true
                    if (hasImages) {
                        folders.add(file)
                    }
                    scanDirectory(file) // Recursive scan
                }
            }
        }

        scanDirectory(root)
        return folders.toList()
    }

    fun humanBytes(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val exp = (Math.log(bytes.toDouble()) / Math.log(1024.0)).toInt()
        val pre = "KMGTPE"[exp - 1]
        return String.format("%.1f %sB", bytes / Math.pow(1024.0, exp.toDouble()), pre)
    }


}