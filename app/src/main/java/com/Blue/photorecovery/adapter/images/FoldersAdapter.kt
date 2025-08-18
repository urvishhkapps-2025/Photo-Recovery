package com.Blue.photorecovery.adapter.images

import android.annotation.SuppressLint
import android.app.Activity
import android.net.Uri
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.Blue.photorecovery.R
import com.Blue.photorecovery.storage.images.FolderSection
import com.bumptech.glide.Glide

class FoldersAdapter(
    private val activity: Activity,
    private val items: List<FolderSection>,
    private val onClick: (FolderSection) -> Unit
) : RecyclerView.Adapter<FoldersAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val textFolderName: TextView = v.findViewById(R.id.textFolderName)
        val btnNext: ImageView = v.findViewById(R.id.btnNext)
        val imgFirst: ImageView = v.findViewById(R.id.imgFirst)
        val imgSecond: ImageView = v.findViewById(R.id.imgSecond)
        val imgThird: ImageView = v.findViewById(R.id.imgThird)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_images, parent, false)
        return VH(v)
    }

    override fun getItemCount() = items.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(h: VH, pos: Int) {

        val sec = items[pos]
        h.textFolderName.text = sec.folder.displayName.replace(".", "") + "(${sec.folder.count})"
        h.textFolderName.setTextSize(TypedValue.COMPLEX_UNIT_PX, 50f)

        bindThumb(h.imgFirst, sec.thumbs.getOrNull(0))
        bindThumb(h.imgSecond, sec.thumbs.getOrNull(1))
        bindThumb(h.imgThird, sec.thumbs.getOrNull(2))


//        val f = items[pos]
//
//        if (items[0].count != null) {
//            h.imgFirst.setImageURI(f.coverUri)
//        }
//        if (items[1] != null) {
//            h.imgSecond.setImageURI(f.coverUri)
//        } else {
//            h.imgSecond.visibility = View.GONE
//        }
//        if (items[2] != null) {
//            h.imgThird.setImageURI(f.coverUri)
//        } else {
//            h.imgThird.visibility = View.GONE
//        }

        h.btnNext.setOnClickListener {
            onClick(items[pos])
        }

        h.imgFirst.setOnClickListener {
            h.btnNext.performClick()
        }

        h.imgSecond.setOnClickListener {
            h.btnNext.performClick()
        }

        h.imgThird.setOnClickListener {
            h.btnNext.performClick()
        }


    }

    @SuppressLint("CheckResult")
    fun bindThumb(imgView: ImageView, uri: Uri?) {
        if (uri == null) {
            imgView.visibility = View.GONE
        } else {
            Log.i("TAG", "bindThumb: " + uri)
            Glide.with(activity).load(uri).into(imgView)
            imgView.visibility = View.VISIBLE
//            imgView.load(uri) {
//                crossfade(true)
//                // Coil auto-sizes to the ImageView; you can also add .placeholder() if you like
//            }
//            imgView.setImageURI(uri)
        }

    }

}
