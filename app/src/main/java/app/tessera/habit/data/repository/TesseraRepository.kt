package app.tessera.habit.data.repository

import android.content.Context
import app.tessera.habit.data.local.HabitDao
import app.tessera.habit.data.local.HabitEntity
import app.tessera.habit.data.local.HabitLogEntity
import app.tessera.habit.data.model.HabitWithLogs
import app.tessera.habit.widget.HabitWidgetUpdater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.Locale

class TesseraRepository(
    context: Context,
    private val habitDao: HabitDao
) {
    internal val appContext = context.applicationContext

    val allHabitsWithLogs: Flow<List<HabitWithLogs>> = habitDao.observeAllHabitsWithLogs()
    val activeHabitsWithLogs: Flow<List<HabitWithLogs>> = habitDao.observeActiveHabitsWithLogs()
    val archivedHabitsWithLogs: Flow<List<HabitWithLogs>> = habitDao.observeArchivedHabitsWithLogs()

    suspend fun getAllHabitsWithLogsOnce(): List<HabitWithLogs> = withContext(Dispatchers.IO) {
        habitDao.getAllHabitsWithLogsOnce()
    }

    suspend fun getHabitWithLogsByIdOnce(habitId: Long): HabitWithLogs? = withContext(Dispatchers.IO) {
        habitDao.getHabitWithLogsById(habitId)
    }

    suspend fun getHabitWithLogsBySlugOnce(slug: String): HabitWithLogs? = withContext(Dispatchers.IO) {
        habitDao.getHabitWithLogsBySlug(slug.trim())
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        habitDao.deleteAllLogs()
        habitDao.deleteAllHabits()
        HabitWidgetUpdater.updateAllWidgets(appContext)
    }

    suspend fun saveHabit(habit: HabitEntity): Long = withContext(Dispatchers.IO) {
        val baseSlug = habit.slug.ifBlank { HabitEntity.generateSlug(habit.title) }
            .trim()
            .lowercase(Locale.US)
            .replace(Regex("[^a-z0-9-]+"), "-")
            .trim('-')
            .ifEmpty { HabitEntity.generateSlug(habit.title) }

        val existingBySlug = habitDao.getHabitBySlug(baseSlug)
        val resolvedSlug = if (existingBySlug != null && existingBySlug.id != habit.id) {
            "$baseSlug-${System.currentTimeMillis() % 1000}"
        } else {
            baseSlug
        }

        val normalized = habit.copy(
            slug = resolvedSlug,
            title = habit.title.trim().ifEmpty { "Untitled habit" },
            targetPerDay = habit.targetPerDay.coerceIn(1, 100),
            weeklyGoalDays = habit.weeklyGoalDays.coerceIn(1, 7)
        )

        val savedId = if (normalized.id > 0L) {
            habitDao.updateHabit(normalized)
            normalized.id
        } else {
            habitDao.insertHabit(normalized)
        }
        HabitWidgetUpdater.updateAllWidgets(appContext)
        savedId
    }

    suspend fun deleteHabit(habitId: Long) = withContext(Dispatchers.IO) {
        habitDao.deleteHabitById(habitId)
        HabitWidgetUpdater.updateAllWidgets(appContext)
    }

    suspend fun setHabitArchived(habitId: Long, archived: Boolean) = withContext(Dispatchers.IO) {
        habitDao.setHabitArchived(habitId, archived)
        HabitWidgetUpdater.updateAllWidgets(appContext)
    }

    /**
     * One-tap check-in.
     * - targetPerDay <= 1: toggles 0 <-> 1.
     * - targetPerDay > 1: counts up 0 -> 1 -> ... -> target, then wraps to 0.
     * - alwaysIncrement = true: always adds one.
     */
    suspend fun smartCheckIn(
        habitId: Long,
        date: LocalDate = LocalDate.now(),
        note: String? = null,
        alwaysIncrement: Boolean = false
    ): Int = withContext(Dispatchers.IO) {
        val habit = habitDao.getHabitById(habitId) ?: return@withContext 0
        val epochDay = date.toEpochDay()
        val existingLog = habitDao.getLogForDate(habitId, epochDay)
        val currentCount = existingLog?.count ?: 0
        val target = habit.targetPerDay.coerceAtLeast(1)

        val nextCount = when {
            alwaysIncrement -> currentCount + 1
            target <= 1 -> if (currentCount >= 1) 0 else 1
            else -> if (currentCount >= target) 0 else currentCount + 1
        }

        val mergedNote = note ?: existingLog?.note.orEmpty()
        writeCountInternal(habitId, epochDay, nextCount, mergedNote, existingLog?.id ?: 0L)
        HabitWidgetUpdater.updateAllWidgets(appContext)
        nextCount
    }

    suspend fun incrementHabit(
        habitId: Long,
        date: LocalDate = LocalDate.now(),
        delta: Int = 1,
        note: String? = null
    ): Int = withContext(Dispatchers.IO) {
        val epochDay = date.toEpochDay()
        val existingLog = habitDao.getLogForDate(habitId, epochDay)
        val currentCount = existingLog?.count ?: 0
        val nextCount = (currentCount + delta).coerceAtLeast(0)
        val mergedNote = when {
            !note.isNullOrBlank() -> note
            else -> existingLog?.note.orEmpty()
        }
        writeCountInternal(habitId, epochDay, nextCount, mergedNote, existingLog?.id ?: 0L)
        HabitWidgetUpdater.updateAllWidgets(appContext)
        nextCount
    }

    suspend fun setHabitCount(
        habitId: Long,
        date: LocalDate = LocalDate.now(),
        count: Int,
        note: String? = null
    ) = withContext(Dispatchers.IO) {
        val epochDay = date.toEpochDay()
        val existingLog = habitDao.getLogForDate(habitId, epochDay)
        val clampedCount = count.coerceAtLeast(0)
        val mergedNote = note ?: existingLog?.note.orEmpty()
        writeCountInternal(habitId, epochDay, clampedCount, mergedNote, existingLog?.id ?: 0L)
        HabitWidgetUpdater.updateAllWidgets(appContext)
    }

    private suspend fun writeCountInternal(
        habitId: Long,
        epochDay: Long,
        count: Int,
        note: String,
        existingId: Long
    ) {
        if (count <= 0 && note.isBlank()) {
            habitDao.deleteLogForDate(habitId, epochDay)
        } else {
            habitDao.upsertLog(
                HabitLogEntity(
                    id = existingId,
                    habitId = habitId,
                    dateEpochDay = epochDay,
                    count = count.coerceAtLeast(0),
                    note = note,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    /** Entry point for the automation API: broadcasts (`HabitApiReceiver`) and deep links. */
    suspend fun logByProgrammaticApi(
        slugOrId: String,
        mode: String = "increment",
        delta: Int = 1,
        count: Int? = null,
        date: LocalDate = LocalDate.now(),
        note: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        val cleanedKey = slugOrId.trim().removePrefix("#")
        if (cleanedKey.isEmpty()) return@withContext false

        val habit = habitDao.getHabitBySlug(cleanedKey)
            ?: cleanedKey.toLongOrNull()?.let { habitDao.getHabitById(it) }
            ?: return@withContext false

        val normalizedMode = mode.trim().lowercase(Locale.US)
        when {
            normalizedMode == "set" || (count != null && normalizedMode != "increment" && normalizedMode != "toggle") -> {
                setHabitCount(
                    habitId = habit.id,
                    date = date,
                    count = (count ?: habit.targetPerDay).coerceAtLeast(0),
                    note = note
                )
            }

            normalizedMode == "toggle" || normalizedMode == "smart" -> {
                smartCheckIn(
                    habitId = habit.id,
                    date = date,
                    note = note,
                    alwaysIncrement = false
                )
            }

            count != null && delta == 1 && normalizedMode == "increment" -> {
                // If caller explicitly passed `count` without specifying mode="set", honour `count`
                setHabitCount(
                    habitId = habit.id,
                    date = date,
                    count = count.coerceAtLeast(0),
                    note = note
                )
            }

            else -> {
                incrementHabit(
                    habitId = habit.id,
                    date = date,
                    delta = delta,
                    note = note
                )
            }
        }
        true
    }
}
