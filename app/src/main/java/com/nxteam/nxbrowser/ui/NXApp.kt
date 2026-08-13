package com.nxteam.nxbrowser.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import com.nxteam.nxbrowser.NXApplication
import com.nxteam.nxbrowser.browser.BrowserHost
import com.nxteam.nxbrowser.browser.WebViewFactory
import com.nxteam.nxbrowser.data.BookmarkEntry
import com.nxteam.nxbrowser.data.HistoryEntry
import com.nxteam.nxbrowser.privacy.ClearOptions
import com.nxteam.nxbrowser.privacy.DataCleaner
import com.nxteam.nxbrowser.ui.theme.NXTheme
import com.nxteam.nxbrowser.util.UrlUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val ROUTE_BROWSER = "browser"
private const val ROUTE_TABS = "tabs"
private const val ROUTE_HISTORY = "history"
private const val ROUTE_BOOKMARKS = "bookmarks"
private const val ROUTE_SETTINGS = "settings"
private const val ROUTE_EXTENSIONS = "extensions"

@Composable
fun NXApp(host: BrowserHost, pendingUrl: String?, onPendingUrlConsumed: () -> Unit) {
    val app = NXApplication.instance
    val tabManager = app.tabManager
    val historyDao = remember { app.database.historyDao() }
    val bookmarkDao = remember { app.database.bookmarkDao() }
    val scope = rememberCoroutineScope()

    val prefs by app.settings.flow.collectAsState()
    val recent by remember { historyDao.recent(6) }.collectAsState(initial = emptyList<HistoryEntry>())
    val historyAll by remember { historyDao.recent(500) }.collectAsState(initial = emptyList<HistoryEntry>())
    val topSites by remember { historyDao.topSites(8) }.collectAsState(initial = emptyList())
    val pinned by remember { bookmarkDao.pinned(8) }.collectAsState(initial = emptyList<BookmarkEntry>())
    val bookmarks by remember { bookmarkDao.all() }.collectAsState(initial = emptyList<BookmarkEntry>())

    var route by remember { mutableStateOf(ROUTE_BROWSER) }
    var editing by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf(TextFieldValue("")) }
    var suggestions by remember { mutableStateOf(emptyList<HistoryEntry>()) }
    var showMenu by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (tabManager.tabs.isEmpty()) {
            tabManager.newTab(incognito = false)
        }
    }

    LaunchedEffect(query.text, editing) {
        suggestions = if (editing && query.text.isNotBlank()) {
            withContext(Dispatchers.IO) { historyDao.search(query.text, 8) }
        } else {
            emptyList()
        }
    }

    LaunchedEffect(prefs) {
        tabManager.tabs.forEach { tab ->
            tab.webView?.let { WebViewFactory.applyPrefs(it, prefs) }
        }
    }

    LaunchedEffect(tabManager.incognitoTabs.size) {
        if (tabManager.incognitoTabs.isEmpty()) {
            DataCleaner.clearIncognitoSession(app)
        }
    }

    fun loadInCurrentTab(rawUrl: String) {
        val url = UrlUtils.resolve(rawUrl, prefs.searchEngine)
        if (url.isBlank()) return
        val tab = tabManager.currentTab ?: tabManager.newTab()
        tab.url = url
        tab.showHome = false
        tab.title = UrlUtils.prettyUrl(url)
        val webView = tab.webView
        if (webView != null) {
            if (prefs.doNotTrack) {
                webView.loadUrl(url, mapOf("DNT" to "1"))
            } else {
                webView.loadUrl(url)
            }
        }
        editing = false
        query = TextFieldValue("")
        route = ROUTE_BROWSER
    }

    LaunchedEffect(pendingUrl) {
        val url = pendingUrl
        if (!url.isNullOrBlank()) {
            tabManager.newTab(incognito = tabManager.incognitoMode)
            loadInCurrentTab(url)
            onPendingUrlConsumed()
        }
    }

    fun runClear(options: ClearOptions) {
        scope.launch {
            DataCleaner.clear(app, app.database, options)
        }
    }

    NXTheme(themeMode = prefs.themeMode, incognito = tabManager.incognitoMode) {
        val tab = tabManager.currentTab

        when (route) {
            ROUTE_TABS -> {
                TabSwitcherScreen(
                    tabManager = tabManager,
                    incognito = tabManager.incognitoMode,
                    onSelectTab = {
                        tabManager.selectTab(it)
                        route = ROUTE_BROWSER
                    },
                    onNewTab = {
                        tabManager.newTab(incognito = tabManager.incognitoMode)
                        route = ROUTE_BROWSER
                    },
                    onSwitchMode = { incognito ->
                        tabManager.switchIncognitoMode(incognito)
                        if (tabManager.visibleTabs(incognito).isEmpty()) {
                            tabManager.newTab(incognito = incognito)
                        } else {
                            tabManager.selectTab(tabManager.visibleTabs(incognito).last().id)
                        }
                    },
                    onClose = {
                        if (tabManager.currentTab == null) {
                            tabManager.newTab(incognito = tabManager.incognitoMode)
                        }
                        route = ROUTE_BROWSER
                    }
                )
                BackHandler { route = ROUTE_BROWSER }
            }

            ROUTE_HISTORY -> {
                HistoryScreen(
                    entries = historyAll,
                    onOpenUrl = { loadInCurrentTab(it) },
                    onDeleteEntry = { id ->
                        scope.launch(Dispatchers.IO) { historyDao.deleteById(id) }
                    },
                    onClear = { runClear(it) },
                    onBack = { route = ROUTE_BROWSER }
                )
                BackHandler { route = ROUTE_BROWSER }
            }

            ROUTE_BOOKMARKS -> {
                BookmarksScreen(
                    bookmarks = bookmarks,
                    onOpenUrl = { loadInCurrentTab(it) },
                    onTogglePin = { entry ->
                        scope.launch(Dispatchers.IO) {
                            bookmarkDao.setPinned(entry.url, !entry.pinned)
                        }
                    },
                    onDelete = { entry ->
                        scope.launch(Dispatchers.IO) { bookmarkDao.delete(entry) }
                    },
                    onBack = { route = ROUTE_BROWSER }
                )
                BackHandler { route = ROUTE_BROWSER }
            }

            ROUTE_SETTINGS -> {
                SettingsScreen(
                    prefs = prefs,
                    settings = app.settings,
                    onOpenClearData = { showClearDialog = true },
                    onBack = { route = ROUTE_BROWSER }
                )
                BackHandler { route = ROUTE_BROWSER }
            }

            ROUTE_EXTENSIONS -> {
                ExtensionsScreen(onBack = { route = ROUTE_BROWSER })
                BackHandler { route = ROUTE_BROWSER }
            }

            else -> {
                if (tab != null) {
                    BrowserScreen(
                        tab = tab,
                        host = host,
                        historyDao = historyDao,
                        scope = scope,
                        prefs = prefs,
                        tabCount = tabManager.visibleTabs(tabManager.incognitoMode).size,
                        editing = editing,
                        query = query,
                        suggestions = suggestions,
                        topSites = topSites,
                        pinned = pinned,
                        recent = recent,
                        onQueryChange = { query = it },
                        onStartEditing = {
                            query = selectAllValue(if (tab.showHome) "" else tab.url)
                            editing = true
                        },
                        onCancelEditing = {
                            editing = false
                            query = TextFieldValue("")
                        },
                        onSubmit = { loadInCurrentTab(query.text) },
                        onOpenUrl = { loadInCurrentTab(it) },
                        onReload = {
                            val webView = tab.webView
                            if (tab.isLoading) webView?.stopLoading() else webView?.reload()
                        },
                        onBack = { tab.webView?.goBack() },
                        onForward = { tab.webView?.goForward() },
                        onGoHome = {
                            tab.showHome = true
                            editing = false
                        },
                        onOpenTabs = { route = ROUTE_TABS },
                        onOpenMenu = { showMenu = true },
                        onOpenHistory = { route = ROUTE_HISTORY },
                        onOpenBookmarks = { route = ROUTE_BOOKMARKS },
                        onOpenExtensions = { route = ROUTE_EXTENSIONS },
                        onToggleIncognito = {
                            val target = !tabManager.incognitoMode
                            tabManager.switchIncognitoMode(target)
                            val existing = tabManager.visibleTabs(target)
                            if (existing.isEmpty()) {
                                tabManager.newTab(incognito = target)
                            } else {
                                tabManager.selectTab(existing.last().id)
                            }
                        },
                        onQuickClean = { showClearDialog = true },
                        onRemoveTile = { url ->
                            scope.launch(Dispatchers.IO) {
                                historyDao.deleteByHost(UrlUtils.host(url))
                            }
                        }
                    )
                }

                BackHandler {
                    val current = tabManager.currentTab
                    when {
                        editing -> {
                            editing = false
                            query = TextFieldValue("")
                        }
                        current != null && !current.showHome && current.canGoBack -> {
                            current.webView?.goBack()
                        }
                        current != null && !current.showHome -> {
                            current.showHome = true
                        }
                        tabManager.visibleTabs(tabManager.incognitoMode).size > 1 && current != null -> {
                            tabManager.closeTab(current.id)
                        }
                        else -> host.exitApp()
                    }
                }
            }
        }

        if (showMenu && tab != null) {
            MenuSheet(
                incognito = tab.incognito,
                desktopMode = tab.desktopMode,
                canShare = !tab.showHome && tab.url.isNotBlank(),
                onDismiss = { showMenu = false },
                onNewTab = {
                    tabManager.newTab(incognito = false)
                    showMenu = false
                    route = ROUTE_BROWSER
                },
                onNewIncognitoTab = {
                    tabManager.newTab(incognito = true)
                    showMenu = false
                    route = ROUTE_BROWSER
                },
                onAddBookmark = {
                    if (!tab.showHome && tab.url.isNotBlank()) {
                        val entry = BookmarkEntry(
                            url = tab.url,
                            title = tab.title,
                            host = UrlUtils.host(tab.url)
                        )
                        scope.launch(Dispatchers.IO) { bookmarkDao.insert(entry) }
                    }
                    showMenu = false
                },
                onOpenBookmarks = {
                    showMenu = false
                    route = ROUTE_BOOKMARKS
                },
                onOpenHistory = {
                    showMenu = false
                    route = ROUTE_HISTORY
                },
                onOpenExtensions = {
                    showMenu = false
                    route = ROUTE_EXTENSIONS
                },
                onShare = {
                    host.shareUrl(tab.url, tab.title)
                    showMenu = false
                },
                onToggleDesktop = { enabled ->
                    val webView = tab.webView
                    if (webView != null) {
                        WebViewFactory.applyDesktopMode(webView, tab, enabled)
                    } else {
                        tab.desktopMode = enabled
                    }
                },
                onOpenSettings = {
                    showMenu = false
                    route = ROUTE_SETTINGS
                },
                onClearData = {
                    showMenu = false
                    showClearDialog = true
                },
                onExit = {
                    showMenu = false
                    host.exitApp()
                }
            )
        }

        if (showClearDialog) {
            ClearDataDialog(
                onDismiss = { showClearDialog = false },
                onConfirm = {
                    runClear(it)
                    showClearDialog = false
                }
            )
        }
    }
}
