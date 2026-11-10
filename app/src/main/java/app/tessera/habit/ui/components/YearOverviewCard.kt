package app.tessera.habit.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.tessera.habit.data.model.HabitWithLogs
import app.tessera.habit.ui.theme.TesseraTheme
import app.tessera.habit.ui.theme.TesseraType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Epoch days on which the habit's daily target was met (same rule as `isCompletedOn`). */
internal fun HabitWithLogs.completedEpochDays(): Set<Long> {
    val target = habit.targetPerDay.coerceAtLeast(1)
    val days = HashSet<Long>()
    for (log in logs) {
        if (log.count >= target) days.add(log.dateEpochDay)
    }
    return days
}

/**
 * The first day the habit counts towards a completion rate: the day it was created,
 * or its earliest check-in if one was logged for a date before that.
 */
internal fun HabitWithLogs.firstTrackedEpochDay(): Long {
    val createdDay = Instant.ofEpochMilli(habit.createdAt)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .toEpochDay()
    var first = createdDay
    for (log in logs) {
        if (log.count > 0 && log.dateEpochDay < first) first = log.dateEpochDay
    }
    return first
}

private class TrackedHabit(
    val firstDay: Long,
    val completedDays: Set<Long>
)

private const val YEAR_WEEKS = 52

/** Share of habits done on a day as a heatmap level: 0 none, 4 all, quartiles between. */
private fun levelForShare(done: Int, tracked: Int): Int {
    if (done <= 0 || tracked <= 0) return 0
    if (done >= tracked) return 4
    val share = done.toFloat() / tracked.toFloat()
    return when {
        share >= 0.5f -> 3
        share >= 0.25f -> 2
        else -> 1
    }
}

/**
 * One heatmap for all habits together: each day is shaded by the share of the given
 * habits that were completed that day. Habits only count from the day they started.
 */
@Composable
fun YearOverviewCard(
    habitsWithLogs: List<HabitWithLogs>,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now()
) {
    val colors = TesseraTheme.colors
    var selectedDate by remember { mutableStateOf(today) }

    val tracked = remember(habitsWithLogs) {
        habitsWithLogs.map { hwl ->
            TrackedHabit(
                firstDay = hwl.firstTrackedEpochDay(),
                completedDays = hwl.completedEpochDays()
            )
        }
    }

    // The grid is fed levels directly: a "count" of 0..4 against a target of 4
    // maps one-to-one onto the five heatmap steps.
    val levelByEpochDay = remember(tracked, today) {
        val map = HashMap<Long, Int>()
        val todayEpoch = today.toEpochDay()
        val firstEpoch = todayEpoch - (YEAR_WEEKS + 1) * 7L
        var day = firstEpoch
        while (day <= todayEpoch) {
            var trackedCount = 0
            var doneCount = 0
            for (habit in tracked) {
                if (day >= habit.firstDay) {
                    trackedCount++
                    if (habit.completedDays.contains(day)) doneCount++
                }
            }
            val level = levelForShare(doneCount, trackedCount)
            if (level > 0) map[day] = level
            day++
        }
        map
    }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEE d MMM", Locale.getDefault()) }
    val selectedEpoch = selectedDate.toEpochDay()
    val selectedTracked = tracked.count { selectedEpoch >= it.firstDay }
    val selectedDone = tracked.count {
        selectedEpoch >= it.firstDay && it.completedDays.contains(selectedEpoch)
    }
    val selectedSummary = when {
        selectedTracked == 0 -> "${selectedDate.format(dateFormatter)} · no habits yet"
        selectedTracked == 1 -> "${selectedDate.format(dateFormatter)} · $selectedDone of 1 habit done"
        else -> "${selectedDate.format(dateFormatter)} · $selectedDone of $selectedTracked habits done"
    }

    TesseraCard(modifier = modifier) {
        Text(
            text = "This year",
            style = TesseraType.Label,
            color = colors.textPrimary
        )

        Spacer(modifier = Modifier.height(TesseraDimens.SpaceM))

        GithubHeatmapGrid(
            countsByEpochDay = levelByEpochDay,
            targetPerDay = 4,
            modifier = Modifier.fillMaxWidth(),
            weeks = YEAR_WEEKS,
            showWeekdayLabels = true,
            showMonthLabels = true,
            scrollable = true,
            today = today,
            selectedDate = selectedDate,
            onDateClick = { clicked -> selectedDate = clicked }
        )

        Spacer(modifier = Modifier.height(TesseraDimens.SpaceM))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeatmapIntensityLegend()
        }

        Spacer(modifier = Modifier.height(TesseraDimens.SpaceS))

        Text(
            text = selectedSummary,
            style = TesseraType.Supporting,
            color = colors.textSecondary
        )
    }
}
