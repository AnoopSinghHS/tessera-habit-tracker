package app.tessera.habit.data.model

import kotlinx.serialization.Serializable

@Serializable
data class TesseraBackupPayload(
    val version: Int = 1,
    val exportedAtEpochMs: Long = System.currentTimeMillis(),
    val appId: String = "app.tessera.habit",
    val habits: List<BackupHabitDto> = emptyList()
)

@Serializable
data class BackupHabitDto(
    val slug: String,
    val title: String,
    val description: String = "",
    val category: String = "General",
    val iconKey: String = "bolt",
    val colorKey: String = "emerald",
    val unitLabel: String = "times",
    val targetPerDay: Int = 1,
    val weeklyGoalDays: Int = 7,
    val isArchived: Boolean = false,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val logs: List<BackupHabitLogDto> = emptyList()
)

@Serializable
data class BackupHabitLogDto(
    val dateIso: String,
    val dateEpochDay: Long,
    val count: Int,
    val note: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
