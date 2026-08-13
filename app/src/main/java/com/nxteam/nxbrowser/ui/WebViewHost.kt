package com.nxteam.nxbrowser.ui

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.nxteam.nxbrowser.browser.BrowserHost
import com.nxteam.nxbrowser.browser.BrowserTab
import com.nxteam.nxbrowser.browser.WebViewFactory
import com.nxteam.nxbrowser.data.HistoryDao
import com.nxteam.nxbrowser.data.Prefs
import kotlinx.coroutines.CoroutineScope

@Composable
fun WebViewHost(
    tab: BrowserTab,
    host: BrowserHost,
    historyDao: HistoryDao,
    scope: CoroutineScope,
    prefs: () -> Prefs,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context -> FrameLayout(context) },
        update = { container ->
            val webView = tab.webView ?: WebViewFactory.create(
                context = container.context,
                tab = tab,
                host = host,
                historyDao = historyDao,
                scope = scope,
                prefs = prefs
            ).also { created ->
                tab.webView = created
                val state = tab.savedState
                if (state != null) {
                    created.restoreState(state)
                } else if (tab.url.isNotBlank()) {
                    created.loadUrl(tab.url)
                }
            }

            if (container.childCount == 0 || container.getChildAt(0) !== webView) {
                container.removeAllViews()
                (webView.parent as? ViewGroup)?.removeView(webView)
                container.addView(
                    webView,
                    FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                )
            }
        }
    )
}
