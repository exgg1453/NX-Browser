package com.nxteam.nxbrowser

import android.Manifest
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.ViewGroup
import android.webkit.PermissionRequest
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.nxteam.nxbrowser.browser.BrowserHost
import com.nxteam.nxbrowser.privacy.ClearOptions
import com.nxteam.nxbrowser.privacy.DataCleaner
import com.nxteam.nxbrowser.ui.NXApp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity(), BrowserHost {

    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null
    private var fullscreenContainer: FrameLayout? = null
    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private var pendingPermissionRequest: PermissionRequest? = null

    private var pendingUrlState by mutableStateOf<String?>(null)

    private val fileChooserLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uris = WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data)
        filePathCallback?.onReceiveValue(uris)
        filePathCallback = null
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        val request = pendingPermissionRequest
        pendingPermissionRequest = null
        if (request != null) {
            if (granted.values.all { it }) {
                request.grant(request.resources)
            } else {
                request.deny()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        pendingUrlState = extractUrl(intent)

        setContent {
            NXApp(
                host = this,
                pendingUrl = pendingUrlState,
                onPendingUrlConsumed = { pendingUrlState = null }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val url = extractUrl(intent)
        if (!url.isNullOrBlank()) {
            pendingUrlState = url
        }
    }

    private fun extractUrl(intent: Intent?): String? {
        if (intent == null) return null
        return when (intent.action) {
            Intent.ACTION_VIEW -> intent.dataString
            Intent.ACTION_SEND -> intent.getStringExtra(Intent.EXTRA_TEXT)
            else -> null
        }
    }

    override fun openTabFromWeb(url: String, incognito: Boolean) {
        val app = application as NXApplication
        app.tabManager.newTab(incognito = incognito, url = url)
        pendingUrlState = null
    }

    override fun showCustomView(view: View, callback: WebChromeClient.CustomViewCallback) {
        if (customView != null) {
            callback.onCustomViewHidden()
            return
        }
        customView = view
        customViewCallback = callback

        val decor = window.decorView as FrameLayout
        val container = FrameLayout(this)
        container.setBackgroundColor(Color.BLACK)
        container.addView(
            view,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        decor.addView(
            container,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        fullscreenContainer = container

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, decor).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    override fun hideCustomView() {
        val container = fullscreenContainer ?: return
        val decor = window.decorView as FrameLayout
        container.removeAllViews()
        decor.removeView(container)
        fullscreenContainer = null
        customView = null
        customViewCallback?.onCustomViewHidden()
        customViewCallback = null

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, decor).show(WindowInsetsCompat.Type.systemBars())
    }

    override fun openFileChooser(
        params: WebChromeClient.FileChooserParams,
        callback: ValueCallback<Array<Uri>>
    ): Boolean {
        filePathCallback?.onReceiveValue(null)
        filePathCallback = callback
        return try {
            fileChooserLauncher.launch(params.createIntent())
            true
        } catch (e: ActivityNotFoundException) {
            filePathCallback = null
            false
        }
    }

    override fun handleDownload(
        url: String,
        userAgent: String,
        contentDisposition: String,
        mimeType: String,
        contentLength: Long
    ) {
        try {
            val fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
            val request = DownloadManager.Request(Uri.parse(url))
            request.setMimeType(mimeType)
            request.addRequestHeader("User-Agent", userAgent)
            request.setTitle(fileName)
            request.setDescription("NX Browser")
            request.setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            )
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            val manager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            manager.enqueue(request)
            Toast.makeText(this, "İndiriliyor: $fileName", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "İndirme başlatılamadı", Toast.LENGTH_SHORT).show()
        }
    }

    override fun handlePermissionRequest(request: PermissionRequest) {
        runOnUiThread {
            val needed = request.resources.mapNotNull { resource ->
                when (resource) {
                    PermissionRequest.RESOURCE_VIDEO_CAPTURE -> Manifest.permission.CAMERA
                    PermissionRequest.RESOURCE_AUDIO_CAPTURE -> Manifest.permission.RECORD_AUDIO
                    else -> null
                }
            }
            val missing = needed.filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }
            if (needed.isEmpty()) {
                request.deny()
            } else if (missing.isEmpty()) {
                request.grant(request.resources)
            } else {
                pendingPermissionRequest = request
                permissionLauncher.launch(missing.toTypedArray())
            }
        }
    }

    override fun shareUrl(url: String, title: String) {
        if (url.isBlank()) return
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, title)
        intent.putExtra(Intent.EXTRA_TEXT, url)
        startActivity(Intent.createChooser(intent, "Paylaş"))
    }

    override fun launchExternalIntent(url: String): Boolean {
        return try {
            val intent = if (url.startsWith("intent:")) {
                Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
            } else {
                Intent(Intent.ACTION_VIEW, Uri.parse(url))
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            true
        } catch (e: Exception) {
            Toast.makeText(this, "Bu bağlantıyı açacak uygulama yok", Toast.LENGTH_SHORT).show()
            true
        }
    }

    override fun exitApp() {
        finish()
    }

    override fun onDestroy() {
        val app = application as NXApplication
        if (isFinishing) {
            val clearOnExit = app.settings.current.clearOnExit
            app.tabManager.closeEverything()
            if (clearOnExit) {
                app.appScope.launch {
                    DataCleaner.clear(app, app.database, ClearOptions(bookmarks = false))
                }
            }
            app.appScope.launch {
                DataCleaner.clearIncognitoSession(app)
            }
        }
        super.onDestroy()
    }
}
