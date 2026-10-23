package com.aaryan.weather

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.webkit.*
import androidx.webkit.WebViewAssetLoader

class MainActivity : Activity() {
    private lateinit var web: WebView
    private val host = "appassets.androidplatform.net"

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        window.statusBarColor = Color.BLACK
        window.navigationBarColor = Color.BLACK
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), 1)

        // Serves everything in assets/www at https://appassets.androidplatform.net/ so absolute paths like /sw.js work.
        val loader = WebViewAssetLoader.Builder().addPathHandler("/", WebViewAssetLoader.PathHandler { p ->
            try {
                val f = if (p.isEmpty() || p.endsWith("/")) p + "index.html" else p
                val ext = MimeTypeMap.getFileExtensionFromUrl(f)
                val mime = when (ext) {
                    "js", "mjs" -> "application/javascript"
                    "json", "webmanifest" -> "application/json"
                    "svg" -> "image/svg+xml"
                    "wasm" -> "application/wasm"
                    else -> MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "text/html"
                }
                WebResourceResponse(mime, "utf-8", assets.open("www/$f"))
            } catch (e: Exception) { null }
        }).build()

        web = WebView(this)
        web.setBackgroundColor(Color.BLACK)
        setContentView(web)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.setGeolocationEnabled(true)
        web.settings.mediaPlaybackRequiresUserGesture = false
        web.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(v: WebView, r: WebResourceRequest) = loader.shouldInterceptRequest(r.url)
            override fun shouldOverrideUrlLoading(v: WebView, r: WebResourceRequest): Boolean {
                if (r.url.host == host) return false
                startActivity(Intent(Intent.ACTION_VIEW, r.url)); return true
            }
        }
        web.webChromeClient = object : WebChromeClient() {
            override fun onGeolocationPermissionsShowPrompt(o: String, cb: GeolocationPermissions.Callback) {
                cb.invoke(o, checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED, false)
            }
        }
        web.loadUrl("https://$host/index.html")
    }

    @Deprecated("Use web history for back")
    override fun onBackPressed() { if (web.canGoBack()) web.goBack() else super.onBackPressed() }
}
