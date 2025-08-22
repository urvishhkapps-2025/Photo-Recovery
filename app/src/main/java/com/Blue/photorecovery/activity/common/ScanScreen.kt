package com.Blue.photorecovery.activity.common

import android.content.Intent
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.util.TypedValue
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.Blue.photorecovery.R
import com.Blue.photorecovery.activity.storage.PreparingToScan
import com.Blue.photorecovery.announcement.Canny
import com.Blue.photorecovery.common.LinearGradientSpan
import com.Blue.photorecovery.databinding.ActivityScanScreenBinding
import com.Blue.photorecovery.utils.PermissionUtils

class ScanScreen : AppCompatActivity() {

    private lateinit var binding: ActivityScanScreenBinding
    var count = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        binding.apply {

            count = intent.getIntExtra("count", 1)

            btnBack.setOnClickListener {
                finish()
            }

            val callback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    btnBack.performClick()
                }
            }
            onBackPressedDispatcher.addCallback(this@ScanScreen, callback)

            if (count == 1) {
                textTitle.text = "Photo Recover"
                textDescription.text = "Tap to scan All Photos"
            } else if (count == 2) {
                textTitle.text = "Videos Recover"
                textDescription.text = "Tap to scan All Videos"
            } else if (count == 3) {
                textTitle.text = "Documents Recover"
                textDescription.text = "Tap to scan All Documents"
            } else if (count == 4) {
                textTitle.text = "Audio Recover"
                textDescription.text = "Tap to scan All Audio"
            }

            textTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, 55f)
            textDescription.setTextSize(TypedValue.COMPLEX_UNIT_PX, 60f)
            clickScan.setTextSize(TypedValue.COMPLEX_UNIT_PX, 80f)

            setGradientOnlyOnEasyRecovery(clickScan)

            clickScan.setOnClickListener {
                val hasPermission = PermissionUtils.isStoragePermissionGranted(this@ScanScreen)
                if (!hasPermission) {
                    getPermission()
                } else {
                    val intent = Intent(this@ScanScreen, PreparingToScan::class.java)
                    intent.putExtra("count", count)
                    startActivity(intent)
                    finish()
                }

            }

        }
    }

    fun setGradientOnlyOnEasyRecovery(tv: TextView) {
        val fullText = " Tap "
        val target = "Tap"
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


    private fun getPermission() {
        try {
            val fm: FragmentManager = supportFragmentManager
            val canny = Canny(this@ScanScreen) {
                if (it) {
                    PermissionUtils.openAppSettingsForPermission(this@ScanScreen)
                }
            }
            canny.isCancelable = false
            canny.setStyle(
                DialogFragment.STYLE_NORMAL,
                android.R.style.Theme_Light_NoTitleBar_Fullscreen
            )
            canny.show(
                fm,
                Canny::class.java.name
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}