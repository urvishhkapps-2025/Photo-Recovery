package com.Blue.photorecovery.activity.storage

import ImageItem
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.Blue.photorecovery.R
import com.Blue.photorecovery.adapter.common.GalleryAdapter
import com.Blue.photorecovery.common.DeleteDialogFragment
import com.Blue.photorecovery.common.LinearGradientSpan
import com.Blue.photorecovery.common.UserDataManager
import com.Blue.photorecovery.databinding.ActivityRecoverDetailsBinding
import com.Blue.photorecovery.storage.images.EventBus
import com.Blue.photorecovery.storage.images.FolderItem
import com.Blue.photorecovery.storage.images.GalleryRow
import com.Blue.photorecovery.storage.images.ImageUtils
import com.Blue.photorecovery.storage.images.ImageUtils.isImageFile
import com.Blue.photorecovery.storage.video.VideoUtils.isVideoFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale


class RecoverDetails : AppCompatActivity() {

    private lateinit var binding: ActivityRecoverDetailsBinding
    private var updatingGlobal = false
    private lateinit var adapter: GalleryAdapter
    private var folderPath: String = ""
    private var folderName: String = ""
    private var allImages: List<ImageItem> = emptyList()
    private var isLowQualityHidden = false
    private var isSelectAll = false

    var count = 1


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

        folderPath = intent.getStringExtra("folder_path") ?: ""
        folderName = intent.getStringExtra("folder_name") ?: ""
        count = intent.getIntExtra("count", 1)

        setupUI()
        setupAdapter()
        if (count == 1) {
            loadImages()
        } else if (count == 2) {
            loadVideos()
        }
        setupClickListeners()

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (adapter.getSelectedItems().isNotEmpty()) {
                    adapter.deselectAll()
                } else {
                    binding.btnBack.performClick()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this@RecoverDetails, callback)
    }

    private fun setupUI() {

        binding.apply {
            // Set text sizes
            txt1.setTextSize(TypedValue.COMPLEX_UNIT_PX, 55f)
            textSort.setTextSize(TypedValue.COMPLEX_UNIT_PX, 50f)
            textRecover.setTextSize(TypedValue.COMPLEX_UNIT_PX, 50f)
            textDelete.setTextSize(TypedValue.COMPLEX_UNIT_PX, 50f)
            textHigh.setTextSize(TypedValue.COMPLEX_UNIT_PX, 50f)
            textAllSelect.setTextSize(TypedValue.COMPLEX_UNIT_PX, 40f)
            text3.setTextSize(TypedValue.COMPLEX_UNIT_PX, 60f)
            textCountOfRecover.setTextSize(TypedValue.COMPLEX_UNIT_PX, 40f)
            btnContinues.setTextSize(TypedValue.COMPLEX_UNIT_PX, 50f)
            btnView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 50f)

            setGradientOnlyOnEasyRecovery(btnContinues)
            // Set folder name
            txt1.text = folderName.replace(".", "")

            // Setup toolbar
            btnBack.setOnClickListener { finish() }

            // Initial button states
            updateActionButtons(0)

        }

    }

    fun setGradientOnlyOnEasyRecovery(tv: TextView) {
        val fullText = " Continues "
        val target = "Continues"
        val start = fullText.indexOf(target)
        val end = start + target.length

        if (start >= 0) {
            val ss = SpannableString(fullText)
            ss.setSpan(
                LinearGradientSpan(
                    Color.parseColor("#005EEC"), // orange
                    Color.parseColor("#67DCFC")  // pink
                ),
                start, end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            tv.text = ss
        } else {
            tv.text = fullText // fallback
        }
    }

    private fun setupAdapter() {
        adapter = GalleryAdapter(
            count,
            activity = this,
            onPhotoClick = { image ->
                previewImage(image)
            },
            onSelectionChanged = { selectedCount ->
                updateActionButtons(selectedCount)
                updateSelectAllText()
            }
        )

        val glm = GridLayoutManager(this, 3).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (adapter.currentList.getOrNull(position) is GalleryRow.Header) 3 else 1
                }
            }

        }

        binding.recyclerView.layoutManager = glm
        binding.recyclerView.adapter = adapter
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadImages() {
        lifecycleScope.launch(Dispatchers.IO) {
            val images = loadImagesFromFolder(folderPath)
            allImages = images

            withContext(Dispatchers.Main) {
                applyFilters()
            }
        }
    }

    private fun loadImagesFromFolder(path: String): List<ImageItem> {
        val folder = File(path)
        return folder.listFiles()?.filter { it.isFile && isImageFile(it) }?.map { file ->
            ImageItem(
                id = file.absolutePath.hashCode().toLong(),
                uri = Uri.fromFile(file),
                name = file.name,
                sizeBytes = file.length(),
                dateTakenMillis = file.lastModified(),
                bucketId = null,
                bucketName = folder.name,
                relativePath = folder.absolutePath,
                isTrashed = false,
                volume = "fs:primary"
            )
        } ?: emptyList()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadVideos() {
        lifecycleScope.launch(Dispatchers.IO) {
            val videos = loadVideosFromFolder(folderPath)
            allImages = videos
            withContext(Dispatchers.Main) {
                applyFilters()
            }
        }
    }

    private fun loadVideosFromFolder(path: String): List<ImageItem> {
        val folder = File(path)
        return folder.listFiles()?.filter { it.isFile && isVideoFile(it) }?.map { file ->
            ImageItem(
                id = file.absolutePath.hashCode().toLong(),
                uri = Uri.fromFile(file),
                name = file.name,
                sizeBytes = file.length(),
                dateTakenMillis = file.lastModified(),
                bucketId = null,
                bucketName = folder.name,
                relativePath = folder.absolutePath,
                isTrashed = false,
                volume = "fs:primary"
            )
        } ?: emptyList()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun applyFilters() {
        var filteredImages = allImages

        // Apply quality filter
        if (isLowQualityHidden) {
            filteredImages = filteredImages.filter { it.sizeBytes >= 1_000_000 } // 1MB threshold
        }

        // Apply date sorting (newest first)
        filteredImages = filteredImages.sortedByDescending { it.dateTakenMillis ?: 0 }

        val rows = buildRowsGroupedByDay(
            filteredImages,
            ZoneId.systemDefault(),
            DateTimeFormatter.ofPattern("dd MMM, yyyy", Locale.getDefault())
        )

        adapter.submitList(rows)
        updateSelectAllText()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupClickListeners() {

        binding.apply {

            btnSwitch.setOnClickListener {
                if (isLowQualityHidden) {
                    isLowQualityHidden = false
                    btnSwitch.setImageResource(R.drawable.ic_uncheck_t)
                } else {
                    isLowQualityHidden = true
                    btnSwitch.setImageResource(R.drawable.ic_check_t)
                }
                adapter.toggleSelectAll(false)
                applyFilters()
            }

            cbPhoto.setOnCheckedChangeListener { _, checked ->
                if (updatingGlobal) return@setOnCheckedChangeListener   // prevent loop
                adapter?.toggleSelectAll(checked)
                if (adapter?.areAllSelected() == true) {
                    textAllSelect.text = "Unselect All Photos"
                } else {
                    textAllSelect.text = "Select All Photos"
                }
            }

            // Sort by date
            binding.textSort.setOnClickListener {
                toggleSortOrder()
            }

            // Recover selected
            binding.clickRecover.setOnClickListener {
                recoverSelectedImages()
            }

            // Delete selected
            binding.clickDelete.setOnClickListener {
                deleteSelectedImages()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun toggleSortOrder() {
        // Toggle between ascending/descending
        binding.textSort.isSelected = !binding.textSort.isSelected
        applyFilters()
    }

    private fun updateSelectAllText() {
        val totalSelectable = adapter.currentList.count { it is GalleryRow.Photo }
        val selectedCount = adapter.getSelectedItems().size

        updatingGlobal = true
        isSelectAll = selectedCount == totalSelectable && totalSelectable > 0
        binding.textAllSelect.text = if (isSelectAll) "Unselect All Photos" else "Select All Photos"
        binding.cbPhoto.isChecked = isSelectAll
        updatingGlobal = false

    }

    private fun updateActionButtons(selectedCount: Int) {
        // Enable/disable buttons
        val isEnabled = selectedCount > 0
        binding.clickRecover.isEnabled = isEnabled
        binding.clickDelete.isEnabled = isEnabled

        // Update button backgrounds
        val bgResource = if (isEnabled) R.drawable.btn_bg else R.drawable.btn_sec_bg
        binding.clickRecover.setBackgroundResource(bgResource)
        binding.clickDelete.setBackgroundResource(bgResource)
    }

    private fun recoverSelectedImages() {
        val selected = adapter.getSelectedItems()
        if (selected.isEmpty()) {
            Toast.makeText(this, "No images selected", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val allRecovered = UserDataManager.instance!!.getRecoverImages() ?: mutableListOf()
            selected.forEach { image ->
                val uriString = image.uri.toString()
                if (!allRecovered.contains(uriString)) {
                    allRecovered.add(uriString)
                }
            }
            UserDataManager.instance!!.setRecoverImages(allRecovered as ArrayList<String>?)

            withContext(Dispatchers.Main) {

                binding.apply {

                    layRecover.visibility = View.GONE
                    layRecoverSccessfull.visibility = View.VISIBLE

                    btnColse.setOnClickListener {
                        layRecover.visibility = View.VISIBLE
                        layRecoverSccessfull.visibility = View.GONE
                    }

                    loaderSplash.loop(true)
                    loaderSplash.setAnimation(R.raw.done)
                    loaderSplash.repeatCount = 0
                    loaderSplash.playAnimation()

                    textCountOfRecover.text = "${selected.size} photo have been saved to the device"

                    btnContinues.setOnClickListener {
                        btnColse.performClick()
                    }

                    btnView.setOnClickListener {

                    }

                }
                adapter.deselectAll()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun deleteSelectedImages() {
        val selected = adapter.getSelectedItems()
        if (selected.isEmpty()) {
            Toast.makeText(this, "No images selected", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val fm: FragmentManager = supportFragmentManager
            val dialog = DeleteDialogFragment(count,selected.size) {
                if (it) {
                    performDeletion(selected)
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun performDeletion(images: List<ImageItem>) {
        lifecycleScope.launch(Dispatchers.IO) {
            var deletedCount = 0

            images.forEach { image ->
                val file = File(image.uri.path ?: "")
                if (file.exists() && file.delete()) {
                    deletedCount++
                }
            }

            withContext(Dispatchers.Main) {
                if (deletedCount > 0) {
                    Toast.makeText(
                        this@RecoverDetails,
                        "Deleted $deletedCount images",
                        Toast.LENGTH_SHORT
                    ).show()

                    lifecycleScope.launch {
                        EventBus.notifyFolderUpdated(folderPath)
                    }

                    // Reload images after deletion
                    if (count == 1) {
                        loadImages()
                    } else if (count == 2) {
                        loadVideos()
                    }

                    // If all images were deleted, close activity
                    if (allImages.size - deletedCount == 0) {
                        setResult(RESULT_OK, Intent().putExtra("deleted_folder_path", folderPath))
                        finish()
                    }
                } else {
                    Toast.makeText(
                        this@RecoverDetails,
                        "Failed to delete images",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun previewImage(image: ImageItem) {
        // Implement image preview functionality
        // You can start a new activity or show a dialog for image preview
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun buildRowsGroupedByDay(
        images: List<ImageItem>,
        zoneId: ZoneId,
        formatter: DateTimeFormatter
    ): List<GalleryRow> {
        val rows = mutableListOf<GalleryRow>()

        val groupedByDay = images.groupBy { image ->
            image.dateTakenMillis?.let {
                Instant.ofEpochMilli(it).atZone(zoneId).toLocalDate()
            }
        }

        groupedByDay.entries.sortedByDescending { it.key }.forEach { (date, dayImages) ->
            val title = date?.let { formatter.format(it) } ?: "Unknown Date"
            val headerDate = dayImages.first().dateTakenMillis ?: 0L

            rows.add(GalleryRow.Header(title, headerDate))
            rows.addAll(dayImages.map { GalleryRow.Photo(it) })
        }

        return rows
    }

}