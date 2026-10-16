package app.tessera.habit.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.tessera.habit.data.model.HabitStats
import app.tessera.habit.data.model.HabitWithLogs
import app.tessera.habit.ui.components.HabitIcons
import app.tessera.habit.ui.components.StreakPillBadge
import app.tessera.habit.ui.components.YearOverviewCard
import app.tessera.habit.ui.theme.HabitPalette
import app.tessera.habit.ui.theme.HabitPaletteSpec
import app.tessera.habit.ui.theme.TesseraTheme
import app.tessera.habit.ui.theme.TesseraType
import app.tessera.habit.ui.viewmodel.TesseraUiState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

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
        }.sortedByDescending {
            it.stats.currentStreak * 100 + (it.stats.completionRate30Days * 100).roundToInt()
        }
    }

    val globalConsistencyRatio = remember(telemetryRows) {
        if (telemetryRows.isEmpty()) 0f
        else (telemetryRows.sumOf { it.stats.completionRate30Days.toDouble() } / telemetryRows.size).toFloat()
    }
    val globalConsistencyPct = (globalConsistencyRatio * 100f).roundToInt()

    val topActiveStreak = remember(telemetryRows) {
        telemetryRows.maxOfOrNull { it.stats.currentStreak } ?: 0
    }
    val topBestStreak = remember(telemetryRows) {
        telemetryRows.maxOfOrNull { it.stats.bestStreak } ?: 0
    }
    val totalCheckIns = remember(telemetryRows) {
        telemetryRows.sumOf { it.stats.totalCompletions }
    }

    // Reorder weekday distribution (Mon..Sun -> Sun..Sat) for the "S M T W T F S" Braun chart
    val sundayFirstWeekdayCounts = remember(telemetryRows) {
        val monFirst = IntArray(7)
        telemetryRows.forEach { row ->
            row.stats.weekdayDistribution.forEachIndexed { idx, v ->
                if (idx in 0..6) monFirst[idx] += v
            }
        }
        // Index 6 is Sunday in Mon..Sun (0..6)
        listOf(
            monFirst[6], // S
            monFirst[0], // M
            monFirst[1], // T
            monFirst[2], // W
            monFirst[3], // T
            monFirst[4], // F
            monFirst[5]  // S
        )
    }

    val formattedDate = remember(today) {
        today.format(DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.US))
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.surfaceAbyss),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Centered Editorial Header (clearly explaining this screen's purpose)
        item(key = "insights_header") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Habit Insights & Telemetry",
                    style = TesseraType.EditorialDateHeader,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Long-term consistency & cadence across ${activeHabits.size} active habits",
                    style = TesseraType.CardSubtitle,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }

        // 2. Aggregated 52-Week Contribution Matrix (Warm Ochre / Botanical palette)
        item(key = "year_overview_card") {
            YearOverviewCard(
                habitsWithLogs = activeHabits,
                palette = HabitPalette.CyberAmber,
                today = today
            )
        }

        // 3. Signature Braun Horology Dial + 7-Day Vertical Pill Chart (matching Stitch concept!)
        item(key = "braun_dial_and_weekly_bars") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                BraunRadialConsistencyCard(
                    consistencyRatio = globalConsistencyRatio,
                    palette = HabitPalette.CyberAmber,
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                )

                BraunWeeklyPillChartCard(
                    sundayFirstCounts = sundayFirstWeekdayCounts,
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                )
            }
        }

        // 4. Global 2x2 Summary Bento
        item(key = "global_bento_summary") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GlobalStatTile(
                        label = "30-Day Consistency",
                        value = "$globalConsistencyPct%",
                        subtitle = "Rolling average",
                        highlight = true,
                        modifier = Modifier.weight(1f)
                    )
                    GlobalStatTile(
                        label = "Longest Active",
                        value = "$topActiveStreak days",
                        subtitle = "Current streak",
                        highlight = false,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GlobalStatTile(
                        label = "All-Time Best",
                        value = "$topBestStreak days",
                        subtitle = "Personal record",
                        highlight = false,
                        modifier = Modifier.weight(1f)
                    )
                    GlobalStatTile(
                        label = "Total Check-ins",
                        value = "$totalCheckIns",
                        subtitle = "All-time volume",
                        highlight = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 5. Habit Breakdown List
        item(key = "leaderboard_header") {
            Text(
                text = "Habit Breakdown",
                style = TesseraType.SectionTitle,
                color = colors.textPrimary,
                modifier = Modifier.padding(top = 6.dp)
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

/**
 * Braun Horology Radial Tick Dial (clarified with explicit title & percentage).
 * Renders 48 fine radial tick marks with rolling 30-day percentage in the center.
 */
@Composable
fun BraunRadialConsistencyCard(
    consistencyRatio: Float,
    palette: HabitPaletteSpec,
    modifier: Modifier = Modifier
) {
    val colors = TesseraTheme.colors
    val clampedRatio = consistencyRatio.coerceIn(0f, 1f)
    val pct = (clampedRatio * 100f).roundToInt()
    val activeAccent = palette.colorForLevel(4, colors.isDark)
    val warmTick = palette.colorForLevel(3, colors.isDark)
    val inactiveTick = colors.borderSpecular

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = colors.surfaceCard,
        border = BorderStroke(1.dp, colors.borderHairline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "30-DAY CONSISTENCY",
                    style = TesseraType.MicroAxis,
                    color = colors.textMuted
                )
                Text(
                    text = "Rolling rate",
                    style = TesseraType.CardSubtitle,
                    color = colors.textSecondary
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val totalTicks = 48
                    val litTicks = (clampedRatio * totalTicks).roundToInt().coerceIn(0, totalTicks)
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val outerRadius = (size.minDimension / 2f) * 0.95f
                    val innerRadius = outerRadius * 0.78f
                    val strokeWidthPx = 1.6.dp.toPx()

                    for (i in 0 until totalTicks) {
                        val angleDeg = -90.0 + (i * (360.0 / totalTicks))
                        val angleRad = Math.toRadians(angleDeg)
                        val cosA = cos(angleRad).toFloat()
                        val sinA = sin(angleRad).toFloat()

                        val start = Offset(
                            x = center.x + innerRadius * cosA,
                            y = center.y + innerRadius * sinA
                        )
                        val end = Offset(
                            x = center.x + outerRadius * cosA,
                            y = center.y + outerRadius * sinA
                        )

                        val tickColor = when {
                            i < litTicks / 2 -> activeAccent
                            i < litTicks -> warmTick
                            else -> inactiveTick
                        }

                        drawLine(
                            color = tickColor,
                            start = start,
                            end = end,
                            strokeWidth = strokeWidthPx,
                            cap = StrokeCap.Round
                        )
                    }
                }

                // Center Typography: Big Percentage + "RATE"
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$pct%",
                        style = TesseraType.TelemetryHero,
                        color = activeAccent
                    )
                    Text(
                        text = "CONSISTENCY",
                        style = TesseraType.MicroAxis,
                        color = colors.textMuted
                    )
                }
            }
        }
    }
}

/**
 * 7-Bar Vertical Pill Chart ("S M T W T F S") with explicit title and day counts.
 */
@Composable
fun BraunWeeklyPillChartCard(
    sundayFirstCounts: List<Int>,
    modifier: Modifier = Modifier
) {
    val colors = TesseraTheme.colors
    val maxCount = (sundayFirstCounts.maxOrNull() ?: 1).coerceAtLeast(1)
    val dayInitials = listOf("S", "M", "T", "W", "T", "F", "S")
    val fillPillColor = colors.textPrimary

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = colors.surfaceCard,
        border = BorderStroke(1.dp, colors.borderHairline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "WEEKDAY RHYTHM",
                    style = TesseraType.MicroAxis,
                    color = colors.textMuted
                )
                Text(
                    text = "Cadence by day",
                    style = TesseraType.CardSubtitle,
                    color = colors.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                for (i in 0..6) {
                    val count = sundayFirstCounts.getOrElse(i) { 0 }
                    val fillFraction = if (maxCount > 0 && count > 0) {
                        (count.toFloat() / maxCount.toFloat()).coerceIn(0.18f, 1f)
                    } else {
                        0.08f
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        if (count > 0) {
                            Text(
                                text = count.toString(),
                                style = TesseraType.MicroAxis,
                                color = colors.textMuted,
                                fontSize = 8.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                        }

                        Box(
                            modifier = Modifier
                                .width(7.dp)
                                .weight(1f, fill = false)
                                .height(52.dp)
                                .clip(CircleShape)
                                .background(colors.surfaceRecessed),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(7.dp)
                                    .fillMaxHeight(fillFraction)
                                    .clip(CircleShape)
                                    .background(if (count > 0) fillPillColor else colors.surfaceRecessed)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = dayInitials[i],
                            style = TesseraType.MicroAxis,
                            color = if (count > 0) colors.textPrimary else colors.textMuted
                        )
                    }
                }
            }
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
    val accent = HabitPalette.EmeraldMatrix.colorForLevel(4, colors.isDark)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceCard,
        border = BorderStroke(1.dp, colors.borderHairline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                style = TesseraType.MicroAxis,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = TesseraType.TelemetryHero,
                color = if (highlight) accent else colors.textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = TesseraType.CardSubtitle,
                color = colors.textMuted
            )
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
    val accent = palette.colorForLevel(4, colors.isDark)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = HabitIcons.resolve(habit.iconKey),
                    contentDescription = habit.title,
                    tint = colors.textPrimary,
                    modifier = Modifier.size(22.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = habit.title,
                        style = TesseraType.CardTitle,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Best ${stats.bestStreak} days · ${stats.totalCompletions} total check-ins",
                        style = TesseraType.StreakLabel,
                        color = colors.textSecondary
                    )
                }

                StreakPillBadge(
                    streakDays = stats.currentStreak,
                    palette = palette,
                    isActive = stats.currentStreak > 0
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

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
                            .fillMaxWidth(stats.completionRate30Days.coerceIn(0.04f, 1f))
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(accent)
                    )
                }

                Text(
                    text = "$ratePct%",
                    style = TesseraType.MicroAxis,
                    color = accent
                )
            }
        }
    }
}
