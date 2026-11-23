package app.tessera.habit.widget

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.ColorUtils

/**
 * The colours a habit can have on its widget. Each habit stores one of these keys in
 * `HabitEntity.colorKey`; the key names are the ones older versions of the app used, so
 * existing habits and backups keep a sensible colour.
 */
object WidgetPalette {

    class Swatch(
        val key: String,
        val label: String,
        /** Hue in degrees, 0..360. */
        val hue: Float,
        /** 1 for a coloured swatch, 0 for grey. */
        val saturationScale: Float
    ) {
        /** A mid-tone of the swatch, for the colour picker in the habit editor. */
        val previewColor: Color
            get() = Color(WidgetPalette.hsl(hue, 72f, 52f, saturationScale))
    }

    val swatches: List<Swatch> = listOf(
        Swatch("amber", "Amber", 38f, 1f),
        Swatch("emerald", "Green", 152f, 1f),
        Swatch("violet", "Violet", 262f, 1f),
        Swatch("rose", "Rose", 350f, 1f),
        Swatch("cobalt", "Blue", 215f, 1f),
        Swatch("cyan", "Teal", 186f, 1f),
        Swatch("crimson", "Red", 8f, 1f),
        Swatch("monochrome", "Grey", 215f, 0f)
    )

    /** Keys given to new habits in turn, so neighbouring habits get different colours. */
    private val rotation = listOf("amber", "emerald", "violet", "rose", "cobalt", "cyan", "crimson")

    fun defaultKeyFor(habitIndex: Int): String =
        rotation[Math.floorMod(habitIndex, rotation.size)]

    /** Finds the swatch for a stored key, including the names older versions used. */
    fun resolve(key: String?): Swatch {
        val canonical = when (key?.trim()?.lowercase()) {
            "amber", "cyber_amber", "ochre", "orange", "gold" -> "amber"
            "emerald", "emerald_matrix", "green", "lime", "forest" -> "emerald"
            "violet", "violet_nebula", "purple", "fig" -> "violet"
            "rose", "rose_quartz", "quartz", "pink", "clay" -> "rose"
            "cobalt", "cobalt_pulse", "blue", "indigo", "slate" -> "cobalt"
            "cyan", "cyan_glacier", "glacier", "teal", "fjord" -> "cyan"
            "crimson", "crimson_ember", "terracotta", "red" -> "crimson"
            "monochrome", "monochrome_ink", "mono", "ink", "espresso" -> "monochrome"
            else -> "emerald"
        }
        return swatches.first { it.key == canonical }
    }

    /** ARGB colour from hue (0..360), saturation and lightness (both 0..100). */
    internal fun hsl(hue: Float, saturation: Float, lightness: Float, saturationScale: Float): Int {
        val s = (saturation * saturationScale / 100f).coerceIn(0f, 1f)
        val l = (lightness / 100f).coerceIn(0f, 1f)
        return ColorUtils.HSLToColor(floatArrayOf(hue, s, l))
    }
}
