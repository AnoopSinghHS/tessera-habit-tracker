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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
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
import androidx.compose.ui.unit.sp
import app.tessera.habit.data.model.HabitWithLogs
import app.tessera.habit.ui.theme.HabitPalette
import app.tessera.habit.ui.theme.HabitPaletteSpec
import app.tessera.habit.ui.theme.TesseraTheme
import app.tessera.habit.ui.theme.TesseraType
import java.time.LocalDate

/**
 * Signature Asymmetric Split Habit Card (matching the Stitch / Fable concept).
 *
 * Top Row:
 * - Left: Minimalist vector icon (22.dp, ink-tinted, no box) + 12.dp spacer + Habit Title (17.sp Medium).
 * - Right: Tactile 34.dp circular check button filled with pigment accent when completed.
 *
 * Bottom Row (Asymmetric Split):
 * - Left Column (width = 88.dp, aligned bottom-start): Large 34.sp Editorial Serif streak number
 *   in pigment accent + "day streak" label below.
 * - Right Column (weight(1f)): 7-row contribution heatmap grid showing the last ~15 weeks of softly
 *   rounded (3.dp radius) squares.
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
    val accentColor = palette.colorForLevel(4, colors.isDark)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(18.dp),
        color = colors.surfaceCard,
        border = BorderStroke(1.dp, colors.borderHairline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Top Row: Minimalist Icon + Title + Circular Check Button
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

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = habit.title,
                            style = TesseraType.CardTitle,
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (target > 1) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(colors.surfaceRecessed)
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$todayCount/$target",
                                    style = TesseraType.MicroAxis,
                                    color = if (todayCount >= target) accentColor else colors.textSecondary
                                )
                            }
                        }
                    }

                    val subtitleText = when {
                        habit.cueAnchor.isNotBlank() -> "After I ${habit.cueAnchor}"
                        habit.microStarter.isNotBlank() -> "2m: ${habit.microStarter}"
                        habit.description.isNotBlank() -> habit.description
                        else -> ""
                    }
                    if (subtitleText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitleText,
                            style = TesseraType.CardSubtitle,
                            color = colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

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

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Row: Asymmetric Split Layout (Sculptural Serif Streak Left + Heatmap Matrix Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.width(88.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = stats.currentStreak.toString(),
                        style = TesseraType.StreakNumber,
                        color = accentColor
                    )
                    Text(
                        text = "day streak",
                        style = TesseraType.StreakLabel,
                        color = colors.textSecondary
                    )
                }

                GithubHeatmapGrid(
                    countsByEpochDay = countsByEpochDay,
                    targetPerDay = target,
                    palette = palette,
                    modifier = Modifier.weight(1f),
                    weeks = 15,
                    cellSize = 10.5.dp,
                    cellGap = 2.5.dp,
                    cornerRadius = 3.dp,
                    showWeekdayLabels = false,
                    showMonthLabels = false,
                    scrollable = false,
                    today = today,
                    onDateClick = onDateCellClick ?: { onCardClick() }
                )
            }
        }
    }
}

/**
 * Compact 2-Column Bento Card for Dense Dashboard View.
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
    val accentColor = palette.colorForLevel(4, colors.isDark)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(16.dp),
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = HabitIcons.resolve(habit.iconKey),
                    contentDescription = habit.title,
                    tint = colors.textPrimary,
                    modifier = Modifier.size(20.dp)
                )

                KineticCheckInButton(
                    todayCount = todayCount,
                    targetPerDay = target,
                    palette = palette,
                    sizeDp = 30,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onCheckInClick()
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = habit.title,
                style = TesseraType.CardTitle.copy(fontSize = 15.sp),
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "${stats.currentStreak} day streak",
                    style = TesseraType.StreakLabel,
                    color = accentColor
                )
                if (target > 1) {
                    Text(
                        text = "$todayCount/$target",
                        style = TesseraType.MicroAxis,
                        color = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            GithubHeatmapGrid(
                countsByEpochDay = countsByEpochDay,
                targetPerDay = target,
                palette = palette,
                weeks = 11,
                cellSize = 9.dp,
                cellGap = 2.5.dp,
                cornerRadius = 2.5.dp,
                showWeekdayLabels = false,
                showMonthLabels = false,
                scrollable = false,
                today = today
            )
        }
    }
}

/**
 * Clean Editorial Streak Pill Badge (zero emojis or fire icons).
 */
@Composable
fun StreakPillBadge(
    streakDays: Int,
    palette: HabitPaletteSpec,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = TesseraTheme.colors
    val accent = palette.colorForLevel(4, colors.isDark)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isActive) palette.colorForLevel(1, colors.isDark) else colors.surfaceElevated
            )
            .border(
                width = 1.dp,
                color = if (isActive) accent.copy(alpha = 0.3f) else colors.borderHairline,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$streakDays day streak",
            style = TesseraType.MicroAxis,
            color = if (isActive) accent else colors.textMuted
        )
    }
}

/**
 * Tactile 34.dp Circular Check Button.
 * Filled with the habit's organic pigment color and crisp ivory checkmark when completed;
 * subtle warm recessed circle when uncompleted.
 */
@Composable
fun KineticCheckInButton(
    todayCount: Int,
    targetPerDay: Int,
    palette: HabitPaletteSpec,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    sizeDp: Int = 34
) {
    val colors = TesseraTheme.colors
    val safeTarget = targetPerDay.coerceAtLeast(1)
    val isComplete = todayCount >= safeTarget
    val isPartial = todayCount in 1 until safeTarget
    val accentColor = palette.colorForLevel(4, colors.isDark)

    val scale by animateFloatAsState(
        targetValue = if (isComplete) 1.0f else 0.96f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "checkScale"
    )

    val bgColor by animateColorAsState(
        targetValue = when {
            isComplete -> accentColor
            isPartial -> palette.colorForLevel(1, colors.isDark)
            else -> colors.surfaceRecessed
        },
        label = "checkBg"
    )

    val strokeColor = when {
        isComplete -> accentColor
        isPartial -> palette.colorForLevel(3, colors.isDark)
        else -> colors.borderSpecular
    }

    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(bgColor)
            .border(1.dp, strokeColor, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        when {
            isComplete -> {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Completed",
                    tint = palette.onAccentFor(colors.isDark),
                    modifier = Modifier.size(18.dp)
                )
            }
            isPartial -> {
                CircularProgressIndicator(
                    progress = { (todayCount.toFloat() / safeTarget.toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier.size((sizeDp - 6).dp),
                    color = accentColor,
                    trackColor = colors.borderHairline,
                    strokeWidth = 2.dp
                )
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Increment",
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
            }
            else -> {
                Icon(
                    imageVector = if (safeTarget > 1) Icons.Rounded.Add else Icons.Rounded.Check,
                    contentDescription = "Check in",
                    tint = colors.textMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
