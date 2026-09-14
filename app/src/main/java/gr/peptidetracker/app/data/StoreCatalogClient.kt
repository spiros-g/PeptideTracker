package gr.peptidetracker.app.data

import java.util.Locale

object StoreCatalogClient {
    private const val genericPeptideVial =
        "asset://vials/peptide_vial.webp"

    private val curatedIndex = mapOf(
        "retatrutide" to genericPeptideVial,
        "tirzepatide" to genericPeptideVial,
        "semaglutide" to genericPeptideVial,
        "bpc157" to genericPeptideVial,
        "tb500" to genericPeptideVial,
        "ghkcu" to genericPeptideVial,
        "tesamorelin" to genericPeptideVial,
        "ipamorelin" to genericPeptideVial,
        "cjc1295" to genericPeptideVial,
        "sermorelin" to genericPeptideVial,
        "aod9604" to genericPeptideVial,
        "motsc" to genericPeptideVial,
        "ss31" to genericPeptideVial,
        "kpv" to genericPeptideVial,
        "epitalon" to genericPeptideVial
    )

    suspend fun loadImageIndex(): Map<String, String> = curatedIndex

    fun resolveImage(index: Map<String, String>, query: String): String {
        val normalized = normalize(query)

        index[normalized]?.let { return it }

        index.entries.firstOrNull { (key, _) ->
            key.contains(normalized) || normalized.contains(key)
        }?.value?.let { return it }

        return genericPeptideVial
    }

    private fun normalize(value: String): String =
        value.lowercase(Locale.ROOT).filter { it.isLetterOrDigit() }
}
