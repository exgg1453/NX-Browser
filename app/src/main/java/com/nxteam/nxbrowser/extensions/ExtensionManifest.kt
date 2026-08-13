package com.nxteam.nxbrowser.extensions

import org.json.JSONArray
import org.json.JSONObject

data class ContentScript(
    val matches: List<MatchPattern>,
    val excludeMatches: List<MatchPattern>,
    val jsFiles: List<String>,
    val cssFiles: List<String>,
    val runAt: String,
    val allFrames: Boolean
) {
    fun appliesTo(url: String): Boolean {
        if (excludeMatches.any { it.matches(url) }) return false
        return matches.any { it.matches(url) }
    }
}

data class ExtensionManifest(
    val manifestVersion: Int,
    val name: String,
    val version: String,
    val description: String,
    val contentScripts: List<ContentScript>,
    val permissions: List<String>,
    val hostPermissions: List<String>
) {

    companion object {

        fun parse(raw: String): ExtensionManifest? {
            return try {
                val root = JSONObject(raw)
                val scripts = mutableListOf<ContentScript>()
                val scriptsArray = root.optJSONArray("content_scripts") ?: JSONArray()
                for (index in 0 until scriptsArray.length()) {
                    val entry = scriptsArray.getJSONObject(index)
                    scripts.add(
                        ContentScript(
                            matches = MatchPattern.parseAll(stringList(entry.optJSONArray("matches"))),
                            excludeMatches = MatchPattern.parseAll(
                                stringList(entry.optJSONArray("exclude_matches"))
                            ),
                            jsFiles = stringList(entry.optJSONArray("js")),
                            cssFiles = stringList(entry.optJSONArray("css")),
                            runAt = entry.optString("run_at", "document_idle"),
                            allFrames = entry.optBoolean("all_frames", false)
                        )
                    )
                }

                ExtensionManifest(
                    manifestVersion = root.optInt("manifest_version", 2),
                    name = root.optString("name", "Adsız eklenti"),
                    version = root.optString("version", "0"),
                    description = root.optString("description", ""),
                    contentScripts = scripts,
                    permissions = stringList(root.optJSONArray("permissions")),
                    hostPermissions = stringList(root.optJSONArray("host_permissions"))
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        private fun stringList(array: JSONArray?): List<String> {
            if (array == null) return emptyList()
            val result = mutableListOf<String>()
            for (index in 0 until array.length()) {
                val value = array.optString(index, "")
                if (value.isNotBlank()) result.add(value)
            }
            return result
        }
    }
}
