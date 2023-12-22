package com.fast.open.ss.dual.agreement.ui.agreement

import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.fast.open.ss.dual.agreement.R
import com.fast.open.ss.dual.agreement.base.BaseActivity
import com.fast.open.ss.dual.agreement.databinding.ActivityWebNetBinding
import com.fast.open.ss.dual.agreement.model.WebViewModel
import com.fast.open.ss.dual.agreement.utils.SmileKey

class AgreementActivity : BaseActivity<ActivityWebNetBinding, WebViewModel>(
    R.layout.activity_web_net, WebViewModel::class.java
) {
    override fun intiView() {
        binding.imgBack.setOnClickListener {
            finish()
        }
    }

    override fun initData() {
        binding.webViewSmile.loadUrl(SmileKey.web_smile_url)
        binding.webViewSmile.settings.javaScriptEnabled = true
        binding.webViewSmile.webChromeClient=object :WebChromeClient(){

        }
        binding.webViewSmile.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url?.startsWith("http") == true) {
                    view?.loadUrl(url)
                }
                return true
            }
        }
    }
}