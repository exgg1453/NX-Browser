package com.nxteam.nxbrowser.extensions

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class ExtensionManager(private val context: Context) {

    private val rootDir: File = File(context.filesDir, "extensions").apply { mkdirs() }
    private val indexFile: File = File(rootDir, "index.json")

    val extensions = mutableStateListOf<InstalledExtension>()

    init {
        load()
    }

    fun install(uri: Uri): ExtensionInstallResult {
        val result = ExtensionInstaller.install(context, uri, rootDir)
        val extension = result.extension
        if (result.success && extension != null) {
            extensions.add(extension)
            persist()
        }
        return result
    }

    fun setEnabled(id: String, enabled: Boolean) {
        extensions.firstOrNull { it.id == id }?.enabled = enabled
        persist()
    }

    fun remove(id: String) {
        val extension = extensions.firstOrNull { it.id == id } ?: return
        extensions.remove(extension)
        extension.directory.deleteRecursively()
        persist()
    }

    fun scriptsFor(url: String, runAt: String): List<String> {
        if (url.isBlank()) return emptyList()
        if (!url.startsWith("http://") && !url.startsWith("https://")) return emptyList()

        val payloads = mutableListOf<String>()
        for (extension in extensions) {
            if (!extension.enabled) continue
            for (script in extension.manifest.contentScripts) {
                if (normalizeRunAt(script.runAt) != runAt) continue
                if (!script.appliesTo(url)) continue

                for (cssPath in script.cssFiles) {
                    val css = extension.readAsset(cssPath) ?: continue
                    payloads.add(buildCssPayload(css))
                }
                for (jsPath in script.jsFiles) {
                    val js = extension.readAsset(jsPath) ?: continue
                    payloads.add(buildJsPayload(extension, js))
                }
            }
        }
        return payloads
    }

    private fun normalizeRunAt(value: String): String = when (value) {
        "document_start" -> "document_start"
        "document_end" -> "document_end"
        else -> "document_idle"
    }

    private fun buildCssPayload(css: String): String {
        val encoded = JSONObject.quote(css)
        return """
            (function() {
                var style = document.createElement('style');
                style.type = 'text/css';
                style.setAttribute('data-nx-extension', '1');
                style.appendChild(document.createTextNode($encoded));
                (document.head || document.documentElement).appendChild(style);
            })();
        """.trimIndent()
    }

    private fun buildJsPayload(extension: InstalledExtension, js: String): String {
        val extensionId = JSONObject.quote(extension.id)
        val extensionName = JSONObject.quote(extension.name)
        val extensionVersion = JSONObject.quote(extension.version)
        return """
            (function() {
                if (typeof window.__nxExtensionBridge === 'undefined') {
                    window.__nxExtensionBridge = {};
                }
                var runtime = {
                    id: $extensionId,
                    getManifest: function() {
                        return { name: $extensionName, version: $extensionVersion };
                    },
                    getURL: function(path) { return path; },
                    sendMessage: function() {},
                    onMessage: { addListener: function() {}, removeListener: function() {} }
                };
                var storageArea = {
                    get: function(keys, callback) { if (callback) callback({}); },
                    set: function(items, callback) { if (callback) callback(); },
                    remove: function(keys, callback) { if (callback) callback(); },
                    clear: function(callback) { if (callback) callback(); }
                };
                var api = {
                    runtime: runtime,
                    storage: { local: storageArea, sync: storageArea },
                    i18n: { getMessage: function() { return ''; } },
                    extension: { getURL: runtime.getURL }
                };
                if (typeof window.chrome === 'undefined') { window.chrome = api; }
                else {
                    if (!window.chrome.runtime) { window.chrome.runtime = runtime; }
                    if (!window.chrome.storage) { window.chrome.storage = api.storage; }
                }
                if (typeof window.browser === 'undefined') { window.browser = window.chrome; }
                try {
                    $js
                } catch (error) {
                    console.error('NX extension error', $extensionName, error);
                }
            })();
        """.trimIndent()
    }

    private fun load() {
        if (!indexFile.exists()) return
        try {
            val array = JSONArray(indexFile.readText())
            for (index in 0 until array.length()) {
                val entry = array.getJSONObject(index)
                val id = entry.getString("id")
                val directory = File(rootDir, id)
                val manifestFile = File(directory, "manifest.json")
                if (!manifestFile.exists()) continue
                val manifest = ExtensionManifest.parse(manifestFile.readText()) ?: continue
                extensions.add(
                    InstalledExtension(
                        id = id,
                        directory = directory,
                        manifest = manifest,
                        enabled = entry.optBoolean("enabled", true),
                        installedAt = entry.optLong("installedAt", 0L)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun persist() {
        try {
            val array = JSONArray()
            for (extension in extensions) {
                val entry = JSONObject()
                entry.put("id", extension.id)
                entry.put("enabled", extension.enabled)
                entry.put("installedAt", extension.installedAt)
                array.put(entry)
            }
            indexFile.writeText(array.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
