package com.velmora.vic.wolf

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

class WolfWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "WolfWebView"
    }

    interface WolfInteractionListener {
        fun onModelLoaded() {}
        fun onWolfTapped() {}
    }

    var listener: WolfInteractionListener? = null
    private var isPageLoaded = false
    private var pendingState: String? = null

    init {
        setupWebView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        setBackgroundColor(Color.TRANSPARENT)
        setLayerType(View.LAYER_TYPE_HARDWARE, null)

        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            cacheMode = WebSettings.LOAD_NO_CACHE
            mediaPlaybackRequiresUserGesture = false
            loadWithOverviewMode = true
            useWideViewPort = true
        }

        addJavascriptInterface(object {
            @JavascriptInterface
            fun onModelReady() {
                post {
                    isPageLoaded = true
                    listener?.onModelLoaded()
                    pendingState?.let {
                        setWolfState(it)
                        pendingState = null
                    }
                }
            }

            @JavascriptInterface
            fun onWolfTapped() {
                post {
                    listener?.onWolfTapped()
                }
            }
        }, "AndroidBridge")

        webChromeClient = WebChromeClient()
        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.d(TAG, "Wolf companion page finished loading: $url")
            }
        }

        loadUrl("file:///android_asset/web/wolf_companion.html")
    }

    fun setWolfState(state: String) {
        if (!isPageLoaded) {
            pendingState = state
            return
        }
        evaluateJavascript("if (window.setWolfState) window.setWolfState('$state');", null)
    }

    fun setVoiceLevel(level: Float) {
        if (!isPageLoaded) return
        evaluateJavascript("if (window.setVoiceLevel) window.setVoiceLevel($level);", null)
    }
}
