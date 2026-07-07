package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "history_table")
data class HistoryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "TTS", "OCR", "TRANSLATION", "AI_SUMMARY"
    val sourceText: String,
    val resultText: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history_table ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<HistoryItem>>

    @Query("SELECT * FROM history_table WHERE type = :type ORDER BY timestamp DESC")
    fun getHistoryByType(type: String): Flow<List<HistoryItem>>

    @Insert
    suspend fun insert(item: HistoryItem)

    @Query("DELETE FROM history_table")
    suspend fun clearHistory()
}

@Database(entities = [HistoryItem::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
}
