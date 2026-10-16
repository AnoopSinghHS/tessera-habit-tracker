package app.tessera.habit.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import app.tessera.habit.data.model.HabitWithLogs
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Transaction
    @Query("SELECT * FROM habits ORDER BY isArchived ASC, sortOrder ASC, id ASC")
    fun observeAllHabitsWithLogs(): Flow<List<HabitWithLogs>>

    @Transaction
    @Query("SELECT * FROM habits WHERE isArchived = 0 ORDER BY sortOrder ASC, id ASC")
    fun observeActiveHabitsWithLogs(): Flow<List<HabitWithLogs>>

    @Transaction
    @Query("SELECT * FROM habits WHERE isArchived = 1 ORDER BY sortOrder ASC, id ASC")
    fun observeArchivedHabitsWithLogs(): Flow<List<HabitWithLogs>>

    @Transaction
    @Query("SELECT * FROM habits ORDER BY isArchived ASC, sortOrder ASC, id ASC")
    suspend fun getAllHabitsWithLogsOnce(): List<HabitWithLogs>

    @Transaction
    @Query("SELECT * FROM habits WHERE id = :habitId LIMIT 1")
    suspend fun getHabitWithLogsById(habitId: Long): HabitWithLogs?

    @Transaction
    @Query("SELECT * FROM habits WHERE LOWER(slug) = LOWER(:slug) LIMIT 1")
    suspend fun getHabitWithLogsBySlug(slug: String): HabitWithLogs?

    @Query("SELECT * FROM habits WHERE id = :habitId LIMIT 1")
    suspend fun getHabitById(habitId: Long): HabitEntity?

    @Query("SELECT * FROM habits WHERE LOWER(slug) = LOWER(:slug) LIMIT 1")
    suspend fun getHabitBySlug(slug: String): HabitEntity?

    @Query("SELECT COUNT(*) FROM habits")
    suspend fun getHabitCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabits(habits: List<HabitEntity>): List<Long>

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Query("UPDATE habits SET isArchived = :archived WHERE id = :habitId")
    suspend fun setHabitArchived(habitId: Long, archived: Boolean)

    @Query("DELETE FROM habits WHERE id = :habitId")
    suspend fun deleteHabitById(habitId: Long)

    @Query("DELETE FROM habits")
    suspend fun deleteAllHabits()

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND dateEpochDay = :dateEpochDay LIMIT 1")
    suspend fun getLogForDate(habitId: Long, dateEpochDay: Long): HabitLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLog(log: HabitLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLogs(logs: List<HabitLogEntity>)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND dateEpochDay = :dateEpochDay")
    suspend fun deleteLogForDate(habitId: Long, dateEpochDay: Long)

    @Query("DELETE FROM habit_logs")
    suspend fun deleteAllLogs()
}
