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
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
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
import com.Blue.photorecovery.storage.images.GalleryRow
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
    private var isNewestFirst = true
    private var isSmallToFirst = false
    private var isLargeToFirst = false
    var count = 1
    var filterType = 0
    private var mainPopupWindow: PopupWindow? = null
    private var customizePopupWindow: PopupWindow? = null
    private var selectedLabel = "All Days"

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
                filterType = 0
                applyFilters(0)
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
                filterType = 0
                applyFilters(0)
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

    @SuppressLint("NewApi")
    private fun applyFilters(filter: Int) {
        var filteredImages = allImages
        val today = java.time.LocalDate.now()
        val yesterday = today.minusDays(1)
        if (isLowQualityHidden) {
            filteredImages = filteredImages.filter { it.sizeBytes >= 1_000_000 }
                .toMutableList() // 1MB threshold
        }
        when (filter) {
            1 -> { /* Filter 1 logic */
            }

            2 -> {
                // Today
                filteredImages = filteredImages.filter { image ->
                    val date = Instant.ofEpochMilli(
                        image.dateTakenMillis ?: image.getFallbackDate()
                    ).atZone(ZoneId.systemDefault()).toLocalDate()
                    date == today
                }
            }

            3 -> {
                // Yesterday
                filteredImages = filteredImages.filter { image ->
                    val date = Instant.ofEpochMilli(
                        image.dateTakenMillis ?: image.getFallbackDate()
                    ).atZone(ZoneId.systemDefault()).toLocalDate()
                    date == yesterday
                }
            }

            4 -> {
                // Within 7 Days
                val sevenDaysAgo = today.minusDays(7)
                filteredImages = filteredImages.filter { image ->
                    val date = Instant.ofEpochMilli(
                        image.dateTakenMillis ?: image.getFallbackDate()
                    ).atZone(ZoneId.systemDefault()).toLocalDate()
                    date.isAfter(sevenDaysAgo) || date.isEqual(sevenDaysAgo)
                }
            }

            5 -> {
                // Apply date sorting (newest first)
                isNewestFirst = true
            }

            6 -> {
                // Apply date sorting (oldest first)
                isNewestFirst = false
            }

            7 -> {
                // Small first
                isSmallToFirst = true
            }

            8 -> {
                // Large first
                isLargeToFirst = true
            }

        }

        val rows = buildRowsGroupedByDay(
            filteredImages,
            ZoneId.systemDefault(),
            DateTimeFormatter.ofPattern("dd MMM, yyyy", Locale.getDefault())
        )

        adapter.submitList(rows) {
            binding.recyclerView.scrollToPosition(0)
        }
        updateSelectAllText()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun buildRowsGroupedByDay(
        images: List<ImageItem>,
        zoneId: ZoneId,
        formatter: DateTimeFormatter
    ): List<GalleryRow> {
        val rows = mutableListOf<GalleryRow>()

        // Group images by date
        val groupedByDay = images.groupBy { image ->
            image.dateTakenMillis?.let {
                Instant.ofEpochMilli(it).atZone(zoneId).toLocalDate()
            } ?: run {
                // Fallback for images without dateTakenMillis
                Instant.ofEpochMilli(image.getFallbackDate()).atZone(zoneId).toLocalDate()
            }
        }

        // Sort dates based on current sorting preference
        val sortedDates = if (isNewestFirst) {
            groupedByDay.entries.sortedByDescending { it.key } // Newest first
        } else {
            groupedByDay.entries.sortedBy { it.key } // Newest last
        }

        // Create rows
        sortedDates.forEach { (date, dayImages) ->
            val title = formatter.format(date)
            val headerDate =
                dayImages.first().dateTakenMillis ?: dayImages.first().getFallbackDate()

            rows.add(GalleryRow.Header(title, headerDate))
            val sortedDayImages = when {
                isSmallToFirst -> dayImages.sortedBy { it.sizeBytes }
                isLargeToFirst -> dayImages.sortedByDescending { it.sizeBytes }
                else -> dayImages // default order
            }
            rows.addAll(sortedDayImages.map { GalleryRow.Photo(it) })
        }

        if (rows.isNotEmpty()) {
            // Add Empty Message For A Screen
            binding.layAll.visibility = View.VISIBLE
        } else {
            binding.layAll.visibility = View.GONE
        }

        isSmallToFirst = false
        isLargeToFirst = false
        return rows
    }

    private fun ImageItem.getFallbackDate(): Long {
        // Try to get date from file last modified or use current time as fallback
        return try {
            val file = File(uri.path ?: "")
            if (file.exists()) file.lastModified() else System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
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
                applyFilters(filterType)
            }

            cbPhoto.setOnCheckedChangeListener { _, checked ->
                if (updatingGlobal) return@setOnCheckedChangeListener   // prevent loop
                adapter?.toggleSelectAll(checked)
                if (adapter?.areAllSelected() == true) {
                    textAllSelect.text = "Unselect All"
                } else {
                    textAllSelect.text = "Select All"
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

    private fun toggleSortOrder() {
        showMainPopup()
    }

    private fun showMainPopup() {
        if (mainPopupWindow?.isShowing == true) {
            mainPopupWindow?.dismiss()
            return
        }

        val popupView = LayoutInflater.from(this).inflate(R.layout.layout_dropdown, null)
        mainPopupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            isOutsideTouchable = true
            isFocusable = true
        }

        setupRadio(popupView, R.id.rb_alldays, "All Days")
        setupRadio(popupView, R.id.rb_today, "Today")
        setupRadio(popupView, R.id.rb_yesterday, "Yesterday")
        setupRadio(popupView, R.id.rb_within_7, "Within 7 Days")

        val rbCustomize = popupView.findViewById<RadioButton>(R.id.rb_customize)
        val customizeParent = rbCustomize.parent as View

        val customizeListener = View.OnClickListener {
            mainPopupWindow?.dismiss()
            showCustomizePopup()
        }

        customizeParent.setOnClickListener(customizeListener)
        rbCustomize.setOnClickListener(customizeListener)

        highlightMainSelection(popupView)

        mainPopupWindow?.showAsDropDown(binding.img, 0, 10, Gravity.START)
    }

    private fun showCustomizePopup() {
        if (customizePopupWindow?.isShowing == true) {
            customizePopupWindow?.dismiss()
            return
        }

        val customizeView = LayoutInflater.from(this).inflate(R.layout.layout_customize, null)
        customizePopupWindow = PopupWindow(
            customizeView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            isOutsideTouchable = true
            isFocusable = true
        }

        setupRadio(customizeView, R.id.rb_new, "Date (New to Old)")
        setupRadio(customizeView, R.id.rb_old, "Date (Old to New)")
        setupRadio(customizeView, R.id.rb_small_large, "Size (Small to Large)")
        setupRadio(customizeView, R.id.rb_large_small, "Size (Large to Small)")

        highlightCustomizeSelection(customizeView)

        customizePopupWindow?.showAsDropDown(binding.img, 0, 10, Gravity.START)
    }

    @SuppressLint("NewApi")
    private fun setupRadio(parentView: View, radioId: Int, fullLabel: String) {
        val radio = parentView.findViewById<RadioButton>(radioId)
        val parent = radio.parent as View

        val listener = View.OnClickListener {
            selectedLabel = fullLabel
            binding.textSort.text = selectedLabel
            if (selectedLabel == "All Days") {
                filterType = 1
                applyFilters(1)
            } else if (selectedLabel == "Today") {
                filterType = 2
                applyFilters(2)
            } else if (selectedLabel == "Yesterday") {
                filterType = 3
                applyFilters(3)
            } else if (selectedLabel == "Within 7 Days") {
                filterType = 4
                applyFilters(4)
            } else if (selectedLabel == "Date (New to Old)") {
                filterType = 5
                applyFilters(5)
            } else if (selectedLabel == "Date (Old to New)") {
                filterType = 6
                applyFilters(6)
            } else if (selectedLabel == "Size (Small to Large)") {
                filterType = 7
                applyFilters(7)
            } else if (selectedLabel == "Size (Large to Small)") {
                filterType = 8
                applyFilters(8)
            }
            mainPopupWindow?.dismiss()
            customizePopupWindow?.dismiss()
        }

        parent.setOnClickListener(listener)
        radio.setOnClickListener(listener)
    }

    private fun getShortLabel(fullLabel: String): String {
        return when {
            fullLabel.startsWith("Date") -> "Date"
            fullLabel.startsWith("Size") -> "Size"
            else -> fullLabel
        }
    }

    private fun highlightMainSelection(parentView: View) {
        val rbAll = parentView.findViewById<RadioButton>(R.id.rb_alldays)
        val rbToday = parentView.findViewById<RadioButton>(R.id.rb_today)
        val rbYesterday = parentView.findViewById<RadioButton>(R.id.rb_yesterday)
        val rbWithin7 = parentView.findViewById<RadioButton>(R.id.rb_within_7)
        val rbCustomize = parentView.findViewById<RadioButton>(R.id.rb_customize)

        rbAll.isChecked = selectedLabel == "All Days"
        rbToday.isChecked = selectedLabel == "Today"
        rbYesterday.isChecked = selectedLabel == "Yesterday"
        rbWithin7.isChecked = selectedLabel == "Within 7 Days"
        rbCustomize.isChecked = selectedLabel.startsWith("Date") || selectedLabel.startsWith("Size")
    }

    private fun highlightCustomizeSelection(parentView: View) {
        val rbNew = parentView.findViewById<RadioButton>(R.id.rb_new)
        val rbOld = parentView.findViewById<RadioButton>(R.id.rb_old)
        val rbSmall = parentView.findViewById<RadioButton>(R.id.rb_small_large)
        val rbLarge = parentView.findViewById<RadioButton>(R.id.rb_large_small)

        rbNew.isChecked = selectedLabel == "Date (New to Old)"
        rbOld.isChecked = selectedLabel == "Date (Old to New)"
        rbSmall.isChecked = selectedLabel == "Size (Small to Large)"
        rbLarge.isChecked = selectedLabel == "Size (Large to Small)"
    }

    private fun updateSelectAllText() {
        val totalSelectable = adapter.currentList.count { it is GalleryRow.Photo }
        val selectedCount = adapter.getSelectedItems().size

        updatingGlobal = true
        isSelectAll = selectedCount == totalSelectable && totalSelectable > 0
        binding.textAllSelect.text = if (isSelectAll) "Unselect All" else "Select All"
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
            if (count == 1) {
                val allRecovered = UserDataManager.instance!!.getRecoverImages()
                selected.forEach { image ->
                    val uriString = image.uri.toString()
                    if (!allRecovered.contains(uriString)) {
                        allRecovered.add(uriString)
                    }
                }
                UserDataManager.instance!!.setRecoverImages(allRecovered as ArrayList<String>?)
            } else if (count == 2) {
                val allRecovered = UserDataManager.instance!!.getRecoverVideo()
                selected.forEach { image ->
                    val uriString = image.uri.toString()
                    if (!allRecovered.contains(uriString)) {
                        allRecovered.add(uriString)
                    }
                }
                UserDataManager.instance!!.setRecoverVideo(allRecovered as ArrayList<String>?)
            }

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
                        btnColse.performClick()
                        val intent = Intent(this@RecoverDetails, RecoveredHistory::class.java)
                        startActivity(intent)
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
            val dialog = DeleteDialogFragment(count, selected.size) {
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

}