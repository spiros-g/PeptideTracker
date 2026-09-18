package gr.peptidetracker.app.data

import android.content.Context
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import gr.peptidetracker.app.domain.InventoryLedger
import gr.peptidetracker.app.domain.ReminderCadence
import gr.peptidetracker.app.i18n.setLanguageOverride
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal

@Entity(tableName = "tracker_entries")
data class TrackerEntry(
    @PrimaryKey val id: Long,
    val peptide: String,
    val amount: String,
    val note: String,
    val createdAt: Long,
    val amountValue: Double? = null,
    val unit: String = "",
    val site: String = "",
    val inventoryId: Long? = null,
    val inventoryAppliedMg: Double? = null
)

@Entity(tableName = "progress_entries")
data class ProgressEntry(
    @PrimaryKey val id: Long,
    val weight: Double,
    val waist: Double?,
    val note: String,
    val createdAt: Long
)

@Entity(tableName = "inventory_entries")
data class InventoryEntry(
    @PrimaryKey val id: Long,
    val peptide: String,
    val vialMg: Double,
    val quantity: Int,
    val batch: String,
    val remainingMg: Double? = null,
    val active: Boolean = false,
    val openedAt: Long? = null,
    val diluentMl: Double? = null,
    val syringeUnitsPerMl: Int = 100,
    val vendor: String = "",
    val note: String = "",
    val purchaseDate: Long? = null,
    val expiryDate: Long? = null
) {
    @get:Ignore
    val effectiveRemainingMg: Double
        get() = remainingMg ?: vialMg

    @get:Ignore
    val isReconstituted: Boolean
        get() = diluentMl != null && diluentMl > 0

    @get:Ignore
    val concentrationMgPerMl: Double?
        get() = diluentMl?.takeIf { it > 0 }?.let { vialMg / it }

    @get:Ignore
    val mcgPerSyringeUnit: Double?
        get() = concentrationMgPerMl?.let { it * 1000.0 / syringeUnitsPerMl.coerceAtLeast(1) }
}

@Entity(tableName = "reminder_entries")
data class ReminderEntry(
    @PrimaryKey val id: Long,
    val peptide: String,
    val note: String,
    val scheduledAt: Long,
    val repeatDays: Int = 0,
    val enabled: Boolean = true,
    val createdAt: Long
)

@Entity(tableName = "saved_calculations")
data class SavedCalculationEntry(
    @PrimaryKey val id: Long,
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

data class BackupSummary(
    val schema: Int,
    val entries: Int,
    val inventory: Int,
    val progress: Int,
    val reminders: Int,
    val savedCalculations: Int,
    val customPeptides: Int
)

class LocalStore(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("peptide_tracker", Context.MODE_PRIVATE)
    private val database = AppDatabase.get(appContext)
    private val dao = database.dao()

    init {
        setLanguageOverride(
            prefs.getString(KEY_APP_LANGUAGE, LANGUAGE_SYSTEM)
                ?.takeUnless { it == LANGUAGE_SYSTEM }
        )
        migrateLegacyToRoomIfNeeded()
    }

    private fun <T> io(block: () -> T): T = runBlocking(Dispatchers.IO) { block() }

    private fun safe(value: String) = value.replace("|", "/").replace("\n", " ")
    private fun formatNumber(value: Double): String =
        BigDecimal.valueOf(value).stripTrailingZeros().toPlainString()

    fun defaultSyringeUnitsPerMl(): Int =
        prefs.getInt("default_syringe_units_per_ml", 100)
            .takeIf { it == 40 || it == 100 } ?: 100

    fun setDefaultSyringeUnitsPerMl(value: Int) {
        require(value == 40 || value == 100)
        prefs.edit().putInt("default_syringe_units_per_ml", value).apply()
    }

    fun appLanguage(): String =
        prefs.getString(KEY_APP_LANGUAGE, LANGUAGE_SYSTEM)
            ?.takeIf { it == LANGUAGE_SYSTEM || it == "el" || it == "en" }
            ?: LANGUAGE_SYSTEM

    fun setAppLanguage(value: String) {
        require(value == LANGUAGE_SYSTEM || value == "el" || value == "en")
        prefs.edit().putString(KEY_APP_LANGUAGE, value).apply()
        setLanguageOverride(value.takeUnless { it == LANGUAGE_SYSTEM })
    }

    fun appTheme(): String =
        prefs.getString(KEY_APP_THEME, THEME_SYSTEM)
            ?.takeIf { it == THEME_SYSTEM || it == THEME_DARK || it == THEME_LIGHT }
            ?: THEME_SYSTEM

    fun setAppTheme(value: String) {
        require(value == THEME_SYSTEM || value == THEME_DARK || value == THEME_LIGHT)
        prefs.edit().putString(KEY_APP_THEME, value).apply()
    }

    fun onboardingComplete() = prefs.getBoolean("onboarding_complete", false)
    fun setOnboardingComplete(value: Boolean) =
        prefs.edit().putBoolean("onboarding_complete", value).apply()

    fun appLockEnabled() = prefs.getBoolean("app_lock_enabled", false)
    fun setAppLockEnabled(value: Boolean) =
        prefs.edit().putBoolean("app_lock_enabled", value).apply()

    fun notificationDetailsVisible() =
        prefs.getBoolean("notification_details_visible", false)

    fun setNotificationDetailsVisible(value: Boolean) =
        prefs.edit().putBoolean("notification_details_visible", value).apply()

    fun shouldCheckForUpdates(now: Long = System.currentTimeMillis()): Boolean {
        val lastCheck = prefs.getLong(KEY_LAST_UPDATE_CHECK_AT, 0L)
        return lastCheck <= 0L || now - lastCheck >= UPDATE_CHECK_INTERVAL_MS
    }

    fun markUpdateCheck(now: Long = System.currentTimeMillis()) {
        prefs.edit().putLong(KEY_LAST_UPDATE_CHECK_AT, now).apply()
    }

    fun favorites(): Set<String> = io { dao.favoriteIds().toSet() }

    fun toggleFavorite(id: String) {
        io {
            if (id in dao.favoriteIds()) {
                dao.deleteFavorite(id)
            } else {
                dao.insertFavorite(FavoriteEntry(id))
            }
        }
    }

    fun customPeptides(): List<String> = io { dao.customPeptideNames() }

    fun peptideNames(): List<String> =
        (peptideCatalog.map { it.name } + customPeptides())
            .distinctBy { it.lowercase() }
            .sortedBy { it.lowercase() }

    fun addCustomPeptide(name: String): Boolean {
        val cleaned = safe(name.trim())
        if (cleaned.length < 2) return false
        if (peptideCatalog.any { it.name.equals(cleaned, ignoreCase = true) }) return false
        if (customPeptides().any { it.equals(cleaned, ignoreCase = true) }) return false
        io { dao.insertCustomPeptide(CustomPeptideEntry(cleaned, System.currentTimeMillis())) }
        return true
    }

    fun deleteCustomPeptide(name: String) {
        io { dao.deleteCustomPeptide(name) }
    }

    fun entries(): List<TrackerEntry> = io { dao.trackerEntries() }

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
    ): Boolean {
        require(peptide.isNotBlank())
        require(amountValue > 0)

        val normalizedUnit = unit.trim().ifBlank { "mcg" }
        val amountMg = if (subtractFromInventory && inventoryId != null) {
            amountToMg(amountValue, normalizedUnit) ?: return false
        } else {
            null
        }
        val id = uniqueId()

        return io {
            var saved = false
            database.runInTransaction {
                if (amountMg != null && inventoryId != null) {
                    val inventoryRows = dao.inventoryEntries()
                    val target = inventoryRows.firstOrNull { it.id == inventoryId }
                        ?: return@runInTransaction
                    val updated = InventoryLedger.consume(target, amountMg)
                        ?: return@runInTransaction

                    val nextInventory = inventoryRows.map {
                        if (it.id == inventoryId) updated else it
                    }
                    dao.clearInventoryEntries()
                    if (nextInventory.isNotEmpty()) dao.insertInventoryEntries(nextInventory)
                }

                val nextEntries = dao.trackerEntries().toMutableList()
                nextEntries += TrackerEntry(
                    id = id,
                    peptide = safe(peptide.trim()),
                    amount = formatNumber(amountValue) + " " + normalizedUnit,
                    note = safe(note.trim()),
                    createdAt = createdAt,
                    amountValue = amountValue,
                    unit = normalizedUnit,
                    site = safe(site.trim()),
                    inventoryId = if (amountMg != null) inventoryId else null,
                    inventoryAppliedMg = amountMg
                )
                dao.clearTrackerEntries()
                dao.insertTrackerEntries(nextEntries)
                saved = true
            }
            saved
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
    ): Boolean {
        require(peptide.isNotBlank())
        require(amountValue > 0)

        val normalizedUnit = unit.trim().ifBlank { "mcg" }

        return io {
            var saved = false
            database.runInTransaction {
                val rows = dao.trackerEntries()
                val current = rows.firstOrNull { it.id == id } ?: return@runInTransaction
                val inventoryRows = dao.inventoryEntries().toMutableList()
                var inventoryChanged = false

                var nextInventoryId = current.inventoryId
                var nextAppliedMg = current.inventoryAppliedMg

                if (current.inventoryId != null && current.inventoryAppliedMg != null) {
                    val inventoryIndex = inventoryRows.indexOfFirst { it.id == current.inventoryId }
                    if (inventoryIndex < 0) {
                        nextInventoryId = null
                        nextAppliedMg = null
                    } else {
                        val stillSamePeptide =
                            current.peptide.equals(peptide.trim(), ignoreCase = true)
                        val requestedMg = amountToMg(amountValue, normalizedUnit)

                        if (!stillSamePeptide || requestedMg == null) {
                            InventoryLedger.restore(
                                row = inventoryRows[inventoryIndex],
                                amountMg = current.inventoryAppliedMg,
                                now = System.currentTimeMillis()
                            )?.let { restored ->
                                inventoryRows[inventoryIndex] = restored
                                inventoryChanged = true
                            }
                            nextInventoryId = null
                            nextAppliedMg = null
                        } else {
                            val delta = requestedMg - current.inventoryAppliedMg
                            when {
                                delta > 0.0000001 -> {
                                    val updated = InventoryLedger.consume(
                                        inventoryRows[inventoryIndex],
                                        delta
                                    ) ?: return@runInTransaction
                                    inventoryRows[inventoryIndex] = updated
                                    inventoryChanged = true
                                }

                                delta < -0.0000001 -> {
                                    val updated = InventoryLedger.restore(
                                        row = inventoryRows[inventoryIndex],
                                        amountMg = -delta,
                                        now = System.currentTimeMillis()
                                    ) ?: return@runInTransaction
                                    inventoryRows[inventoryIndex] = updated
                                    inventoryChanged = true
                                }
                            }
                            nextAppliedMg = requestedMg
                        }
                    }
                } else if (current.inventoryId != null) {
                    // Legacy entries may point to inventory without proving stock was deducted.
                    nextInventoryId = null
                    nextAppliedMg = null
                }

                if (inventoryChanged) {
                    dao.clearInventoryEntries()
                    if (inventoryRows.isNotEmpty()) dao.insertInventoryEntries(inventoryRows)
                }

                val nextEntries = rows.map { entry ->
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
                            site = safe(site.trim()),
                            inventoryId = nextInventoryId,
                            inventoryAppliedMg = nextAppliedMg
                        )
                    }
                }
                dao.clearTrackerEntries()
                if (nextEntries.isNotEmpty()) dao.insertTrackerEntries(nextEntries)
                saved = true
            }
            saved
        }
    }

    fun deleteEntry(id: Long): TrackerEntry? = io {
        var deleted: TrackerEntry? = null

        database.runInTransaction {
            val rows = dao.trackerEntries()
            val current = rows.firstOrNull { it.id == id } ?: return@runInTransaction
            val inventoryRows = dao.inventoryEntries().toMutableList()

            if (current.inventoryId != null && current.inventoryAppliedMg != null) {
                val inventoryIndex = inventoryRows.indexOfFirst { it.id == current.inventoryId }
                if (inventoryIndex >= 0) {
                    val restored = InventoryLedger.restore(
                        row = inventoryRows[inventoryIndex],
                        amountMg = current.inventoryAppliedMg,
                        now = System.currentTimeMillis()
                    ) ?: return@runInTransaction

                    inventoryRows[inventoryIndex] = restored
                    dao.clearInventoryEntries()
                    if (inventoryRows.isNotEmpty()) dao.insertInventoryEntries(inventoryRows)
                }
            }

            val nextEntries = rows.filterNot { it.id == id }
            dao.clearTrackerEntries()
            if (nextEntries.isNotEmpty()) dao.insertTrackerEntries(nextEntries)
            deleted = current
        }

        deleted
    }

    fun restoreDeletedEntry(entry: TrackerEntry): Boolean = io {
        var restoredSuccessfully = false

        database.runInTransaction {
            val rows = dao.trackerEntries()
            if (rows.any { it.id == entry.id }) return@runInTransaction

            val inventoryRows = dao.inventoryEntries().toMutableList()
            var restored = entry

            if (entry.inventoryId != null && entry.inventoryAppliedMg != null) {
                val inventoryIndex = inventoryRows.indexOfFirst { it.id == entry.inventoryId }
                if (inventoryIndex >= 0) {
                    val updated = InventoryLedger.consume(
                        inventoryRows[inventoryIndex],
                        entry.inventoryAppliedMg
                    )
                    if (updated != null) {
                        inventoryRows[inventoryIndex] = updated
                        dao.clearInventoryEntries()
                        if (inventoryRows.isNotEmpty()) dao.insertInventoryEntries(inventoryRows)
                    } else {
                        restored = entry.copy(inventoryId = null, inventoryAppliedMg = null)
                    }
                } else {
                    restored = entry.copy(inventoryId = null, inventoryAppliedMg = null)
                }
            }

            val nextEntries = rows + restored
            dao.clearTrackerEntries()
            dao.insertTrackerEntries(nextEntries)
            restoredSuccessfully = true
        }

        restoredSuccessfully
    }

    fun progress(): List<ProgressEntry> = io { dao.progressEntries() }

    fun addProgress(weight: Double, waist: Double?, note: String) {
        require(weight > 0)
        require(waist == null || waist > 0)
        val now = System.currentTimeMillis()
        val next = progress().toMutableList()
        next += ProgressEntry(now, weight, waist, safe(note), now)
        saveProgress(next)
    }

    fun updateProgress(id: Long, weight: Double, waist: Double?, note: String, createdAt: Long) {
        require(weight > 0)
        require(waist == null || waist > 0)
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

    fun restoreDeletedProgress(row: ProgressEntry): Boolean {
        if (progress().any { it.id == row.id }) return false
        saveProgress(progress() + row)
        return true
    }

    fun inventory(): List<InventoryEntry> = io { dao.inventoryEntries() }

    fun addInventory(
        peptide: String,
        vial: Double,
        quantity: Int,
        batch: String,
        diluentMl: Double? = null,
        syringeUnitsPerMl: Int = 100,
        vendor: String = "",
        note: String = "",
        purchaseDate: Long? = null,
        expiryDate: Long? = null
    ) {
        require(peptide.isNotBlank() && vial > 0 && quantity > 0)
        require(diluentMl == null || diluentMl > 0)
        require(syringeUnitsPerMl == 40 || syringeUnitsPerMl == 100)
        require(purchaseDate == null || expiryDate == null || expiryDate >= purchaseDate)
        val next = inventory().toMutableList()
        next += InventoryEntry(
            id = uniqueId(),
            peptide = safe(peptide.trim()),
            vialMg = vial,
            quantity = quantity,
            batch = safe(batch.trim()),
            remainingMg = vial,
            diluentMl = diluentMl,
            syringeUnitsPerMl = syringeUnitsPerMl,
            vendor = safe(vendor.trim()),
            note = safe(note.trim()),
            purchaseDate = purchaseDate,
            expiryDate = expiryDate
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

    fun updateInventoryDetails(
        id: Long,
        peptide: String,
        vialMg: Double,
        quantity: Int,
        batch: String,
        remainingMg: Double?,
        diluentMl: Double?,
        syringeUnitsPerMl: Int,
        vendor: String,
        note: String,
        purchaseDate: Long?,
        expiryDate: Long?
    ) {
        require(peptide.isNotBlank() && vialMg > 0 && quantity >= 0)
        require(diluentMl == null || diluentMl > 0)
        require(syringeUnitsPerMl == 40 || syringeUnitsPerMl == 100)
        require(purchaseDate == null || expiryDate == null || expiryDate >= purchaseDate)
        require(remainingMg == null || remainingMg in 0.0..vialMg)
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
                        syringeUnitsPerMl = syringeUnitsPerMl,
                        vendor = safe(vendor.trim()),
                        note = safe(note.trim()),
                        purchaseDate = purchaseDate,
                        expiryDate = expiryDate
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
                        remainingMg = if ((it.remainingMg ?: 0.0) <= 0.0) it.vialMg else it.remainingMg,
                        openedAt = now
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

    fun restoreDeletedInventory(row: InventoryEntry): Boolean {
        if (inventory().any { it.id == row.id }) return false
        saveInventory(inventory() + row)
        return true
    }

    fun consumeInventory(id: Long, amountMg: Double): Boolean {
        val rows = inventory()
        val target = rows.firstOrNull { it.id == id } ?: return false
        val updated = InventoryLedger.consume(target, amountMg) ?: return false
        saveInventory(rows.map { if (it.id == id) updated else it })
        return true
    }

    fun restoreInventory(id: Long, amountMg: Double): Boolean {
        val rows = inventory()
        val target = rows.firstOrNull { it.id == id } ?: return false
        val updated = InventoryLedger.restore(
            row = target,
            amountMg = amountMg,
            now = System.currentTimeMillis()
        ) ?: return false
        saveInventory(rows.map { if (it.id == id) updated else it })
        return true
    }

    fun reminders(): List<ReminderEntry> = io { dao.reminderEntries() }

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
                val next = ReminderCadence.nextScheduledAt(
                    scheduledAt = row.scheduledAt,
                    repeatDays = row.repeatDays,
                    now = now
                ) ?: return@map row.copy(enabled = false).also { nextRow = it }
                row.copy(scheduledAt = next, enabled = true).also { nextRow = it }
            }
        }
        saveReminders(rows)
        return nextRow
    }

    fun savedCalculations(): List<SavedCalculationEntry> = io { dao.savedCalculations() }

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
            .put("schema", 9)
            .put("generatedAt", System.currentTimeMillis())
            .put("defaultSyringeUnitsPerMl", defaultSyringeUnitsPerMl())
            .put("onboardingComplete", onboardingComplete())
            .put("favorites", JSONArray(favorites().toList()))
            .put("entries", encodeEntries(entries()))
            .put("inventory", encodeInventory(inventory()))
            .put("progress", encodeProgress(progress()))
            .put("reminders", encodeReminders(reminders()))
            .put("savedCalculations", encodeCalculations(savedCalculations()))
            .put("customPeptides", JSONArray(customPeptides()))
        return root.toString(2)
    }

    fun inspectBackup(raw: String): BackupSummary? = runCatching {
        val root = JSONObject(raw)
        val schema = root.optInt("schema", 1)
        require(schema in 1..9)
        BackupSummary(
            schema = schema,
            entries = root.optJSONArray("entries")?.length() ?: 0,
            inventory = root.optJSONArray("inventory")?.length() ?: 0,
            progress = root.optJSONArray("progress")?.length() ?: 0,
            reminders = root.optJSONArray("reminders")?.length() ?: 0,
            savedCalculations = root.optJSONArray("savedCalculations")?.length() ?: 0,
            customPeptides = root.optJSONArray("customPeptides")?.length() ?: 0
        )
    }.getOrNull()

    fun restoreJson(raw: String): Boolean = runCatching {
        val root = JSONObject(raw)
        val schema = root.optInt("schema", 1)
        require(schema in 1..9)
        val previousReminderIds = reminders().map { it.id }
        prefs.edit().putString(KEY_PRE_RESTORE_BACKUP, exportJson()).commit()

        val decodedEntries = decodeEntries(root.optJSONArray("entries") ?: JSONArray())
        val decodedInventory = decodeInventory(root.optJSONArray("inventory") ?: JSONArray())
        val decodedProgress = decodeProgress(root.optJSONArray("progress") ?: JSONArray())
        val decodedReminders = decodeReminders(root.optJSONArray("reminders") ?: JSONArray())
        val decodedCalculations = decodeCalculations(root.optJSONArray("savedCalculations") ?: JSONArray())
        val decodedCustomPeptides = buildList {
            val customJson = root.optJSONArray("customPeptides") ?: JSONArray()
            for (i in 0 until customJson.length()) {
                customJson.optString(i).trim().takeIf { it.length >= 2 }?.let(::add)
            }
        }

        val favoriteSet = mutableSetOf<String>()
        val favoritesJson = root.optJSONArray("favorites") ?: JSONArray()
        for (i in 0 until favoritesJson.length()) {
            favoritesJson.optString(i).takeIf { it.isNotBlank() }?.let(favoriteSet::add)
        }

        io {
            database.runInTransaction {
                dao.clearTrackerEntries()
                dao.clearInventoryEntries()
                dao.clearProgressEntries()
                dao.clearReminderEntries()
                dao.clearSavedCalculations()
                dao.clearFavorites()
                dao.clearCustomPeptides()
                if (decodedEntries.isNotEmpty()) dao.insertTrackerEntries(decodedEntries)
                if (decodedInventory.isNotEmpty()) dao.insertInventoryEntries(decodedInventory)
                if (decodedProgress.isNotEmpty()) dao.insertProgressEntries(decodedProgress)
                if (decodedReminders.isNotEmpty()) dao.insertReminderEntries(decodedReminders)
                if (decodedCalculations.isNotEmpty()) dao.insertSavedCalculations(decodedCalculations)
                if (favoriteSet.isNotEmpty()) dao.insertFavorites(favoriteSet.map(::FavoriteEntry))
                decodedCustomPeptides.forEach { dao.insertCustomPeptide(CustomPeptideEntry(it, System.currentTimeMillis())) }
            }
        }

        prefs.edit()
            .putInt(
                "default_syringe_units_per_ml",
                root.optInt("defaultSyringeUnitsPerMl", defaultSyringeUnitsPerMl())
                    .takeIf { it == 40 || it == 100 } ?: 100
            )
            .putBoolean("onboarding_complete", root.optBoolean("onboardingComplete", true))
            .commit()

        previousReminderIds.forEach { ReminderScheduler.cancel(appContext, it) }
        ReminderScheduler.cancelAllSnoozes(appContext)
        ReminderScheduler.rescheduleAll(appContext, reminders())
        true
    }.getOrDefault(false)

    fun mergeJson(raw: String): Boolean = runCatching {
        val root = JSONObject(raw)
        val schema = root.optInt("schema", 1)
        require(schema in 1..9)

        val importedEntries = decodeEntries(root.optJSONArray("entries") ?: JSONArray())
        val importedInventory = decodeInventory(root.optJSONArray("inventory") ?: JSONArray())
        val importedProgress = decodeProgress(root.optJSONArray("progress") ?: JSONArray())
        val importedReminders = decodeReminders(root.optJSONArray("reminders") ?: JSONArray())
        val importedCalculations = decodeCalculations(root.optJSONArray("savedCalculations") ?: JSONArray())

        val importedFavorites = buildSet {
            val array = root.optJSONArray("favorites") ?: JSONArray()
            for (i in 0 until array.length()) {
                array.optString(i).takeIf { it.isNotBlank() }?.let(::add)
            }
        }

        val importedCustomPeptides = buildList {
            val array = root.optJSONArray("customPeptides") ?: JSONArray()
            for (i in 0 until array.length()) {
                array.optString(i).trim().takeIf { it.length >= 2 }?.let(::add)
            }
        }

        val mergedEntries = (entries() + importedEntries).distinctBy { it.id }
        val mergedInventory = (inventory() + importedInventory).distinctBy { it.id }
        val mergedProgress = (progress() + importedProgress).distinctBy { it.id }
        val mergedReminders = (reminders() + importedReminders).distinctBy { it.id }
        val mergedCalculations = (savedCalculations() + importedCalculations).distinctBy { it.id }
        val mergedFavorites = favorites() + importedFavorites
        val mergedCustomPeptides = (customPeptides() + importedCustomPeptides)
            .distinctBy { it.lowercase() }

        io {
            database.runInTransaction {
                dao.clearTrackerEntries()
                dao.clearInventoryEntries()
                dao.clearProgressEntries()
                dao.clearReminderEntries()
                dao.clearSavedCalculations()
                dao.clearFavorites()
                dao.clearCustomPeptides()

                if (mergedEntries.isNotEmpty()) dao.insertTrackerEntries(mergedEntries)
                if (mergedInventory.isNotEmpty()) dao.insertInventoryEntries(mergedInventory)
                if (mergedProgress.isNotEmpty()) dao.insertProgressEntries(mergedProgress)
                if (mergedReminders.isNotEmpty()) dao.insertReminderEntries(mergedReminders)
                if (mergedCalculations.isNotEmpty()) dao.insertSavedCalculations(mergedCalculations)
                if (mergedFavorites.isNotEmpty()) dao.insertFavorites(mergedFavorites.map(::FavoriteEntry))
                mergedCustomPeptides.forEach {
                    dao.insertCustomPeptide(CustomPeptideEntry(it, System.currentTimeMillis()))
                }
            }
        }

        ReminderScheduler.rescheduleAll(appContext, reminders())
        true
    }.getOrDefault(false)

    fun hasPreRestoreBackup(): Boolean =
        !prefs.getString(KEY_PRE_RESTORE_BACKUP, null).isNullOrBlank()

    fun restorePreRestoreBackup(): Boolean {
        val raw = prefs.getString(KEY_PRE_RESTORE_BACKUP, null) ?: return false
        return restoreJson(raw)
    }

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

    private fun saveEntries(rows: List<TrackerEntry>) = io {
        database.runInTransaction {
            dao.clearTrackerEntries()
            if (rows.isNotEmpty()) dao.insertTrackerEntries(rows)
        }
    }

    private fun saveProgress(rows: List<ProgressEntry>) = io {
        database.runInTransaction {
            dao.clearProgressEntries()
            if (rows.isNotEmpty()) dao.insertProgressEntries(rows)
        }
    }

    private fun saveInventory(rows: List<InventoryEntry>) = io {
        database.runInTransaction {
            dao.clearInventoryEntries()
            if (rows.isNotEmpty()) dao.insertInventoryEntries(rows)
        }
    }

    private fun saveReminders(rows: List<ReminderEntry>) = io {
        database.runInTransaction {
            dao.clearReminderEntries()
            if (rows.isNotEmpty()) dao.insertReminderEntries(rows)
        }
    }

    private fun saveCalculations(rows: List<SavedCalculationEntry>) = io {
        database.runInTransaction {
            dao.clearSavedCalculations()
            if (rows.isNotEmpty()) dao.insertSavedCalculations(rows)
        }
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
                    .put("inventoryAppliedMg", row.inventoryAppliedMg ?: JSONObject.NULL)
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
                    inventoryId = if (o.has("inventoryId") && !o.isNull("inventoryId")) o.optLong("inventoryId") else null,
                    inventoryAppliedMg = if (o.has("inventoryAppliedMg") && !o.isNull("inventoryAppliedMg")) o.optDouble("inventoryAppliedMg") else null
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
                    .put("vendor", row.vendor)
                    .put("note", row.note)
                    .put("purchaseDate", row.purchaseDate ?: JSONObject.NULL)
                    .put("expiryDate", row.expiryDate ?: JSONObject.NULL)
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
                    syringeUnitsPerMl = o.optInt("syringeUnitsPerMl", 100).takeIf { it == 40 || it == 100 } ?: 100,
                    vendor = o.optString("vendor"),
                    note = o.optString("note"),
                    purchaseDate = if (o.has("purchaseDate") && !o.isNull("purchaseDate")) o.optLong("purchaseDate") else null,
                    expiryDate = if (o.has("expiryDate") && !o.isNull("expiryDate")) o.optLong("expiryDate") else null
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

    private fun migrateLegacyToRoomIfNeeded() {
        if (prefs.getBoolean(KEY_ROOM_MIGRATED, false)) return

        val legacyEntries = runCatching {
            prefs.getString(KEY_ENTRIES_V2, null)?.let { decodeEntries(JSONArray(it)) }
                ?: prefs.getStringSet("entries", emptySet()).orEmpty().mapNotNull {
                    val p = it.split("|", limit = 5)
                    if (p.size != 5) return@mapNotNull null
                    TrackerEntry(
                        id = p[0].toLongOrNull() ?: return@mapNotNull null,
                        peptide = p[1],
                        amount = p[2],
                        note = p[3],
                        createdAt = p[4].toLongOrNull() ?: return@mapNotNull null
                    )
                }
        }.getOrDefault(emptyList())

        val legacyInventory = runCatching {
            prefs.getString(KEY_INVENTORY_V2, null)?.let { decodeInventory(JSONArray(it)) }
                ?: prefs.getStringSet("inventory", emptySet()).orEmpty().mapNotNull {
                    val p = it.split("|", limit = 5)
                    if (p.size < 5) return@mapNotNull null
                    InventoryEntry(
                        id = p[0].toLongOrNull() ?: return@mapNotNull null,
                        peptide = p[1],
                        vialMg = p[2].toDoubleOrNull() ?: return@mapNotNull null,
                        quantity = p[3].toIntOrNull() ?: return@mapNotNull null,
                        batch = p[4]
                    )
                }
        }.getOrDefault(emptyList())

        val legacyProgress = runCatching {
            prefs.getString(KEY_PROGRESS_V2, null)?.let { decodeProgress(JSONArray(it)) }
                ?: prefs.getStringSet("progress", emptySet()).orEmpty().mapNotNull {
                    val p = it.split("|", limit = 5)
                    if (p.size != 5) return@mapNotNull null
                    ProgressEntry(
                        id = p[0].toLongOrNull() ?: return@mapNotNull null,
                        weight = p[1].toDoubleOrNull() ?: return@mapNotNull null,
                        waist = p[2].takeIf(String::isNotBlank)?.toDoubleOrNull(),
                        note = p[3],
                        createdAt = p[4].toLongOrNull() ?: return@mapNotNull null
                    )
                }
        }.getOrDefault(emptyList())

        val legacyReminders = runCatching {
            prefs.getString(KEY_REMINDERS_V1, null)?.let { decodeReminders(JSONArray(it)) }
        }.getOrNull().orEmpty()

        val legacyCalculations = runCatching {
            prefs.getString(KEY_CALCULATIONS_V1, null)?.let { decodeCalculations(JSONArray(it)) }
        }.getOrNull().orEmpty()

        val legacyFavorites = prefs.getStringSet("favorites", emptySet()).orEmpty()

        io {
            database.runInTransaction {
                if (dao.trackerEntries().isEmpty() && legacyEntries.isNotEmpty()) dao.insertTrackerEntries(legacyEntries)
                if (dao.inventoryEntries().isEmpty() && legacyInventory.isNotEmpty()) dao.insertInventoryEntries(legacyInventory)
                if (dao.progressEntries().isEmpty() && legacyProgress.isNotEmpty()) dao.insertProgressEntries(legacyProgress)
                if (dao.reminderEntries().isEmpty() && legacyReminders.isNotEmpty()) dao.insertReminderEntries(legacyReminders)
                if (dao.savedCalculations().isEmpty() && legacyCalculations.isNotEmpty()) dao.insertSavedCalculations(legacyCalculations)
                if (dao.favoriteIds().isEmpty() && legacyFavorites.isNotEmpty()) {
                    dao.insertFavorites(legacyFavorites.map(::FavoriteEntry))
                }
            }
        }

        prefs.edit().putBoolean(KEY_ROOM_MIGRATED, true).commit()
    }

    private fun csv(value: String): String =
        "\"" + value.replace("\"", "\"\"") + "\""

    private companion object {
        const val KEY_ENTRIES_V2 = "entries_v2_json"
        const val KEY_INVENTORY_V2 = "inventory_v2_json"
        const val KEY_PROGRESS_V2 = "progress_v2_json"
        const val KEY_REMINDERS_V1 = "reminders_v1_json"
        const val KEY_CALCULATIONS_V1 = "calculations_v1_json"
        const val KEY_PRE_RESTORE_BACKUP = "pre_restore_backup_json"
        const val KEY_ROOM_MIGRATED = "room_migrated_v1"
        const val KEY_LAST_UPDATE_CHECK_AT = "last_update_check_at"
        const val KEY_APP_LANGUAGE = "app_language"
        const val KEY_APP_THEME = "app_theme"
        const val LANGUAGE_SYSTEM = "system"
        const val THEME_SYSTEM = "system"
        const val THEME_DARK = "dark"
        const val THEME_LIGHT = "light"
        const val UPDATE_CHECK_INTERVAL_MS = 24L * 60L * 60L * 1000L
    }
}
