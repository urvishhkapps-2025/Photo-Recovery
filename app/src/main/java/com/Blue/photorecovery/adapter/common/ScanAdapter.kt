package com.Blue.photorecovery.adapter.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.Blue.photorecovery.R
import com.facebook.shimmer.ShimmerFrameLayout

class ScanAdapter(
    private val count: Int,
) : RecyclerView.Adapter<ScanAdapter.ImageViewHolder>() {

    // ViewHolder class
    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgDemo: ImageView = itemView.findViewById(R.id.imgDemo)
        val shimmer: ShimmerFrameLayout = itemView.findViewById(R.id.shimmer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_scan, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {

        if (count == 1) {
            holder.imgDemo.setImageResource(R.drawable.bg_fake_image)
        } else if (count == 2) {
            holder.imgDemo.setImageResource(R.drawable.bg_fake_video)
        } else if (count == 3) {
            holder.imgDemo.setImageResource(R.drawable.bg_fake_music)
        }

        holder.shimmer.startShimmer()

    }

    override fun getItemCount(): Int = 12

}