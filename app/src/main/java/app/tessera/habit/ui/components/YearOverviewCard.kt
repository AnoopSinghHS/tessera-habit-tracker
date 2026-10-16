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
 * Aggregated 52-Week Contribution Matrix Card.
 * Combines daily completions across all active habits into a unified tactile year overview.
 */
@Composable
fun YearOverviewCard(
    habitsWithLogs: List<HabitWithLogs>,
    modifier: Modifier = Modifier,
    palette: HabitPaletteSpec = HabitPalette.CyberAmber,
    today: LocalDate = LocalDate.now()
) {
    val colors = TesseraTheme.colors
    var selectedDate by remember { mutableStateOf(today) }

    val aggregatedByEpochDay = remember(habitsWithLogs) {
        val map = HashMap<Long, Int>()
        habitsWithLogs.forEach { hwl ->
            hwl.logs.forEach { log ->
                if (log.count > 0) {
                    map[log.dateEpochDay] = (map[log.dateEpochDay] ?: 0) + 1
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
        shape = RoundedCornerShape(18.dp),
        color = colors.surfaceCard,
        border = BorderStroke(1.dp, colors.borderHairline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Annual Contribution Matrix",
                        style = TesseraType.CardSubtitle,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$totalRawCheckIns check-ins",
                        style = TesseraType.SectionTitle,
                        color = colors.textPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$activeDaysYear / 365 days",
                        style = TesseraType.TelemetryBadge,
                        color = palette.colorForLevel(4, colors.isDark)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Active days",
                        style = TesseraType.MicroAxis,
                        color = colors.textMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            GithubHeatmapGrid(
                countsByEpochDay = aggregatedByEpochDay,
                targetPerDay = dailyTargetHabits,
                palette = palette,
                weeks = 52,
                cellSize = 11.5.dp,
                cellGap = 3.dp,
                cornerRadius = 3.dp,
                showWeekdayLabels = true,
                showMonthLabels = true,
                scrollable = true,
                today = today,
                selectedDate = selectedDate,
                onDateClick = { clicked -> selectedDate = clicked }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Selected Day Summary + Intensity Legend Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.surfaceElevated)
                    .border(1.dp, colors.borderHairline, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${selectedDate.format(dateFormatter)} · $selectedHabitsLogged of ${habitsWithLogs.size} habits",
                    style = TesseraType.MicroAxis,
                    color = colors.textSecondary
                )

                HeatmapIntensityLegend(palette = palette)
            }
        }
    }
}
