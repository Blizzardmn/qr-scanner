package com.tools.easy.scanner.ui.other

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import com.tools.easy.scanner.basic.BasicActivity
import com.tools.easy.scanner.databinding.ActivityHtmlBinding

/**
 *  description :
 */
class HtmlActivity: BasicActivity<ActivityHtmlBinding>() {

    override fun vBinding(): ActivityHtmlBinding {
        return ActivityHtmlBinding.inflate(layoutInflater)
    }

    companion object {
        fun openPrivacy(activity: Activity) {
            val intent = Intent(activity, HtmlActivity::class.java)
            intent.putExtra("html", "")
            activity.startActivity(intent)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.imgBack.setOnClickListener { finish() }

        val settings = binding.web.settings
        settings.setAppCacheEnabled(true)
        settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        settings.setSupportZoom(true)
        settings.builtInZoomControls = true
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true

        binding.web.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                binding.progressBar.progress = newProgress
                if (newProgress == 100) {
                    binding.progressBar.visibility = View.GONE
                } else {
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        }

        binding.web.loadUrl(intent.getStringExtra("html") ?: "")
    }
}