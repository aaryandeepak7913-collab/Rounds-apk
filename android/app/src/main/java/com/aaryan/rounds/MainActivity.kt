package com.aaryan.rounds

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.webkit.*
import androidx.webkit.WebViewAssetLoader

class MainActivity : Activity() {
    private lateinit var web: WebView

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        window.statusBarColor = Color.parseColor("#0D0E0F")
        window.navigationBarColor = Color.parseColor("#0D0E0F")
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
            (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED))
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.POST_NOTIFICATIONS), 1)
        else setUpReminder()

        val loader = WebViewAssetLoader.Builder().addPathHandler("/", WebViewAssetLoader.PathHandler { p ->
            try {
                val f = if (p.isEmpty() || p.endsWith("/")) p + "index.html" else p
                val ext = MimeTypeMap.getFileExtensionFromUrl(f)
                WebResourceResponse(MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "text/html", "utf-8", assets.open(f))
            } catch (e: Exception) { null }
        }).build()

        web = WebView(this)
        web.setBackgroundColor(Color.parseColor("#0D0E0F"))
        web.keepScreenOn = true // the round timer shouldn't let the screen sleep
        setContentView(web)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.mediaPlaybackRequiresUserGesture = false // lets the bell sound play
        web.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(v: WebView, r: WebResourceRequest) = loader.shouldInterceptRequest(r.url)
        }
        web.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(r: PermissionRequest) { runOnUiThread { r.grant(r.resources) } }
        }
        // The page tells us whether today already has a logged workout, so the 9 PM reminder can stay quiet.
        web.addJavascriptInterface(object {
            @JavascriptInterface fun setLogged(date: String) { getSharedPreferences("r", 0).edit().putString("logged", date).apply() }
        }, "RoundsNative")
        web.loadUrl("https://appassets.androidplatform.net/index.html")
    }

    private fun setUpReminder() {
        Reminder.schedule(this)
        val prefs = getSharedPreferences("r", 0)
        // Android 14 needs the "alarms and reminders" switch for an exact 9:00 PM. Ask once.
        if (Build.VERSION.SDK_INT >= 31 && !getSystemService(AlarmManager::class.java).canScheduleExactAlarms() && !prefs.getBoolean("askedExact", false)) {
            prefs.edit().putBoolean("askedExact", true).apply()
            try { startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName"))) } catch (e: Exception) {}
        }
    }

    override fun onRequestPermissionsResult(rc: Int, p: Array<out String>, g: IntArray) {
        super.onRequestPermissionsResult(rc, p, g)
        setUpReminder()
    }

    @Deprecated("Back goes through page history first")
    override fun onBackPressed() { if (web.canGoBack()) web.goBack() else moveTaskToBack(true) }
}
