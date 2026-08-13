package com.nxteam.nxbrowser.browser

import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.nxteam.nxbrowser.data.HistoryEntry
import com.nxteam.nxbrowser.data.HistoryDao
import com.nxteam.nxbrowser.util.UrlUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NXWebViewClient(
    private val tab: BrowserTab,
    private val host: BrowserHost,
    private val historyDao: HistoryDao,
    private val scope: CoroutineScope
) : WebViewClient() {

    private var lastRecordedUrl: String = ""

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val url = request.url.toString()
        val scheme = request.url.scheme?.lowercase() ?: return false
        if (scheme == "http" || scheme == "https" || scheme == "about" || scheme == "data" || scheme == "file") {
            return false
        }
        return host.launchExternalIntent(url)
    }

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        tab.isLoading = true
        tab.progress = 5
        tab.url = url
        tab.showHome = false
        tab.canGoBack = view.canGoBack()
        tab.canGoForward = view.canGoForward()
    }

    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        tab.isLoading = false
        tab.progress = 100
        tab.url = url
        tab.title = view.title ?: UrlUtils.prettyUrl(url)
        tab.canGoBack = view.canGoBack()
        tab.canGoForward = view.canGoForward()
        recordHistory(url, tab.title)
    }

    override fun doUpdateVisitedHistory(view: WebView, url: String, isReload: Boolean) {
        super.doUpdateVisitedHistory(view, url, isReload)
        tab.canGoBack = view.canGoBack()
        tab.canGoForward = view.canGoForward()
    }

    private fun recordHistory(url: String, title: String) {
        if (tab.incognito) return
        if (url.isBlank() || url == "about:blank") return
        if (url == lastRecordedUrl) return
        lastRecordedUrl = url
        val entry = HistoryEntry(
            url = url,
            title = title,
            host = UrlUtils.host(url),
            visitedAt = System.currentTimeMillis()
        )
        scope.launch(Dispatchers.IO) {
            historyDao.insert(entry)
        }
    }
}
