package gr.peptidetracker.app.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

@Entity(tableName = "favorite_peptides")
data class FavoriteEntry(
    @PrimaryKey val id: String
)

@Entity(tableName = "custom_peptides")
data class CustomPeptideEntry(
    @PrimaryKey val name: String,
    val createdAt: Long
)

@Dao
interface AppDao {
    @Query("SELECT * FROM tracker_entries ORDER BY createdAt DESC")
    fun trackerEntries(): List<TrackerEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTrackerEntries(rows: List<TrackerEntry>)

    @Query("DELETE FROM tracker_entries")
    fun clearTrackerEntries()

    @Query("SELECT * FROM inventory_entries ORDER BY active DESC, peptide COLLATE NOCASE ASC")
    fun inventoryEntries(): List<InventoryEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertInventoryEntries(rows: List<InventoryEntry>)

    @Query("DELETE FROM inventory_entries")
    fun clearInventoryEntries()

    @Query("SELECT * FROM progress_entries ORDER BY createdAt DESC")
    fun progressEntries(): List<ProgressEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProgressEntries(rows: List<ProgressEntry>)

    @Query("DELETE FROM progress_entries")
    fun clearProgressEntries()

    @Query("SELECT * FROM reminder_entries ORDER BY enabled DESC, scheduledAt ASC")
    fun reminderEntries(): List<ReminderEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertReminderEntries(rows: List<ReminderEntry>)

    @Query("DELETE FROM reminder_entries")
    fun clearReminderEntries()

    @Query("SELECT * FROM saved_calculations ORDER BY createdAt DESC")
    fun savedCalculations(): List<SavedCalculationEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSavedCalculations(rows: List<SavedCalculationEntry>)

    @Query("DELETE FROM saved_calculations")
    fun clearSavedCalculations()

    @Query("SELECT id FROM favorite_peptides ORDER BY id")
    fun favoriteIds(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertFavorite(row: FavoriteEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFavorites(rows: List<FavoriteEntry>)

    @Query("DELETE FROM favorite_peptides WHERE id = :id")
    fun deleteFavorite(id: String)

    @Query("DELETE FROM favorite_peptides")
    fun clearFavorites()

    @Query("SELECT name FROM custom_peptides ORDER BY name COLLATE NOCASE")
    fun customPeptideNames(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertCustomPeptide(row: CustomPeptideEntry)

    @Query("DELETE FROM custom_peptides WHERE name = :name")
    fun deleteCustomPeptide(name: String)

    @Query("DELETE FROM custom_peptides")
    fun clearCustomPeptides()
}

@Database(
    entities = [
        TrackerEntry::class,
        InventoryEntry::class,
        ProgressEntry::class,
        ReminderEntry::class,
        SavedCalculationEntry::class,
        FavoriteEntry::class,
        CustomPeptideEntry::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): AppDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "peptide_tracker.db"
                ).build().also { instance = it }
            }
    }
}
