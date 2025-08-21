package com.Blue.photorecovery.storage.video

import java.io.File

object VideoUtils {
    val VIDEO_EXTS = setOf(
        "mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "mpeg", "mpg", "3gp", "m4v"
    )

    fun isVideoFile(file: File): Boolean {
        val extension = file.extension.lowercase()
        return VIDEO_EXTS.contains(extension)
    }

    fun getVideoFolders(root: File): List<File> {
        val folders = mutableSetOf<File>()

        fun scanDirectory(dir: File) {
            dir.listFiles()?.forEach { file ->
                if (file.isDirectory) {
                    val hasVideos = file.listFiles()?.any { it.isFile && isVideoFile(it) } == true
                    if (hasVideos) {
                        folders.add(file)
                    }
                    scanDirectory(file) // Recursive scan
                }
            }
        }

        scanDirectory(root)
        return folders.toList()
    }

}
