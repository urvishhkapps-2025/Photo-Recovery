package com.Blue.photorecovery.activity.storage

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.Blue.photorecovery.R
import com.Blue.photorecovery.databinding.ActivityPreparingToScanBinding
import com.Blue.photorecovery.storage.images.GetAllImagesFolder.humanBytes
import com.Blue.photorecovery.storage.images.GetAllImagesFolder.scanAllImagesFromFileSystem
import com.Blue.photorecovery.storage.images.GetAllImagesFromFolder.loadImagesInFolder
import com.Blue.photorecovery.storage.scan.ScanImages
import com.Blue.photorecovery.storage.images.buildSectionsTop3
import com.Blue.photorecovery.storage.scan.ScanCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PreparingToScan : AppCompatActivity() {

    private lateinit var binding: ActivityPreparingToScanBinding
    var count = 1


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

        count = intent.getIntExtra("count", 1)
        scanAllImages()
        binding.apply {

            loaderSplash.loop(true)
            loaderSplash.setAnimation(R.raw.time)
            loaderSplash.playAnimation()

            startShimmer()

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

            btnViewResults.setOnClickListener {
                if (ScanCache.result != null) {
                    startActivity(Intent(this@PreparingToScan, Recover::class.java))
                } else {
                    scanAllImages()
                }
            }

            val callback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finish()
                }
            }
            onBackPressedDispatcher.addCallback(this@PreparingToScan, callback)

            btnViewResults.setOnClickListener {
                val intent = Intent(this@PreparingToScan, Recover::class.java)
                intent.putExtra("count", count)
                startActivity(intent)
                finish()
            }

        }
    }

    @SuppressLint("SetTextI18n")
    private fun scanAllImages() {
        lifecycleScope.launch {
            val result = scanAllImagesFromFileSystem(this@PreparingToScan)
            ScanCache.result = result
            val count = result.totalCount
            val totalSize = humanBytes(result.totalBytes)
            val folders = result.folders
            val sections = withContext(Dispatchers.Default) {
                buildSectionsTop3(result.folders, result.images)
            }
            ScanImages.sections = sections


            val imagesInFirstFolder = loadImagesInFolder(this@PreparingToScan, folders[0])

            binding.apply {

                stopShimmer()
                txt1.visibility = View.GONE
                layScan.visibility = View.GONE
                layData.visibility = View.VISIBLE
                loaderSplash.loop(true)
                loaderSplash.setAnimation(R.raw.done)
                loaderSplash.repeatCount = 0
                loaderSplash.playAnimation()

                try {
                    textTotalSize.text = totalSize
                    textTotalImage.text = count.toString()
                    textTotalImages.text = "+" + (count - 4).toString()

                    imgFirst.setImageURI(imagesInFirstFolder[0].uri)
                    imgSecond.setImageURI(imagesInFirstFolder[1].uri)
                    imgThird.setImageURI(imagesInFirstFolder[2].uri)
                    imgFourth.setImageURI(imagesInFirstFolder[3].uri)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        }
    }

    private fun startShimmer() {
        binding.apply {
            shimmer1.startShimmer()
            shimmer2.startShimmer()
            shimmer3.startShimmer()
            shimmer4.startShimmer()
            shimmer5.startShimmer()
            shimmer6.startShimmer()
            shimmer7.startShimmer()
            shimmer8.startShimmer()
            shimmer9.startShimmer()
            shimmer10.startShimmer()
            shimmer10.startShimmer()
            shimmer11.startShimmer()
            shimmer12.startShimmer()
        }
    }

    private fun stopShimmer() {
        binding.apply {
            shimmer1.stopShimmer()
            shimmer2.stopShimmer()
            shimmer3.stopShimmer()
            shimmer4.stopShimmer()
            shimmer5.stopShimmer()
            shimmer6.stopShimmer()
            shimmer7.stopShimmer()
            shimmer8.stopShimmer()
            shimmer9.stopShimmer()
            shimmer10.stopShimmer()
            shimmer10.stopShimmer()
            shimmer11.stopShimmer()
            shimmer12.stopShimmer()
        }
    }


}