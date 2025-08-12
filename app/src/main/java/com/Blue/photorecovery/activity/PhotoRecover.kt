package com.Blue.photorecovery.activity

import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.Blue.photorecovery.R
import com.Blue.photorecovery.databinding.ActivityPhotoRecoverBinding

class PhotoRecover : AppCompatActivity() {

    private lateinit var binding: ActivityPhotoRecoverBinding

    var count = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhotoRecoverBinding.inflate(layoutInflater)
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

            textTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, 60f)
            textDescription.setTextSize(TypedValue.COMPLEX_UNIT_PX, 60f)
            clickScan.setTextSize(TypedValue.COMPLEX_UNIT_PX, 80f)

            val paint = clickScan.paint
            val width = paint.measureText(clickScan.text.toString())
            val textShader = LinearGradient(
                0f, 0f, width, clickScan.textSize, // Gradient from left to right
                intArrayOf(
                    "#005EEC".toColorInt(), // Start color
                    "#67DCFC".toColorInt(), // End color
                ),
                null,
                Shader.TileMode.CLAMP
            )
            clickScan.paint.shader = textShader

        }

    }
}