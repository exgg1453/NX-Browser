package com.nxteam.nxbrowser

import android.app.Application
import android.os.Build
import android.webkit.WebView
import com.nxteam.nxbrowser.browser.TabManager
import com.nxteam.nxbrowser.data.AppDatabase
import com.nxteam.nxbrowser.data.SessionStore
import com.nxteam.nxbrowser.extensions.ExtensionManager
import com.nxteam.nxbrowser.data.SettingsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class NXApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var settings: SettingsStore
        private set

    lateinit var tabManager: TabManager
        private set

    lateinit var session: SessionStore
        private set

    lateinit var extensionManager: ExtensionManager
        private set

    val appScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.build(this)
        settings = SettingsStore(this)
        tabManager = TabManager()
        session = SessionStore(this)
        extensionManager = ExtensionManager(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val process = getProcessName()
            if (packageName != process) {
                WebView.setDataDirectorySuffix(process)
            }
        }
    }

    companion object {
        lateinit var instance: NXApplication
            private set
    }
}
