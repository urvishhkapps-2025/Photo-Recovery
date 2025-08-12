package com.Blue.photorecovery.activity

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.Blue.photorecovery.R
import com.Blue.photorecovery.common.UserDataManager
import com.Blue.photorecovery.databinding.ActivitySplashBinding

class Splash : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    private var countDownTimerForSplash: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

            }
        }
        onBackPressedDispatcher.addCallback(this@Splash, callback)

        countDownTimerForSplash = object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
                if (UserDataManager.instance!!.getUserFirstTime()) {
                    UserDataManager.instance!!.setUserFirstTime(false)
                    val intent = Intent(this@Splash, IntroScreen::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val intent = Intent(this@Splash, AllFileRecovery::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }.start()

    }
}