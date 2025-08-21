package com.Blue.photorecovery.activity.storage

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.Blue.photorecovery.R
import com.Blue.photorecovery.adapter.common.FolderAdapter
import com.Blue.photorecovery.databinding.ActivityRecoverBinding
import com.Blue.photorecovery.storage.images.EventBus
import com.Blue.photorecovery.storage.images.FolderItem
import com.Blue.photorecovery.storage.images.ImageUtils
import com.Blue.photorecovery.storage.scan.ScanResultManager
import com.Blue.photorecovery.storage.video.VideoUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File

class Recover : AppCompatActivity() {

    var count = 1
    private lateinit var binding: ActivityRecoverBinding
    private lateinit var folderAdapter: FolderAdapter
    private var observationJob: Job? = null


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecoverBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        count = intent.getIntExtra("count", 1)


        binding.apply {

            txt1.setTextSize(TypedValue.COMPLEX_UNIT_PX, 55f)

            if (count == 1) {
                txt1.text = "Recover Photos"
            } else if (count == 2) {
                txt1.text = "Recover Videos"
            }

            setupFolderAdapter()
            loadImagesFoldersFromScanResult()
            setupFolderUpdateListener()

            binding.btnBack.setOnClickListener {
                finish()
            }

            val callback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finish()
                }
            }
            onBackPressedDispatcher.addCallback(this@Recover, callback)

        }

    }

    private fun setupFolderUpdateListener() {
        observationJob = lifecycleScope.launch {
            EventBus.folderUpdateEvent.collect { folderPath ->
                if (count == 1) {
                    refreshFolder(folderPath)
                } else if (count == 2) {
                    refreshFolderVideo(folderPath)
                }
            }
        }
    }

    private fun setupFolderAdapter() {
        folderAdapter = FolderAdapter(
            count,
            onFolderClick = { folder ->
                openImagesFolderDetails(folder)
            }
        )

        binding.recyclerViewForItem.apply {
            adapter = folderAdapter
            layoutManager = LinearLayoutManager(this@Recover)
            setHasFixedSize(true)
        }
    }

    private fun loadImagesFoldersFromScanResult() {
        val folders = ScanResultManager.getFolders()
        folderAdapter.submitList(folders)
    }

    private fun openImagesFolderDetails(folder: FolderItem) {
        val intent = Intent(this, RecoverDetails::class.java).apply {
            putExtra("folder_path", folder.path)
            putExtra("folder_name", folder.name)
            putExtra("count", count)
        }
        startActivityForResult(intent, REQUEST_FOLDER_DETAILS)
    }

    private fun refreshFolder(folderPath: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val folder = File(folderPath)
            if (folder.exists()) {
                val images = folder.listFiles()?.filter { it.isFile && ImageUtils.isImageFile(it) }
                    ?: emptyList()
                val coverUris = images.take(3).map { it.toUri() }

                val updatedFolder = FolderItem(
                    path = folderPath,
                    name = folder.name,
                    imageCount = images.size,
                    coverUris = coverUris,
                    lastModified = folder.lastModified(),
                )
                updateFolderInScanResult(updatedFolder)
            } else {
                removeFolderFromScanResult(folderPath)
            }
        }
    }

    private fun refreshFolderVideo(folderPath: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val folder = File(folderPath)
            if (folder.exists()) {
                val videos = folder.listFiles()?.filter { it.isFile && VideoUtils.isVideoFile(it) }
                    ?: emptyList()
                val coverUris = videos.take(3).map { it.toUri() }
                val updatedFolder = FolderItem(
                    path = folderPath,
                    name = folder.name,
                    imageCount = videos.size,
                    coverUris = coverUris,
                    lastModified = folder.lastModified(),
                )
                updateFolderInScanResult(updatedFolder)
            } else {
                removeFolderFromScanResult(folderPath)
            }
        }
    }

    private fun updateFolderInScanResult(updatedFolder: FolderItem) {
        val currentFolders = ScanResultManager.getFolders().toMutableList()
        val index = currentFolders.indexOfFirst { it.path == updatedFolder.path }

        if (index != -1) {
            currentFolders[index] = updatedFolder
            ScanResultManager.scanResult = ScanResultManager.scanResult?.copy(
                scannedFolders = currentFolders
            )

            runOnUiThread {
                folderAdapter.updateFolder(updatedFolder.path, updatedFolder)
            }
        }
    }

    private fun removeFolderFromScanResult(folderPath: String) {
        ScanResultManager.removeFolder(folderPath)
        runOnUiThread {
            folderAdapter.removeFolder(folderPath)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_FOLDER_DETAILS && resultCode == RESULT_OK) {
            // Folder was deleted in details activity
            val deletedFolderPath = data?.getStringExtra("deleted_folder_path")
            deletedFolderPath?.let {
                removeFolderFromScanResult(it)
            }
        }
    }

    companion object {
        private const val REQUEST_FOLDER_DETAILS = 1001
    }

}