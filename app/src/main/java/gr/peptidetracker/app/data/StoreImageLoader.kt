package gr.peptidetracker.app.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

object StoreImageLoader {
    private const val maxDiskFiles = 80
    private const val connectTimeoutMs = 7_000
    private const val readTimeoutMs = 12_000

    private val memoryCache = object : LruCache<String, Bitmap>(24 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.allocationByteCount / 1024
        }
    }

    suspend fun load(context: Context, imageUrl: String): Bitmap? =
        withContext(Dispatchers.IO) {
            memoryCache.get(imageUrl)?.let { return@withContext it }

            val cacheDir = File(context.cacheDir, "peptidiastore-vials").apply { mkdirs() }
            val diskFile = File(cacheDir, sha256(imageUrl) + ".img")

            if (diskFile.isFile && diskFile.length() > 0L) {
                BitmapFactory.decodeFile(diskFile.absolutePath)?.let { bitmap ->
                    memoryCache.put(imageUrl, bitmap)
                    diskFile.setLastModified(System.currentTimeMillis())
                    return@withContext bitmap
                }
                diskFile.delete()
            }

            val bytes = download(imageUrl) ?: return@withContext null
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                ?: return@withContext null

            runCatching {
                diskFile.writeBytes(bytes)
                prune(cacheDir)
            }

            memoryCache.put(imageUrl, bitmap)
            bitmap
        }

    private fun download(imageUrl: String): ByteArray? {
        val connection = (URL(imageUrl).openConnection() as HttpURLConnection).apply {
            connectTimeout = connectTimeoutMs
            readTimeout = readTimeoutMs
            requestMethod = "GET"
            instanceFollowRedirects = true
            useCaches = true
            setRequestProperty("Accept", "image/*")
            setRequestProperty("User-Agent", "PeptideTrackerGR/2.0")
        }

        return try {
            if (connection.responseCode !in 200..299) return null
            connection.inputStream.use { it.readBytes() }
        } catch (_: Exception) {
            null
        } finally {
            connection.disconnect()
        }
    }

    private fun prune(cacheDir: File) {
        val files = cacheDir.listFiles()
            ?.filter { it.isFile }
            ?.sortedByDescending { it.lastModified() }
            .orEmpty()

        files.drop(maxDiskFiles).forEach { it.delete() }
    }

    private fun sha256(value: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
            .joinToString("") { byte -> "%02x".format(byte) }
}
