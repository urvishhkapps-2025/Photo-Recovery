package com.Blue.photorecovery.activity.storage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.Blue.photorecovery.R
import com.Blue.photorecovery.adapter.images.FoldersAdapter
import com.Blue.photorecovery.databinding.ActivityRecoverBinding
import com.Blue.photorecovery.storage.images.GetAllImagesFolder.humanBytes
import com.Blue.photorecovery.storage.images.GetAllImagesFolder.scanAllImagesFromFileSystem
import com.Blue.photorecovery.storage.images.buildSectionsTop3
import com.Blue.photorecovery.storage.scan.ScanCache
import com.Blue.photorecovery.storage.scan.ScanImages
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

        binding.txt1.setTextSize(TypedValue.COMPLEX_UNIT_PX, 55f)
        setAdapter()

    }

    fun setAdapter(){
        val result = ScanImages.sections
        binding.recyclerViewForItem.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false).also {
                it.initialPrefetchItemCount = 8
            }
            adapter = FoldersAdapter(this@Recover,result) { clicked ->
                ScanImages.sections = listOf(clicked)
                val intent = Intent(this@Recover, RecoverDetails::class.java)
                intent.putExtra("count", 1)
                startActivity(intent)
            }

            setHasFixedSize(true)
            isNestedScrollingEnabled = false
            itemAnimator = null
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val result = scanAllImagesFromFileSystem(this@Recover)
            ScanCache.result = result
            val sections = withContext(Dispatchers.Default) {
                buildSectionsTop3(result.folders, result.images)
            }
            ScanImages.sections = sections
            setAdapter()
        }
    }

}