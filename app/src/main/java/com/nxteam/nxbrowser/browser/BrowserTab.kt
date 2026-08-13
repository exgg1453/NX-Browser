package com.nxteam.nxbrowser.browser

import android.graphics.Bitmap
import android.os.Bundle
import android.webkit.WebView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class BrowserTab(
    val id: String,
    val incognito: Boolean,
    val createdAt: Long = System.currentTimeMillis()
) {

    var title by mutableStateOf("Yeni sekme")

    var url by mutableStateOf("")

    var progress by mutableStateOf(0)

    var isLoading by mutableStateOf(false)

    var showHome by mutableStateOf(true)

    var canGoBack by mutableStateOf(false)

    var canGoForward by mutableStateOf(false)

    var groupId by mutableStateOf<String?>(null)

    var favicon by mutableStateOf<Bitmap?>(null)

    var desktopMode by mutableStateOf(false)

    var webView: WebView? = null

    var savedState: Bundle? = null

    val displayTitle: String
        get() = when {
            showHome -> if (incognito) "Gizli sekme" else "Yeni sekme"
            title.isNotBlank() -> title
            url.isNotBlank() -> url
            else -> "Yeni sekme"
        }
}
