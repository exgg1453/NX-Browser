package com.nxteam.nxbrowser.browser

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.webkit.ProfileStore
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import com.nxteam.nxbrowser.data.HistoryDao
import com.nxteam.nxbrowser.data.Prefs
import com.nxteam.nxbrowser.util.UrlUtils
import com.nxteam.nxbrowser.util.UserAgents
import kotlinx.coroutines.CoroutineScope

object WebViewFactory {

    const val INCOGNITO_PROFILE = "nx_incognito"

    @SuppressLint("SetJavaScriptEnabled")
    fun create(
        context: Context,
        tab: BrowserTab,
        host: BrowserHost,
        historyDao: HistoryDao,
        scope: CoroutineScope,
        prefs: () -> Prefs
    ): WebView {
        val webView = WebView(context)
        webView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        if (tab.incognito) {
            applyIncognitoProfile(webView)
        }

        val settings = webView.settings
        val current = prefs()

        settings.javaScriptEnabled = current.javaScriptEnabled
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.loadsImagesAutomatically = current.loadImages
        settings.blockNetworkImage = !current.loadImages
        settings.javaScriptCanOpenWindowsAutomatically = !current.blockPopups
        settings.setSupportMultipleWindows(true)
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        settings.mediaPlaybackRequiresUserGesture = true
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        settings.cacheMode = if (tab.incognito) {
            WebSettings.LOAD_NO_CACHE
        } else {
            WebSettings.LOAD_DEFAULT
        }
        settings.setGeolocationEnabled(false)
        settings.allowFileAccess = false
        settings.allowContentAccess = false
        settings.textZoom = 100

        if (tab.incognito) {
            settings.saveFormData = false
        }

        val desktop = tab.desktopMode || current.desktopModeDefault
        settings.userAgentString = if (desktop) UserAgents.DESKTOP else UserAgents.mobile(settings.userAgentString)
        tab.desktopMode = desktop

        applyDarkening(webView, current)

        webView.isScrollbarFadingEnabled = true
        webView.isVerticalScrollBarEnabled = true
        webView.isHorizontalScrollBarEnabled = false
        webView.overScrollMode = WebView.OVER_SCROLL_NEVER

        webView.webViewClient = NXWebViewClient(tab, host, historyDao, scope)
        webView.webChromeClient = NXWebChromeClient(tab, host) { prefs().blockPopups }

        webView.setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
            host.handleDownload(url, userAgent, contentDisposition, mimeType, contentLength)
        }

        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(webView, !tab.incognito)

        return webView
    }

    fun applyDesktopMode(webView: WebView, tab: BrowserTab, enabled: Boolean) {
        tab.desktopMode = enabled
        val settings = webView.settings
        settings.userAgentString = if (enabled) UserAgents.DESKTOP else null
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.setSupportZoom(true)
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        val url = tab.url
        if (url.isNotBlank() && url != "about:blank") {
            webView.loadUrl(url)
        } else {
            webView.reload()
        }
    }

    fun applyPrefs(webView: WebView, prefs: Prefs) {
        val settings = webView.settings
        settings.javaScriptEnabled = prefs.javaScriptEnabled
        settings.loadsImagesAutomatically = prefs.loadImages
        settings.blockNetworkImage = !prefs.loadImages
        settings.javaScriptCanOpenWindowsAutomatically = !prefs.blockPopups
        applyDarkening(webView, prefs)
    }

    fun applyDarkening(webView: WebView, prefs: Prefs) {
        if (!WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) return
        val dark = prefs.darkWebContent &&
            UrlUtils.isDarkTheme(webView.context, prefs.themeMode)
        try {
            WebSettingsCompat.setAlgorithmicDarkeningAllowed(webView.settings, dark)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun applyIncognitoProfile(webView: WebView) {
        if (!WebViewFeature.isFeatureSupported(WebViewFeature.MULTI_PROFILE)) return
        try {
            ProfileStore.getInstance().getOrCreateProfile(INCOGNITO_PROFILE)
            WebViewCompat.setProfile(webView, INCOGNITO_PROFILE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteIncognitoProfile() {
        if (!WebViewFeature.isFeatureSupported(WebViewFeature.MULTI_PROFILE)) return
        try {
            ProfileStore.getInstance().deleteProfile(INCOGNITO_PROFILE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
