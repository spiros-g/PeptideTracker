package gr.peptidetracker.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal

data class TrackerEntry(
    val id: Long,
    val peptide: String,
    val amount: String,
    val note: String,
    val createdAt: Long,
    val amountValue: Double? = null,
    val unit: String = "",
    val site: String = "",
    val inventoryId: Long? = null
)

data class ProgressEntry(
    val id: Long,
    val weight: Double,
    val waist: Double?,
    val note: String,
    val createdAt: Long
)

data class InventoryEntry(
    val id: Long,
    val peptide: String,
    val vialMg: Double,
    val quantity: Int,
    val batch: String,
    val remainingMg: Double? = null,
    val active: Boolean = false,
    val openedAt: Long? = null,
    val diluentMl: Double? = null,
    val syringeUnitsPerMl: Int = 100
) {
    val effectiveRemainingMg: Double
        get() = remainingMg ?: vialMg

    val isReconstituted: Boolean
        get() = diluentMl != null && diluentMl > 0

    val concentrationMgPerMl: Double?
        get() = diluentMl?.takeIf { it > 0 }?.let { vialMg / it }

    val mcgPerSyringeUnit: Double?
        get() = concentrationMgPerMl?.let { it * 1000.0 / syringeUnitsPerMl.coerceAtLeast(1) }
}

data class ReminderEntry(
    val id: Long,
    val peptide: String,
    val note: String,
    val scheduledAt: Long,
    val repeatDays: Int = 0,
    val enabled: Boolean = true,
    val createdAt: Long
)

data class SavedCalculationEntry(
    val id: Long,
    val peptide: String,
    val vialMg: Double,
    val diluentMl: Double,
    val targetAmount: Double,
    val targetUnit: String,
    val syringeUnitsPerMl: Int,
    val syringeCapacity: Int,
    val syringeUnits: Double,
    val createdAt: Long
)

class LocalStore(context: Context) {
    private val prefs = context.getSharedPreferences("peptide_tracker", Context.MODE_PRIVATE)

    private fun safe(value: String) = value.replace("|", "/").replace("\n", " ")
    private fun formatNumber(value: Double): String =
        BigDecimal.valueOf(value).stripTrailingZeros().toPlainString()

    fun darkMode() = prefs.getBoolean("dark", true)
    fun setDarkMode(value: Boolean) = prefs.edit().putBoolean("dark", value).apply()

    fun defaultSyringeUnitsPerMl(): Int =
        prefs.getInt("default_syringe_units_per_ml", 100)
            .takeIf { it == 40 || it == 100 } ?: 100

    fun setDefaultSyringeUnitsPerMl(value: Int) {
        require(value == 40 || value == 100)
        prefs.edit().putInt("default_syringe_units_per_ml", value).apply()
    }

    fun onboardingComplete() = prefs.getBoolean("onboarding_complete", false)
    fun setOnboardingComplete(value: Boolean) =
        prefs.edit().putBoolean("onboarding_complete", value).apply()

    fun favorites() = prefs.getStringSet("favorites", emptySet()).orEmpty()
    fun toggleFavorite(id: String) {
        val next = favorites().toMutableSet()
        if (!next.add(id)) next.remove(id)
        prefs.edit().putStringSet("favorites", next).apply()
    }

    fun entries(): List<TrackerEntry> {
        val json = prefs.getString(KEY_ENTRIES_V2, null)
        if (json != null) {
            return runCatching { decodeEntries(JSONArray(json)) }
                .getOrDefault(emptyList())
                .sortedByDescending { it.createdAt }
        }

        return prefs.getStringSet("entries", emptySet()).orEmpty().mapNotNull {
            val p = it.split("|", limit = 5)
            if (p.size == 5) {
                TrackerEntry(
                    id = p[0].toLongOrNull() ?: return@mapNotNull null,
                    peptide = p[1],
                    amount = p[2],
                    note = p[3],
                    createdAt = p[4].toLongOrNull() ?: return@mapNotNull null
                )
            } else {
                null
            }
        }.sortedByDescending { it.createdAt }
    }

    fun add(peptide: String, amount: String, note: String) {
        val now = System.currentTimeMillis()
        val next = entries().toMutableList()
        next += TrackerEntry(
            id = now,
            peptide = safe(peptide),
            amount = safe(amount),
            note = safe(note),
            createdAt = now
        )
        saveEntries(next)
    }

    fun addEntry(
        peptide: String,
        amountValue: Double,
        unit: String,
        note: String,
        site: String,
        createdAt: Long,
        inventoryId: Long? = null,
        subtractFromInventory: Boolean = false
    ) {
        require(peptide.isNotBlank())
        require(amountValue > 0)
        val normalizedUnit = unit.trim().ifBlank { "mcg" }
        val id = uniqueId()
        val next = entries().toMutableList()
        next += TrackerEntry(
            id = id,
            peptide = safe(peptide.trim()),
            amount = formatNumber(amountValue) + " " + normalizedUnit,
            note = safe(note.trim()),
            createdAt = createdAt,
            amountValue = amountValue,
            unit = normalizedUnit,
            site = safe(site.trim()),
            inventoryId = inventoryId
        )
        saveEntries(next)

        if (subtractFromInventory && inventoryId != null) {
            amountToMg(amountValue, normalizedUnit)?.let { consumeInventory(inventoryId, it) }
        }
    }

    fun updateEntry(
        id: Long,
        peptide: String,
        amountValue: Double,
        unit: String,
        note: String,
        site: String,
        createdAt: Long
    ) {
        require(peptide.isNotBlank())
        require(amountValue > 0)
        val normalizedUnit = unit.trim().ifBlank { "mcg" }
        val next = entries().map { entry ->
            if (entry.id != id) {
                entry
            } else {
                entry.copy(
                    peptide = safe(peptide.trim()),
                    amount = formatNumber(amountValue) + " " + normalizedUnit,
                    note = safe(note.trim()),
                    createdAt = createdAt,
                    amountValue = amountValue,
                    unit = normalizedUnit,
                    site = safe(site.trim())
                )
            }
        }
        saveEntries(next)
    }

    fun deleteEntry(id: Long) {
        saveEntries(entries().filterNot { it.id == id })
    }

    fun progress(): List<ProgressEntry> {
        val json = prefs.getString(KEY_PROGRESS_V2, null)
        if (json != null) {
            return runCatching { decodeProgress(JSONArray(json)) }
                .getOrDefault(emptyList())
                .sortedByDescending { it.createdAt }
        }

        return prefs.getStringSet("progress", emptySet()).orEmpty().mapNotNull {
            val p = it.split("|", limit = 5)
            if (p.size == 5) {
                ProgressEntry(
                    id = p[0].toLongOrNull() ?: return@mapNotNull null,
                    weight = p[1].toDoubleOrNull() ?: return@mapNotNull null,
                    waist = p[2].takeIf(String::isNotBlank)?.toDoubleOrNull(),
                    note = p[3],
                    createdAt = p[4].toLongOrNull() ?: return@mapNotNull null
                )
            } else {
                null
            }
        }.sortedByDescending { it.createdAt }
    }

    fun addProgress(weight: Double, waist: Double?, note: String) {
        require(weight > 0)
        val now = System.currentTimeMillis()
        val next = progress().toMutableList()
        next += ProgressEntry(now, weight, waist, safe(note), now)
        saveProgress(next)
    }

    fun updateProgress(id: Long, weight: Double, waist: Double?, note: String, createdAt: Long) {
        require(weight > 0)
        saveProgress(
            progress().map {
                if (it.id == id) it.copy(
                    weight = weight,
                    waist = waist,
                    note = safe(note),
                    createdAt = createdAt
                ) else it
            }
        )
    }

    fun deleteProgress(id: Long) {
        saveProgress(progress().filterNot { it.id == id })
    }

    fun inventory(): List<InventoryEntry> {
        val json = prefs.getString(KEY_INVENTORY_V2, null)
        if (json != null) {
            return runCatching { decodeInventory(JSONArray(json)) }
                .getOrDefault(emptyList())
                .sortedWith(compareByDescending<InventoryEntry> { it.active }.thenBy { it.peptide })
        }

        return prefs.getStringSet("inventory", emptySet()).orEmpty().mapNotNull {
            val p = it.split("|", limit = 5)
            if (p.size >= 5) {
                InventoryEntry(
                    id = p[0].toLongOrNull() ?: return@mapNotNull null,
                    peptide = p[1],
                    vialMg = p[2].toDoubleOrNull() ?: return@mapNotNull null,
                    quantity = p[3].toIntOrNull() ?: return@mapNotNull null,
                    batch = p[4]
                )
            } else {
                null
            }
        }.sortedBy { it.peptide }
    }

    fun addInventory(
        peptide: String,
        vial: Double,
        quantity: Int,
        batch: String,
        diluentMl: Double? = null,
        syringeUnitsPerMl: Int = 100
    ) {
        require(peptide.isNotBlank() && vial > 0 && quantity > 0)
        require(diluentMl == null || diluentMl > 0)
        require(syringeUnitsPerMl == 40 || syringeUnitsPerMl == 100)
        val next = inventory().toMutableList()
        next += InventoryEntry(
            id = uniqueId(),
            peptide = safe(peptide.trim()),
            vialMg = vial,
            quantity = quantity,
            batch = safe(batch.trim()),
            remainingMg = vial,
            diluentMl = diluentMl,
            syringeUnitsPerMl = syringeUnitsPerMl
        )
        saveInventory(next)
    }

    fun updateInventory(
        id: Long,
        peptide: String,
        vialMg: Double,
        quantity: Int,
        batch: String,
        remainingMg: Double?,
        diluentMl: Double? = null,
        syringeUnitsPerMl: Int = 100
    ) {
        require(peptide.isNotBlank() && vialMg > 0 && quantity >= 0)
        require(diluentMl == null || diluentMl > 0)
        require(syringeUnitsPerMl == 40 || syringeUnitsPerMl == 100)
        saveInventory(
            inventory().map {
                if (it.id == id) {
                    it.copy(
                        peptide = safe(peptide.trim()),
                        vialMg = vialMg,
                        quantity = quantity,
                        batch = safe(batch.trim()),
                        remainingMg = remainingMg?.coerceIn(0.0, vialMg),
                        diluentMl = diluentMl,
                        syringeUnitsPerMl = syringeUnitsPerMl
                    )
                } else {
                    it
                }
            }
        )
    }

    fun activateInventory(id: Long) {
        val rows = inventory()
        val target = rows.firstOrNull { it.id == id } ?: return
        val now = System.currentTimeMillis()
        saveInventory(
            rows.map {
                when {
                    it.id == id -> it.copy(
                        active = it.quantity > 0,
                        remainingMg = it.remainingMg ?: it.vialMg,
                        openedAt = it.openedAt ?: now
                    )
                    it.peptide.equals(target.peptide, ignoreCase = true) -> it.copy(active = false)
                    else -> it
                }
            }
        )
    }

    fun deleteInventory(id: Long) {
        saveInventory(inventory().filterNot { it.id == id })
    }

    fun consumeInventory(id: Long, amountMg: Double): Boolean {
        if (amountMg <= 0) return false
        var consumed = false

        val next = inventory().map { row ->
            if (row.id != id || row.quantity <= 0) return@map row

            val remaining = row.effectiveRemainingMg
            when {
                amountMg < remaining -> {
                    consumed = true
                    row.copy(remainingMg = remaining - amountMg)
                }
                amountMg == remaining -> {
                    consumed = true
                    if (row.quantity > 1) {
                        row.copy(
                            quantity = row.quantity - 1,
                            remainingMg = row.vialMg,
                            active = true,
                            openedAt = System.currentTimeMillis()
                        )
                    } else {
                        row.copy(quantity = 0, remainingMg = 0.0, active = false)
                    }
                }
                else -> row
            }
        }

        if (consumed) saveInventory(next)
        return consumed
    }

    fun reminders(): List<ReminderEntry> {
        val json = prefs.getString(KEY_REMINDERS_V1, null) ?: return emptyList()
        return runCatching { decodeReminders(JSONArray(json)) }
            .getOrDefault(emptyList())
            .sortedWith(compareByDescending<ReminderEntry> { it.enabled }.thenBy { it.scheduledAt })
    }

    fun addReminder(
        peptide: String,
        note: String,
        scheduledAt: Long,
        repeatDays: Int = 0
    ): ReminderEntry {
        require(peptide.isNotBlank())
        require(scheduledAt > 0)
        require(repeatDays >= 0)
        val now = System.currentTimeMillis()
        val row = ReminderEntry(
            id = uniqueId(),
            peptide = safe(peptide.trim()),
            note = safe(note.trim()),
            scheduledAt = scheduledAt,
            repeatDays = repeatDays,
            enabled = true,
            createdAt = now
        )
        saveReminders(reminders() + row)
        return row
    }

    fun updateReminder(
        id: Long,
        peptide: String,
        note: String,
        scheduledAt: Long,
        repeatDays: Int,
        enabled: Boolean
    ): ReminderEntry? {
        require(peptide.isNotBlank())
        require(scheduledAt > 0)
        require(repeatDays >= 0)
        var updated: ReminderEntry? = null
        val rows = reminders().map { row ->
            if (row.id == id) {
                row.copy(
                    peptide = safe(peptide.trim()),
                    note = safe(note.trim()),
                    scheduledAt = scheduledAt,
                    repeatDays = repeatDays,
                    enabled = enabled
                ).also { updated = it }
            } else {
                row
            }
        }
        saveReminders(rows)
        return updated
    }

    fun setReminderEnabled(id: Long, enabled: Boolean): ReminderEntry? {
        var updated: ReminderEntry? = null
        val rows = reminders().map { row ->
            if (row.id == id) row.copy(enabled = enabled).also { updated = it } else row
        }
        saveReminders(rows)
        return updated
    }

    fun deleteReminder(id: Long) {
        saveReminders(reminders().filterNot { it.id == id })
    }

    fun advanceReminderAfterFire(id: Long, now: Long = System.currentTimeMillis()): ReminderEntry? {
        var nextRow: ReminderEntry? = null
        val rows = reminders().map { row ->
            if (row.id != id || !row.enabled) {
                row
            } else if (row.repeatDays <= 0) {
                row.copy(enabled = false).also { nextRow = it }
            } else {
                val stepMs = row.repeatDays.toLong() * DAY_MS
                var next = row.scheduledAt + stepMs
                while (next <= now) next += stepMs
                row.copy(scheduledAt = next, enabled = true).also { nextRow = it }
            }
        }
        saveReminders(rows)
        return nextRow
    }

    fun savedCalculations(): List<SavedCalculationEntry> {
        val json = prefs.getString(KEY_CALCULATIONS_V1, null) ?: return emptyList()
        return runCatching { decodeCalculations(JSONArray(json)) }
            .getOrDefault(emptyList())
            .sortedByDescending { it.createdAt }
    }

    fun addSavedCalculation(
        peptide: String,
        vialMg: Double,
        diluentMl: Double,
        targetAmount: Double,
        targetUnit: String,
        syringeUnitsPerMl: Int,
        syringeCapacity: Int,
        syringeUnits: Double
    ): SavedCalculationEntry {
        require(vialMg > 0 && diluentMl > 0 && targetAmount > 0 && syringeUnits > 0)
        require(targetUnit == "mg" || targetUnit == "mcg")
        require(syringeUnitsPerMl == 40 || syringeUnitsPerMl == 100)
        require(syringeCapacity > 0)
        val now = System.currentTimeMillis()
        val row = SavedCalculationEntry(
            id = uniqueId(),
            peptide = safe(peptide.trim()),
            vialMg = vialMg,
            diluentMl = diluentMl,
            targetAmount = targetAmount,
            targetUnit = targetUnit,
            syringeUnitsPerMl = syringeUnitsPerMl,
            syringeCapacity = syringeCapacity,
            syringeUnits = syringeUnits,
            createdAt = now
        )
        saveCalculations((listOf(row) + savedCalculations()).distinctBy { it.id }.take(12))
        return row
    }

    fun deleteSavedCalculation(id: Long) {
        saveCalculations(savedCalculations().filterNot { it.id == id })
    }

    fun clearSavedCalculations() {
        saveCalculations(emptyList())
    }

    fun exportJson(): String {
        val root = JSONObject()
            .put("schema", 6)
            .put("generatedAt", System.currentTimeMillis())
            .put("darkMode", darkMode())
            .put("defaultSyringeUnitsPerMl", defaultSyringeUnitsPerMl())
            .put("onboardingComplete", onboardingComplete())
            .put("favorites", JSONArray(favorites().toList()))
            .put("entries", encodeEntries(entries()))
            .put("inventory", encodeInventory(inventory()))
            .put("progress", encodeProgress(progress()))
            .put("reminders", encodeReminders(reminders()))
            .put("savedCalculations", encodeCalculations(savedCalculations()))
        return root.toString(2)
    }

    fun restoreJson(raw: String): Boolean = runCatching {
        val root = JSONObject(raw)
        val schema = root.optInt("schema", 1)
        require(schema in 1..6)

        val decodedEntries = decodeEntries(root.optJSONArray("entries") ?: JSONArray())
        val decodedInventory = decodeInventory(root.optJSONArray("inventory") ?: JSONArray())
        val decodedProgress = decodeProgress(root.optJSONArray("progress") ?: JSONArray())
        val decodedReminders = decodeReminders(root.optJSONArray("reminders") ?: JSONArray())
        val decodedCalculations = decodeCalculations(root.optJSONArray("savedCalculations") ?: JSONArray())

        val favoriteSet = mutableSetOf<String>()
        val favoritesJson = root.optJSONArray("favorites") ?: JSONArray()
        for (i in 0 until favoritesJson.length()) {
            favoritesJson.optString(i).takeIf { it.isNotBlank() }?.let(favoriteSet::add)
        }

        prefs.edit()
            .putString(KEY_ENTRIES_V2, encodeEntries(decodedEntries).toString())
            .putString(KEY_INVENTORY_V2, encodeInventory(decodedInventory).toString())
            .putString(KEY_PROGRESS_V2, encodeProgress(decodedProgress).toString())
            .putString(KEY_REMINDERS_V1, encodeReminders(decodedReminders).toString())
            .putString(KEY_CALCULATIONS_V1, encodeCalculations(decodedCalculations).toString())
            .putStringSet("favorites", favoriteSet)
            .putBoolean("dark", root.optBoolean("darkMode", darkMode()))
            .putInt(
                "default_syringe_units_per_ml",
                root.optInt("defaultSyringeUnitsPerMl", defaultSyringeUnitsPerMl())
                    .takeIf { it == 40 || it == 100 } ?: 100
            )
            .putBoolean("onboarding_complete", root.optBoolean("onboardingComplete", true))
            .apply()
        true
    }.getOrDefault(false)

    fun exportEntriesCsv(): String {
        val rows = mutableListOf("id,peptide,amount,unit,note,site,created_at")
        entries().sortedBy { it.createdAt }.forEach { entry ->
            rows += listOf(
                entry.id.toString(),
                csv(entry.peptide),
                csv(entry.amountValue?.let(::formatNumber) ?: entry.amount),
                csv(entry.unit),
                csv(entry.note),
                csv(entry.site),
                entry.createdAt.toString()
            ).joinToString(",")
        }
        return rows.joinToString("\n")
    }

    private fun amountToMg(value: Double, unit: String): Double? = when (unit.lowercase()) {
        "mg" -> value
        "mcg", "μg", "ug" -> value / 1000.0
        else -> null
    }

    private fun uniqueId(): Long {
        var id = System.currentTimeMillis()
        val used = entries().map { it.id }.toSet() +
            progress().map { it.id }.toSet() +
            inventory().map { it.id }.toSet() +
            reminders().map { it.id }.toSet() +
            savedCalculations().map { it.id }.toSet()
        while (id in used) id++
        return id
    }

    private fun saveEntries(rows: List<TrackerEntry>) {
        prefs.edit().putString(KEY_ENTRIES_V2, encodeEntries(rows).toString()).apply()
    }

    private fun saveProgress(rows: List<ProgressEntry>) {
        prefs.edit().putString(KEY_PROGRESS_V2, encodeProgress(rows).toString()).apply()
    }

    private fun saveInventory(rows: List<InventoryEntry>) {
        prefs.edit().putString(KEY_INVENTORY_V2, encodeInventory(rows).toString()).apply()
    }

    private fun saveReminders(rows: List<ReminderEntry>) {
        prefs.edit().putString(KEY_REMINDERS_V1, encodeReminders(rows).toString()).apply()
    }

    private fun saveCalculations(rows: List<SavedCalculationEntry>) {
        prefs.edit().putString(KEY_CALCULATIONS_V1, encodeCalculations(rows).toString()).apply()
    }

    private fun encodeEntries(rows: List<TrackerEntry>) = JSONArray().apply {
        rows.forEach { row ->
            put(
                JSONObject()
                    .put("id", row.id)
                    .put("peptide", row.peptide)
                    .put("amount", row.amount)
                    .put("note", row.note)
                    .put("createdAt", row.createdAt)
                    .put("amountValue", row.amountValue ?: JSONObject.NULL)
                    .put("unit", row.unit)
                    .put("site", row.site)
                    .put("inventoryId", row.inventoryId ?: JSONObject.NULL)
            )
        }
    }

    private fun decodeEntries(array: JSONArray): List<TrackerEntry> = buildList {
        for (i in 0 until array.length()) {
            val o = array.optJSONObject(i) ?: continue
            add(
                TrackerEntry(
                    id = o.optLong("id"),
                    peptide = o.optString("peptide"),
                    amount = o.optString("amount"),
                    note = o.optString("note"),
                    createdAt = o.optLong("createdAt"),
                    amountValue = if (o.has("amountValue") && !o.isNull("amountValue")) o.optDouble("amountValue") else null,
                    unit = o.optString("unit"),
                    site = o.optString("site"),
                    inventoryId = if (o.has("inventoryId") && !o.isNull("inventoryId")) o.optLong("inventoryId") else null
                )
            )
        }
    }

    private fun encodeProgress(rows: List<ProgressEntry>) = JSONArray().apply {
        rows.forEach { row ->
            put(
                JSONObject()
                    .put("id", row.id)
                    .put("weight", row.weight)
                    .put("waist", row.waist ?: JSONObject.NULL)
                    .put("note", row.note)
                    .put("createdAt", row.createdAt)
            )
        }
    }

    private fun decodeProgress(array: JSONArray): List<ProgressEntry> = buildList {
        for (i in 0 until array.length()) {
            val o = array.optJSONObject(i) ?: continue
            add(
                ProgressEntry(
                    id = o.optLong("id"),
                    weight = o.optDouble("weight"),
                    waist = if (o.has("waist") && !o.isNull("waist")) o.optDouble("waist") else null,
                    note = o.optString("note"),
                    createdAt = o.optLong("createdAt")
                )
            )
        }
    }

    private fun encodeInventory(rows: List<InventoryEntry>) = JSONArray().apply {
        rows.forEach { row ->
            put(
                JSONObject()
                    .put("id", row.id)
                    .put("peptide", row.peptide)
                    .put("vialMg", row.vialMg)
                    .put("quantity", row.quantity)
                    .put("batch", row.batch)
                    .put("remainingMg", row.remainingMg ?: JSONObject.NULL)
                    .put("active", row.active)
                    .put("openedAt", row.openedAt ?: JSONObject.NULL)
                    .put("diluentMl", row.diluentMl ?: JSONObject.NULL)
                    .put("syringeUnitsPerMl", row.syringeUnitsPerMl)
            )
        }
    }

    private fun decodeInventory(array: JSONArray): List<InventoryEntry> = buildList {
        for (i in 0 until array.length()) {
            val o = array.optJSONObject(i) ?: continue
            add(
                InventoryEntry(
                    id = o.optLong("id"),
                    peptide = o.optString("peptide"),
                    vialMg = o.optDouble("vialMg"),
                    quantity = o.optInt("quantity"),
                    batch = o.optString("batch"),
                    remainingMg = if (o.has("remainingMg") && !o.isNull("remainingMg")) o.optDouble("remainingMg") else null,
                    active = o.optBoolean("active"),
                    openedAt = if (o.has("openedAt") && !o.isNull("openedAt")) o.optLong("openedAt") else null,
                    diluentMl = if (o.has("diluentMl") && !o.isNull("diluentMl")) o.optDouble("diluentMl") else null,
                    syringeUnitsPerMl = o.optInt("syringeUnitsPerMl", 100).takeIf { it == 40 || it == 100 } ?: 100
                )
            )
        }
    }

    private fun encodeReminders(rows: List<ReminderEntry>) = JSONArray().apply {
        rows.forEach { row ->
            put(
                JSONObject()
                    .put("id", row.id)
                    .put("peptide", row.peptide)
                    .put("note", row.note)
                    .put("scheduledAt", row.scheduledAt)
                    .put("repeatDays", row.repeatDays)
                    .put("enabled", row.enabled)
                    .put("createdAt", row.createdAt)
            )
        }
    }

    private fun decodeReminders(array: JSONArray): List<ReminderEntry> = buildList {
        for (i in 0 until array.length()) {
            val o = array.optJSONObject(i) ?: continue
            val scheduledAt = o.optLong("scheduledAt")
            if (scheduledAt <= 0) continue
            add(
                ReminderEntry(
                    id = o.optLong("id"),
                    peptide = o.optString("peptide"),
                    note = o.optString("note"),
                    scheduledAt = scheduledAt,
                    repeatDays = o.optInt("repeatDays", 0).coerceAtLeast(0),
                    enabled = o.optBoolean("enabled", true),
                    createdAt = o.optLong("createdAt", scheduledAt)
                )
            )
        }
    }

    private fun encodeCalculations(rows: List<SavedCalculationEntry>) = JSONArray().apply {
        rows.forEach { row ->
            put(
                JSONObject()
                    .put("id", row.id)
                    .put("peptide", row.peptide)
                    .put("vialMg", row.vialMg)
                    .put("diluentMl", row.diluentMl)
                    .put("targetAmount", row.targetAmount)
                    .put("targetUnit", row.targetUnit)
                    .put("syringeUnitsPerMl", row.syringeUnitsPerMl)
                    .put("syringeCapacity", row.syringeCapacity)
                    .put("syringeUnits", row.syringeUnits)
                    .put("createdAt", row.createdAt)
            )
        }
    }

    private fun decodeCalculations(array: JSONArray): List<SavedCalculationEntry> = buildList {
        for (i in 0 until array.length()) {
            val o = array.optJSONObject(i) ?: continue
            val vialMg = o.optDouble("vialMg")
            val diluentMl = o.optDouble("diluentMl")
            val targetAmount = o.optDouble("targetAmount")
            val syringeUnits = o.optDouble("syringeUnits")
            if (vialMg <= 0 || diluentMl <= 0 || targetAmount <= 0 || syringeUnits <= 0) continue
            add(
                SavedCalculationEntry(
                    id = o.optLong("id"),
                    peptide = o.optString("peptide"),
                    vialMg = vialMg,
                    diluentMl = diluentMl,
                    targetAmount = targetAmount,
                    targetUnit = o.optString("targetUnit", "mg").takeIf { it == "mg" || it == "mcg" } ?: "mg",
                    syringeUnitsPerMl = o.optInt("syringeUnitsPerMl", 100).takeIf { it == 40 || it == 100 } ?: 100,
                    syringeCapacity = o.optInt("syringeCapacity", 30).coerceAtLeast(1),
                    syringeUnits = syringeUnits,
                    createdAt = o.optLong("createdAt")
                )
            )
        }
    }

    private fun csv(value: String): String =
        "\"" + value.replace("\"", "\"\"") + "\""

    private companion object {
        const val KEY_ENTRIES_V2 = "entries_v2_json"
        const val KEY_INVENTORY_V2 = "inventory_v2_json"
        const val KEY_PROGRESS_V2 = "progress_v2_json"
        const val KEY_REMINDERS_V1 = "reminders_v1_json"
        const val KEY_CALCULATIONS_V1 = "calculations_v1_json"
        const val DAY_MS = 24L * 60L * 60L * 1000L
    }
}
