package com.nxteam.nxbrowser.extensions

import android.content.Context
import android.net.Uri
import com.nxteam.nxbrowser.R
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipInputStream

class ExtensionInstallResult(
    val success: Boolean,
    val message: String,
    val extension: InstalledExtension? = null
)

object ExtensionInstaller {

    private const val CRX_MAGIC = "Cr24"

    fun install(context: Context, uri: Uri, rootDir: File): ExtensionInstallResult {
        val stream = try {
            context.contentResolver.openInputStream(uri)
        } catch (e: Exception) {
            null
        }
        if (stream == null) {
            return ExtensionInstallResult(false, context.getString(R.string.err_file_open))
        }
        return installFromStream(context, stream, rootDir)
    }

    fun install(context: Context, file: File, rootDir: File): ExtensionInstallResult {
        return try {
            installFromStream(context, file.inputStream(), rootDir)
        } catch (e: Exception) {
            ExtensionInstallResult(
                false,
                context.getString(R.string.err_install_failed, e.message ?: "")
            )
        }
    }

    private fun installFromStream(
        context: Context,
        stream: InputStream,
        rootDir: File
    ): ExtensionInstallResult {
        val id = "ext_" + System.currentTimeMillis().toString(36)
        val target = File(rootDir, id)

        return try {
            stream.use { input ->
                val buffered = BufferedInputStream(input)
                val zipStream = skipCrxHeader(buffered)
                extract(zipStream, target)
            }

            val manifestFile = findManifest(target)
            if (manifestFile == null) {
                target.deleteRecursively()
                return ExtensionInstallResult(
                    false,
                    context.getString(R.string.err_manifest_missing)
                )
            }

            if (manifestFile.parentFile != target && manifestFile.parentFile != null) {
                flatten(manifestFile.parentFile!!, target)
            }

            val manifestText = File(target, "manifest.json").readText()
            val manifest = ExtensionManifest.parse(manifestText)
            if (manifest == null) {
                target.deleteRecursively()
                return ExtensionInstallResult(
                    false,
                    context.getString(R.string.err_manifest_unreadable)
                )
            }

            val extension = InstalledExtension(
                id = id,
                directory = target,
                manifest = manifest,
                enabled = true,
                installedAt = System.currentTimeMillis()
            )
            ExtensionInstallResult(
                true,
                context.getString(R.string.msg_installed, manifest.name),
                extension
            )
        } catch (e: Exception) {
            target.deleteRecursively()
            ExtensionInstallResult(
                false,
                context.getString(R.string.err_install_failed, e.message ?: "")
            )
        }
    }

    private fun skipCrxHeader(input: BufferedInputStream): InputStream {
        input.mark(16)
        val magic = ByteArray(4)
        val read = input.read(magic)
        if (read < 4 || String(magic) != CRX_MAGIC) {
            input.reset()
            return input
        }

        val data = DataInputStream(input)
        val version = readLittleEndianInt(data)
        if (version == 2) {
            val publicKeyLength = readLittleEndianInt(data)
            val signatureLength = readLittleEndianInt(data)
            skipFully(input, publicKeyLength.toLong() + signatureLength.toLong())
        } else {
            val headerLength = readLittleEndianInt(data)
            skipFully(input, headerLength.toLong())
        }
        return input
    }

    private fun readLittleEndianInt(stream: DataInputStream): Int {
        val bytes = ByteArray(4)
        stream.readFully(bytes)
        return (bytes[0].toInt() and 0xFF) or
            ((bytes[1].toInt() and 0xFF) shl 8) or
            ((bytes[2].toInt() and 0xFF) shl 16) or
            ((bytes[3].toInt() and 0xFF) shl 24)
    }

    private fun skipFully(stream: InputStream, count: Long) {
        var remaining = count
        while (remaining > 0) {
            val skipped = stream.skip(remaining)
            if (skipped <= 0) {
                if (stream.read() < 0) return
                remaining -= 1
            } else {
                remaining -= skipped
            }
        }
    }

    private fun extract(stream: InputStream, target: File) {
        target.mkdirs()
        val canonicalTarget = target.canonicalPath
        val zip = ZipInputStream(stream)
        var entry = zip.nextEntry
        val buffer = ByteArray(8192)

        while (entry != null) {
            val outFile = File(target, entry.name)
            if (!outFile.canonicalPath.startsWith(canonicalTarget)) {
                throw SecurityException("Invalid archive path: " + entry.name)
            }
            if (entry.isDirectory) {
                outFile.mkdirs()
            } else {
                outFile.parentFile?.mkdirs()
                FileOutputStream(outFile).use { output ->
                    while (true) {
                        val count = zip.read(buffer)
                        if (count <= 0) break
                        output.write(buffer, 0, count)
                    }
                }
            }
            zip.closeEntry()
            entry = zip.nextEntry
        }
    }

    private fun findManifest(root: File): File? {
        val direct = File(root, "manifest.json")
        if (direct.exists()) return direct
        val children = root.listFiles() ?: return null
        for (child in children) {
            if (child.isDirectory) {
                val nested = File(child, "manifest.json")
                if (nested.exists()) return nested
            }
        }
        return null
    }

    private fun flatten(source: File, target: File) {
        val children = source.listFiles() ?: return
        for (child in children) {
            val destination = File(target, child.name)
            child.renameTo(destination)
        }
        source.deleteRecursively()
    }
}
