package app.tessera.habit.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import app.tessera.habit.data.model.HabitStats
import app.tessera.habit.data.model.HabitWithLogs
import app.tessera.habit.ui.components.HabitIcons
import app.tessera.habit.ui.components.StreakPillBadge
import app.tessera.habit.ui.components.YearOverviewCard
import app.tessera.habit.ui.theme.HabitPalette
import app.tessera.habit.ui.theme.TesseraTheme
import app.tessera.habit.ui.theme.TesseraType
import app.tessera.habit.ui.viewmodel.TesseraUiState
import java.time.LocalDate
import kotlin.math.roundToInt

private data class HabitTelemetryRow(
    val habitWithLogs: HabitWithLogs,
    val stats: HabitStats
)

@Composable
fun AnalyticsScreen(
    uiState: TesseraUiState,
    onOpenHabitDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = TesseraTheme.colors
    val today = remember { LocalDate.now() }
    val activeHabits = uiState.activeHabits

    val telemetryRows = remember(activeHabits, today) {
        activeHabits.map { hwl ->
            HabitTelemetryRow(
                habitWithLogs = hwl,
                stats = hwl.computeStats(today)
            )
        }.sortedByDescending { it.stats.currentStreak * 100 + (it.stats.completionRate30Days * 100).roundToInt() }
    }

    val globalConsistencyPct = remember(telemetryRows) {
        if (telemetryRows.isEmpty()) 0
        else ((telemetryRows.sumOf { it.stats.completionRate30Days.toDouble() } / telemetryRows.size) * 100.0).roundToInt()
    }
    val topActiveStreak = remember(telemetryRows) {
        telemetryRows.maxOfOrNull { it.stats.currentStreak } ?: 0
    }
    val topBestStreak = remember(telemetryRows) {
        telemetryRows.maxOfOrNull { it.stats.bestStreak } ?: 0
    }
    val totalCheckIns = remember(telemetryRows) {
        telemetryRows.sumOf { it.stats.totalCompletions }
    }

    val globalWeekdayDistribution = remember(telemetryRows) {
        val acc = IntArray(7)
        telemetryRows.forEach { row ->
            row.stats.weekdayDistribution.forEachIndexed { idx, v ->
                if (idx in 0..6) acc[idx] += v
            }
        }
        acc.toList()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.surfaceAbyss),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Editorial Header
        item(key = "telemetry_header") {
            Column {
                Text(
                    text = "PORTFOLIO INSTRUMENTATION · ${activeHabits.size} ACTIVE RITUALS",
                    style = TesseraType.TelemetryBadge,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Telemetry",
                    style = TesseraType.DisplayMomentum,
                    color = colors.textPrimary
                )
            }
        }

        // 2. Aggregated 52-Week Contribution Matrix
        item(key = "year_overview_card") {
            YearOverviewCard(
                habitsWithLogs = activeHabits,
                palette = HabitPalette.EmeraldMatrix,
                today = today
            )
        }

        // 3. Global 2x2 Bento Summary
        item(key = "global_bento_summary") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlobalStatTile(
                        label = "CONSISTENCY INDEX",
                        value = "$globalConsistencyPct%",
                        subtitle = "30-day portfolio avg",
                        highlight = true,
                        modifier = Modifier.weight(1f)
                    )
                    GlobalStatTile(
                        label = "PEAK ACTIVE STREAK",
                        value = "$topActiveStreak DAYS",
                        subtitle = "Current leader",
                        highlight = false,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlobalStatTile(
                        label = "ALL-TIME RECORD",
                        value = "$topBestStreak DAYS",
                        subtitle = "Longest sequence",
                        highlight = false,
                        modifier = Modifier.weight(1f)
                    )
                    GlobalStatTile(
                        label = "LIFETIME VOLUME",
                        value = "$totalCheckIns",
                        subtitle = "Total logged units",
                        highlight = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 4. Global Weekday Rhythm Distribution
        item(key = "weekday_rhythm") {
            GlobalWeekdayRhythmCard(weekdayCounts = globalWeekdayDistribution)
        }

        // 5. Streak Leaderboard & Ritual Breakdown Header
        item(key = "leaderboard_header") {
            Text(
                text = "STREAK LEADERBOARD & RITUAL BREAKDOWN",
                style = TesseraType.TelemetryBadge,
                color = colors.textSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        items(
            items = telemetryRows,
            key = { it.habitWithLogs.habit.id }
        ) { row ->
            HabitBreakdownRowCard(
                row = row,
                onClick = { onOpenHabitDetail(row.habitWithLogs.habit.id) }
            )
        }
    }
}

@Composable
private fun GlobalStatTile(
    label: String,
    value: String,
    subtitle: String,
    highlight: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = TesseraTheme.colors
    val emerald = HabitPalette.EmeraldMatrix.colorForLevel(4, colors.isDark)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = colors.surfaceCard,
        border = BorderStroke(1.dp, colors.borderHairline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = label,
                style = TesseraType.MicroAxis,
                color = colors.textMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = TesseraType.TelemetryHero,
                color = if (highlight) emerald else colors.textPrimary
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = subtitle,
                style = TesseraType.CardSubtitle,
                color = colors.textSecondary
            )
        }
    }
}

@Composable
private fun GlobalWeekdayRhythmCard(weekdayCounts: List<Int>) {
    val colors = TesseraTheme.colors
    val palette = HabitPalette.CobaltPulse
    val maxCount = (weekdayCounts.maxOrNull() ?: 1).coerceAtLeast(1)
    val peakIndex = weekdayCounts.indices.maxByOrNull { weekdayCounts[it] } ?: 0
    val dayNames = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")

    Surface(
        modifier = Modifier.fillMaxWidth(),
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "WEEKDAY RHYTHM CADENCE",
                    style = TesseraType.TelemetryBadge,
                    color = colors.textSecondary
                )
                Text(
                    text = "PEAK: ${dayNames[peakIndex]}",
                    style = TesseraType.MicroAxis,
                    color = palette.colorForLevel(4, colors.isDark)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(98.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                for (i in 0..6) {
                    val count = weekdayCounts.getOrElse(i) { 0 }
                    val ratio = (count.toFloat() / maxCount.toFloat()).coerceIn(0.08f, 1f)
                    val level = when {
                        count == 0 -> 0
                        ratio >= 0.85f -> 4
                        ratio >= 0.55f -> 3
                        else -> 2
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = count.toString(),
                            style = TesseraType.MicroAxis,
                            color = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.68f)
                                .height(58.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(colors.surfaceRecessed),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(ratio)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(palette.colorForLevel(level, colors.isDark))
                            )
                        }
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = dayNames[i],
                            style = TesseraType.MicroAxis,
                            color = if (i == peakIndex) palette.colorForLevel(4, colors.isDark) else colors.textMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HabitBreakdownRowCard(
    row: HabitTelemetryRow,
    onClick: () -> Unit
) {
    val colors = TesseraTheme.colors
    val habit = row.habitWithLogs.habit
    val stats = row.stats
    val palette = remember(habit.colorKey) { HabitPalette.resolve(habit.colorKey) }
    val ratePct = (stats.completionRate30Days * 100f).roundToInt()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = colors.surfaceCard,
        border = BorderStroke(1.dp, colors.borderHairline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(palette.colorForLevel(1, colors.isDark))
                        .border(1.dp, palette.level3.copy(alpha = 0.35f), RoundedCornerShape(9.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = HabitIcons.resolve(habit.iconKey),
                        contentDescription = habit.title,
                        tint = palette.colorForLevel(4, colors.isDark),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = habit.title,
                        style = TesseraType.CardTitle,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "#${habit.slug} · BEST ${stats.bestStreak}D · ${stats.totalCompletions} LOGS",
                        style = TesseraType.MicroAxis,
                        color = colors.textMuted
                    )
                }

                StreakPillBadge(
                    streakDays = stats.currentStreak,
                    palette = palette,
                    isActive = stats.currentStreak > 0
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(colors.surfaceRecessed)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(stats.completionRate30Days.coerceIn(0.03f, 1f))
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(palette.colorForLevel(4, colors.isDark))
                    )
                }

                Text(
                    text = "30D $ratePct%",
                    style = TesseraType.MicroAxis,
                    color = palette.colorForLevel(4, colors.isDark)
                )
            }
        }
    }
}
