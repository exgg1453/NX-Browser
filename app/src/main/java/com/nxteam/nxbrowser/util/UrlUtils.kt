package com.nxteam.nxbrowser.util

import android.net.Uri
import com.nxteam.nxbrowser.data.SearchEngines
import java.net.URLEncoder
import java.util.Locale

object UrlUtils {

    private val schemeRegex = Regex("^[a-zA-Z][a-zA-Z0-9+.-]*://.*")
    private val hostRegex = Regex("^[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)+(:[0-9]+)?(/.*)?$")
    private val ipRegex = Regex("^[0-9]{1,3}(\\.[0-9]{1,3}){3}(:[0-9]+)?(/.*)?$")

    fun resolve(input: String, searchEngineId: String): String {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return ""

        if (trimmed.startsWith("about:") || trimmed.startsWith("file:") ||
            trimmed.startsWith("data:") || trimmed.startsWith("javascript:")
        ) {
            return trimmed
        }

        if (schemeRegex.matches(trimmed)) return trimmed
        if (trimmed.startsWith("localhost")) return "http://$trimmed"
        if (ipRegex.matches(trimmed)) return "http://$trimmed"

        if (!trimmed.contains(' ') && hostRegex.matches(trimmed)) {
            return "https://$trimmed"
        }

        val engine = SearchEngines.byId(searchEngineId)
        return engine.queryUrl + URLEncoder.encode(trimmed, "UTF-8")
    }

    fun host(url: String): String {
        return try {
            val h = Uri.parse(url).host ?: return ""
            if (h.startsWith("www.")) h.substring(4) else h
        } catch (e: Exception) {
            ""
        }
    }

    fun prettyUrl(url: String): String {
        if (url.isEmpty()) return ""
        val host = host(url)
        return if (host.isEmpty()) url else host
    }

    fun initial(value: String): String {
        val cleaned = value.trim()
        if (cleaned.isEmpty()) return "?"
        return cleaned.substring(0, 1).uppercase(Locale.getDefault())
    }

    fun colorForHost(host: String): Long {
        var hash = 0
        for (c in host) {
            hash = c.code + ((hash shl 5) - hash)
        }
        val palette = listOf(
            0xFF5B8CFFL, 0xFF7C5CFFL, 0xFF00B3A4L, 0xFFE0703AL,
            0xFFD9455FL, 0xFF3AA655L, 0xFF9B51E0L, 0xFF2D9CDBL
        )
        val index = ((hash % palette.size) + palette.size) % palette.size
        return palette[index]
    }

    fun isSecure(url: String): Boolean = url.startsWith("https://")
}
