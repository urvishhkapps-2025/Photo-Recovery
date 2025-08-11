package com.Blue.photorecovery.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.Blue.photorecovery.R
import com.Blue.photorecovery.databinding.ActivityIntroScreenBinding

class IntroScreen : AppCompatActivity() {

    private lateinit var binding: ActivityIntroScreenBinding
    private var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        addDotsIndicator(count)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

            }
        }
        onBackPressedDispatcher.addCallback(this@IntroScreen, callback)

        binding.apply {

            btnBack.setOnClickListener {
                count--
                next(count)
            }

            btnNext.setOnClickListener {
                count++
                next(count)
            }

        }

    }

    private fun next(count: Int) {
        binding.apply {
            if (count == 0) {
                imageOfScreen.setImageResource(R.drawable.intro_1)
                textOfScreen.text =
                    "Restore Deleted Photos &amp; Videos To Your Phone At Anytime Anywhere"
                btnBack.visibility = View.GONE
            } else if (count == 1) {
                imageOfScreen.setImageResource(R.drawable.intro_2)
                textOfScreen.text =
                    "Easily Recover Delete Photos, Videos, Files, Messages, & Contacts With This App."
                btnBack.visibility = View.VISIBLE
            } else if (count == 2) {
                imageOfScreen.setImageResource(R.drawable.intro_3)
                textOfScreen.text = "Recover All Deleted Photos And Videos"
                btnBack.visibility = View.VISIBLE
            } else if (count == 3) {
                val intent = Intent(this@IntroScreen, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            addDotsIndicator(count)
        }
    }

    private fun addDotsIndicator(selectedPosition: Int) {
        binding.dotsContainer.removeAllViews()

        for (i in 0 until 3) {
            val dot = layoutInflater.inflate(R.layout.dot_indicator, binding.dotsContainer, false)
            val dotView = dot.findViewById<View>(R.id.dot)

            val sizeInPx = if (i == selectedPosition) {
                dpToPx(20, this@IntroScreen)
            } else {
                dpToPx(10, this@IntroScreen)
            }

            val params = LinearLayout.LayoutParams(sizeInPx, dpToPx(10, this@IntroScreen))
            params.marginStart = dpToPx(2, this@IntroScreen)
            params.marginEnd = dpToPx(2, this@IntroScreen)
            dotView.layoutParams = params

            // Change color based on selection
            dotView.setBackgroundResource(if (i == selectedPosition) R.drawable.dot_active else R.drawable.dot_inactive)

            binding.dotsContainer.addView(dot)
        }
    }

    fun dpToPx(dp: Int, activity: Activity): Int {
        return (dp * activity.resources.displayMetrics.density).toInt()
    }

}