package app.tessera.habit.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/**
 * Curated 5-Stop Architectural Chromatic Heatmap Palettes.
 *
 * Exposes both Compose [Color] properties ([accent], [level1]..[level4], [onAccent])
 * and Android ARGB [Int] helpers ([levelColorArgb]) so both Compose Canvas and
 * RemoteViews Home Screen Widget bitmap renderers share the exact same color logic.
 */
@Immutable
data class HabitPaletteSpec(
    val key: String,
    val displayName: String,
    val subtitle: String,
    val level1: Color,
    val level2: Color,
    val level3: Color,
    val level4: Color,
    val onAccent: Color,
    val lightLevel1: Color = level1.copy(alpha = 0.22f),
    val lightLevel2: Color = level2.copy(alpha = 0.48f),
    val lightLevel3: Color = level3.copy(alpha = 0.78f),
    val lightLevel4: Color = level4
) {
    val accent: Color get() = level4

    /**
     * Returns the Compose [Color] for a heatmap intensity level (0..4).
     */
    fun colorForLevel(level: Int, isDark: Boolean = true): Color {
        val clamped = level.coerceIn(0, 4)
        if (clamped == 0) {
            return if (isDark) SurfaceRecessed else SurfaceRecessedLight
        }
        if (key == "monochrome" && !isDark) {
            return when (clamped) {
                1 -> Color(0xFFD4D2CA)
                2 -> Color(0xFF9EA3AE)
                3 -> Color(0xFF4B5262)
                else -> Color(0xFF111317)
            }
        }
        return if (isDark) {
            when (clamped) {
                1 -> level1
                2 -> level2
                3 -> level3
                else -> level4
            }
        } else {
            when (clamped) {
                1 -> lightLevel1
                2 -> lightLevel2
                3 -> lightLevel3
                else -> lightLevel4
            }
        }
    }

    /**
     * Returns an opaque Android ARGB [Int] suitable for [android.graphics.Paint.setColor]
     * in `WidgetHeatmapBitmapRenderer`.
     */
    fun levelColorArgb(level: Int, isDark: Boolean = true): Int {
        val clamped = level.coerceIn(0, 4)
        if (clamped == 0) {
            return if (isDark) 0xFF181B22.toInt() else 0xFFEDEBE4.toInt()
        }
        if (key == "monochrome" && !isDark) {
            return when (clamped) {
                1 -> 0xFFD4D2CA.toInt()
                2 -> 0xFF9EA3AE.toInt()
                3 -> 0xFF4B5262.toInt()
                else -> 0xFF111317.toInt()
            }
        }
        return when (clamped) {
            1 -> if (isDark) level1.toArgb() else blendOnLight(level4, 0.25f)
            2 -> if (isDark) level2.toArgb() else blendOnLight(level4, 0.50f)
            3 -> if (isDark) level3.toArgb() else blendOnLight(level4, 0.76f)
            else -> level4.toArgb()
        }
    }

    /**
     * Calculates the discrete intensity level (0..4) for a given [count] and [targetPerDay].
     */
    fun intensityLevel(count: Int, targetPerDay: Int): Int {
        return HabitPalette.calculateIntensityLevel(count, targetPerDay)
    }

    private fun blendOnLight(fg: Color, ratio: Float): Int {
        val bgR = 0xED
        val bgG = 0xEB
        val bgB = 0xE4
        val fgArgb = fg.toArgb()
        val fgR = (fgArgb shr 16) and 0xFF
        val fgG = (fgArgb shr 8) and 0xFF
        val fgB = fgArgb and 0xFF
        val r = (bgR + (fgR - bgR) * ratio).toInt().coerceIn(0, 255)
        val g = (bgG + (fgG - bgG) * ratio).toInt().coerceIn(0, 255)
        val b = (bgB + (fgB - bgB) * ratio).toInt().coerceIn(0, 255)
        return (0xFF shl 24) or (r shl 16) or (g shl 8) or b
    }
}

object HabitPalette {

    val EmeraldMatrix = HabitPaletteSpec(
        key = "emerald",
        displayName = "Emerald Matrix",
        subtitle = "Classic GitHub Phosphor",
        level1 = Color(0xFF0E3A2A),
        level2 = Color(0xFF166544),
        level3 = Color(0xFF229965),
        level4 = Color(0xFF34D399),
        onAccent = Color(0xFF052E1E),
        lightLevel1 = Color(0xFFC6F0DC),
        lightLevel2 = Color(0xFF7CE0B3),
        lightLevel3 = Color(0xFF34C78A),
        lightLevel4 = Color(0xFF10B981)
    )

    val CyberAmber = HabitPaletteSpec(
        key = "amber",
        displayName = "Cyber Amber",
        subtitle = "Teenage Eng. Warm Signal",
        level1 = Color(0xFF45230B),
        level2 = Color(0xFF85420E),
        level3 = Color(0xFFC26116),
        level4 = Color(0xFFF59E0B),
        onAccent = Color(0xFF271202),
        lightLevel1 = Color(0xFFFDE6B8),
        lightLevel2 = Color(0xFFFBC868),
        lightLevel3 = Color(0xFFF59E0B),
        lightLevel4 = Color(0xFFD97706)
    )

    val CobaltPulse = HabitPaletteSpec(
        key = "cobalt",
        displayName = "Cobalt Pulse",
        subtitle = "Linear Electric Blue",
        level1 = Color(0xFF152956),
        level2 = Color(0xFF1E40AF),
        level3 = Color(0xFF3B66F5),
        level4 = Color(0xFF60A5FA),
        onAccent = Color(0xFF08142C),
        lightLevel1 = Color(0xFFCEE0FD),
        lightLevel2 = Color(0xFF8BBAFA),
        lightLevel3 = Color(0xFF4B8DF7),
        lightLevel4 = Color(0xFF2563EB)
    )

    val CrimsonEmber = HabitPaletteSpec(
        key = "crimson",
        displayName = "Crimson Ember",
        subtitle = "Nothing OS Signal Red",
        level1 = Color(0xFF47141A),
        level2 = Color(0xFF821D29),
        level3 = Color(0xFFBE293B),
        level4 = Color(0xFFF43F5E),
        onAccent = Color(0xFF28070B),
        lightLevel1 = Color(0xFFFCCDD6),
        lightLevel2 = Color(0xFFF994A6),
        lightLevel3 = Color(0xFFF45B75),
        lightLevel4 = Color(0xFFE11D48)
    )

    val VioletNebula = HabitPaletteSpec(
        key = "violet",
        displayName = "Violet Nebula",
        subtitle = "Deep Synth Purple",
        level1 = Color(0xFF2B184E),
        level2 = Color(0xFF4C2889),
        level3 = Color(0xFF7442C8),
        level4 = Color(0xFFA78BFA),
        onAccent = Color(0xFF170B2E),
        lightLevel1 = Color(0xFFE2D6FC),
        lightLevel2 = Color(0xFFC0A6FA),
        lightLevel3 = Color(0xFF966FF5),
        lightLevel4 = Color(0xFF7C3AED)
    )

    val CyanGlacier = HabitPaletteSpec(
        key = "cyan",
        displayName = "Cyan Glacier",
        subtitle = "Cryo Telemetry",
        level1 = Color(0xFF0C3644),
        level2 = Color(0xFF145E75),
        level3 = Color(0xFF0891B2),
        level4 = Color(0xFF22D3EE),
        onAccent = Color(0xFF04222C),
        lightLevel1 = Color(0xFFC2EFF7),
        lightLevel2 = Color(0xFF79DDF0),
        lightLevel3 = Color(0xFF22B8D6),
        lightLevel4 = Color(0xFF0891B2)
    )

    val RoseQuartz = HabitPaletteSpec(
        key = "rose",
        displayName = "Rose Quartz",
        subtitle = "Editorial Coral Pink",
        level1 = Color(0xFF461533),
        level2 = Color(0xFF7D2259),
        level3 = Color(0xFFB83280),
        level4 = Color(0xFFF472B6),
        onAccent = Color(0xFF29081D),
        lightLevel1 = Color(0xFFFBD0E8),
        lightLevel2 = Color(0xFFF79AD0),
        lightLevel3 = Color(0xFFEC5BA9),
        lightLevel4 = Color(0xFFDB2777)
    )

    val MonochromeInk = HabitPaletteSpec(
        key = "monochrome",
        displayName = "Monochrome Ink",
        subtitle = "Swiss High-Contrast",
        level1 = Color(0xFF272B33),
        level2 = Color(0xFF4B5262),
        level3 = Color(0xFF8A92A6),
        level4 = Color(0xFFF3F4F6),
        onAccent = Color(0xFF07080B),
        lightLevel1 = Color(0xFFD4D2CA),
        lightLevel2 = Color(0xFF9EA3AE),
        lightLevel3 = Color(0xFF4B5262),
        lightLevel4 = Color(0xFF111317)
    )

    /**
     * All 8 primary architectural palettes shown in the Habit Editor swatch picker.
     */
    val allPalettes: List<HabitPaletteSpec> = listOf(
        EmeraldMatrix,
        CyberAmber,
        CobaltPulse,
        CrimsonEmber,
        VioletNebula,
        CyanGlacier,
        RoseQuartz,
        MonochromeInk
    )

    /**
     * Resolves any palette key or alias (`"emerald"`, `"cyan"`, `"glacier"`, `"cobalt"`, `"indigo"`,
     * `"amber"`, `"orange"`, `"crimson"`, `"rose"`, `"quartz"`, `"violet"`, `"lime"`, `"monochrome"`)
     * to a [HabitPaletteSpec].
     */
    fun resolve(key: String?): HabitPaletteSpec {
        return when (key?.trim()?.lowercase()) {
            "emerald", "emerald_matrix", "green", "lime" -> EmeraldMatrix
            "amber", "cyber_amber", "orange", "gold" -> CyberAmber
            "cobalt", "cobalt_pulse", "blue", "indigo" -> CobaltPulse
            "crimson", "crimson_ember", "red" -> CrimsonEmber
            "violet", "violet_nebula", "purple" -> VioletNebula
            "cyan", "cyan_glacier", "glacier", "teal" -> CyanGlacier
            "rose", "rose_quartz", "quartz", "pink" -> RoseQuartz
            "monochrome", "monochrome_ink", "mono", "ink", "slate" -> MonochromeInk
            else -> EmeraldMatrix
        }
    }

    /**
     * Computes the GitHub heatmap quartile level (0..4) for a given completion count and target.
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
