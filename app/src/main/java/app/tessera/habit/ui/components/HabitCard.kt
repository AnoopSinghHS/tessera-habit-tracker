package app.tessera.habit.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.tessera.habit.data.model.HabitWithLogs
import app.tessera.habit.ui.theme.HabitPalette
import app.tessera.habit.ui.theme.HabitPaletteSpec
import app.tessera.habit.ui.theme.TesseraTheme
import app.tessera.habit.ui.theme.TesseraType
import java.time.LocalDate
import kotlin.math.roundToInt

/**
 * Full-width Architectural Dual-Zone Habit Card.
 * Top Zone: Tinted icon squircle, title, monospace streak pill, quantitative counter, and kinetic check trigger.
 * Bottom Zone: Precision-aligned GitHub contribution matrix + micro telemetry strip.
 */
@Composable
fun HabitCard(
    habitWithLogs: HabitWithLogs,
    onCheckInClick: () -> Unit,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
    onDateCellClick: ((LocalDate) -> Unit)? = null
) {
    val colors = TesseraTheme.colors
    val haptic = LocalHapticFeedback.current
    val habit = habitWithLogs.habit
    val palette = remember(habit.colorKey) { HabitPalette.resolve(habit.colorKey) }
    val stats = remember(habitWithLogs, today) { habitWithLogs.computeStats(today) }
    val countsByEpochDay = remember(habitWithLogs.logs) {
        habitWithLogs.logs.associate { it.dateEpochDay to it.count }
    }

    val todayCount = habitWithLogs.countForDate(today)
    val target = habit.targetPerDay.coerceAtLeast(1)
    val isCompletedToday = todayCount >= target

    val borderColor by animateColorAsState(
        targetValue = if (isCompletedToday) {
            palette.level3.copy(alpha = 0.55f)
        } else {
            colors.borderHairline
        },
        label = "cardBorderColor"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceCard,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Top Metadata + Kinetic Trigger Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tinted Icon Squircle
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(palette.colorForLevel(1, colors.isDark))
                        .border(
                            width = 1.dp,
                            color = palette.level3.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = HabitIcons.resolve(habit.iconKey),
                        contentDescription = habit.title,
                        tint = palette.colorForLevel(4, colors.isDark),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title, Streak Badge & Subtitle
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = habit.title,
                            style = TesseraType.CardTitle,
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        // Streak Monospace Pill
                        StreakPillBadge(
                            streakDays = stats.currentStreak,
                            palette = palette,
                            isActive = stats.currentStreak > 0
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    val statusSubtitle = buildString {
                        if (target > 1) {
                            append("$todayCount/$target ${habit.unitLabel} today")
                        } else if (isCompletedToday) {
                            append("Completed today")
                        } else {
                            append(habit.description.ifBlank { "Daily ritual" })
                        }
                        append(" · #${habit.slug}")
                    }

                    Text(
                        text = statusSubtitle,
                        style = TesseraType.CardSubtitle,
                        color = if (isCompletedToday) palette.colorForLevel(4, colors.isDark) else colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Kinetic Check-In Trigger Button
                KineticCheckInButton(
                    todayCount = todayCount,
                    targetPerDay = target,
                    palette = palette,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onCheckInClick()
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // GitHub Contribution Heatmap Matrix
            GithubHeatmapGrid(
                countsByEpochDay = countsByEpochDay,
                targetPerDay = target,
                palette = palette,
                weeks = 28,
                cellSize = 11.dp,
                cellGap = 3.dp,
                cornerRadius = 2.5.dp,
                showWeekdayLabels = true,
                showMonthLabels = false,
                scrollable = true,
                today = today,
                onDateClick = onDateCellClick ?: { onCardClick() }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Micro Telemetry Footer Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${habit.category.uppercase()} · ${stats.weeklyCompletedDays}/${habit.weeklyGoalDays} WK",
                        style = TesseraType.MicroAxis,
                        color = colors.textMuted
                    )
                    Text(
                        text = "30D ${(stats.completionRate30Days * 100).roundToInt()}%",
                        style = TesseraType.MicroAxis,
                        color = colors.textSecondary
                    )
                }

                Text(
                    text = "${stats.totalCompletions} TOTAL",
                    style = TesseraType.MicroAxis,
                    color = colors.textMuted
                )
            }
        }
    }
}

/**
 * Compact 2-Column Bento Card for Dense Matrix Dashboard View.
 * Renders a 10-week × 7-day mini GitHub grid inside a tight architectural tile.
 */
@Composable
fun HabitCompactBentoCard(
    habitWithLogs: HabitWithLogs,
    onCheckInClick: () -> Unit,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now()
) {
    val colors = TesseraTheme.colors
    val haptic = LocalHapticFeedback.current
    val habit = habitWithLogs.habit
    val palette = remember(habit.colorKey) { HabitPalette.resolve(habit.colorKey) }
    val stats = remember(habitWithLogs, today) { habitWithLogs.computeStats(today) }
    val countsByEpochDay = remember(habitWithLogs.logs) {
        habitWithLogs.logs.associate { it.dateEpochDay to it.count }
    }

    val todayCount = habitWithLogs.countForDate(today)
    val target = habit.targetPerDay.coerceAtLeast(1)
    val isCompletedToday = todayCount >= target

    val borderColor = if (isCompletedToday) {
        palette.level3.copy(alpha = 0.55f)
    } else {
        colors.borderHairline
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(14.dp),
        color = colors.surfaceCard,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(palette.colorForLevel(1, colors.isDark)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = HabitIcons.resolve(habit.iconKey),
                        contentDescription = habit.title,
                        tint = palette.colorForLevel(4, colors.isDark),
                        modifier = Modifier.size(17.dp)
                    )
                }

                KineticCheckInButton(
                    todayCount = todayCount,
                    targetPerDay = target,
                    palette = palette,
                    sizeDp = 34,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onCheckInClick()
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = habit.title,
                style = TesseraType.CardTitle.copy(fontSize = TesseraType.CardTitle.fontSize * 0.92f),
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${stats.currentStreak}D STREAK",
                    style = TesseraType.MicroAxis,
                    color = if (stats.currentStreak > 0) palette.colorForLevel(4, colors.isDark) else colors.textMuted
                )
                Text(
                    text = if (target > 1) "$todayCount/$target" else if (isCompletedToday) "DONE" else "0/1",
                    style = TesseraType.MicroAxis,
                    color = colors.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            GithubHeatmapGrid(
                countsByEpochDay = countsByEpochDay,
                targetPerDay = target,
                palette = palette,
                weeks = 11,
                cellSize = 9.dp,
                cellGap = 2.5.dp,
                cornerRadius = 2.dp,
                showWeekdayLabels = false,
                showMonthLabels = false,
                scrollable = false,
                today = today
            )
        }
    }
}

@Composable
fun StreakPillBadge(
    streakDays: Int,
    palette: HabitPaletteSpec,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = TesseraTheme.colors
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (isActive) palette.colorForLevel(1, colors.isDark) else colors.surfaceElevated
            )
            .border(
                width = 1.dp,
                color = if (isActive) palette.level3.copy(alpha = 0.4f) else colors.borderHairline,
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.LocalFireDepartment,
            contentDescription = null,
            tint = if (isActive) palette.colorForLevel(4, colors.isDark) else colors.textMuted,
            modifier = Modifier.size(11.dp)
        )
        Text(
            text = "${streakDays}D",
            style = TesseraType.TelemetryBadge,
            color = if (isActive) palette.colorForLevel(4, colors.isDark) else colors.textMuted
        )
    }
}

@Composable
fun KineticCheckInButton(
    todayCount: Int,
    targetPerDay: Int,
    palette: HabitPaletteSpec,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    sizeDp: Int = 40
) {
    val colors = TesseraTheme.colors
    val safeTarget = targetPerDay.coerceAtLeast(1)
    val isComplete = todayCount >= safeTarget
    val isPartial = todayCount in 1 until safeTarget

    val scale by animateFloatAsState(
        targetValue = if (isComplete) 1.0f else 0.97f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "checkScale"
    )

    val bgColor by animateColorAsState(
        targetValue = when {
            isComplete -> palette.colorForLevel(4, colors.isDark)
            isPartial -> palette.colorForLevel(1, colors.isDark)
            else -> colors.surfaceElevated
        },
        label = "checkBg"
    )

    val strokeColor = when {
        isComplete -> palette.colorForLevel(4, colors.isDark)
        isPartial -> palette.level3
        else -> colors.borderHairline
    }

    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, strokeColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        when {
            isComplete -> {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Completed",
                    tint = palette.onAccent,
                    modifier = Modifier.size((sizeDp * 0.52f).dp)
                )
            }
            isPartial -> {
                CircularProgressIndicator(
                    progress = { (todayCount.toFloat() / safeTarget.toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier.size((sizeDp - 8).dp),
                    color = palette.colorForLevel(4, colors.isDark),
                    trackColor = colors.borderHairline,
                    strokeWidth = 2.dp
                )
                Text(
                    text = "$todayCount/$safeTarget",
                    style = TesseraType.MicroAxis,
                    color = palette.colorForLevel(4, colors.isDark)
                )
            }
            else -> {
                Icon(
                    imageVector = if (safeTarget > 1) Icons.Rounded.Add else Icons.Rounded.Check,
                    contentDescription = "Check in",
                    tint = colors.textSecondary,
                    modifier = Modifier.size((sizeDp * 0.48f).dp)
                )
            }
        }
    }
}
