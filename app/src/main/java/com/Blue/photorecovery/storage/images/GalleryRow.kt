package com.Blue.photorecovery.storage.images

import ImageItem

sealed class GalleryRow {
    data class Header(val title: String, val date: Long) : GalleryRow()
    data class Photo(val image: ImageItem) : GalleryRow()
}