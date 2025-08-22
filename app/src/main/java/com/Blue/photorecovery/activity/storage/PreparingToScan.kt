package com.Blue.photorecovery.activity.storage

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.util.TypedValue
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.Blue.photorecovery.R
import com.Blue.photorecovery.adapter.common.ScanAdapter
import com.Blue.photorecovery.databinding.ActivityPreparingToScanBinding
import com.Blue.photorecovery.storage.images.FolderItem
import com.Blue.photorecovery.storage.images.ImageUtils
import com.Blue.photorecovery.storage.images.ImageUtils.humanBytes
import com.Blue.photorecovery.storage.scan.ScanResult
import com.Blue.photorecovery.storage.scan.ScanResultManager
import com.Blue.photorecovery.storage.video.VideoUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PreparingToScan : AppCompatActivity() {

    private lateinit var binding: ActivityPreparingToScanBinding

    var count = 1
    var adapter: ScanAdapter? = null
    private var totalImages = 0
    private var totalSizeBytes = 0L
    private var totalFolders = 0
    private var firstFolderImages: List<Uri> = emptyList()


    @SuppressLint("SetTextI18n", "SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreparingToScanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.apply {

            count = intent.getIntExtra("count", 1)
            if (count == 1) {
                startScanningImages()
                adapter = ScanAdapter(1)
                loaderSplash.setAnimation(R.raw.scan_image)
            } else if (count == 2) {
                vidFirst.visibility = View.VISIBLE
                vidSecond.visibility = View.VISIBLE
                vidThird.visibility = View.VISIBLE
                vidFourth.visibility = View.VISIBLE
                adapter = ScanAdapter(2)
                loaderSplash.setAnimation(R.raw.scan_video)
                startScanningVideos()
            }

            scanRecycler.adapter = adapter

            loaderSplash.loop(true)
            loaderSplash.playAnimation()

            txt1.setTextSize(TypedValue.COMPLEX_UNIT_PX, 55f)
            text3.setTextSize(TypedValue.COMPLEX_UNIT_PX, 60f)
            textTotalImage.setTextSize(TypedValue.COMPLEX_UNIT_PX, 50f)
            textFile.setTextSize(TypedValue.COMPLEX_UNIT_PX, 40f)
            textSize.setTextSize(TypedValue.COMPLEX_UNIT_PX, 40f)
            textTotalSize.setTextSize(TypedValue.COMPLEX_UNIT_PX, 50f)
            textTotalImages.setTextSize(TypedValue.COMPLEX_UNIT_PX, 50f)
            btnViewResults.setTextSize(TypedValue.COMPLEX_UNIT_PX, 50f)

            btnBack.setOnClickListener {
                finish()
            }

            val callback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finish()
                }
            }
            onBackPressedDispatcher.addCallback(this@PreparingToScan, callback)

        }
    }

    private fun startScanningImages() {
        lifecycleScope.launch(Dispatchers.IO) {

            val externalDir = Environment.getExternalStorageDirectory()
            val imageFolders = ImageUtils.getImageFolders(externalDir)

            val folderItems = mutableListOf<FolderItem>()
            var firstFolderProcessed = false

            imageFolders.forEachIndexed { index, folder ->

                val images = folder.listFiles()?.filter { it.isFile && ImageUtils.isImageFile(it) }
                    ?: emptyList()

                firstFolderImages = images.take(4).map { it.toUri() }

                val folderSize = images.sumOf { it.length() }
                val coverUris = images.take(4).map { it.toUri() }

                totalImages += images.size
                totalSizeBytes += folderSize

                val folderItem = FolderItem(
                    path = folder.absolutePath,
                    name = folder.name,
                    imageCount = images.size,
                    coverUris = coverUris,
                    lastModified = folder.lastModified(),
                )
                folderItems.add(folderItem)
                delay(30)
            }

            val scanResult = ScanResult(
                totalImages = totalImages,
                totalSizeBytes = totalSizeBytes,
                totalFolders = totalFolders,
                scannedFolders = folderItems,
                firstFolderImages = firstFolderImages
            )

            ScanResultManager.scanResult = scanResult

            withContext(Dispatchers.Main) {
                imagesShowScanComplete(scanResult)
                displayFirstFolderImages(firstFolderImages)
            }
        }
    }

    private fun startScanningVideos() {
        lifecycleScope.launch(Dispatchers.IO) {
            val externalDir = Environment.getExternalStorageDirectory()
            val videoFolders = VideoUtils.getVideoFolders(externalDir) // Implement this method

            val folderItems = mutableListOf<FolderItem>()
            var firstFolderProcessed = false

            videoFolders.forEachIndexed { index, folder ->
                val videos = folder.listFiles()?.filter { it.isFile && VideoUtils.isVideoFile(it) }
                    ?: emptyList()

                if (!firstFolderProcessed && videos.isNotEmpty()) {
                    firstFolderImages = videos.take(4).map { it.toUri() }
                    firstFolderProcessed = true

                    withContext(Dispatchers.Main) {
                        displayFirstFolderImages(firstFolderImages)
                    }
                }

                val folderSize = videos.sumOf { it.length() }
                val coverUris = videos.take(4).map { it.toUri() }

                totalImages += videos.size
                totalSizeBytes += folderSize

                val folderItem = FolderItem(
                    path = folder.absolutePath,
                    name = folder.name,
                    imageCount = videos.size,
                    coverUris = coverUris,
                    lastModified = folder.lastModified(),
                )
                folderItems.add(folderItem)
                delay(30)
            }

            val scanResult = ScanResult(
                totalImages = totalImages,
                totalSizeBytes = totalSizeBytes,
                totalFolders = totalFolders,
                scannedFolders = folderItems,
                firstFolderImages = firstFolderImages
            )

            ScanResultManager.scanResult = scanResult

            withContext(Dispatchers.Main) {
                imagesShowScanComplete(scanResult)
            }
        }
    }

    private fun displayFirstFolderImages(images: List<Uri>) {

        images.forEachIndexed { index, uri ->
            val imageView = when (index) {
                0 -> binding.imgFirst
                1 -> binding.imgSecond
                2 -> binding.imgThird
                3 -> binding.imgFourth
                else -> null
            }
            imageView?.apply {
                imageView?.let {
                    Glide.with(this@PreparingToScan)
                        .load(uri)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .into(it)
                    it.visibility = View.VISIBLE // Make sure it's visible
                }
            }
        }
        if (images.size < 4) binding.card1.visibility = View.GONE
        if (images.size < 3) binding.card2.visibility = View.GONE
        if (images.size < 2) binding.card3.visibility = View.GONE
        if (images.isEmpty()) binding.card4.visibility = View.GONE
    }

    private fun imagesShowScanComplete(scanResult: ScanResult) {

        binding.textTotalImages.text = "+${scanResult.totalImages - 4}"
        binding.textTotalImage.text = "${scanResult.totalImages}"
        val totalSize = humanBytes(scanResult.totalSizeBytes)
        binding.textTotalSize.text = totalSize

        binding.apply {
            txt1.visibility = View.GONE
            layScan.visibility = View.GONE
            layData.visibility = View.VISIBLE
            loaderSplash.loop(true)
            loaderSplash.setAnimation(R.raw.done)
            loaderSplash.repeatCount = 0
            loaderSplash.playAnimation()
        }

        binding.btnViewResults.setOnClickListener {
            imagesNavigateToMainActivity()
        }
    }

    private fun imagesNavigateToMainActivity() {
        val intent = Intent(this, Recover::class.java)
        intent.putExtra("count", count)
        startActivity(intent)
        finish()
    }


}