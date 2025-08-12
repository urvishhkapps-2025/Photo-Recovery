package com.Blue.photorecovery.activity

import android.content.Intent
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.Blue.photorecovery.R
import com.Blue.photorecovery.common.AppUtils
import com.Blue.photorecovery.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.apply {

            txt1.setTextSize(TypedValue.COMPLEX_UNIT_PX, 60f)
            txt2.setTextSize(TypedValue.COMPLEX_UNIT_PX, 80f)
            textOfScreen.setTextSize(TypedValue.COMPLEX_UNIT_PX, 45f)
            t1.setTextSize(TypedValue.COMPLEX_UNIT_PX, 40f)
            t2.setTextSize(TypedValue.COMPLEX_UNIT_PX, 40f)
            t3.setTextSize(TypedValue.COMPLEX_UNIT_PX, 40f)
            btnStart.setTextSize(TypedValue.COMPLEX_UNIT_PX, 50f)

            val paint = txt2.paint
            val width = paint.measureText(txt2.text.toString())
            val textShader = LinearGradient(
                0f, 0f, width, txt2.textSize, // Gradient from left to right
                intArrayOf(
                    "#005EEC".toColorInt(), // Start color
                    "#67DCFC".toColorInt(), // End color
                ),
                null,
                Shader.TileMode.CLAMP
            )
            txt2.paint.shader = textShader


            clickPrivacy.setOnClickListener {
                val intent = Intent(this@MainActivity, Privacy::class.java)
                startActivity(intent)
            }

            clickRate.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data =
                    (AppUtils.RATE_APP_LINK + this@MainActivity.packageName).toUri()
                startActivity(intent)
            }

            clickShare.setOnClickListener {
                AppUtils.shareText(
                    this@MainActivity,
                    "Share Application",
                    "Share with.."
                )
            }

            btnStart.setOnClickListener {
                val intent = Intent(this@MainActivity, AllFileRecovery::class.java)
                startActivity(intent)
                finish()
            }

        }

    }
}