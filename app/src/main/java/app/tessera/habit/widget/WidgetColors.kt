package app.tessera.habit.widget

import android.graphics.Color
import app.tessera.habit.widget.WidgetPalette.hsl

/**
 * Colours for one widget: its surface (light or dark), the [WidgetStyle] and the habit's
 * colour. The mock-ups these values came from are in the design canvas for the widget redesign.
 */
class WidgetColors private constructor(
    val isDark: Boolean,
    /** Card colour at the top; [backgroundBottom] is the same unless the style is a wash. */
    val backgroundTop: Int,
    val backgroundBottom: Int,
    val textPrimary: Int,
    val textSecondary: Int,
    val accent: Int,
    val onAccent: Int,
    /** Heatmap ramp, index 0 (empty) to 4 (target met). */
    private val ramp: IntArray
) {
    fun level(level: Int): Int = ramp[level.coerceIn(0, 4)]

    companion object {
        fun of(isDark: Boolean, style: WidgetStyle, swatch: WidgetPalette.Swatch): WidgetColors {
            val h = swatch.hue
            val k = swatch.saturationScale
            return when {
                style == WidgetStyle.TINTED && isDark -> WidgetColors(
                    isDark = true,
                    backgroundTop = 0xFF14171C.toInt(),
                    backgroundBottom = 0xFF14171C.toInt(),
                    textPrimary = 0xFFF3F5F8.toInt(),
                    textSecondary = 0xFFAAB2BF.toInt(),
                    accent = hsl(h, 90f, 68f, k),
                    onAccent = 0xFF15110A.toInt(),
                    ramp = intArrayOf(
                        hsl(h, 16f, 20f, k), hsl(h, 55f, 31f, k), hsl(h, 68f, 43f, k),
                        hsl(h, 78f, 57f, k), hsl(h, 88f, 70f, k)
                    )
                )
                style == WidgetStyle.TINTED -> WidgetColors(
                    isDark = false,
                    backgroundTop = 0xFFFBF6EC.toInt(),
                    backgroundBottom = 0xFFFBF6EC.toInt(),
                    textPrimary = 0xFF2A2016.toInt(),
                    textSecondary = 0xFF665643.toInt(),
                    accent = hsl(h, 80f, 32f, k),
                    onAccent = Color.WHITE,
                    ramp = intArrayOf(
                        hsl(h, 22f, 88f, k), hsl(h, 60f, 78f, k), hsl(h, 68f, 64f, k),
                        hsl(h, 74f, 50f, k), hsl(h, 80f, 38f, k)
                    )
                )
                isDark -> WidgetColors(
                    isDark = true,
                    backgroundTop = hsl(h, 38f, 18f, k),
                    backgroundBottom = hsl(h, 42f, 10f, k),
                    textPrimary = 0xFFF6F2EE.toInt(),
                    textSecondary = hsl(h, 25f, 78f, k),
                    accent = hsl(h, 95f, 72f, k),
                    onAccent = 0xFF15110A.toInt(),
                    ramp = intArrayOf(
                        hsl(h, 28f, 25f, k), hsl(h, 55f, 33f, k), hsl(h, 68f, 45f, k),
                        hsl(h, 78f, 58f, k), hsl(h, 88f, 72f, k)
                    )
                )
                else -> WidgetColors(
                    isDark = false,
                    backgroundTop = hsl(h, 75f, 95f, k),
                    backgroundBottom = hsl(h, 60f, 89f, k),
                    textPrimary = 0xFF2A2016.toInt(),
                    textSecondary = hsl(h, 25f, 28f, k),
                    accent = hsl(h, 80f, 30f, k),
                    onAccent = Color.WHITE,
                    ramp = intArrayOf(
                        hsl(h, 35f, 84f, k), hsl(h, 60f, 76f, k), hsl(h, 68f, 62f, k),
                        hsl(h, 74f, 48f, k), hsl(h, 80f, 36f, k)
                    )
                )
            }
        }
    }
}
