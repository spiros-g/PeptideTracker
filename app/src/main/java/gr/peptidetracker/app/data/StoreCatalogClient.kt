package gr.peptidetracker.app.data

import java.util.Locale
import kotlin.math.absoluteValue

object StoreCatalogClient {
    private val stockVials = listOf(
        "https://bioart.niaid.nih.gov/api/bioarts/962/files/803427",
        "https://bioart.niaid.nih.gov/api/bioarts/962/files/803397",
        "https://bioart.niaid.nih.gov/api/bioarts/966/files/803653",
        "https://bioart.niaid.nih.gov/api/bioarts/966/files/803662",
        "https://bioart.niaid.nih.gov/api/bioarts/966/files/803667",
        "https://bioart.niaid.nih.gov/api/bioarts/966/files/803677",
        "https://bioart.niaid.nih.gov/api/bioarts/966/files/803682",
        "https://bioart.niaid.nih.gov/api/bioarts/966/files/803687"
    )

    private val curatedIndex = mapOf(
        "retatrutide" to stockVials[0],
        "tirzepatide" to stockVials[1],
        "semaglutide" to stockVials[2],
        "bpc157" to stockVials[3],
        "tb500" to stockVials[4],
        "ghkcu" to stockVials[5],
        "tesamorelin" to stockVials[6],
        "ipamorelin" to stockVials[7],
        "cjc1295" to stockVials[0],
        "sermorelin" to stockVials[1],
        "aod9604" to stockVials[2],
        "motsc" to stockVials[3],
        "ss31" to stockVials[4],
        "kpv" to stockVials[5],
        "epitalon" to stockVials[6]
    )

    suspend fun loadImageIndex(): Map<String, String> = curatedIndex

    fun resolveImage(index: Map<String, String>, query: String): String? {
        if (stockVials.isEmpty()) return null

        val normalized = normalize(query)
        if (normalized.isBlank()) return stockVials.first()

        index[normalized]?.let { return it }

        index.entries.firstOrNull { (key, _) ->
            key.contains(normalized) || normalized.contains(key)
        }?.value?.let { return it }

        return stockVials[normalized.hashCode().absoluteValue % stockVials.size]
    }

    private fun normalize(value: String): String =
        value.lowercase(Locale.ROOT).filter { it.isLetterOrDigit() }
}
