package com.nxteam.nxbrowser.extensions

import android.net.Uri

class MatchPattern private constructor(
    private val allUrls: Boolean,
    private val scheme: String,
    private val host: String,
    private val path: String
) {

    fun matches(url: String): Boolean {
        if (allUrls) {
            return url.startsWith("http://") || url.startsWith("https://") || url.startsWith("file://")
        }
        val uri = try {
            Uri.parse(url)
        } catch (e: Exception) {
            return false
        }
        val urlScheme = uri.scheme ?: return false
        val urlHost = uri.host ?: return false
        val urlPath = uri.path ?: "/"

        if (scheme != "*" && scheme != urlScheme) return false
        if (scheme == "*" && urlScheme != "http" && urlScheme != "https") return false
        if (!hostMatches(urlHost)) return false
        return pathMatches(urlPath)
    }

    private fun hostMatches(urlHost: String): Boolean {
        if (host == "*") return true
        if (host.startsWith("*.")) {
            val suffix = host.substring(2)
            return urlHost == suffix || urlHost.endsWith(".$suffix")
        }
        return host.equals(urlHost, ignoreCase = true)
    }

    private fun pathMatches(urlPath: String): Boolean {
        if (path == "/*" || path == "*") return true
        val regex = buildString {
            append('^')
            for (character in path) {
                when (character) {
                    '*' -> append(".*")
                    '?', '+', '.', '(', ')', '[', ']', '{', '}', '^', '$', '|', '\\' -> {
                        append('\\')
                        append(character)
                    }
                    else -> append(character)
                }
            }
            append('$')
        }
        return try {
            Regex(regex).matches(urlPath)
        } catch (e: Exception) {
            false
        }
    }

    companion object {

        fun parse(pattern: String): MatchPattern? {
            if (pattern == "<all_urls>") {
                return MatchPattern(true, "*", "*", "/*")
            }
            val schemeEnd = pattern.indexOf("://")
            if (schemeEnd <= 0) return null
            val scheme = pattern.substring(0, schemeEnd)
            val rest = pattern.substring(schemeEnd + 3)
            val pathStart = rest.indexOf('/')
            if (pathStart < 0) return null
            val host = rest.substring(0, pathStart)
            val path = rest.substring(pathStart)
            if (host.isEmpty()) return null
            return MatchPattern(false, scheme, host, path)
        }

        fun parseAll(patterns: List<String>): List<MatchPattern> =
            patterns.mapNotNull { parse(it) }
    }
}
