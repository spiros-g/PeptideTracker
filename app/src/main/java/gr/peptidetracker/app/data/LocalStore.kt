package gr.peptidetracker.app.data

import android.content.Context

data class TrackerEntry(val id: Long, val peptide: String, val amount: String, val note: String, val createdAt: Long)

class LocalStore(context: Context) {
    private val prefs = context.getSharedPreferences("peptide_tracker", Context.MODE_PRIVATE)
    fun entries(): List<TrackerEntry> = prefs.getStringSet("entries", emptySet()).orEmpty().mapNotNull { raw ->
        val p = raw.split("|", limit = 5); if (p.size == 5) TrackerEntry(p[0].toLong(), p[1], p[2], p[3], p[4].toLong()) else null
    }.sortedByDescending { it.createdAt }
    fun add(peptide: String, amount: String, note: String) {
        val now = System.currentTimeMillis(); val safe = { s: String -> s.replace("|", "/").replace("\n", " ") }
        val next = prefs.getStringSet("entries", emptySet()).orEmpty().toMutableSet()
        next += "$now|${safe(peptide)}|${safe(amount)}|${safe(note)}|$now"
        prefs.edit().putStringSet("entries", next).apply()
    }
    fun delete(id: Long) {
        val next = prefs.getStringSet("entries", emptySet()).orEmpty().filterNot { it.startsWith("$id|") }.toSet()
        prefs.edit().putStringSet("entries", next).apply()
    }
}
