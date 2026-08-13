package com.nxteam.nxbrowser.privacy

import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewDatabase
import com.nxteam.nxbrowser.browser.WebViewFactory
import com.nxteam.nxbrowser.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class ClearOptions(
    val history: Boolean = true,
    val cookies: Boolean = true,
    val cache: Boolean = true,
    val siteStorage: Boolean = true,
    val formData: Boolean = true,
    val bookmarks: Boolean = false,
    val sinceMillis: Long? = null
)

object DataCleaner {

    suspend fun clear(context: Context, database: AppDatabase, options: ClearOptions) {
        if (options.history) {
            val since = options.sinceMillis
            if (since == null) {
                database.historyDao().clearAll()
            } else {
                database.historyDao().clearSince(since)
            }
        }

        if (options.bookmarks) {
            database.bookmarkDao().clearAll()
        }

        withContext(Dispatchers.Main) {
            if (options.cookies) {
                val cookieManager = CookieManager.getInstance()
                cookieManager.removeAllCookies(null)
                cookieManager.removeSessionCookies(null)
                cookieManager.flush()
            }

            if (options.siteStorage) {
                WebStorage.getInstance().deleteAllData()
            }

            if (options.formData) {
                val db = WebViewDatabase.getInstance(context)
                db.clearFormData()
                db.clearHttpAuthUsernamePassword()
            }

            if (options.cache) {
                val temp = WebView(context)
                temp.clearCache(true)
                temp.clearHistory()
                temp.clearFormData()
                temp.destroy()
            }
        }

        if (options.cache) {
            withContext(Dispatchers.IO) {
                deleteRecursively(context.cacheDir)
                context.getDir("webview", Context.MODE_PRIVATE).let { deleteRecursively(it) }
            }
        }
    }

    suspend fun clearIncognitoSession(context: Context) {
        withContext(Dispatchers.Main) {
            WebViewFactory.deleteIncognitoProfile()
            val cookieManager = CookieManager.getInstance()
            cookieManager.removeSessionCookies(null)
            cookieManager.flush()
        }
    }

    private fun deleteRecursively(file: File?) {
        if (file == null || !file.exists()) return
        if (file.isDirectory) {
            file.listFiles()?.forEach { deleteRecursively(it) }
        }
        file.delete()
    }
}
