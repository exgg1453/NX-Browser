package com.nxteam.nxbrowser.browser

import android.graphics.Bitmap
import android.net.Uri
import android.os.Message
import android.view.View
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView

class NXWebChromeClient(
    private val tab: BrowserTab,
    private val host: BrowserHost,
    private val blockPopups: () -> Boolean
) : WebChromeClient() {

    override fun onProgressChanged(view: WebView, newProgress: Int) {
        tab.progress = newProgress
        tab.isLoading = newProgress < 100
    }

    override fun onReceivedTitle(view: WebView, title: String?) {
        if (!title.isNullOrBlank()) tab.title = title
    }

    override fun onReceivedIcon(view: WebView, icon: Bitmap?) {
        tab.favicon = icon
    }

    override fun onCreateWindow(
        view: WebView,
        isDialog: Boolean,
        isUserGesture: Boolean,
        resultMsg: Message
    ): Boolean {
        if (blockPopups() && !isUserGesture) return false
        val transport = resultMsg.obj as? WebView.WebViewTransport ?: return false
        val temp = WebView(view.context)
        temp.webViewClient = object : android.webkit.WebViewClient() {
            override fun shouldOverrideUrlLoading(
                v: WebView,
                request: android.webkit.WebResourceRequest
            ): Boolean {
                host.openTabFromWeb(request.url.toString(), tab.incognito)
                temp.destroy()
                return true
            }
        }
        transport.webView = temp
        resultMsg.sendToTarget()
        return true
    }

    override fun onShowCustomView(view: View, callback: CustomViewCallback) {
        host.showCustomView(view, callback)
    }

    override fun onHideCustomView() {
        host.hideCustomView()
    }

    override fun onShowFileChooser(
        webView: WebView,
        filePathCallback: ValueCallback<Array<Uri>>,
        fileChooserParams: FileChooserParams
    ): Boolean {
        return host.openFileChooser(fileChooserParams, filePathCallback)
    }

    override fun onPermissionRequest(request: PermissionRequest) {
        host.handlePermissionRequest(request)
    }
}
