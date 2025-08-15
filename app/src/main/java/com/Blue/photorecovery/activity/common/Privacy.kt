package com.Blue.photorecovery.activity.common

import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.Blue.photorecovery.R
import com.Blue.photorecovery.databinding.ActivityPrivacyBinding

class Privacy : AppCompatActivity() {

    private lateinit var binding: ActivityPrivacyBinding

    private var privacyLink: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        privacyLink = "https://google.com"

        binding.apply {

            txt1.setTextSize(TypedValue.COMPLEX_UNIT_PX, 60f)

            btnBack.setOnClickListener {
                finish()
            }

            val callback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    btnBack.performClick()
                }
            }
            onBackPressedDispatcher.addCallback(this@Privacy, callback)

            webView.visibility = View.GONE
            WebViewProgressBar.visibility = View.VISIBLE
            webView.settings.javaScriptEnabled = true
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            webView.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    url.let {
                        view.loadUrl(it)
                    }
                    return true
                }

                override fun onPageFinished(view: WebView, url: String) {
                    WebViewProgressBar.visibility = View.GONE
                    webView.visibility = View.VISIBLE
                    super.onPageFinished(view, url)
                }
            }
            webView.loadUrl(privacyLink!!)

        }

    }
}