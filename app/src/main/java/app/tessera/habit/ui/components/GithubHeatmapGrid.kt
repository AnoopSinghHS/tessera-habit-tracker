package app.tessera.habit.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.tessera.habit.ui.theme.HabitPalette
import app.tessera.habit.ui.theme.TesseraTheme
import app.tessera.habit.ui.theme.TesseraType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle as JavaTextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

/**
 * GitHub-style heatmap drawn on one Canvas.
 *
 * Seven rows (Monday at the top, Sunday at the bottom) and [weeks] columns ending on the
 * current week. Days after [today] are left blank. Colours come from the theme's heat ramp.
 */
@Composable
fun GithubHeatmapGrid(
    countsByEpochDay: Map<Long, Int>,
    targetPerDay: Int,
    modifier: Modifier = Modifier,
    weeks: Int = 19,
    cellSize: Dp = 13.dp,
    cellGap: Dp = 3.dp,
    cornerRadius: Dp = 3.dp,
    showWeekdayLabels: Boolean = false,
    showMonthLabels: Boolean = false,
    scrollable: Boolean = true,
    today: LocalDate = LocalDate.now(),
    selectedDate: LocalDate? = null,
    onDateClick: ((LocalDate) -> Unit)? = null
) {
    val colors = TesseraTheme.colors
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val axisTextStyle = TesseraType.Caption.copy(color = colors.textSecondary)

    val safeWeeks = weeks.coerceIn(1, 54)
    val currentWeekMonday = remember(today) {
        today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }
    val startMonday = remember(currentWeekMonday, safeWeeks) {
        currentWeekMonday.minusWeeks((safeWeeks - 1).toLong())
    }
    val todayEpoch = today.toEpochDay()
    val selectedEpoch = selectedDate?.toEpochDay()

    val cellPx = with(density) { cellSize.toPx() }
    val gapPx = with(density) { cellGap.toPx() }
    val stepPx = cellPx + gapPx
    val radiusPx = with(density) { cornerRadius.toPx() }
    val ringPx = with(density) { 1.5.dp.toPx() }
    val glowPx = with(density) { 2.dp.toPx() }

    // Axis text is measured once so the layout follows the user's font scale.
    val weekdayLabels = remember { listOf("Mon", "", "Wed", "", "Fri", "", "Sun") }
    val measuredWeekdays = remember(textMeasurer, axisTextStyle, showWeekdayLabels) {
        if (showWeekdayLabels) {
            weekdayLabels.map { label ->
                if (label.isEmpty()) null else textMeasurer.measure(label, style = axisTextStyle)
            }
        } else {
            emptyList()
        }
    }
    val weekdayColumnWidthPx = measuredWeekdays.maxOfOrNull { it?.size?.width ?: 0 } ?: 0
    val monthHeaderHeightPx = remember(textMeasurer, axisTextStyle, showMonthLabels) {
        if (showMonthLabels) {
            textMeasurer.measure("Jan", style = axisTextStyle).size.height.toFloat()
        } else {
            0f
        }
    } + if (showMonthLabels) with(density) { 4.dp.toPx() } else 0f

    val gridWidthDp = (cellSize * safeWeeks) + (cellGap * (safeWeeks - 1))
    val gridHeightDp = (cellSize * 7) + (cellGap * 6)
    val monthHeaderHeightDp = with(density) { monthHeaderHeightPx.toDp() }
    val totalHeightDp = gridHeightDp + monthHeaderHeightDp

    val monthMarkers = remember(startMonday, safeWeeks, showMonthLabels) {
        if (!showMonthLabels) {
            emptyList()
        } else {
            buildList {
                var lastMonth = -1
                for (col in 0 until safeWeeks) {
                    val weekMonday = startMonday.plusWeeks(col.toLong())
                    val weekSunday = weekMonday.plusDays(6)
                    val representative = if (weekMonday.dayOfMonth <= 7) weekMonday else weekSunday
                    val month = representative.monthValue
                    if (month != lastMonth && (col == 0 || weekMonday.dayOfMonth <= 7)) {
                        add(col to representative.month.getDisplayName(JavaTextStyle.SHORT, Locale.US))
                        lastMonth = month
                    }
                }
            }
        }
    }

    val scrollState = rememberScrollState()
    LaunchedEffect(safeWeeks, scrollable, scrollState.maxValue) {
        if (scrollable) {
            // Start at the right-hand edge so the current week is visible.
            scrollState.scrollTo(scrollState.maxValue)
        }
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.End
    ) {
        if (showWeekdayLabels) {
            Canvas(
                modifier = Modifier
                    .width(with(density) { weekdayColumnWidthPx.toDp() })
                    .height(totalHeightDp)
            ) {
                measuredWeekdays.forEachIndexed { row, measured ->
                    if (measured != null) {
                        val rowCentre = monthHeaderHeightPx + (row * stepPx) + (cellPx / 2f)
                        drawText(
                            textLayoutResult = measured,
                            topLeft = Offset(0f, rowCentre - (measured.size.height / 2f))
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(TesseraDimens.SpaceS))
        }

        val scrollModifier = if (scrollable) Modifier.horizontalScroll(scrollState) else Modifier

        Box(modifier = scrollModifier) {
            val tapModifier = if (onDateClick != null) {
                Modifier.pointerInput(startMonday, safeWeeks, todayEpoch, stepPx, monthHeaderHeightPx, onDateClick) {
                    detectTapGestures { offset ->
                        val gridY = offset.y - monthHeaderHeightPx
                        if (gridY >= 0f && offset.x >= 0f) {
                            val col = (offset.x / stepPx).toInt()
                            val row = (gridY / stepPx).toInt()
                            if (col in 0 until safeWeeks && row in 0..6) {
                                val tappedDate = startMonday.plusDays((col * 7 + row).toLong())
                                if (tappedDate.toEpochDay() <= todayEpoch) {
                                    onDateClick(tappedDate)
                                }
                            }
                        }
                    }
                }
            } else {
                Modifier
            }

            Canvas(
                modifier = Modifier
                    .width(gridWidthDp)
                    .height(totalHeightDp)
                    .then(tapModifier)
            ) {
                if (showMonthLabels) {
                    var lastEndPx = Float.NEGATIVE_INFINITY
                    monthMarkers.forEach { (col, monthLabel) ->
                        val x = col * stepPx
                        if (x >= lastEndPx + gapPx * 2f) {
                            val measured = textMeasurer.measure(monthLabel, style = axisTextStyle)
                            if (x + measured.size.width <= size.width) {
                                drawText(textLayoutResult = measured, topLeft = Offset(x, 0f))
                                lastEndPx = x + measured.size.width
                            }
                        }
                    }
                }

                val startEpoch = startMonday.toEpochDay()
                val corner = CornerRadius(radiusPx, radiusPx)
                for (col in 0 until safeWeeks) {
                    val colX = col * stepPx
                    for (row in 0..6) {
                        val cellEpoch = startEpoch + (col * 7L) + row
                        if (cellEpoch > todayEpoch) continue

                        val rowY = monthHeaderHeightPx + (row * stepPx)
                        val count = countsByEpochDay[cellEpoch] ?: 0
                        val level = HabitPalette.calculateIntensityLevel(count, targetPerDay)
                        val isToday = cellEpoch == todayEpoch

                        if (isToday) {
                            // Soft accent glow behind today's cell.
                            drawRoundRect(
                                color = colors.accent.copy(alpha = 0.22f),
                                topLeft = Offset(colX - glowPx, rowY - glowPx),
                                size = Size(cellPx + glowPx * 2f, cellPx + glowPx * 2f),
                                cornerRadius = CornerRadius(radiusPx + glowPx, radiusPx + glowPx)
                            )
                        }

                        drawRoundRect(
                            color = colors.heatLevel(level),
                            topLeft = Offset(colX, rowY),
                            size = Size(cellPx, cellPx),
                            cornerRadius = corner
                        )

                        if (isToday) {
                            drawRoundRect(
                                color = colors.accent,
                                topLeft = Offset(colX + ringPx / 2f, rowY + ringPx / 2f),
                                size = Size(cellPx - ringPx, cellPx - ringPx),
                                cornerRadius = corner,
                                style = Stroke(width = ringPx)
                            )
                        }

                        if (selectedEpoch != null && cellEpoch == selectedEpoch) {
                            drawRoundRect(
                                color = colors.textPrimary,
                                topLeft = Offset(colX - ringPx / 2f, rowY - ringPx / 2f),
                                size = Size(cellPx + ringPx, cellPx + ringPx),
                                cornerRadius = CornerRadius(radiusPx + ringPx / 2f, radiusPx + ringPx / 2f),
                                style = Stroke(width = ringPx)
                            )
                        }
                    }
                }
            }
        }
    }
}

/** "Less [][][][][] More" key for the heatmap ramp. */
@Composable
fun HeatmapIntensityLegend(modifier: Modifier = Modifier) {
    val colors = TesseraTheme.colors
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TesseraDimens.SpaceXs)
    ) {
        Text(
            text = "Less",
            style = TesseraType.Caption,
            color = colors.textSecondary
        )
        for (level in 0..4) {
            val cellColor = colors.heatLevel(level)
            Canvas(modifier = Modifier.size(12.dp)) {
                val radius = TesseraDimens.RadiusCell.toPx()
                drawRoundRect(color = cellColor, cornerRadius = CornerRadius(radius, radius))
            }
        }
        Text(
            text = "More",
            style = TesseraType.Caption,
            color = colors.textSecondary
        )
    }
}
