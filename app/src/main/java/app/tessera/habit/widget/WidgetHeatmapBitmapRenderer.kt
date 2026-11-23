package app.tessera.habit.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import app.tessera.habit.data.model.HabitWithLogs
import app.tessera.habit.ui.theme.HabitPalette
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.floor

/** How a widget's heatmap is laid out, chosen from the widget's shape. */
enum class WidgetShape {
    /** Small or tall: days across, one row per week, with weekday letters. */
    CALENDAR,

    /** In between: one column per week, as many weeks as fit the width. */
    FILL,

    /** Wide: title on the left, heatmap on the right using the full height. */
    SIDE
}

/** Text for the preview on the setup screen. The real widget draws this text with views. */
class WidgetChrome(
    val title: String,
    val streakText: String,
    val statusText: String,
    val glyph: String
)

/**
 * Draws a whole widget card to a bitmap at the widget's real size: the card, the heatmap and
 * the check-in circle. Cells are always square and the heatmap sits 14dp from the card edges.
 * The title, streak and check-in glyph are views on top of this bitmap in the real widget;
 * pass a [WidgetChrome] to have them drawn here too (for the preview on the setup screen).
 *
 * All drawing is in dp: the canvas is scaled by the screen density first.
 */
object WidgetHeatmapBitmapRenderer {

    /** Card padding, and the distance from the card edge to the heatmap. */
    const val PAD_DP = 14f

    private const val CARD_RADIUS = 28f
    const val BUTTON_RADIUS_DP = 18f
    /** Room above the heatmap for a two-line title and a status line. */
    private const val HEADER_HEIGHT = 54f
    private const val SIDE_COLUMN_WIDTH = 104f
    private const val GAP_RATIO = 0.2f
    private const val LABEL_HEIGHT = 14f

    fun shapeFor(widthDp: Float, heightDp: Float): WidgetShape {
        val ratio = widthDp / heightDp.coerceAtLeast(1f)
        return when {
            ratio >= 1.8f -> WidgetShape.SIDE
            ratio <= 1.1f -> WidgetShape.CALENDAR
            else -> WidgetShape.FILL
        }
    }

    fun render(
        context: Context,
        habitWithLogs: HabitWithLogs?,
        widthDp: Float,
        heightDp: Float,
        isDark: Boolean,
        style: WidgetStyle,
        chrome: WidgetChrome? = null
    ): Bitmap {
        val density = context.resources.displayMetrics.density.coerceIn(1.5f, 3.5f)
        val fontScale = context.resources.configuration.fontScale.coerceIn(1f, 1.3f)
        val w = widthDp.coerceAtLeast(100f)
        val h = heightDp.coerceAtLeast(100f)
        val shape = shapeFor(w, h)

        val bitmap = Bitmap.createBitmap(
            (w * density).toInt().coerceAtLeast(1),
            (h * density).toInt().coerceAtLeast(1),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        canvas.scale(density, density)

        val habit = habitWithLogs?.habit
        val colors = WidgetColors.of(isDark, style, WidgetPalette.resolve(habit?.colorKey))
        val target = (habit?.targetPerDay ?: 1).coerceAtLeast(1)
        val logs = habitWithLogs?.logsByEpochDay() ?: emptyMap()
        val today = LocalDate.now()
        val thisMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val todayCount = habitWithLogs?.countForDate(today) ?: 0
        val doneToday = habit != null && todayCount >= target

        // Paint's default style is FILL. (Not set here: `style` would mean the parameter.)
        val fill = Paint(Paint.ANTI_ALIAS_FLAG)
        val rect = RectF()

        // Card.
        if (colors.backgroundTop == colors.backgroundBottom) {
            fill.color = colors.backgroundTop
        } else {
            fill.shader = LinearGradient(
                0f, 0f, w * 0.3f, h,
                colors.backgroundTop, colors.backgroundBottom,
                Shader.TileMode.CLAMP
            )
        }
        rect.set(0f, 0f, w, h)
        canvas.drawRoundRect(rect, CARD_RADIUS, CARD_RADIUS, fill)
        fill.shader = null

        // One cell. Future days are drawn faint; today gets an accent ring around a smaller cell.
        fun drawCell(x: Float, y: Float, size: Float, date: LocalDate) {
            val radius = size * 0.24f
            rect.set(x, y, x + size, y + size)
            if (date.isAfter(today)) {
                fill.color = (colors.level(0) and 0x00FFFFFF) or (0x73 shl 24)
                canvas.drawRoundRect(rect, radius, radius, fill)
                return
            }
            val count = logs[date.toEpochDay()]?.count ?: 0
            val level = HabitPalette.calculateIntensityLevel(count, target)
            fill.color = colors.level(level)
            if (date.isEqual(today)) {
                val stroke = (size * 0.14f).coerceAtLeast(1.2f)
                val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.style = Paint.Style.STROKE
                    strokeWidth = stroke
                    color = colors.accent
                }
                rect.set(x + stroke / 2f, y + stroke / 2f, x + size - stroke / 2f, y + size - stroke / 2f)
                canvas.drawRoundRect(rect, radius, radius, ring)
                rect.set(x + stroke * 2f, y + stroke * 2f, x + size - stroke * 2f, y + size - stroke * 2f)
                canvas.drawRoundRect(rect, radius * 0.5f, radius * 0.5f, fill)
            } else {
                canvas.drawRoundRect(rect, radius, radius, fill)
            }
        }

        val labelPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colors.textSecondary
            textSize = 12f * fontScale.coerceAtMost(1.15f)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }

        // Heatmap.
        when (shape) {
            WidgetShape.FILL -> {
                val gw = w - 2 * PAD_DP
                val gy = PAD_DP + HEADER_HEIGHT + 10f
                val gh = (h - gy - PAD_DP).coerceAtLeast(20f)
                val weeks = ceil(((gw * (7 + 6 * GAP_RATIO)) / gh + GAP_RATIO) / (1 + GAP_RATIO))
                    .toInt().coerceIn(4, 26)
                val cell = gw / (weeks * (1 + GAP_RATIO) - GAP_RATIO)
                val gap = cell * GAP_RATIO
                val top = gy + gh - (7 * cell + 6 * gap)
                for (week in 0 until weeks) {
                    val monday = thisMonday.minusWeeks((weeks - 1 - week).toLong())
                    for (day in 0..6) {
                        drawCell(
                            PAD_DP + week * (cell + gap),
                            top + day * (cell + gap),
                            cell,
                            monday.plusDays(day.toLong())
                        )
                    }
                }
            }

            WidgetShape.CALENDAR -> {
                val gw = w - 2 * PAD_DP
                val cell = gw / (7 + 6 * GAP_RATIO)
                val gap = cell * GAP_RATIO
                val gy = PAD_DP + HEADER_HEIGHT + 8f + LABEL_HEIGHT + 4f
                val gh = (h - gy - PAD_DP).coerceAtLeast(cell)
                val rows = floor((gh + gap) / (cell + gap)).toInt().coerceIn(2, 12)
                val top = h - PAD_DP - (rows * cell + (rows - 1) * gap)
                for (day in 0..6) {
                    val letter = DayOfWeek.of(day + 1)
                        .getDisplayName(TextStyle.NARROW, Locale.getDefault())
                    canvas.drawText(
                        letter,
                        PAD_DP + day * (cell + gap) + cell / 2f,
                        top - 6f,
                        labelPaint
                    )
                }
                for (row in 0 until rows) {
                    val monday = thisMonday.minusWeeks((rows - 1 - row).toLong())
                    for (day in 0..6) {
                        drawCell(
                            PAD_DP + day * (cell + gap),
                            top + row * (cell + gap),
                            cell,
                            monday.plusDays(day.toLong())
                        )
                    }
                }
            }

            WidgetShape.SIDE -> {
                val gh = h - 2 * PAD_DP
                val cell = gh / (7 + 6 * GAP_RATIO)
                val gap = cell * GAP_RATIO
                val available = w - PAD_DP - (PAD_DP + SIDE_COLUMN_WIDTH + 10f)
                val weeks = floor((available + gap) / (cell + gap)).toInt().coerceIn(4, 26)
                val left = w - PAD_DP - (weeks * cell + (weeks - 1) * gap)
                for (week in 0 until weeks) {
                    val monday = thisMonday.minusWeeks((weeks - 1 - week).toLong())
                    for (day in 0..6) {
                        drawCell(
                            left + week * (cell + gap),
                            PAD_DP + day * (cell + gap),
                            cell,
                            monday.plusDays(day.toLong())
                        )
                    }
                }
            }
        }

        // Check-in circle: filled when today is done, an outline otherwise.
        val buttonX: Float
        val buttonY: Float
        if (shape == WidgetShape.SIDE) {
            buttonX = PAD_DP + BUTTON_RADIUS_DP
            buttonY = h - PAD_DP - BUTTON_RADIUS_DP
        } else {
            buttonX = w - PAD_DP - BUTTON_RADIUS_DP
            buttonY = PAD_DP + 2f + BUTTON_RADIUS_DP
        }
        if (doneToday) {
            fill.color = colors.accent
            canvas.drawCircle(buttonX, buttonY, BUTTON_RADIUS_DP, fill)
        } else {
            val outline = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.style = Paint.Style.STROKE
                strokeWidth = 2f
                color = colors.accent
            }
            canvas.drawCircle(buttonX, buttonY, BUTTON_RADIUS_DP - 1f, outline)
        }

        if (chrome != null) {
            drawChrome(canvas, chrome, shape, w, h, buttonX, buttonY, colors, doneToday, fontScale)
        }

        return bitmap
    }

    private fun drawChrome(
        canvas: Canvas,
        chrome: WidgetChrome,
        shape: WidgetShape,
        w: Float,
        h: Float,
        buttonX: Float,
        buttonY: Float,
        colors: WidgetColors,
        doneToday: Boolean,
        fontScale: Float
    ) {
        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colors.textPrimary
            textSize = 16f * fontScale
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        }
        val subPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colors.textSecondary
            textSize = 13f * fontScale
        }
        val glyphPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (doneToday) colors.onAccent else colors.accent
            textSize = 15f
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val textWidth = if (shape == WidgetShape.SIDE) SIDE_COLUMN_WIDTH else w - 2 * PAD_DP - 44f
        val maxTitleLines = 2

        canvas.save()
        canvas.translate(PAD_DP, PAD_DP)
        val titleLayout = StaticLayout.Builder
            .obtain(chrome.title, 0, chrome.title.length, titlePaint, textWidth.toInt())
            .setMaxLines(maxTitleLines)
            .setEllipsize(TextUtils.TruncateAt.END)
            .build()
        titleLayout.draw(canvas)
        canvas.restore()

        val subTop = PAD_DP + titleLayout.height + 2f
        val subBaseline = subTop - subPaint.fontMetrics.ascent
        subPaint.color = colors.accent
        var x = PAD_DP
        val streak = TextUtils.ellipsize(
            chrome.streakText, subPaint, textWidth, TextUtils.TruncateAt.END
        ).toString()
        canvas.drawText(streak, x, subBaseline, subPaint)
        x += subPaint.measureText(streak)
        subPaint.color = colors.textSecondary
        val remaining = PAD_DP + textWidth - x
        if (remaining > 12f && chrome.statusText.isNotEmpty()) {
            val status = TextUtils.ellipsize(
                chrome.statusText, subPaint, remaining, TextUtils.TruncateAt.END
            ).toString()
            canvas.drawText(status, x, subBaseline, subPaint)
        }

        val glyphBaseline = buttonY - (glyphPaint.fontMetrics.ascent + glyphPaint.fontMetrics.descent) / 2f
        canvas.drawText(chrome.glyph, buttonX, glyphBaseline, glyphPaint)
    }
}
