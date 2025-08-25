package com.Blue.photorecovery.adapter.common

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.Blue.photorecovery.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class RecoverHistoryAdapter(
    private val count: Int,
    private val activity: Activity,
    var planList: ArrayList<String>
) : RecyclerView.Adapter<RecoverHistoryAdapter.ImageViewHolder>() {

    private val selectedItems = mutableSetOf<Int>()

    // Listener for selection changes
    var onSelectionChangeListener: OnSelectionChangeListener? = null

    interface OnSelectionChangeListener {
        fun onSelectionChanged(selectedCount: Int, totalCount: Int)
    }


    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imgNew)
        val vidFirst: ImageView = itemView.findViewById(R.id.vidFirst)
        val cbPhoto: CheckBox = itemView.findViewById(R.id.cbPhoto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_photo_square, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ImageViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {

        val item = planList[position]

        Glide.with(activity)
            .load(item)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .dontTransform()
            .into(holder.imageView)

        if (count == 2) {
            holder.vidFirst.visibility = View.VISIBLE
        } else {
            holder.vidFirst.visibility = View.GONE
        }

        holder.cbPhoto.setOnCheckedChangeListener(null)
        holder.cbPhoto.isChecked = selectedItems.contains(position)

        holder.cbPhoto.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedItems.add(position)
            } else {
                selectedItems.remove(position)
            }

            // Notify listener about selection change
            onSelectionChangeListener?.onSelectionChanged(selectedItems.size, planList.size)

        }
    }

    override fun getItemCount(): Int {
        return planList.size
    }

    fun selectAll(select: Boolean) {
        if (select) {
            for (i in planList.indices) {
                selectedItems.add(i)
            }
        } else selectedItems.clear()
        notifyDataSetChanged()
        onSelectionChangeListener?.onSelectionChanged(selectedItems.size, planList.size)
    }

    fun areAllSelected(): Boolean {
        val totalPhotos = planList.size
        return totalPhotos > 0 && selectedItems.size == totalPhotos
    }

    fun toggleSelectAll(checked: Boolean) {
        if (checked) {
            for (i in planList.indices) {
                selectedItems.add(i)
            }
        } else selectedItems.clear()
        notifyDataSetChanged()
        onSelectionChangeListener?.onSelectionChanged(selectedItems.size, planList.size)
    }


    fun getSelectedItems(): List<String> {
        return selectedItems.map { planList[it] }
    }

    fun getSelectedCount(): Int {
        return selectedItems.size
    }

}