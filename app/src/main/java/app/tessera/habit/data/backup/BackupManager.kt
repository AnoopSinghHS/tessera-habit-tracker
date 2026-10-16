package app.tessera.habit.data.backup

import android.content.Context
import android.net.Uri
import app.tessera.habit.data.local.HabitEntity
import app.tessera.habit.data.model.BackupHabitDto
import app.tessera.habit.data.model.BackupHabitLogDto
import app.tessera.habit.data.model.TesseraBackupPayload
import app.tessera.habit.data.repository.TesseraRepository
import app.tessera.habit.widget.HabitWidgetUpdater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate

class BackupManager(
    context: Context,
    private val repository: TesseraRepository
) {
    constructor(repository: TesseraRepository) : this(repository.appContext, repository)

    private val appContext = context.applicationContext

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun exportJson(): String = exportToJsonString()

    suspend fun importJson(rawJson: String, replaceExisting: Boolean = false): Int {
        return importFromJsonString(rawJson, replaceExisting).getOrDefault(0)
    }

    suspend fun exportToJsonString(): String = withContext(Dispatchers.IO) {
        val habitsWithLogs = repository.getAllHabitsWithLogsOnce()
        val payload = TesseraBackupPayload(
            version = 1,
            exportedAtEpochMs = System.currentTimeMillis(),
            appId = "app.tessera.habit",
            habits = habitsWithLogs.map { item ->
                BackupHabitDto(
                    slug = item.habit.slug,
                    title = item.habit.title,
                    description = item.habit.description,
                    category = item.habit.category,
                    iconKey = item.habit.iconKey,
                    colorKey = item.habit.colorKey,
                    unitLabel = item.habit.unitLabel,
                    targetPerDay = item.habit.targetPerDay,
                    weeklyGoalDays = item.habit.weeklyGoalDays,
                    isArchived = item.habit.isArchived,
                    sortOrder = item.habit.sortOrder,
                    createdAt = item.habit.createdAt,
                    logs = item.logs
                        .sortedBy { it.dateEpochDay }
                        .map { log ->
                            BackupHabitLogDto(
                                dateIso = LocalDate.ofEpochDay(log.dateEpochDay).toString(),
                                dateEpochDay = log.dateEpochDay,
                                count = log.count,
                                note = log.note,
                                updatedAt = log.updatedAt
                            )
                        }
                )
            }
        )
        json.encodeToString(payload)
    }

    suspend fun exportToUri(uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val jsonContent = exportToJsonString()
            appContext.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(jsonContent.toByteArray(Charsets.UTF_8))
                outputStream.flush()
            } ?: error("Unable to open output stream for $uri")
            repository.getAllHabitsWithLogsOnce().size
        }
    }

    suspend fun importFromJsonString(
        rawJson: String,
        replaceExisting: Boolean = false
    ): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val payload = json.decodeFromString<TesseraBackupPayload>(rawJson)
            if (replaceExisting) {
                repository.clearAllData()
            }

            var importedCount = 0
            for (dto in payload.habits) {
                val existing = repository.getHabitWithLogsBySlugOnce(dto.slug)
                val habitId = repository.saveHabit(
                    HabitEntity(
                        id = existing?.habit?.id ?: 0L,
                        slug = dto.slug,
                        title = dto.title,
                        description = dto.description,
                        category = dto.category,
                        iconKey = dto.iconKey,
                        colorKey = dto.colorKey,
                        unitLabel = dto.unitLabel,
                        targetPerDay = dto.targetPerDay,
                        weeklyGoalDays = dto.weeklyGoalDays,
                        isArchived = dto.isArchived,
                        sortOrder = dto.sortOrder,
                        createdAt = dto.createdAt
                    )
                )
                for (logDto in dto.logs) {
                    val date = runCatching { LocalDate.parse(logDto.dateIso) }
                        .getOrElse { LocalDate.ofEpochDay(logDto.dateEpochDay) }
                    repository.setHabitCount(
                        habitId = habitId,
                        date = date,
                        count = logDto.count,
                        note = logDto.note
                    )
                }
                importedCount++
            }

            HabitWidgetUpdater.updateAllWidgets(appContext)
            importedCount
        }
    }

    suspend fun importFromUri(
        uri: Uri,
        replaceExisting: Boolean = false
    ): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val rawJson = appContext.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader(Charsets.UTF_8).readText()
            } ?: error("Unable to open input stream for $uri")
            importFromJsonString(rawJson, replaceExisting).getOrThrow()
        }
    }
}
