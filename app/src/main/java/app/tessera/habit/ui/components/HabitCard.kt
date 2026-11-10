package app.tessera.habit.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.tessera.habit.data.model.HabitWithLogs
import app.tessera.habit.ui.theme.TesseraTheme
import app.tessera.habit.ui.theme.TesseraType
import java.time.LocalDate

/**
 * A habit on the Today screen: name, streak line, one-tap check-in and a heatmap that
 * fills the card width. Tapping the card opens the habit's detail.
 */
@Composable
fun HabitCard(
    habitWithLogs: HabitWithLogs,
    onCheckInClick: () -> Unit,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now()
) {
    val colors = TesseraTheme.colors
    val habit = habitWithLogs.habit
    val stats = remember(habitWithLogs, today) { habitWithLogs.computeStats(today) }
    val countsByEpochDay = remember(habitWithLogs.logs) {
        habitWithLogs.logs.associate { it.dateEpochDay to it.count }
    }
    val target = habit.targetPerDay.coerceAtLeast(1)
    val todayCount = habitWithLogs.countForDate(today)
    val streakText = streakLabel(stats.currentStreak)
    val detailText = streakDetailLabel(stats.currentStreak, todayCount, target)

    TesseraCard(modifier = modifier, onClick = onCardClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.title,
                    style = TesseraType.Title,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = buildAnnotatedString {
                        if (streakText != null) {
                            withStyle(
                                SpanStyle(color = colors.textPrimary, fontWeight = FontWeight.SemiBold)
                            ) {
                                append(streakText)
                            }
                            if (detailText != null) append(" · ")
                        }
                        if (detailText != null) append(detailText)
                    },
                    style = TesseraType.Supporting,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(TesseraDimens.SpaceM))
            CheckInButton(
                habitTitle = habit.title,
                count = todayCount,
                targetPerDay = target,
                onClick = onCheckInClick
            )
        }

        Spacer(modifier = Modifier.height(TesseraDimens.SpaceM))

        CardWidthHeatmap(
            countsByEpochDay = countsByEpochDay,
            targetPerDay = target,
            today = today,
            baseCellSize = 13.dp
        )
    }
}

/** The smaller card used in the two-column grid view. */
@Composable
fun HabitCompactCard(
    habitWithLogs: HabitWithLogs,
    onCheckInClick: () -> Unit,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now()
) {
    val colors = TesseraTheme.colors
    val habit = habitWithLogs.habit
    val stats = remember(habitWithLogs, today) { habitWithLogs.computeStats(today) }
    val countsByEpochDay = remember(habitWithLogs.logs) {
        habitWithLogs.logs.associate { it.dateEpochDay to it.count }
    }
    val target = habit.targetPerDay.coerceAtLeast(1)
    val todayCount = habitWithLogs.countForDate(today)
    val isPartial = target > 1 && todayCount in 1 until target
    val statusText = when {
        isPartial -> "$todayCount of $target today"
        else -> streakLabel(stats.currentStreak) ?: "Start today"
    }

    TesseraCard(
        modifier = modifier,
        onClick = onCardClick,
        contentPadding = TesseraDimens.SpaceM
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Two lines are always reserved so cards in the same row line up.
                Text(
                    text = habit.title,
                    style = TesseraType.Label,
                    color = colors.textPrimary,
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(TesseraDimens.SpaceXs))
                Text(
                    text = statusText,
                    style = TesseraType.Supporting,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(TesseraDimens.SpaceXs))
            CheckInButton(
                habitTitle = habit.title,
                count = todayCount,
                targetPerDay = target,
                onClick = onCheckInClick,
                diameter = 36.dp
            )
        }

        Spacer(modifier = Modifier.height(TesseraDimens.SpaceS))

        CardWidthHeatmap(
            countsByEpochDay = countsByEpochDay,
            targetPerDay = target,
            today = today,
            baseCellSize = 11.dp
        )
    }
}

/** A small pill showing a streak length, e.g. "23-day streak". */
@Composable
fun StreakPillBadge(
    streak: Int,
    modifier: Modifier = Modifier
) {
    val colors = TesseraTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(colors.surfaceElevated)
            .padding(horizontal = TesseraDimens.SpaceM, vertical = TesseraDimens.SpaceXs),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = streakLabel(streak) ?: "No streak",
            style = TesseraType.Label,
            color = if (streak > 0) colors.textPrimary else colors.textSecondary,
            maxLines = 1
        )
    }
}

/**
 * Round one-tap check-in button.
 *
 * Once-a-day habits show a check that fills with the accent when done. Habits with a daily
 * target above one show a progress ring with "2/3" inside until the target is met.
 * The touch area is always at least 48dp, whatever [diameter] is drawn.
 */
@Composable
fun CheckInButton(
    habitTitle: String,
    count: Int,
    targetPerDay: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    diameter: Dp = 48.dp
) {
    val colors = TesseraTheme.colors
    val haptic = LocalHapticFeedback.current
    val target = targetPerDay.coerceAtLeast(1)
    val isDone = count >= target
    val isMultiCount = target > 1
    val haloWidth = 4.dp
    val touchSize = (diameter + haloWidth * 2).coerceAtLeast(TesseraDimens.TouchTarget)

    val restingFill = if (isMultiCount) colors.accent.copy(alpha = 0f) else colors.surfaceElevated
    val fillColor by animateColorAsState(
        targetValue = if (isDone) colors.accent else restingFill,
        label = "checkInFill"
    )
    val haloAlpha by animateFloatAsState(
        targetValue = if (isDone) 0.22f else 0f,
        label = "checkInHalo"
    )
    val progress by animateFloatAsState(
        targetValue = (count.toFloat() / target.toFloat()).coerceIn(0f, 1f),
        label = "checkInProgress"
    )
    val scale by animateFloatAsState(
        targetValue = if (isDone) 1f else 0.94f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "checkInScale"
    )

    val description = when {
        isDone && isMultiCount -> "$habitTitle: done. Tap to reset"
        isDone -> "$habitTitle: done. Tap to undo"
        isMultiCount -> "$habitTitle: $count of $target. Tap to add one"
        else -> "Mark $habitTitle done"
    }

    Box(
        modifier = modifier
            .size(touchSize)
            .clip(CircleShape)
            .clickable(role = Role.Button) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(touchSize)
                .scale(scale)
        ) {
            val radius = diameter.toPx() / 2f
            if (haloAlpha > 0f) {
                drawCircle(
                    color = colors.accent.copy(alpha = haloAlpha),
                    radius = radius + haloWidth.toPx()
                )
            }
            drawCircle(color = fillColor, radius = radius)

            if (!isDone) {
                if (isMultiCount) {
                    val ringWidth = 4.dp.toPx()
                    val ringRadius = radius - ringWidth / 2f
                    drawCircle(
                        color = colors.surfaceRecessed,
                        radius = ringRadius,
                        style = Stroke(width = ringWidth)
                    )
                    if (progress > 0f) {
                        drawArc(
                            color = colors.accent,
                            startAngle = -90f,
                            sweepAngle = 360f * progress,
                            useCenter = false,
                            topLeft = Offset(center.x - ringRadius, center.y - ringRadius),
                            size = Size(ringRadius * 2f, ringRadius * 2f),
                            style = Stroke(width = ringWidth, cap = StrokeCap.Round)
                        )
                    }
                } else {
                    val outlineWidth = 1.5.dp.toPx()
                    drawCircle(
                        color = colors.borderSpecular,
                        radius = radius - outlineWidth / 2f,
                        style = Stroke(width = outlineWidth)
                    )
                }
            }
        }

        if (isMultiCount && !isDone) {
            // A long "10/12" does not fit inside the small ring, so show the count alone there.
            val ringText = if (diameter < 44.dp && target >= 10) "$count" else "$count/$target"
            Text(
                text = ringText,
                style = TesseraType.Label,
                color = colors.textPrimary,
                maxLines = 1,
                softWrap = false
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = if (isDone) colors.onAccent else colors.textPrimary,
                modifier = Modifier
                    .size(if (diameter < 44.dp) 20.dp else 24.dp)
                    .scale(scale)
            )
        }
    }
}

/**
 * A non-scrolling heatmap that shows as many weeks as fit the available width.
 * Cells are stretched by a fraction of a dp so the grid ends flush with both edges.
 */
@Composable
private fun CardWidthHeatmap(
    countsByEpochDay: Map<Long, Int>,
    targetPerDay: Int,
    today: LocalDate,
    baseCellSize: Dp
) {
    val gap = 3.dp
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val weeks = ((maxWidth + gap) / (baseCellSize + gap)).toInt().coerceIn(1, 54)
        val cellSize = ((maxWidth - gap * (weeks - 1)) / weeks).coerceAtLeast(baseCellSize)
        GithubHeatmapGrid(
            countsByEpochDay = countsByEpochDay,
            targetPerDay = targetPerDay,
            weeks = weeks,
            cellSize = cellSize,
            cellGap = gap,
            cornerRadius = TesseraDimens.RadiusCell,
            showWeekdayLabels = false,
            showMonthLabels = false,
            scrollable = false,
            today = today
        )
    }
}

private val StreakMilestones = listOf(7, 14, 30, 60, 100, 200, 365)

/** The next streak length worth aiming for: 7, 14, 30, 60, 100, 200, 365, then every 365. */
internal fun nextStreakMilestone(streak: Int): Int {
    val onLadder = StreakMilestones.firstOrNull { it > streak }
    if (onLadder != null) return onLadder
    return ((streak / 365) + 1) * 365
}

/** "23-day streak", or null when there is no streak. */
internal fun streakLabel(streak: Int): String? {
    return if (streak > 0) "$streak-day streak" else null
}

/**
 * The second half of a card's status line: today's partial count on a multi-count habit,
 * "Start today" with no streak, otherwise the distance to the next milestone.
 */
internal fun streakDetailLabel(streak: Int, todayCount: Int, targetPerDay: Int): String? {
    val target = targetPerDay.coerceAtLeast(1)
    if (target > 1 && todayCount in 1 until target) {
        return "$todayCount of $target today"
    }
    if (streak <= 0) return "Start today"
    val milestone = nextStreakMilestone(streak)
    val remaining = milestone - streak
    return if (remaining == 1) "1 day to $milestone" else "$remaining days to $milestone"
}
