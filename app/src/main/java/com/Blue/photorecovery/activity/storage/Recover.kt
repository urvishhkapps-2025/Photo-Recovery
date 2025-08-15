package com.Blue.photorecovery.activity.storage

import ImageFolder
import android.os.Bundle
import android.util.TypedValue
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.Blue.photorecovery.R
import com.Blue.photorecovery.adapter.images.FoldersAdapter
import com.Blue.photorecovery.databinding.ActivityRecoverBinding
import com.Blue.photorecovery.storage.images.FolderSection
import com.Blue.photorecovery.storage.images.GetAllImagesFromFolder.loadImagesInFolder
import com.Blue.photorecovery.storage.images.ScanImages
import com.Blue.photorecovery.storage.scan.ScanCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Suppress("UNCHECKED_CAST")
class Recover : AppCompatActivity() {

    private lateinit var binding: ActivityRecoverBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecoverBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnBack.setOnClickListener {
            finish()
        }


        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        }
        onBackPressedDispatcher.addCallback(this@Recover, callback)

        binding.txt1.setTextSize(TypedValue.COMPLEX_UNIT_PX, 60f)

        val result = ScanCache.result
        if (result == null) {
            finish() // nothing to show
            return
        }
//        val sections = ScanImages.sections  //
//        binding.recyclerViewForItem.adapter = FoldersAdapter(sections) { clicked ->
////                lifecycleScope.launch {
////                    val images: List<ImageItem> = loadImagesInFolder(
////                        context = this@Recover,
////                        folder = clicked,
////                        safTrees = emptyList(),
////                        includeSubdirs = true
////                    )
//            // TODO: show these images (e.g., open a new Activity with an ImagesAdapter)
////                }
//        }

//        lifecycleScope.launch(Dispatchers.IO) {
//            val sections = buildSectionsWithOneQuery(this@Recover, result.folders)
//            Log.i("TAG", "onCreate: "+sections)
//            withContext(Dispatchers.Main) {
//                binding.recyclerViewForItem.adapter = FoldersAdapter(sections, onClick = { /* ... */ })
//            }
//        }

//        val folders: List<ImageFolder> = result.folders
//        lifecycleScope.launch {
//            val sections: List<FolderSection> = withContext(Dispatchers.IO) {
//                folders.map { folder ->
//                    val uris = loadImagesInFolder(this@Recover, folder, emptyList(), true)
//                        .take(3)
//                        .map { it.uri }                    // size 0..3
//                    FolderSection(folder, uris)
//                }
//            }
//
//        }

    }
}