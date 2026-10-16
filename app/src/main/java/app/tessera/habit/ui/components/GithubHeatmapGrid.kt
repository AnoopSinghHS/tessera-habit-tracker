package app.tessera.habit.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.tessera.habit.ui.theme.HabitPalette
import app.tessera.habit.ui.theme.HabitPaletteSpec
import app.tessera.habit.ui.theme.TesseraTheme
import app.tessera.habit.ui.theme.TesseraType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle as JavaTextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

/**
 * Tactile Contribution Heatmap Matrix rendered on a single hardware-accelerated
 * Compose [Canvas] for 120Hz scrolling performance.
 *
 * Rows = 7 (Monday = 0 .. Sunday = 6).
 * Columns = [weeks] weeks ending on the current week (aligned to Monday..Sunday).
 */
@Composable
fun GithubHeatmapGrid(
    countsByEpochDay: Map<Long, Int>,
    targetPerDay: Int,
    palette: HabitPaletteSpec,
    modifier: Modifier = Modifier,
    weeks: Int = 15,
    cellSize: Dp = 11.dp,
    cellGap: Dp = 2.5.dp,
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
    val textMeasurer: TextMeasurer = rememberTextMeasurer()

    val safeWeeks = weeks.coerceIn(4, 54)
    val currentWeekMonday = remember(today) {
        today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }
    val startMonday = remember(currentWeekMonday, safeWeeks) {
        currentWeekMonday.minusWeeks((safeWeeks - 1).toLong())
    }
    val todayEpoch = remember(today) { today.toEpochDay() }
    val selectedEpoch = remember(selectedDate) { selectedDate?.toEpochDay() }

    val cellPx = with(density) { cellSize.toPx() }
    val gapPx = with(density) { cellGap.toPx() }
    val stepPx = cellPx + gapPx
    val radiusPx = with(density) { cornerRadius.toPx() }
    val strokePx = with(density) { 1.dp.toPx() }
    val selectedStrokePx = with(density) { 1.5.dp.toPx() }
    val monthHeaderHeightDp = if (showMonthLabels) 16.dp else 0.dp
    val monthHeaderHeightPx = with(density) { monthHeaderHeightDp.toPx() }

    val canvasWidthDp = (cellSize * safeWeeks) + (cellGap * (safeWeeks - 1).coerceAtLeast(0))
    val gridHeightDp = (cellSize * 7) + (cellGap * 6)
    val totalCanvasHeightDp = gridHeightDp + monthHeaderHeightDp

    val monthMarkers = remember(startMonday, safeWeeks, showMonthLabels) {
        if (!showMonthLabels) emptyList()
        else buildList {
            var lastMonth = -1
            for (col in 0 until safeWeeks) {
                val weekMonday = startMonday.plusWeeks(col.toLong())
                val weekSunday = weekMonday.plusDays(6)
                val representativeDate = if (weekMonday.dayOfMonth <= 7) weekMonday else weekSunday
                val monthVal = representativeDate.monthValue
                if (monthVal != lastMonth && (col == 0 || weekMonday.dayOfMonth <= 7)) {
                    val label = representativeDate.month
                        .getDisplayName(JavaTextStyle.SHORT, Locale.US)
                    add(col to label)
                    lastMonth = monthVal
                }
            }
        }
    }

    val scrollState = rememberScrollState()
    LaunchedEffect(safeWeeks, scrollable) {
        if (scrollable) {
            scrollState.scrollTo(scrollState.maxValue)
        }
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.End
    ) {
        if (showWeekdayLabels) {
            Column(
                modifier = Modifier
                    .padding(end = 6.dp)
                    .height(totalCanvasHeightDp),
                verticalArrangement = Arrangement.Bottom
            ) {
                if (showMonthLabels) {
                    Spacer(modifier = Modifier.height(monthHeaderHeightDp))
                }
                val labels = listOf("M", "", "W", "", "F", "", "S")
                Column(
                    modifier = Modifier.height(gridHeightDp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    labels.forEach { dayLabel ->
                        Box(
                            modifier = Modifier.height(cellSize),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (dayLabel.isNotEmpty()) {
                                Text(
                                    text = dayLabel,
                                    style = TesseraType.MicroAxis,
                                    color = colors.textMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        val canvasContainerModifier = if (scrollable) {
            Modifier.horizontalScroll(scrollState)
        } else {
            Modifier
        }

        Box(
            modifier = canvasContainerModifier,
            contentAlignment = Alignment.CenterEnd
        ) {
            val tapModifier = if (onDateClick != null) {
                Modifier.pointerInput(startMonday, safeWeeks, todayEpoch, onDateClick) {
                    detectTapGestures { offset ->
                        val gridY = offset.y - monthHeaderHeightPx
                        if (gridY >= 0f) {
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

            val axisTextStyle = TesseraType.MicroAxis.copy(color = colors.textMuted)

            Canvas(
                modifier = Modifier
                    .width(canvasWidthDp)
                    .height(totalCanvasHeightDp)
                    .then(tapModifier)
            ) {
                if (showMonthLabels) {
                    var lastEndPx = -10f
                    monthMarkers.forEach { (col, monthLabel) ->
                        val x = col * stepPx
                        if (x >= lastEndPx + 6f) {
                            val measured = textMeasurer.measure(monthLabel, style = axisTextStyle)
                            if (x + measured.size.width <= size.width) {
                                drawText(
                                    textLayoutResult = measured,
                                    topLeft = Offset(x, 0f)
                                )
                                lastEndPx = x + measured.size.width
                            }
                        }
                    }
                }

                val startEpoch = startMonday.toEpochDay()
                for (col in 0 until safeWeeks) {
                    val colX = col * stepPx
                    for (row in 0..6) {
                        val cellEpoch = startEpoch + (col * 7L) + row
                        val rowY = monthHeaderHeightPx + (row * stepPx)

                        if (cellEpoch > todayEpoch) {
                            // Leave future days in the current week blank for the signature staircase edge
                            continue
                        }

                        val count = countsByEpochDay[cellEpoch] ?: 0
                        val level = HabitPalette.calculateIntensityLevel(count, targetPerDay)
                        val fillColor = palette.colorForLevel(level, colors.isDark)

                        drawRoundRect(
                            color = fillColor,
                            topLeft = Offset(colX, rowY),
                            size = Size(cellPx, cellPx),
                            cornerRadius = CornerRadius(radiusPx, radiusPx)
                        )

                        // Subtle ring for today when not yet completed
                        if (cellEpoch == todayEpoch && level == 0) {
                            drawRoundRect(
                                color = palette.colorForLevel(3, colors.isDark).copy(alpha = 0.55f),
                                topLeft = Offset(colX + strokePx / 2f, rowY + strokePx / 2f),
                                size = Size(cellPx - strokePx, cellPx - strokePx),
                                cornerRadius = CornerRadius(radiusPx, radiusPx),
                                style = Stroke(width = strokePx)
                            )
                        }

                        // Selected date highlight ring (in Detail Sheet)
                        if (selectedEpoch != null && cellEpoch == selectedEpoch) {
                            drawRoundRect(
                                color = colors.textPrimary,
                                topLeft = Offset(colX - 1f, rowY - 1f),
                                size = Size(cellPx + 2f, cellPx + 2f),
                                cornerRadius = CornerRadius(radiusPx + 1f, radiusPx + 1f),
                                style = Stroke(width = selectedStrokePx)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 5-Stop Pigment Legend Strip ("Less [0][1][2][3][4] More").
 */
@Composable
fun HeatmapIntensityLegend(
    palette: HabitPaletteSpec,
    modifier: Modifier = Modifier
) {
    val colors = TesseraTheme.colors
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Less",
            style = TesseraType.MicroAxis,
            color = colors.textMuted
        )
        for (level in 0..4) {
            val boxColor: Color = palette.colorForLevel(level, colors.isDark)
            Canvas(modifier = Modifier.width(10.dp).height(10.dp)) {
                drawRoundRect(
                    color = boxColor,
                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                )
            }
        }
        Text(
            text = "More",
            style = TesseraType.MicroAxis,
            color = colors.textMuted
        )
    }
}
