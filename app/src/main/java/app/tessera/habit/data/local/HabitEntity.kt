package app.tessera.habit.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Locale

@Entity(
    tableName = "habits",
    indices = [
        Index(value = ["slug"], unique = true)
    ]
)
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
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
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun generateSlug(title: String): String {
            val cleaned = title
                .trim()
                .lowercase(Locale.US)
                .replace(Regex("[^a-z0-9]+"), "-")
                .trim('-')
            return if (cleaned.isEmpty()) "habit-${System.currentTimeMillis() % 10000}" else cleaned
        }
    }
}
