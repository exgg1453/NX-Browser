package com.nxteam.nxbrowser.browser

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.UUID

class TabManager {

    val tabs = mutableStateListOf<BrowserTab>()

    val groups = mutableStateListOf<TabGroup>()

    var currentTabId by mutableStateOf<String?>(null)
        private set

    var incognitoMode by mutableStateOf(false)
        private set

    val currentTab: BrowserTab?
        get() = tabs.firstOrNull { it.id == currentTabId }

    val normalTabs: List<BrowserTab>
        get() = tabs.filter { !it.incognito }

    val incognitoTabs: List<BrowserTab>
        get() = tabs.filter { it.incognito }

    fun visibleTabs(incognito: Boolean): List<BrowserTab> = tabs.filter { it.incognito == incognito }

    fun setIncognitoMode(value: Boolean) {
        incognitoMode = value
    }

    fun newTab(
        incognito: Boolean = incognitoMode,
        url: String? = null,
        groupId: String? = null,
        select: Boolean = true
    ): BrowserTab {
        val tab = BrowserTab(id = UUID.randomUUID().toString(), incognito = incognito)
        tab.groupId = groupId
        if (!url.isNullOrBlank()) {
            tab.url = url
            tab.showHome = false
        }
        tabs.add(tab)
        if (select) {
            currentTabId = tab.id
            incognitoMode = incognito
        }
        return tab
    }

    fun selectTab(id: String) {
        val tab = tabs.firstOrNull { it.id == id } ?: return
        currentTabId = tab.id
        incognitoMode = tab.incognito
    }

    fun closeTab(id: String) {
        val index = tabs.indexOfFirst { it.id == id }
        if (index < 0) return
        val tab = tabs[index]
        destroyWebView(tab)
        tabs.removeAt(index)

        if (currentTabId == id) {
            val sameMode = tabs.filter { it.incognito == tab.incognito }
            val next = sameMode.getOrNull(index.coerceAtMost(sameMode.size - 1))
                ?: sameMode.lastOrNull()
                ?: tabs.lastOrNull()
            currentTabId = next?.id
            if (next != null) incognitoMode = next.incognito
        }
        cleanupEmptyGroups()
    }

    fun closeTabs(ids: Collection<String>) {
        ids.toList().forEach { closeTab(it) }
    }

    fun closeAll(incognito: Boolean) {
        tabs.filter { it.incognito == incognito }.map { it.id }.forEach { closeTab(it) }
    }

    fun closeEverything() {
        tabs.toList().forEach { destroyWebView(it) }
        tabs.clear()
        groups.clear()
        currentTabId = null
    }

    fun createGroup(name: String, color: Long): TabGroup {
        val group = TabGroup(UUID.randomUUID().toString(), name, color)
        groups.add(group)
        return group
    }

    fun renameGroup(groupId: String, name: String) {
        groups.firstOrNull { it.id == groupId }?.name = name
    }

    fun recolorGroup(groupId: String, color: Long) {
        groups.firstOrNull { it.id == groupId }?.color = color
    }

    fun assignToGroup(tabIds: Collection<String>, groupId: String?) {
        tabIds.forEach { id ->
            tabs.firstOrNull { it.id == id }?.groupId = groupId
        }
        cleanupEmptyGroups()
    }

    fun closeGroup(groupId: String) {
        tabs.filter { it.groupId == groupId }.map { it.id }.forEach { closeTab(it) }
        groups.removeAll { it.id == groupId }
    }

    fun ungroup(groupId: String) {
        tabs.filter { it.groupId == groupId }.forEach { it.groupId = null }
        groups.removeAll { it.id == groupId }
    }

    fun groupOf(tab: BrowserTab): TabGroup? = groups.firstOrNull { it.id == tab.groupId }

    fun tabsInGroup(groupId: String, incognito: Boolean): List<BrowserTab> =
        tabs.filter { it.groupId == groupId && it.incognito == incognito }

    fun ungroupedTabs(incognito: Boolean): List<BrowserTab> =
        tabs.filter { it.groupId == null && it.incognito == incognito }

    fun groupsFor(incognito: Boolean): List<TabGroup> =
        groups.filter { group -> tabs.any { it.groupId == group.id && it.incognito == incognito } }

    fun ensureTab(context: Context) {
        if (tabs.none { it.incognito == incognitoMode }) {
            newTab(incognito = incognitoMode)
        } else if (currentTab == null) {
            currentTabId = tabs.first { it.incognito == incognitoMode }.id
        }
    }

    private fun cleanupEmptyGroups() {
        groups.removeAll { group -> tabs.none { it.groupId == group.id } }
    }

    private fun destroyWebView(tab: BrowserTab) {
        val webView = tab.webView ?: return
        tab.webView = null
        webView.stopLoading()
        webView.loadUrl("about:blank")
        webView.removeAllViews()
        (webView.parent as? android.view.ViewGroup)?.removeView(webView)
        webView.destroy()
    }
}
