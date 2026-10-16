package app.tessera.habit.data.model

import androidx.room.Embedded
import androidx.room.Relation
import app.tessera.habit.data.local.HabitEntity
import app.tessera.habit.data.local.HabitLogEntity
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

data class HabitWithLogs(
    @Embedded
    val habit: HabitEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "habitId"
    )
    val logs: List<HabitLogEntity>
) {
    fun logsByEpochDay(): Map<Long, HabitLogEntity> {
        return logs.associateBy { it.dateEpochDay }
    }

    fun countForDate(date: LocalDate): Int {
        val epochDay = date.toEpochDay()
        return logs.firstOrNull { it.dateEpochDay == epochDay }?.count ?: 0
    }

    fun noteForDate(date: LocalDate): String {
        val epochDay = date.toEpochDay()
        return logs.firstOrNull { it.dateEpochDay == epochDay }?.note.orEmpty()
    }

    fun isCompletedOn(date: LocalDate): Boolean {
        return countForDate(date) >= habit.targetPerDay.coerceAtLeast(1)
    }

    fun intensityLevelForDate(date: LocalDate): Int {
        return HabitStats.calculateIntensityLevel(
            count = countForDate(date),
            targetPerDay = habit.targetPerDay
        )
    }

    fun computeStats(today: LocalDate = LocalDate.now()): HabitStats {
        val target = habit.targetPerDay.coerceAtLeast(1)
        val activeLogs = logs.filter { it.count > 0 }
        val completedEpochDays = activeLogs
            .filter { it.count >= target }
            .map { it.dateEpochDay }
            .toSortedSet()

        val totalCompletions = activeLogs.sumOf { it.count }
        val totalActiveDays = activeLogs.size

        // Current streak: count consecutive completed days ending today (or yesterday if today isn't completed yet)
        val todayEpoch = today.toEpochDay()
        var streakCursor = if (completedEpochDays.contains(todayEpoch)) {
            todayEpoch
        } else {
            todayEpoch - 1L
        }
        var currentStreak = 0
        while (completedEpochDays.contains(streakCursor)) {
            currentStreak++
            streakCursor--
        }

        // Best streak across all historical completed days
        var bestStreak = 0
        var runningStreak = 0
        var prevEpoch: Long? = null
        for (epochDay in completedEpochDays) {
            if (prevEpoch != null && epochDay == prevEpoch + 1L) {
                runningStreak++
            } else {
                runningStreak = 1
            }
            if (runningStreak > bestStreak) {
                bestStreak = runningStreak
            }
            prevEpoch = epochDay
        }

        // 30-day completion rate
        var completedLast30 = 0
        for (offset in 0 until 30) {
            val d = todayEpoch - offset
            if (completedEpochDays.contains(d)) {
                completedLast30++
            }
        }
        val completionRate30Days = (completedLast30.toFloat() / 30f).coerceIn(0f, 1f)

        // Current week completed days (Monday .. Sunday)
        val startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        var weeklyCompletedDays = 0
        for (i in 0..6) {
            val dayEpoch = startOfWeek.plusDays(i.toLong()).toEpochDay()
            if (completedEpochDays.contains(dayEpoch)) {
                weeklyCompletedDays++
            }
        }

        // Weekday distribution (index 0 = Monday .. 6 = Sunday)
        val weekdayCounts = IntArray(7)
        val monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM")
        val monthlyMap = linkedMapOf<String, Int>()

        for (log in activeLogs) {
            val date = LocalDate.ofEpochDay(log.dateEpochDay)
            val dowIndex = date.dayOfWeek.value - 1 // Monday=0 .. Sunday=6
            if (dowIndex in 0..6) {
                weekdayCounts[dowIndex] += 1
            }
            val monthKey = date.format(monthFormatter)
            monthlyMap[monthKey] = (monthlyMap[monthKey] ?: 0) + log.count
        }

        return HabitStats(
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            totalCompletions = totalCompletions,
            totalActiveDays = totalActiveDays,
            completionRate30Days = completionRate30Days,
            weeklyCompletedDays = weeklyCompletedDays,
            weekdayDistribution = weekdayCounts.toList(),
            monthlyCompletions = monthlyMap
        )
    }
}
