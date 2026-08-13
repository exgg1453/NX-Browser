package com.nxteam.nxbrowser.extensions

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.File

class InstalledExtension(
    val id: String,
    val directory: File,
    val manifest: ExtensionManifest,
    enabled: Boolean,
    val installedAt: Long
) {

    var enabled by mutableStateOf(enabled)

    val name: String
        get() = manifest.name

    val version: String
        get() = manifest.version

    val description: String
        get() = manifest.description

    fun readAsset(relativePath: String): String? {
        return try {
            val target = File(directory, relativePath).canonicalFile
            if (!target.path.startsWith(directory.canonicalFile.path)) return null
            if (!target.exists()) return null
            target.readText()
        } catch (e: Exception) {
            null
        }
    }
}
