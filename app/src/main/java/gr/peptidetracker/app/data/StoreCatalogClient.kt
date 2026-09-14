package gr.peptidetracker.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

object StoreCatalogClient {
    private const val catalogUrl =
        "https://www.peptidiastore.gr/wp-json/wc/store/v1/products?per_page=100"

    @Volatile
    private var cachedImages: Map<String, String>? = null

    suspend fun loadImageIndex(): Map<String, String> {
        cachedImages?.let { return it }

        return withContext(Dispatchers.IO) {
            runCatching {
                val connection = (URL(catalogUrl).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 7_000
                    readTimeout = 10_000
                    requestMethod = "GET"
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("User-Agent", "PeptideTrackerGR/2.0")
                }

                try {
                    if (connection.responseCode !in 200..299) return@runCatching emptyMap()

                    val payload = connection.inputStream.bufferedReader().use { it.readText() }
                    val products = JSONArray(payload)
                    buildMap {
                        for (index in 0 until products.length()) {
                            val product = products.optJSONObject(index) ?: continue
                            val images = product.optJSONArray("images") ?: continue
                            val image = images.optJSONObject(0)?.optString("src").orEmpty()
                            if (image.isBlank()) continue

                            val name = product.optString("name")
                            val slug = product.optString("slug")
                            if (name.isNotBlank()) put(normalize(name), image)
                            if (slug.isNotBlank()) put(normalize(slug), image)
                        }
                    }
                } finally {
                    connection.disconnect()
                }
            }.getOrElse { emptyMap() }.also { result ->
                if (result.isNotEmpty()) cachedImages = result
            }
        }
    }

    fun resolveImage(index: Map<String, String>, query: String): String? {
        val normalized = normalize(query)
        index[normalized]?.let { return it }

        return index.entries.firstOrNull { (key, _) ->
            key.contains(normalized) || normalized.contains(key)
        }?.value
    }

    private fun normalize(value: String): String =
        value.lowercase(Locale.ROOT).filter { it.isLetterOrDigit() }
}
