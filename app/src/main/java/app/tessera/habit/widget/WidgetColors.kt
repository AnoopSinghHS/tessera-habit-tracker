package app.tessera.habit.widget

import app.tessera.habit.ui.theme.HabitPalette

/**
 * Colours for one widget surface. Light is the app's Homestead look; dark is a night
 * version for dark wallpapers. The heatmap ramp and accent come from [HabitPalette].
 */
class WidgetColors private constructor(
    val isDark: Boolean,
    val surface: Int,
    val textPrimary: Int,
    val textSecondary: Int,
    val hairline: Int
) {
    val accent: Int get() = HabitPalette.accentArgb(isDark)
    val onAccent: Int get() = HabitPalette.onAccentArgb(isDark)

    fun level(level: Int): Int = HabitPalette.levelColorArgb(level, isDark)

    companion object {
        private val Light = WidgetColors(
            isDark = false,
            surface = 0xFFFAF4E8.toInt(),
            textPrimary = 0xFF2A2016.toInt(),
            textSecondary = 0xFF65533F.toInt(),
            hairline = 0xFFDCCBAE.toInt()
        )
        private val Dark = WidgetColors(
            isDark = true,
            surface = 0xFF0E1420.toInt(),
            textPrimary = 0xFFEAF0FA.toInt(),
            textSecondary = 0xFFA3B1C8.toInt(),
            hairline = 0xFF222C41.toInt()
        )

        fun of(isDark: Boolean): WidgetColors = if (isDark) Dark else Light
    }
}
