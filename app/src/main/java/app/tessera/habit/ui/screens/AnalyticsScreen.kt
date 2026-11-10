package app.tessera.habit.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.tessera.habit.data.model.HabitWithLogs
import app.tessera.habit.ui.components.TesseraCard
import app.tessera.habit.ui.components.TesseraDimens
import app.tessera.habit.ui.components.TesseraDivider
import app.tessera.habit.ui.components.YearOverviewCard
import app.tessera.habit.ui.components.completedEpochDays
import app.tessera.habit.ui.components.firstTrackedEpochDay
import app.tessera.habit.ui.theme.TesseraTheme
import app.tessera.habit.ui.theme.TesseraType
import app.tessera.habit.ui.viewmodel.TesseraUiState
import java.time.LocalDate
import kotlin.math.roundToInt

private const val RECENT_DAYS = 30
private const val WEEKDAY_WEEKS = 12

/** Fewest tracked days a weekday needs before it is named as strongest or weakest. */
private const val MIN_DAYS_PER_WEEKDAY = 4

private val WeekdayLetters = listOf("M", "T", "W", "T", "F", "S", "S")
private val WeekdayNames = listOf(
    "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
)

/** Space left under the last card so it scrolls clear of the bottom bar. */
private val BottomBarClearance = TesseraDimens.SpaceXxl * 3

private class HabitProgressRow(
    val id: Long,
    val title: String,
    val currentStreak: Int,
    val doneDays: Int,
    val trackedDays: Int
)

private class ProgressSummary(
    val rows: List<HabitProgressRow>,
    val doneLast30: Int,
    val trackedLast30: Int,
    /** Index 0 = Monday .. 6 = Sunday, over the last [WEEKDAY_WEEKS] weeks. */
    val weekdayDone: IntArray,
    val weekdayTracked: IntArray
)

/**
 * Totals across all active habits. A habit only counts from the day it started
 * (its creation date, or its first check-in if that is earlier).
 */
private fun buildProgressSummary(habits: List<HabitWithLogs>, today: LocalDate): ProgressSummary {
    val todayEpoch = today.toEpochDay()
    val weekdayDone = IntArray(7)
    val weekdayTracked = IntArray(7)
    var doneLast30 = 0
    var trackedLast30 = 0
    val rows = ArrayList<HabitProgressRow>()

    for (hwl in habits) {
        val firstDay = hwl.firstTrackedEpochDay()
        val completed = hwl.completedEpochDays()

        var done = 0
        var tracked = 0
        for (offset in 0 until RECENT_DAYS) {
            val day = todayEpoch - offset
            if (day >= firstDay) {
                tracked++
                if (completed.contains(day)) done++
            }
        }
        doneLast30 += done
        trackedLast30 += tracked

        for (offset in 0 until WEEKDAY_WEEKS * 7) {
            val day = todayEpoch - offset
            if (day >= firstDay) {
                // Epoch day 0 was a Thursday, so shift by 3 to make Monday index 0.
                val weekday = (((day + 3) % 7 + 7) % 7).toInt()
                weekdayTracked[weekday]++
                if (completed.contains(day)) weekdayDone[weekday]++
            }
        }

        rows.add(
            HabitProgressRow(
                id = hwl.habit.id,
                title = hwl.habit.title,
                currentStreak = hwl.computeStats(today).currentStreak,
                doneDays = done,
                trackedDays = tracked
            )
        )
    }

    val sortedRows = rows.sortedWith(
        compareByDescending<HabitProgressRow> { it.currentStreak }
            .thenByDescending { it.doneDays }
            .thenBy { it.title }
    )

    return ProgressSummary(
        rows = sortedRows,
        doneLast30 = doneLast30,
        trackedLast30 = trackedLast30,
        weekdayDone = weekdayDone,
        weekdayTracked = weekdayTracked
    )
}

@Composable
fun AnalyticsScreen(
    uiState: TesseraUiState,
    onOpenHabitDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = TesseraTheme.colors
    val today = remember { LocalDate.now() }
    val activeHabits = uiState.activeHabits
    val summary = remember(activeHabits, today) { buildProgressSummary(activeHabits, today) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.surfaceAbyss)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = TesseraDimens.ScreenPadding)
            .padding(top = TesseraDimens.SpaceL, bottom = BottomBarClearance),
        verticalArrangement = Arrangement.spacedBy(TesseraDimens.SpaceM)
    ) {
        Text(
            text = "Progress",
            style = TesseraType.Headline,
            color = colors.textPrimary,
            modifier = Modifier.padding(
                horizontal = TesseraDimens.SpaceXs,
                vertical = TesseraDimens.SpaceS
            )
        )

        if (activeHabits.isEmpty()) {
            TesseraCard {
                Text(
                    text = "Add a habit on the Today tab and your progress will show up here.",
                    style = TesseraType.Body,
                    color = colors.textSecondary
                )
            }
        } else {
            RecentRateCard(done = summary.doneLast30, tracked = summary.trackedLast30)

            YearOverviewCard(habitsWithLogs = activeHabits, today = today)

            WeekdayCard(done = summary.weekdayDone, tracked = summary.weekdayTracked)

            HabitListCard(rows = summary.rows, onOpenHabitDetail = onOpenHabitDetail)
        }
    }
}

@Composable
private fun CardTitleRow(title: String, trailing: String? = null) {
    val colors = TesseraTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = TesseraType.Label,
            color = colors.textPrimary,
            modifier = Modifier.weight(1f)
        )
        if (trailing != null) {
            Text(
                text = trailing,
                style = TesseraType.Supporting,
                color = colors.textSecondary
            )
        }
    }
}

/** Overall completion rate across active habits for the last 30 days. */
@Composable
private fun RecentRateCard(done: Int, tracked: Int) {
    val colors = TesseraTheme.colors
    val fraction = if (tracked > 0) (done.toFloat() / tracked.toFloat()).coerceIn(0f, 1f) else 0f
    val percent = (fraction * 100f).roundToInt()

    TesseraCard {
        CardTitleRow(title = "Last 30 days")

        Spacer(modifier = Modifier.height(TesseraDimens.SpaceS))

        Text(
            text = "$percent%",
            style = TesseraType.Display,
            color = colors.accent
        )
        Text(
            text = "$done of $tracked check-ins",
            style = TesseraType.Body,
            color = colors.textSecondary
        )

        Spacer(modifier = Modifier.height(TesseraDimens.SpaceM))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50))
                .background(colors.surfaceRecessed)
        ) {
            if (fraction > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(colors.accent)
                )
            }
        }
    }
}

/** Completion rate per weekday (Monday first) across active habits. */
@Composable
private fun WeekdayCard(done: IntArray, tracked: IntArray) {
    val colors = TesseraTheme.colors
    val rates = List(7) { i ->
        if (tracked[i] > 0) (done[i].toFloat() / tracked[i].toFloat()).coerceIn(0f, 1f) else 0f
    }
    val fontScale = LocalDensity.current.fontScale

    TesseraCard {
        CardTitleRow(title = "By weekday", trailing = "Last $WEEKDAY_WEEKS weeks")

        Spacer(modifier = Modifier.height(TesseraDimens.SpaceL))

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            // "100%" in Caption needs about 36dp at the default font size.
            val showPercent = maxWidth / 7 >= 36.dp * fontScale

            Row(modifier = Modifier.fillMaxWidth()) {
                for (i in 0 until 7) {
                    val percent = (rates[i] * 100f).roundToInt()
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .semantics(mergeDescendants = true) {
                                contentDescription = if (tracked[i] > 0) {
                                    "${WeekdayNames[i]}, $percent%"
                                } else {
                                    "${WeekdayNames[i]}, nothing tracked yet"
                                }
                            },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (showPercent) {
                            Text(
                                text = if (tracked[i] > 0) "$percent%" else "",
                                style = TesseraType.Caption,
                                color = colors.textSecondary,
                                maxLines = 1,
                                softWrap = false,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(TesseraDimens.SpaceXs))
                        }

                        WeekdayBar(fraction = rates[i])

                        Spacer(modifier = Modifier.height(TesseraDimens.SpaceS))

                        Text(
                            text = WeekdayLetters[i],
                            style = TesseraType.Caption,
                            color = colors.textSecondary
                        )
                    }
                }
            }
        }

        val sentence = weekdaySentence(done = done, tracked = tracked)
        if (sentence != null) {
            Spacer(modifier = Modifier.height(TesseraDimens.SpaceM))
            Text(
                text = sentence,
                style = TesseraType.Supporting,
                color = colors.textSecondary
            )
        }
    }
}

@Composable
private fun WeekdayBar(fraction: Float) {
    val colors = TesseraTheme.colors
    val trackColor = colors.surfaceRecessed
    val fillColor = colors.accent

    Canvas(
        modifier = Modifier
            .width(20.dp)
            .height(96.dp)
    ) {
        val radius = CornerRadius(size.width / 2f, size.width / 2f)
        drawRoundRect(
            color = trackColor,
            topLeft = Offset.Zero,
            size = size,
            cornerRadius = radius
        )
        if (fraction > 0f) {
            // Never shorter than it is wide, so the rounded ends stay round.
            val fillHeight = (size.height * fraction).coerceIn(size.width, size.height)
            drawRoundRect(
                color = fillColor,
                topLeft = Offset(0f, size.height - fillHeight),
                size = Size(size.width, fillHeight),
                cornerRadius = radius
            )
        }
    }
}

/** Names the best and weakest weekday, or null when there is too little data to say. */
private fun weekdaySentence(done: IntArray, tracked: IntArray): String? {
    for (i in 0 until 7) {
        if (tracked[i] < MIN_DAYS_PER_WEEKDAY) return null
    }
    var best = 0
    var worst = 0
    for (i in 1 until 7) {
        // Compare done/tracked as cross-products to avoid rounding ties.
        if (done[i].toLong() * tracked[best] > done[best].toLong() * tracked[i]) best = i
        if (done[i].toLong() * tracked[worst] < done[worst].toLong() * tracked[i]) worst = i
    }
    if (done[best].toLong() * tracked[worst] == done[worst].toLong() * tracked[best]) return null
    return "Strongest on ${WeekdayNames[best]}s, weakest on ${WeekdayNames[worst]}s."
}

@Composable
private fun HabitListCard(
    rows: List<HabitProgressRow>,
    onOpenHabitDetail: (Long) -> Unit
) {
    TesseraCard(contentPadding = 0.dp) {
        Box(
            modifier = Modifier.padding(
                start = TesseraDimens.SpaceL,
                end = TesseraDimens.SpaceL,
                top = TesseraDimens.SpaceL,
                bottom = TesseraDimens.SpaceXs
            )
        ) {
            CardTitleRow(title = "Habits")
        }

        rows.forEachIndexed { index, row ->
            if (index > 0) {
                TesseraDivider(modifier = Modifier.padding(horizontal = TesseraDimens.SpaceL))
            }
            HabitRow(row = row, onClick = { onOpenHabitDetail(row.id) })
        }

        Spacer(modifier = Modifier.height(TesseraDimens.SpaceXs))
    }
}

@Composable
private fun HabitRow(row: HabitProgressRow, onClick: () -> Unit) {
    val colors = TesseraTheme.colors
    val streakText = when (row.currentStreak) {
        0 -> "No streak"
        else -> "${row.currentStreak}-day streak"
    }
    val daysText = if (row.trackedDays == 1) {
        "${row.doneDays} of 1 day"
    } else {
        "${row.doneDays} of ${row.trackedDays} days"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = TesseraDimens.SpaceL, vertical = TesseraDimens.SpaceS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = row.title,
                style = TesseraType.BodyStrong,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$streakText · $daysText",
                style = TesseraType.Supporting,
                color = colors.textSecondary
            )
        }

        Spacer(modifier = Modifier.width(TesseraDimens.SpaceM))

        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = colors.textSecondary,
            modifier = Modifier.size(24.dp)
        )
    }
}
