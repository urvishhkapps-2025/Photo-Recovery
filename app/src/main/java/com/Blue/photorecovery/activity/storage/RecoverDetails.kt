package com.Blue.photorecovery.activity.storage

import ImageItem
import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.Blue.photorecovery.R
import com.Blue.photorecovery.adapter.images.GalleryAdapter
import com.Blue.photorecovery.adapter.images.GalleryRow
import com.Blue.photorecovery.adapter.images.buildRowsGroupedByDay
import com.Blue.photorecovery.common.DeleteDialogFragment
import com.Blue.photorecovery.common.LoadingDialog
import com.Blue.photorecovery.common.UserDataManager
import com.Blue.photorecovery.databinding.ActivityRecoverDetailsBinding
import com.Blue.photorecovery.storage.images.GetAllImagesFromFolder.loadImagesInFolder
import com.Blue.photorecovery.storage.images.deleteImagesPermanently
import com.Blue.photorecovery.storage.scan.ScanImages
import com.Blue.photorecovery.storage.scan.sizeBytesOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale


class RecoverDetails : AppCompatActivity() {

    private lateinit var binding: ActivityRecoverDetailsBinding
    private var adapter: GalleryAdapter? = null
    private var selectedFolder: String? = null
    private var isLow = false
    private var allImagesInFolder: List<ImageItem> = emptyList()
    private var updatingGlobal = false
    private var process: LoadingDialog? = null
    var deleteCount = 0


    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ResourceType", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecoverDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.apply {

            binding.txt1.setTextSize(TypedValue.COMPLEX_UNIT_PX, 55f)
            binding.textSort.setTextSize(TypedValue.COMPLEX_UNIT_PX, 50f)
            binding.textRecover.setTextSize(TypedValue.COMPLEX_UNIT_PX, 50f)
            binding.textDelete.setTextSize(TypedValue.COMPLEX_UNIT_PX, 50f)
            binding.textHigh.setTextSize(TypedValue.COMPLEX_UNIT_PX, 50f)
            binding.textAllSelect.setTextSize(TypedValue.COMPLEX_UNIT_PX, 40f)
            isClick(0)
            val result = ScanImages.sections
            txt1.text = "" + result[0].folder.displayName.replace(".", "")

            btnBack.setOnClickListener {
                finish()
            }

            btnSwitch.setOnClickListener {
                if (isLow) {
                    isLow = false
                    btnSwitch.setImageResource(R.drawable.ic_uncheck_t)
                } else {
                    isLow = true
                    btnSwitch.setImageResource(R.drawable.ic_check_t)
                }
                applySizeFilterAndSubmit()
            }

            clickRecover.setOnClickListener {
                val allNewStorage = UserDataManager.instance?.getRecoverImages() ?: ArrayList()
                val selected = adapter?.getSelectedItems().orEmpty()
                if (selected.isEmpty()) {
                    Toast.makeText(this@RecoverDetails, "No images selected", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    selected.forEach {
                        val uriString = it.uri.toString()
                        if (!allNewStorage.contains(uriString)) {
                            allNewStorage.add(uriString)
                            UserDataManager.instance?.setRecoverImages(allNewStorage)
                        }
                    }
                }
            }

            clickDelete.setOnClickListener {
                try {
                    val fm: FragmentManager = supportFragmentManager
                    val dialog = DeleteDialogFragment(deleteCount) {
                        if (it) {
                            showLoading()
                            val selected = adapter?.getSelectedItems().orEmpty()
                            lifecycleScope.launch {
                                deleteImagesPermanently(this@RecoverDetails, selected) { success ->
                                    if (success) {
                                        Toast.makeText(
                                            this@RecoverDetails,
                                            "Images deleted",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        lifecycleScope.launch {
                                            val folderPath = result[0].folder
                                            selectedFolder = folderPath.toString()
                                            allImagesInFolder = loadImagesInFolder(this@RecoverDetails, folderPath)
                                            applySizeFilterAndSubmit()
                                        }
                                    } else {
                                        Toast.makeText(
                                            this@RecoverDetails,
                                            "Failed to delete some images",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        hideLoading()
                                    }
                                }
                            }
                        }
                    }
                    dialog.isCancelable = false
                    dialog.setStyle(
                        DialogFragment.STYLE_NORMAL,
                        android.R.style.Theme_Light_NoTitleBar_Fullscreen
                    )
                    dialog.show(
                        fm,
                        DeleteDialogFragment::class.java.name
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            adapter = GalleryAdapter(
                this@RecoverDetails,
                onPhotoClick = { clicked -> /* preview */ },
                onSelectionChanged = { count ->
                    deleteCount = count
                    isClick(count)
                    val total = adapter!!.currentList.count { it is GalleryRow.Photo }
                    val allChecked = total > 0 && count == total
                    updatingGlobal = true
                    binding.cbPhoto.isChecked = allChecked
                    updatingGlobal = false
                    if (allChecked) {
                        textAllSelect.text = "Unselect All Photos"
                    } else {
                        textAllSelect.text = "Select All Photos"
                    }
                }
            )

            cbPhoto.setOnCheckedChangeListener { _, checked ->
                if (updatingGlobal) return@setOnCheckedChangeListener   // prevent loop
                adapter?.toggleSelectAll(checked)
                if (adapter?.areAllSelected() == true) {
                    textAllSelect.text = "Unselect All Photos"
                } else {
                    textAllSelect.text = "Select All Photos"
                }
            }

            class GridSpace(private val space: Int) : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    v: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    val pos = parent.getChildAdapterPosition(v)
                    val isHeader =
                        (parent.adapter as? GalleryAdapter)?.currentList?.getOrNull(pos) is GalleryRow.Header
                    if (!isHeader) outRect.set(space, space, space, space) else outRect.set(
                        0,
                        space,
                        0,
                        space / 2
                    )
                }
            }
            binding.recyclerView.addItemDecoration(
                GridSpace(
                    space = resources.getDimensionPixelSize(
                        R.dimen.grid_space
                    )
                )
            )

            lifecycleScope.launch {
                val folderPath = result[0].folder
                selectedFolder = folderPath.toString()
                allImagesInFolder = loadImagesInFolder(this@RecoverDetails, folderPath)
                applySizeFilterAndSubmit()
            }

        }

    }

    fun showLoading() {
        try {
            val fm: FragmentManager = supportFragmentManager
            process = LoadingDialog()
            process!!.isCancelable = false
            process!!.setStyle(
                DialogFragment.STYLE_NORMAL,
                android.R.style.Theme_Light_NoTitleBar_Fullscreen
            )
            process!!.show(fm, Process::class.java.name)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun hideLoading() {
        try {
            try {
                if (process != null && process!!.isAdded && process!!.isVisible) {
                    process!!.dismiss()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun applySizeFilterAndSubmit() {
        val oneMB = 1_000_000L

        lifecycleScope.launch {
            val imagesToShow =
                if (!isLow) allImagesInFolder
                else withContext(Dispatchers.IO) {
                    allImagesInFolder.filter {
                        (it.sizeBytesOrNull(this@RecoverDetails) ?: 0L) >= oneMB
                    }
                }

            val rows: List<GalleryRow> = buildRowsGroupedByDay(
                imagesToShow,
                ZoneId.systemDefault(),
                DateTimeFormatter.ofPattern("dd MMM, yyyy", Locale.getDefault())
            )

            val glm = GridLayoutManager(this@RecoverDetails, 3).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (adapter!!.currentList.getOrNull(position) is GalleryRow.Header) 3 else 1
                    }
                }
            }
            binding.recyclerView.layoutManager = glm
            binding.recyclerView.adapter = adapter
            adapter!!.submitList(rows)
            adapter?.deselectAll()
            adapter!!.notifyDataSetChanged()
//            binding.recyclerView.scrollToPosition(0)
            hideLoading()
        }
    }

    private fun isClick(count: Int) {
        binding.apply {
            if (count == 0) {
                clickRecover.isEnabled = false
                clickDelete.isEnabled = false
                clickRecover.setBackgroundResource(R.drawable.btn_sec_bg)
                clickDelete.setBackgroundResource(R.drawable.btn_sec_bg)
            } else {
                clickRecover.isEnabled = true
                clickDelete.isEnabled = true
                clickRecover.setBackgroundResource(R.drawable.btn_bg)
                clickDelete.setBackgroundResource(R.drawable.btn_bg)
            }
        }
    }

}