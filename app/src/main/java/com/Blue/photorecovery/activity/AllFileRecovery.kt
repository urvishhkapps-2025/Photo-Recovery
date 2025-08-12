package com.Blue.photorecovery.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.TypedValue
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.Blue.photorecovery.R
import com.Blue.photorecovery.databinding.ActivityAllFileRecoveryBinding

class AllFileRecovery : AppCompatActivity() {

    private lateinit var binding: ActivityAllFileRecoveryBinding
    private var doubleClick = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllFileRecoveryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (doubleClick) {
                    finish()
                }
                doubleClick = true
                Toast.makeText(
                    this@AllFileRecovery,
                    "Please click BACK again to exit",
                    Toast.LENGTH_SHORT
                ).show()
                Handler().postDelayed({ doubleClick = false }, 2000)
            }
        }
        onBackPressedDispatcher.addCallback(this@AllFileRecovery, callback)

        binding.apply {

            txt1.setTextSize(TypedValue.COMPLEX_UNIT_PX, 60f)
            t1.setTextSize(TypedValue.COMPLEX_UNIT_PX, 40f)
            t2.setTextSize(TypedValue.COMPLEX_UNIT_PX, 40f)
            t3.setTextSize(TypedValue.COMPLEX_UNIT_PX, 40f)
            t4.setTextSize(TypedValue.COMPLEX_UNIT_PX, 40f)
            t5.setTextSize(TypedValue.COMPLEX_UNIT_PX, 40f)
            t6.setTextSize(TypedValue.COMPLEX_UNIT_PX, 40f)
            t7.setTextSize(TypedValue.COMPLEX_UNIT_PX, 40f)
            t8.setTextSize(TypedValue.COMPLEX_UNIT_PX, 40f)
            t9.setTextSize(TypedValue.COMPLEX_UNIT_PX, 40f)

            btnSetting.setOnClickListener { }

            clickPhoto.setOnClickListener {
                val intent = Intent(this@AllFileRecovery, PhotoRecover::class.java)
                intent.putExtra("count", 1)
                startActivity(intent)
            }

            clickVideo.setOnClickListener {
                val intent = Intent(this@AllFileRecovery, PhotoRecover::class.java)
                intent.putExtra("count", 2)
                startActivity(intent)
            }

            clickDocument.setOnClickListener {
                val intent = Intent(this@AllFileRecovery, PhotoRecover::class.java)
                intent.putExtra("count", 3)
                startActivity(intent)
            }

            clickAudio.setOnClickListener {
                val intent = Intent(this@AllFileRecovery, PhotoRecover::class.java)
                intent.putExtra("count", 4)
                startActivity(intent)
            }

            clickContact.setOnClickListener { }

            clickRecyclerView.setOnClickListener { }

            clickJunk.setOnClickListener { }

        }

    }
}