package com.Blue.photorecovery.adapter.images

import ImageItem
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Locale

// Rows the adapter will render
sealed class GalleryRow {
    data class Header(val day: LocalDate, val title: String) : GalleryRow()
    data class Photo(val item: ImageItem) : GalleryRow()
}

@RequiresApi(Build.VERSION_CODES.O)
fun buildRowsGroupedByDay(
    images: List<ImageItem>,
    zone: ZoneId = ZoneId.systemDefault(),
    formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM, yyyy", Locale.getDefault())
): List<GalleryRow> {
    if (images.isEmpty()) return emptyList()

    // newest first (fallback to 0 when null)
    val sorted = images.sortedByDescending { it.dateTakenMillis ?: 0L }

    val rows = ArrayList<GalleryRow>(sorted.size + 16)
    var currentDay: LocalDate? = null

    for (img in sorted) {
        val millis = img.dateTakenMillis ?: 0L
        val day = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()

        if (currentDay != day) {
            currentDay = day
            rows += GalleryRow.Header(day, day.format(formatter))
        }
        rows += GalleryRow.Photo(img)
    }
    return rows
}