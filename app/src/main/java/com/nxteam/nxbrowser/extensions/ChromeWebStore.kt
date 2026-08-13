package com.nxteam.nxbrowser.extensions

import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

object ChromeWebStore {

    private val idRegex = Regex("[a-p]{32}")

    private const val PRODUCT_VERSION = "126.0.6478.126"

    fun extractId(input: String): String? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return null
        return idRegex.find(trimmed)?.value
    }

    fun downloadUrl(id: String): String {
        return "https://clients2.google.com/service/update2/crx" +
            "?response=redirect" +
            "&os=linux" +
            "&arch=x64" +
            "&os_arch=x86_64" +
            "&nacl_arch=x86-64" +
            "&prod=chromiumcrx" +
            "&prodchannel=unknown" +
            "&prodversion=" + PRODUCT_VERSION +
            "&acceptformat=crx2,crx3" +
            "&x=id%3D" + id + "%26uc"
    }

    fun download(id: String, cacheDir: File): File? {
        var currentUrl = downloadUrl(id)
        var redirects = 0

        while (redirects < 6) {
            val connection = URL(currentUrl).openConnection() as HttpURLConnection
            connection.instanceFollowRedirects = false
            connection.connectTimeout = 20000
            connection.readTimeout = 40000
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) Chrome/126.0.0.0")
            connection.connect()

            val code = connection.responseCode
            if (code == HttpURLConnection.HTTP_MOVED_PERM ||
                code == HttpURLConnection.HTTP_MOVED_TEMP ||
                code == HttpURLConnection.HTTP_SEE_OTHER ||
                code == 307 ||
                code == 308
            ) {
                val location = connection.getHeaderField("Location")
                connection.disconnect()
                if (location.isNullOrBlank()) return null
                currentUrl = if (location.startsWith("http")) {
                    location
                } else {
                    URL(URL(currentUrl), location).toString()
                }
                redirects += 1
                continue
            }

            if (code != HttpURLConnection.HTTP_OK) {
                connection.disconnect()
                return null
            }

            val target = File(cacheDir, "store_$id.crx")
            connection.inputStream.use { input ->
                FileOutputStream(target).use { output ->
                    val buffer = ByteArray(16384)
                    while (true) {
                        val count = input.read(buffer)
                        if (count <= 0) break
                        output.write(buffer, 0, count)
                    }
                }
            }
            connection.disconnect()
            return if (target.length() > 0) target else null
        }
        return null
    }
}
