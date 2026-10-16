package app.tessera.habit.data.model

data class HabitStats(
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val totalCompletions: Int = 0,
    val totalActiveDays: Int = 0,
    val completionRate30Days: Float = 0f,
    val weeklyCompletedDays: Int = 0,
    val weekdayDistribution: List<Int> = List(7) { 0 },
    val monthlyCompletions: Map<String, Int> = emptyMap()
) {
    companion object {
        /**
         * Maps a daily completion count and daily target to a GitHub heatmap intensity stop (0..4).
         */
        fun calculateIntensityLevel(count: Int, targetPerDay: Int): Int {
            if (count <= 0) return 0
            val safeTarget = targetPerDay.coerceAtLeast(1)
            if (safeTarget == 1) {
                return 4
            }
            val ratio = count.toFloat() / safeTarget.toFloat()
            return when {
                ratio >= 1.0f -> 4
                ratio >= 0.67f -> 3
                ratio >= 0.34f -> 2
                else -> 1
            }
        }
    }
}
