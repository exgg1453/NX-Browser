package com.nxteam.nxbrowser.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private val Context.nxDataStore: DataStore<Preferences> by preferencesDataStore(name = "nx_settings")

data class Prefs(
    val searchEngine: String = SearchEngines.DEFAULT_ID,
    val javaScriptEnabled: Boolean = true,
    val loadImages: Boolean = true,
    val blockPopups: Boolean = true,
    val doNotTrack: Boolean = true,
    val desktopModeDefault: Boolean = false,
    val clearOnExit: Boolean = false,
    val restoreTabs: Boolean = true,
    val themeMode: String = "system"
)

class SettingsStore(context: Context) {

    private val store = context.applicationContext.nxDataStore
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    val flow: StateFlow<Prefs> = store.data
        .map { p ->
            Prefs(
                searchEngine = p[KEY_SEARCH_ENGINE] ?: SearchEngines.DEFAULT_ID,
                javaScriptEnabled = p[KEY_JAVASCRIPT] ?: true,
                loadImages = p[KEY_IMAGES] ?: true,
                blockPopups = p[KEY_POPUPS] ?: true,
                doNotTrack = p[KEY_DNT] ?: true,
                desktopModeDefault = p[KEY_DESKTOP] ?: false,
                clearOnExit = p[KEY_CLEAR_ON_EXIT] ?: false,
                restoreTabs = p[KEY_RESTORE_TABS] ?: true,
                themeMode = p[KEY_THEME] ?: "system"
            )
        }
        .stateIn(scope, SharingStarted.Eagerly, Prefs())

    val current: Prefs
        get() = flow.value

    fun setSearchEngine(value: String) = put(KEY_SEARCH_ENGINE, value)

    fun setJavaScriptEnabled(value: Boolean) = put(KEY_JAVASCRIPT, value)

    fun setLoadImages(value: Boolean) = put(KEY_IMAGES, value)

    fun setBlockPopups(value: Boolean) = put(KEY_POPUPS, value)

    fun setDoNotTrack(value: Boolean) = put(KEY_DNT, value)

    fun setDesktopModeDefault(value: Boolean) = put(KEY_DESKTOP, value)

    fun setClearOnExit(value: Boolean) = put(KEY_CLEAR_ON_EXIT, value)

    fun setRestoreTabs(value: Boolean) = put(KEY_RESTORE_TABS, value)

    fun setThemeMode(value: String) = put(KEY_THEME, value)

    private fun <T> put(key: Preferences.Key<T>, value: T) {
        scope.launch {
            store.edit { it[key] = value }
        }
    }

    companion object {
        private val KEY_SEARCH_ENGINE = stringPreferencesKey("search_engine")
        private val KEY_JAVASCRIPT = booleanPreferencesKey("javascript")
        private val KEY_IMAGES = booleanPreferencesKey("images")
        private val KEY_POPUPS = booleanPreferencesKey("popups")
        private val KEY_DNT = booleanPreferencesKey("dnt")
        private val KEY_DESKTOP = booleanPreferencesKey("desktop")
        private val KEY_CLEAR_ON_EXIT = booleanPreferencesKey("clear_on_exit")
        private val KEY_RESTORE_TABS = booleanPreferencesKey("restore_tabs")
        private val KEY_THEME = stringPreferencesKey("theme")
    }
}
