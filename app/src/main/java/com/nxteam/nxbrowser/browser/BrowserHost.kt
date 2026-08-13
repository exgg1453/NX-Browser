package com.nxteam.nxbrowser.browser

import android.net.Uri
import android.view.View
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient

interface BrowserHost {

    fun openTabFromWeb(url: String, incognito: Boolean)

    fun showCustomView(view: View, callback: WebChromeClient.CustomViewCallback)

    fun hideCustomView()

    fun openFileChooser(
        params: WebChromeClient.FileChooserParams,
        callback: ValueCallback<Array<Uri>>
    ): Boolean

    fun handleDownload(
        url: String,
        userAgent: String,
        contentDisposition: String,
        mimeType: String,
        contentLength: Long
    )

    fun handlePermissionRequest(request: PermissionRequest)

    fun shareUrl(url: String, title: String)

    fun launchExternalIntent(url: String): Boolean

    fun exitApp()
}
