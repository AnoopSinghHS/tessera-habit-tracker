package app.tessera.habit.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import app.tessera.habit.data.model.HabitWithLogs
import app.tessera.habit.ui.theme.HabitPalette
import app.tessera.habit.ui.theme.HabitPaletteSpec
import app.tessera.habit.ui.theme.TesseraTheme
import app.tessera.habit.ui.theme.TesseraType
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Aggregated 52-Week Portfolio Contribution Matrix Card.
 * Combines daily completions across all active habits into a unified GitHub-style year overview.
 */
@Composable
fun YearOverviewCard(
    habitsWithLogs: List<HabitWithLogs>,
    modifier: Modifier = Modifier,
    palette: HabitPaletteSpec = HabitPalette.EmeraldMatrix,
    today: LocalDate = LocalDate.now()
) {
    val colors = TesseraTheme.colors
    var selectedDate by remember { mutableStateOf(today) }

    val aggregatedByEpochDay = remember(habitsWithLogs) {
        val map = HashMap<Long, Int>()
        habitsWithLogs.forEach { hwl ->
            val target = hwl.habit.targetPerDay.coerceAtLeast(1)
            hwl.logs.forEach { log ->
                if (log.count > 0) {
                    // Weight each habit normalized to 1 completion unit when target met, or raw count
                    val normalized = if (log.count >= target) 1 else 1
                    map[log.dateEpochDay] = (map[log.dateEpochDay] ?: 0) + normalized
                }
            }
        }
        map
    }

    val totalRawCheckIns = remember(habitsWithLogs) {
        habitsWithLogs.sumOf { hwl -> hwl.logs.sumOf { it.count } }
    }
    val activeDaysYear = remember(aggregatedByEpochDay, today) {
        val minEpoch = today.minusDays(364).toEpochDay()
        val maxEpoch = today.toEpochDay()
        aggregatedByEpochDay.count { (epoch, count) -> epoch in minEpoch..maxEpoch && count > 0 }
    }

    val dailyTargetHabits = habitsWithLogs.size.coerceAtLeast(1)
    val selectedEpoch = selectedDate.toEpochDay()
    val selectedHabitsLogged = aggregatedByEpochDay[selectedEpoch] ?: 0
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEE, MMM d, yyyy", Locale.US) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceCard,
        border = BorderStroke(1.dp, colors.borderHairline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "52-WEEK CONTRIBUTION MATRIX",
                        style = TesseraType.TelemetryBadge,
                        color = palette.colorForLevel(4, colors.isDark)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "$totalRawCheckIns Total Check-Ins",
                        style = TesseraType.CardTitle,
                        color = colors.textPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$activeDaysYear / 365 DAYS",
                        style = TesseraType.TelemetryBadge,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "ACTIVE FOOTPRINT",
                        style = TesseraType.MicroAxis,
                        color = colors.textMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            GithubHeatmapGrid(
                countsByEpochDay = aggregatedByEpochDay,
                targetPerDay = dailyTargetHabits,
                palette = palette,
                weeks = 52,
                cellSize = 11.dp,
                cellGap = 3.dp,
                cornerRadius = 2.5.dp,
                showWeekdayLabels = true,
                showMonthLabels = true,
                scrollable = true,
                today = today,
                selectedDate = selectedDate,
                onDateClick = { clicked -> selectedDate = clicked }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Selected Day Inspector + Intensity Legend Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surfaceElevated)
                    .border(1.dp, colors.borderHairline, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${selectedDate.format(dateFormatter).uppercase(Locale.US)} · $selectedHabitsLogged/${habitsWithLogs.size} RITUALS",
                    style = TesseraType.MicroAxis,
                    color = colors.textSecondary
                )

                HeatmapIntensityLegend(palette = palette)
            }
        }
    }
}
