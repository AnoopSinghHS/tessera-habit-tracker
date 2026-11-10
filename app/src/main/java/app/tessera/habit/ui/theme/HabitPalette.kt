package app.tessera.habit.ui.theme

import androidx.compose.ui.graphics.toArgb

/**
 * Heatmap level maths and the ARGB colours used by the home-screen widgets.
 *
 * In-app Compose code should read colours from [TesseraTheme.colors] (`accent`, `heat`).
 * Widgets draw to a bitmap outside Compose, so they get their colours here.
 */
object HabitPalette {

    /** Night variant of the ramp, used only by widgets set to the dark surface. */
    private val nightHeatArgb = intArrayOf(
        0xFF172135.toInt(),
        0xFF1E3D6B.toInt(),
        0xFF2C68AD.toInt(),
        0xFF58A6EA.toInt(),
        0xFFCDEBFF.toInt()
    )

    /** ARGB colour for a heatmap level (0..4) on a light or dark widget surface. */
    fun levelColorArgb(level: Int, isDark: Boolean): Int {
        val clamped = level.coerceIn(0, 4)
        return if (isDark) nightHeatArgb[clamped] else HomesteadColorTokens.heat[clamped].toArgb()
    }

    /** Accent ARGB for widget buttons and badges. */
    fun accentArgb(isDark: Boolean): Int {
        return if (isDark) 0xFF9AD4FF.toInt() else HomesteadColorTokens.accent.toArgb()
    }

    /** Colour for content drawn on top of [accentArgb]. */
    fun onAccentArgb(isDark: Boolean): Int {
        return if (isDark) 0xFF03182B.toInt() else HomesteadColorTokens.onAccent.toArgb()
    }

    /**
     * Heatmap level (0..4) for a day's count against the habit's daily target.
     * A once-a-day habit is either empty (0) or full (4).
     */
    fun calculateIntensityLevel(count: Int, targetPerDay: Int): Int {
        if (count <= 0) return 0
        if (targetPerDay <= 1) return 4
        val ratio = count.toFloat() / targetPerDay.toFloat()
        return when {
            ratio >= 1.0f -> 4
            ratio >= 0.67f -> 3
            ratio >= 0.34f -> 2
            else -> 1
        }
    }
}
