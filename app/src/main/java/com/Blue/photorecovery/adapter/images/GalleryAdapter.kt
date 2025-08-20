package com.Blue.photorecovery.adapter.images

import ImageItem
import android.app.Activity
import android.os.Build
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.Blue.photorecovery.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class GalleryAdapter(
    private val activity: Activity,
    private val onPhotoClick: (ImageItem) -> Unit = {},
    private val onSelectionChanged: (Int) -> Unit = {} // notify Activity about count
) : ListAdapter<GalleryRow, RecyclerView.ViewHolder>(DIFF) {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_PHOTO = 1

        private val DIFF = object : DiffUtil.ItemCallback<GalleryRow>() {
            override fun areItemsTheSame(old: GalleryRow, new: GalleryRow): Boolean =
                when {
                    old is GalleryRow.Header && new is GalleryRow.Header -> old.day == new.day
                    old is GalleryRow.Photo && new is GalleryRow.Photo ->
                        old.item.id == new.item.id && old.item.volume == new.item.volume

                    else -> false
                }

            override fun areContentsTheSame(old: GalleryRow, new: GalleryRow): Boolean = old == new
        }
    }

    /** Selected items tracked by a stable id */
    private val selectedIds = linkedSetOf<String>()

    /** Index photos by date so a header can toggle its day quickly */
    private val photosByDay = mutableMapOf<LocalDate, MutableList<ImageItem>>()
    private val photoPositionsByDay = mutableMapOf<LocalDate, MutableList<Int>>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun submitList(list: List<GalleryRow>?) {
        photosByDay.clear()
        photoPositionsByDay.clear()
        list?.forEachIndexed  {  index,row ->
            if (row is GalleryRow.Photo) {
                row.item.dateTakenMillis?.let {
                    val d = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    photosByDay.getOrPut(d) { mutableListOf() }.add(row.item)
                    photoPositionsByDay.getOrPut(d) { mutableListOf() }.add(index) // <- Int positions
                }
            }
        }
        super.submitList(list)
        onSelectionChanged(selectedIds.size)
    }

    override fun getItemViewType(position: Int) =
        if (getItem(position) is GalleryRow.Header) TYPE_HEADER else TYPE_PHOTO

    class HeaderVH(v: View) : RecyclerView.ViewHolder(v) {
        val tvDate: TextView = v.findViewById(R.id.tvDate)
        val tevText: TextView = v.findViewById(R.id.tevText)
        val allSelectDay: CheckBox = v.findViewById(R.id.cbSelectDay)
    }

    class PhotoVH(val root: View, onPhotoClick: (Int) -> Unit) : RecyclerView.ViewHolder(root) {
        val imgNew: ImageView = root.findViewById(R.id.imgNew)
        val photoSelect: CheckBox = root.findViewById(R.id.cbPhoto)

        init {
            root.setOnClickListener { onPhotoClick(adapterPosition) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inf = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HEADER) {
            HeaderVH(inf.inflate(R.layout.item_header_date, parent, false))
        } else {
            PhotoVH(inf.inflate(R.layout.item_photo_square, parent, false)) { pos ->
                val row = getItem(pos) as GalleryRow.Photo
                onPhotoClick(row.item)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val row = getItem(position)) {
            is GalleryRow.Header -> bindHeader(holder as HeaderVH, row)
            is GalleryRow.Photo -> bindPhoto(holder as PhotoVH, row.item)
        }
    }

    private fun bindHeader(vh: HeaderVH, row: GalleryRow.Header) {
        vh.tvDate.setTextSize(TypedValue.COMPLEX_UNIT_PX, 40f)
        vh.tevText.setTextSize(TypedValue.COMPLEX_UNIT_PX, 40f)
        vh.tvDate.text = row.title
        val photos = photosByDay[row.day].orEmpty()
        val allSelected = photos.isNotEmpty() && photos.all { selectedIds.contains(it.stableId()) }

        vh.allSelectDay.setOnCheckedChangeListener(null)
        vh.allSelectDay.isChecked = allSelected

        val itemsInDay = photosByDay[row.day].orEmpty()


        vh.allSelectDay.setOnCheckedChangeListener { _, isChecked ->
            itemsInDay.forEach { img ->
                if (isChecked) selectedIds.add(img.stableId()) else selectedIds.remove(img.stableId())
            }
            onSelectionChanged(selectedIds.size)

            // refresh only the photo rows under this header
            photoPositionsByDay[row.day].orEmpty().forEach { pos: Int ->
                notifyItemChanged(pos)
            }
            // refresh header checkbox state too
            val headerPos = vh.adapterPosition
            if (headerPos != RecyclerView.NO_POSITION) notifyItemChanged(headerPos)
        }
    }

    private fun bindPhoto(vh: PhotoVH, item: ImageItem) {

        Log.i("TAG", "bindPhoto: "+item.uri)

        Glide.with(activity)
            .load(item.uri)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .centerCrop()
            .into(vh.imgNew)

        vh.photoSelect.setOnCheckedChangeListener(null)
        vh.photoSelect.isChecked = selectedIds.contains(item.stableId())

        // tapping the whole tile toggles the checkbox
//        vh.root.setOnClickListener { vh.cb.performClick() }

        vh.photoSelect.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                selectedIds.add(item.stableId())
            } else {
                selectedIds.remove(item.stableId())
            }
            notifyDataSetChanged()
            onSelectionChanged(selectedIds.size)
        }
    }

    private fun notifyHeaderAbove(pos: Int) {
        var i = pos
        while (i >= 0 && getItemViewType(i) != TYPE_HEADER) i--
        if (i >= 0) notifyItemChanged(i)
    }

    fun selectAll() {
        currentList.forEach { row ->
            if (row is GalleryRow.Photo) selectedIds.add(row.item.stableId())
        }
        notifyDataSetChanged()
        onSelectionChanged(selectedIds.size)
    }

    /** Clear all selections */
    fun deselectAll() {
        selectedIds.clear()
        notifyDataSetChanged()
        onSelectionChanged(0)
    }

    fun toggleSelectAll(checked: Boolean) {
        if (checked) {
            currentList.forEach { if (it is GalleryRow.Photo) selectedIds.add(it.item.stableId()) }
        } else selectedIds.clear()
        notifyDataSetChanged()
        onSelectionChanged(selectedIds.size)
    }

    /** True if every photo is selected */
    fun areAllSelected(): Boolean {
        val totalPhotos = currentList.count { it is GalleryRow.Photo }
        return totalPhotos > 0 && selectedIds.size == totalPhotos
    }

    fun selectedItems(): List<ImageItem> =
        currentList.mapNotNull { (it as? GalleryRow.Photo)?.item }
            .filter { selectedIds.contains(it.stableId()) }

    fun getSelectedItems(): List<ImageItem> {
        val ids = selectedIds.toSet()
        return currentList.mapNotNull { (it as? GalleryRow.Photo)?.item }
            .filter { ids.contains(it.stableId()) }
    }

}

/** make a stable id from your ImageItem fields */
private fun ImageItem.stableId(): String =
    this.id.toString() + "|" + this.volume.toString() + "|" + this.uri.toString()
