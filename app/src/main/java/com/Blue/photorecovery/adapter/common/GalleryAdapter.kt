package com.Blue.photorecovery.adapter.common

import ImageItem
import android.app.Activity
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.Blue.photorecovery.R
import com.Blue.photorecovery.storage.images.GalleryRow
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class GalleryAdapter(
    private val count: Int,
    private val activity: Activity,
    private val onPhotoClick: (ImageItem) -> Unit = {},
    private val onSelectionChanged: (Int) -> Unit = {}
) : ListAdapter<GalleryRow, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_PHOTO = 1

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<GalleryRow>() {
            override fun areItemsTheSame(oldItem: GalleryRow, newItem: GalleryRow): Boolean {
                return when {
                    oldItem is GalleryRow.Header && newItem is GalleryRow.Header -> oldItem.title == newItem.title
                    oldItem is GalleryRow.Photo && newItem is GalleryRow.Photo -> oldItem.image.id == newItem.image.id
                    else -> false
                }
            }

            override fun areContentsTheSame(oldItem: GalleryRow, newItem: GalleryRow): Boolean {
                return oldItem == newItem
            }
        }
    }

    private val selectedItems = mutableSetOf<Long>()
    private var isProgrammaticSelection = false

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tevText: TextView = view.findViewById(R.id.tevText)
        val cbSelectDay: CheckBox = view.findViewById(R.id.cbSelectDay)
    }

    class PhotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val imageView: ImageView = view.findViewById(R.id.imgNew)
        val vidFirst: ImageView = view.findViewById(R.id.vidFirst)
        val cbPhoto: CheckBox = view.findViewById(R.id.cbPhoto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_header_date, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_photo_square, parent, false)
            PhotoViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is GalleryRow.Header -> bindHeader(holder as HeaderViewHolder, item)
            is GalleryRow.Photo -> bindPhoto(holder as PhotoViewHolder, item.image)
        }
    }

    private fun bindHeader(holder: HeaderViewHolder, header: GalleryRow.Header) {
        holder.tvDate.setTextSize(TypedValue.COMPLEX_UNIT_PX, 40f)
        holder.tevText.setTextSize(TypedValue.COMPLEX_UNIT_PX, 40f)
        holder.tvDate.text = header.title
        holder.cbSelectDay.setOnCheckedChangeListener(null)

        // Check if all photos in this day are selected
        val dayPhotos = currentList.filterIsInstance<GalleryRow.Photo>()
            .filter {
                it.image.dateTakenMillis?.let { time ->
                    isSameDay(time, header.date)
                } == true
            }

        val allSelected = dayPhotos.isNotEmpty() && dayPhotos.all { selectedItems.contains(it.image.id) }
        holder.cbSelectDay.isChecked = allSelected

        holder.cbSelectDay.setOnCheckedChangeListener { _, isChecked ->
            if (!isProgrammaticSelection) {
                toggleDaySelection(header.date, isChecked)
            }
        }
    }

    private fun bindPhoto(holder: PhotoViewHolder, image: ImageItem) {

        var images = R.drawable.bg_fake_image
        if (count == 1){
            images = R.drawable.bg_fake_image
        }else if (count == 2){
            images = R.drawable.bg_fake_video
        }

        // Load image with Glide
        Glide.with(activity)
            .load(image.uri)
            .error(images)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .dontTransform()
            .into(holder.imageView)

        Log.i("TAG", "bindPhoto: "+image.uri)
        Log.i("TAG", "bindPhoto: "+image)

        if (count == 2) {
            holder.vidFirst.visibility = View.VISIBLE
        } else {
            holder.vidFirst.visibility = View.GONE
        }
        // Set selection state
        holder.cbPhoto.setOnCheckedChangeListener(null)
        holder.cbPhoto.isChecked = selectedItems.contains(image.id)

        holder.cbPhoto.setOnCheckedChangeListener { _, isChecked ->
            val newChecked = !selectedItems.contains(image.id)
            updateSelection(image.id, newChecked)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position) is GalleryRow.Header) TYPE_HEADER else TYPE_PHOTO
    }

    fun getSelectedItems(): List<ImageItem> {
        return currentList.filterIsInstance<GalleryRow.Photo>()
            .filter { selectedItems.contains(it.image.id) }
            .map { it.image }
    }

    fun selectAll() {
        isProgrammaticSelection = true
        currentList.filterIsInstance<GalleryRow.Photo>()
            .forEach { selectedItems.add(it.image.id) }
        notifyDataSetChanged()
        onSelectionChanged(selectedItems.size)
        isProgrammaticSelection = false
    }

    fun deselectAll() {
        isProgrammaticSelection = true
        selectedItems.clear()
        notifyDataSetChanged()
        onSelectionChanged(0)
        isProgrammaticSelection = false
    }
    private fun getPhotosForDay(dayTimestamp: Long): List<GalleryRow.Photo> {
        return currentList.filterIsInstance<GalleryRow.Photo>()
            .filter { it.image.dateTakenMillis?.let { time -> isSameDay(time, dayTimestamp) } == true }
    }

    private fun toggleDaySelection(dayTimestamp: Long, isSelected: Boolean) {
        isProgrammaticSelection = true

        val dayPhotos = getPhotosForDay(dayTimestamp)
        dayPhotos.forEach { photo ->
            if (isSelected) {
                selectedItems.add(photo.image.id)
            } else {
                selectedItems.remove(photo.image.id)
            }
        }

        // Find all header positions for this day and update them
        currentList.forEachIndexed { index, row ->
            if (row is GalleryRow.Header && isSameDay(row.date, dayTimestamp)) {
                notifyItemChanged(index)
            }
        }

        // Also update all photo items in this day
        dayPhotos.forEach { photo ->
            val photoIndex = currentList.indexOf(photo)
            if (photoIndex != -1) {
                notifyItemChanged(photoIndex)
            }
        }

        onSelectionChanged(selectedItems.size)
        isProgrammaticSelection = false
    }

    private fun updateSelection(imageId: Long, isSelected: Boolean) {
        if (isSelected) {
            selectedItems.add(imageId)
        } else {
            selectedItems.remove(imageId)
        }

        // Update header states
        currentList.forEachIndexed { index, row ->
            if (row is GalleryRow.Header) {
                notifyItemChanged(index)
            }
        }
        onSelectionChanged(selectedItems.size)
    }



    fun areAllSelected(): Boolean {
        val totalPhotos = currentList.count { it is GalleryRow.Photo }
        return totalPhotos > 0 && selectedItems.size == totalPhotos
    }

    fun toggleSelectAll(checked: Boolean) {
        if (checked) {
            currentList.forEach { if (it is GalleryRow.Photo) selectedItems.add(it.image.id) }
        } else selectedItems.clear()
        notifyDataSetChanged()
        onSelectionChanged(selectedItems.size)
    }


    private fun isSameDay(time1: Long, time2: Long): Boolean {
        val date1 = java.util.Date(time1)
        val date2 = java.util.Date(time2)
        return date1.year == date2.year && date1.month == date2.month && date1.date == date2.date
    }
}