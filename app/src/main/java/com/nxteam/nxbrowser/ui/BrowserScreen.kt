package com.nxteam.nxbrowser.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nxteam.nxbrowser.R
import androidx.compose.ui.unit.sp
import com.nxteam.nxbrowser.browser.BrowserHost
import com.nxteam.nxbrowser.browser.BrowserTab
import com.nxteam.nxbrowser.data.BookmarkEntry
import com.nxteam.nxbrowser.data.HistoryDao
import com.nxteam.nxbrowser.data.HistoryEntry
import com.nxteam.nxbrowser.data.Prefs
import com.nxteam.nxbrowser.data.TopSite
import com.nxteam.nxbrowser.util.UrlUtils
import kotlinx.coroutines.CoroutineScope

@Composable
fun BrowserScreen(
    tab: BrowserTab,
    host: BrowserHost,
    historyDao: HistoryDao,
    scope: CoroutineScope,
    prefs: Prefs,
    tabCount: Int,
    editing: Boolean,
    query: androidx.compose.ui.text.input.TextFieldValue,
    suggestions: List<HistoryEntry>,
    topSites: List<TopSite>,
    pinned: List<BookmarkEntry>,
    recent: List<HistoryEntry>,
    onQueryChange: (androidx.compose.ui.text.input.TextFieldValue) -> Unit,
    onStartEditing: () -> Unit,
    onCancelEditing: () -> Unit,
    onSubmit: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onReload: () -> Unit,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onGoHome: () -> Unit,
    onOpenTabs: () -> Unit,
    onOpenMenu: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenExtensions: () -> Unit,
    onToggleIncognito: () -> Unit,
    onQuickClean: () -> Unit,
    onRemoveTile: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        UrlBar(
            editing = editing,
            query = query,
            displayUrl = if (tab.showHome) "" else tab.url,
            incognito = tab.incognito,
            progress = tab.progress,
            isLoading = tab.isLoading,
            onQueryChange = onQueryChange,
            onStartEditing = onStartEditing,
            onCancelEditing = onCancelEditing,
            onSubmit = onSubmit,
            onReload = onReload
        )

        Box(modifier = Modifier.weight(1f)) {
            if (tab.showHome) {
                HomeScreen(
                    incognito = tab.incognito,
                    searchEngineLabel = com.nxteam.nxbrowser.data.SearchEngines.byId(prefs.searchEngine).label,
                    topSites = topSites,
                    pinned = pinned,
                    recent = recent,
                    onSearchBarClick = onStartEditing,
                    onOpenUrl = onOpenUrl,
                    onRemoveTile = onRemoveTile,
                    onOpenIncognito = onToggleIncognito,
                    onOpenHistory = onOpenHistory,
                    onOpenBookmarks = onOpenBookmarks,
                    onOpenExtensions = onOpenExtensions,
                    onQuickClean = onQuickClean
                )
            } else {
                WebViewHost(
                    tab = tab,
                    host = host,
                    historyDao = historyDao,
                    scope = scope,
                    prefs = { prefs },
                    modifier = Modifier.fillMaxSize()
                )
            }

            if (editing) {
                SuggestionOverlay(
                    query = query.text,
                    suggestions = suggestions,
                    searchEngineLabel = com.nxteam.nxbrowser.data.SearchEngines.byId(prefs.searchEngine).label,
                    onOpenUrl = onOpenUrl,
                    onSubmit = onSubmit
                )
            }
        }

        BottomBar(
            canGoBack = tab.canGoBack,
            canGoForward = tab.canGoForward,
            tabCount = tabCount,
            incognito = tab.incognito,
            onBack = onBack,
            onForward = onForward,
            onGoHome = onGoHome,
            onOpenTabs = onOpenTabs,
            onOpenMenu = onOpenMenu
        )
    }
}

@Composable
private fun SuggestionOverlay(
    query: String,
    suggestions: List<HistoryEntry>,
    searchEngineLabel: String,
    onOpenUrl: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (query.isNotBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSubmit() }
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        text = query,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(R.string.search_with, searchEngineLabel),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(suggestions, key = { it.id }) { entry ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenUrl(entry.url) }
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SiteAvatar(host = entry.host, label = entry.title, size = 32.dp, corner = 10.dp)
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = entry.title.ifBlank { UrlUtils.prettyUrl(entry.url) },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = entry.url,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomBar(
    canGoBack: Boolean,
    canGoForward: Boolean,
    tabCount: Int,
    incognito: Boolean,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onGoHome: () -> Unit,
    onOpenTabs: () -> Unit,
    onOpenMenu: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack, enabled = canGoBack) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = if (canGoBack) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.outline
                    }
                )
            }
            IconButton(onClick = onForward, enabled = canGoForward) {
                Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = stringResource(R.string.forward),
                    tint = if (canGoForward) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.outline
                    }
                )
            }
            IconButton(onClick = onGoHome) {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = stringResource(R.string.home),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onOpenTabs() },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .border(
                            width = 2.dp,
                            color = if (incognito) {
                                MaterialTheme.colorScheme.secondary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            shape = RoundedCornerShape(7.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (tabCount > 99) "99" else tabCount.toString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (incognito) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
            IconButton(onClick = onOpenMenu) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = stringResource(R.string.menu),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
