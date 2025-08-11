package com.Blue.photorecovery.activity

import android.content.res.Resources
import android.os.Bundle
import android.util.TypedValue
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.Blue.photorecovery.R
import com.Blue.photorecovery.databinding.ActivityAllFileRecoveryBinding

class AllFileRecovery : AppCompatActivity() {

    private lateinit var binding: ActivityAllFileRecoveryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllFileRecoveryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.apply {

            btnBack.setOnClickListener {
                finish()
            }

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

        }

    }
}