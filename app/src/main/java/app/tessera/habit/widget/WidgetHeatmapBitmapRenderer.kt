package app.tessera.habit.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import app.tessera.habit.data.model.HabitWithLogs
import app.tessera.habit.ui.theme.HabitPalette
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlin.math.min

/**
 * Draws a habit's heatmap to a bitmap for the home-screen widgets (and the preview on the
 * widget setup screen). Columns are weeks, Monday at the top; the last column is this week.
 */
object WidgetHeatmapBitmapRenderer {

    /** Weeks shown by the 4x2 widget. */
    const val WIDE_WEEKS = 22

    /** Weeks shown by the 2x2 widget. */
    const val COMPACT_WEEKS = 8

    fun renderWideHeatmap(
        context: Context,
        habitWithLogs: HabitWithLogs?,
        isDarkTheme: Boolean = true,
        weeks: Int = WIDE_WEEKS
    ): Bitmap {
        return renderHeatmap(
            context = context,
            habitWithLogs = habitWithLogs,
            isDarkTheme = isDarkTheme,
            weeks = weeks,
            targetWidthDp = 350f,
            targetHeightDp = 96f,
            showMonthLabels = true
        )
    }

    fun renderCompactHeatmap(
        context: Context,
        habitWithLogs: HabitWithLogs?,
        isDarkTheme: Boolean = true,
        weeks: Int = COMPACT_WEEKS
    ): Bitmap {
        return renderHeatmap(
            context = context,
            habitWithLogs = habitWithLogs,
            isDarkTheme = isDarkTheme,
            weeks = weeks,
            targetWidthDp = 148f,
            targetHeightDp = 88f,
            showMonthLabels = false
        )
    }

    fun renderHeatmap(
        context: Context,
        habitWithLogs: HabitWithLogs?,
        isDarkTheme: Boolean,
        weeks: Int,
        targetWidthDp: Float,
        targetHeightDp: Float,
        showMonthLabels: Boolean
    ): Bitmap {
        val density = context.resources.displayMetrics.density.coerceIn(1.5f, 3.5f)
        val widthPx = (targetWidthDp * density).toInt().coerceAtLeast(120)
        val heightPx = (targetHeightDp * density).toInt().coerceAtLeast(64)

        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val colors = WidgetColors.of(isDarkTheme)
        val targetPerDay = (habitWithLogs?.habit?.targetPerDay ?: 1).coerceAtLeast(1)
        val logsByEpochDay = habitWithLogs?.logsByEpochDay() ?: emptyMap()

        val today = LocalDate.now()
        val currentWeekMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val firstWeekMonday = currentWeekMonday.minusWeeks((weeks - 1).toLong())

        val monthLabelSizePx = 12f * density
        val topHeaderPx = if (showMonthLabels) (16f * density) else 0f
        val availableGridWidth = widthPx.toFloat()
        val availableGridHeight = (heightPx - topHeaderPx).coerceAtLeast(32f)

        val gapRatio = 0.22f
        val cellByWidth = availableGridWidth / (weeks + (weeks - 1) * gapRatio)
        val cellByHeight = availableGridHeight / (7 + 6 * gapRatio)
        val cellSize = min(cellByWidth, cellByHeight)
        val gap = cellSize * gapRatio
        val cornerRadius = cellSize * 0.24f

        // Centre the grid in the space available.
        val totalGridWidth = weeks * cellSize + (weeks - 1) * gap
        val totalGridHeight = 7 * cellSize + 6 * gap
        val startX = ((availableGridWidth - totalGridWidth) / 2f).coerceAtLeast(0f)
        val startY = topHeaderPx + ((availableGridHeight - totalGridHeight) / 2f).coerceAtLeast(0f)

        val emptyColor = colors.level(0)
        // Days that have not happened yet: the empty colour at about 45% opacity.
        val futureColor = (emptyColor and 0x00FFFFFF) or (0x73 shl 24)

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
        val todayRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = (cellSize * 0.14f).coerceAtLeast(2f)
            color = colors.accent
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colors.textSecondary
            textSize = monthLabelSizePx
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val rect = RectF()
        var lastLabeledMonth = -1
        var lastLabelX = -10000f

        for (weekIdx in 0 until weeks) {
            val weekStart = firstWeekMonday.plusWeeks(weekIdx.toLong())
            val colX = startX + weekIdx * (cellSize + gap)

            // Month name above the first column of each month, when there is room for it.
            if (showMonthLabels) {
                val month = weekStart.monthValue
                val monthLabel = weekStart.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                val labelWidth = textPaint.measureText(monthLabel)
                val fitsAfterPrevious = (colX - lastLabelX) >= labelWidth + gap * 2f
                val fitsInBitmap = colX + labelWidth <= widthPx
                if (month != lastLabeledMonth && fitsAfterPrevious && fitsInBitmap) {
                    canvas.drawText(monthLabel, colX, startY - 5f * density, textPaint)
                    lastLabeledMonth = month
                    lastLabelX = colX
                }
            }

            for (dayIdx in 0..6) {
                val cellDate = weekStart.plusDays(dayIdx.toLong())
                val rowY = startY + dayIdx * (cellSize + gap)
                rect.set(colX, rowY, colX + cellSize, rowY + cellSize)

                if (cellDate.isAfter(today)) {
                    fillPaint.color = futureColor
                    canvas.drawRoundRect(rect, cornerRadius, cornerRadius, fillPaint)
                    continue
                }

                val count = logsByEpochDay[cellDate.toEpochDay()]?.count ?: 0
                val level = HabitPalette.calculateIntensityLevel(count, targetPerDay)
                fillPaint.color = colors.level(level)

                if (cellDate.isEqual(today)) {
                    // Today: an accent ring around a smaller cell, so it shows at every level.
                    val stroke = todayRingPaint.strokeWidth
                    val ringInset = stroke / 2f
                    rect.inset(ringInset, ringInset)
                    canvas.drawRoundRect(rect, cornerRadius, cornerRadius, todayRingPaint)

                    val fillInset = stroke * 2f
                    rect.set(
                        colX + fillInset,
                        rowY + fillInset,
                        colX + cellSize - fillInset,
                        rowY + cellSize - fillInset
                    )
                    val innerRadius = cornerRadius * 0.5f
                    canvas.drawRoundRect(rect, innerRadius, innerRadius, fillPaint)
                } else {
                    canvas.drawRoundRect(rect, cornerRadius, cornerRadius, fillPaint)
                }
            }
        }

        return bitmap
    }
}
