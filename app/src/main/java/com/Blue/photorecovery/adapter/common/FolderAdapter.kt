package com.Blue.photorecovery.adapter.common

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.Blue.photorecovery.R
import com.Blue.photorecovery.storage.images.FolderItem
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class FolderAdapter(
    private val count: Int,
    private val onFolderClick: (FolderItem) -> Unit,
) : ListAdapter<FolderItem, FolderAdapter.FolderViewHolder>(FolderDiffCallback) {

    companion object {
        private val FolderDiffCallback = object : DiffUtil.ItemCallback<FolderItem>() {
            override fun areItemsTheSame(oldItem: FolderItem, newItem: FolderItem): Boolean {
                return oldItem.path == newItem.path
            }

            override fun areContentsTheSame(oldItem: FolderItem, newItem: FolderItem): Boolean {
                return oldItem.name == newItem.name &&
                        oldItem.imageCount == newItem.imageCount &&
                        oldItem.coverUris == newItem.coverUris &&
                        oldItem.lastModified == newItem.lastModified
            }

            override fun getChangePayload(oldItem: FolderItem, newItem: FolderItem): Any? {
                val payload = mutableSetOf<String>()

                if (oldItem.name != newItem.name) payload.add("name")
                if (oldItem.imageCount != newItem.imageCount) payload.add("count")
                if (oldItem.coverUris != newItem.coverUris) payload.add("covers")
                if (oldItem.lastModified != newItem.lastModified) payload.add("modified")

                return if (payload.isEmpty()) null else payload
            }
        }
    }

    class FolderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val folderName: TextView = view.findViewById(R.id.textFolderName)
        val image1: ImageView = view.findViewById(R.id.imgFirst)
        val image2: ImageView = view.findViewById(R.id.imgSecond)
        val image3: ImageView = view.findViewById(R.id.imgThird)
        val vidFirst: ImageView = view.findViewById(R.id.vidFirst)
        val vidSecond: ImageView = view.findViewById(R.id.vidSecond)
        val vidThird: ImageView = view.findViewById(R.id.vidThird)

        val root: View = view.findViewById(R.id.btnNext)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_images_new, parent, false)
        return FolderViewHolder(view)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val folder = getItem(position)

        holder.folderName.text = folder.name.replace(".", "") + "(${folder.imageCount})"
        holder.folderName.setTextSize(TypedValue.COMPLEX_UNIT_PX, 50f)

        // Load cover images with Glide
        folder.coverUris.take(3).forEachIndexed { index, uri ->
            val imageView = when (index) {
                0 -> holder.image1
                1 -> holder.image2
                2 -> holder.image3
                else -> null
            }

            imageView?.let {
                var images = R.drawable.bg_fake_image
                if (count == 1) {
                    images = R.drawable.bg_fake_image
                } else if (count == 2) {
                    images = R.drawable.bg_fake_video
                }
                Glide.with(holder.itemView)
                    .load(uri)
                    .error(images)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .dontTransform()
                    .into(it)
                it.visibility = View.VISIBLE // Make sure it's visible
            }
        }

        // Handle empty cover slots - show placeholders
        if (folder.coverUris.size < 3) {
            holder.image3.visibility = View.INVISIBLE
            if (count == 2) {
                holder.vidThird.visibility = View.INVISIBLE
            }
        } else {
            holder.image3.visibility = View.VISIBLE
            if (count == 2) {
                holder.vidThird.visibility = View.VISIBLE
            }
        }

        if (folder.coverUris.size < 2) {
            holder.image2.visibility = View.INVISIBLE
            if (count == 2) {
                holder.vidSecond.visibility = View.INVISIBLE
            }
        } else {
            holder.image2.visibility = View.VISIBLE
            if (count == 2) {
                holder.vidSecond.visibility = View.VISIBLE
            }
        }

        if (folder.coverUris.isEmpty()) {
            holder.image1.visibility = View.INVISIBLE
            if (count == 2) {
                holder.vidFirst.visibility = View.INVISIBLE
            }
        } else {
            holder.image1.visibility = View.VISIBLE
            if (count == 2) {
                holder.vidFirst.visibility = View.VISIBLE
            }
        }

        holder.root.setOnClickListener {
            onFolderClick(folder)
        }

        holder.image1.setOnClickListener { holder.root.performClick() }
        holder.image3.setOnClickListener { holder.root.performClick() }
        holder.image2.setOnClickListener { holder.root.performClick() }

    }

    fun updateFolder(path: String, updatedFolder: FolderItem) {
        val currentList = currentList.toMutableList()
        val index = currentList.indexOfFirst { it.path == path }
        if (index != -1) {
            currentList[index] = updatedFolder
            submitList(currentList) // This is the correct way
        }
    }

    fun removeFolder(path: String) {
        val newList = currentList.filterNot { it.path == path }
        submitList(newList)
    }

    override fun getItemCount(): Int = currentList.size
}