package app.tessera.habit.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/**
 * Curated 5-Stop Organic Architectural Pigment Heatmap Palettes.
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
    val lightLevel1: Color,
    val lightLevel2: Color,
    val lightLevel3: Color,
    val lightLevel4: Color,
    val lightOnAccent: Color = Color(0xFFFCFBF9)
) {
    val accent: Color get() = lightLevel4

    fun accentFor(isDark: Boolean): Color = if (isDark) level4 else lightLevel4

    fun onAccentFor(isDark: Boolean): Color = if (isDark) onAccent else lightOnAccent

    /**
     * Returns the Compose [Color] for a heatmap intensity level (0..4).
     */
    fun colorForLevel(level: Int, isDark: Boolean = false): Color {
        val clamped = level.coerceIn(0, 4)
        if (clamped == 0) {
            return if (isDark) SurfaceRecessed else SurfaceRecessedLight
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
            return if (isDark) 0xFF262421.toInt() else 0xFFEAE6DF.toInt()
        }
        return if (isDark) {
            when (clamped) {
                1 -> level1.toArgb()
                2 -> level2.toArgb()
                3 -> level3.toArgb()
                else -> level4.toArgb()
            }
        } else {
            when (clamped) {
                1 -> lightLevel1.toArgb()
                2 -> lightLevel2.toArgb()
                3 -> lightLevel3.toArgb()
                else -> lightLevel4.toArgb()
            }
        }
    }

    /**
     * Calculates the discrete intensity level (0..4) for a given [count] and [targetPerDay].
     */
    fun intensityLevel(count: Int, targetPerDay: Int): Int {
        return HabitPalette.calculateIntensityLevel(count, targetPerDay)
    }
}

object HabitPalette {

    val EmeraldMatrix = HabitPaletteSpec(
        key = "emerald",
        displayName = "Botanical Forest",
        subtitle = "Deep Woodland Pigment",
        level1 = Color(0xFF1F3329),
        level2 = Color(0xFF2B4D3C),
        level3 = Color(0xFF396B52),
        level4 = Color(0xFF488B69),
        onAccent = Color(0xFFFCFBF9),
        lightLevel1 = Color(0xFFD5E2DA),
        lightLevel2 = Color(0xFF9ABAA7),
        lightLevel3 = Color(0xFF5B8A71),
        lightLevel4 = Color(0xFF2D5A43),
        lightOnAccent = Color(0xFFFCFBF9)
    )

    val CrimsonEmber = HabitPaletteSpec(
        key = "crimson",
        displayName = "Braun Terracotta",
        subtitle = "Warm Architectural Clay",
        level1 = Color(0xFF3B241C),
        level2 = Color(0xFF6B3A2A),
        level3 = Color(0xFF9E5038),
        level4 = Color(0xFFD46A48),
        onAccent = Color(0xFFFCFBF9),
        lightLevel1 = Color(0xFFEED6CD),
        lightLevel2 = Color(0xFFDCA490),
        lightLevel3 = Color(0xFFCA795D),
        lightLevel4 = Color(0xFFB85435),
        lightOnAccent = Color(0xFFFCFBF9)
    )

    val CobaltPulse = HabitPaletteSpec(
        key = "cobalt",
        displayName = "Prussian Slate",
        subtitle = "Mineral Maritime Ink",
        level1 = Color(0xFF1F2A36),
        level2 = Color(0xFF314459),
        level3 = Color(0xFF455E7A),
        level4 = Color(0xFF5B7A9C),
        onAccent = Color(0xFFFCFBF9),
        lightLevel1 = Color(0xFFD5DCE3),
        lightLevel2 = Color(0xFF9FB0C2),
        lightLevel3 = Color(0xFF698199),
        lightLevel4 = Color(0xFF3B526B),
        lightOnAccent = Color(0xFFFCFBF9)
    )

    val CyberAmber = HabitPaletteSpec(
        key = "amber",
        displayName = "Warm Ochre",
        subtitle = "Raw Sienna Earth",
        level1 = Color(0xFF3B2C1A),
        level2 = Color(0xFF6B4E2B),
        level3 = Color(0xFFA3753E),
        level4 = Color(0xFFDFA055),
        onAccent = Color(0xFF1C1B19),
        lightLevel1 = Color(0xFFEFE0CD),
        lightLevel2 = Color(0xFFE0BC92),
        lightLevel3 = Color(0xFFD19B5F),
        lightLevel4 = Color(0xFFC27D38),
        lightOnAccent = Color(0xFFFCFBF9)
    )

    val VioletNebula = HabitPaletteSpec(
        key = "violet",
        displayName = "Muted Fig",
        subtitle = "Botanical Plum",
        level1 = Color(0xFF2D2434),
        level2 = Color(0xFF483954),
        level3 = Color(0xFF675278),
        level4 = Color(0xFF886FA0),
        onAccent = Color(0xFFFCFBF9),
        lightLevel1 = Color(0xFFE0DAE4),
        lightLevel2 = Color(0xFFB7A8C2),
        lightLevel3 = Color(0xFF8A7799),
        lightLevel4 = Color(0xFF5E4B6B),
        lightOnAccent = Color(0xFFFCFBF9)
    )

    val CyanGlacier = HabitPaletteSpec(
        key = "cyan",
        displayName = "Nordic Fjord",
        subtitle = "Weathered Sea Slate",
        level1 = Color(0xFF1E3133),
        level2 = Color(0xFF2F5053),
        level3 = Color(0xFF447276),
        level4 = Color(0xFF5C969A),
        onAccent = Color(0xFFFCFBF9),
        lightLevel1 = Color(0xFFD3E1E2),
        lightLevel2 = Color(0xFF9CBBC0),
        lightLevel3 = Color(0xFF699397),
        lightLevel4 = Color(0xFF3E6B6E),
        lightOnAccent = Color(0xFFFCFBF9)
    )

    val RoseQuartz = HabitPaletteSpec(
        key = "rose",
        displayName = "Dusty Clay",
        subtitle = "Pressed Rose Terracotta",
        level1 = Color(0xFF382226),
        level2 = Color(0xFF613940),
        level3 = Color(0xFF91555E),
        level4 = Color(0xFFC47680),
        onAccent = Color(0xFFFCFBF9),
        lightLevel1 = Color(0xFFEAD7DA),
        lightLevel2 = Color(0xFFD1A4AB),
        lightLevel3 = Color(0xFFB87C85),
        lightLevel4 = Color(0xFF9E5A63),
        lightOnAccent = Color(0xFFFCFBF9)
    )

    val MonochromeInk = HabitPaletteSpec(
        key = "monochrome",
        displayName = "Espresso Ink",
        subtitle = "Warm Archival Charcoal",
        level1 = Color(0xFF33312E),
        level2 = Color(0xFF5E5A54),
        level3 = Color(0xFF969188),
        level4 = Color(0xFFD6D2C9),
        onAccent = Color(0xFF1C1B19),
        lightLevel1 = Color(0xFFD8D4CC),
        lightLevel2 = Color(0xFFA8A39A),
        lightLevel3 = Color(0xFF6E6A63),
        lightLevel4 = Color(0xFF2B2927),
        lightOnAccent = Color(0xFFFCFBF9)
    )

    /**
     * All 8 primary architectural pigment palettes shown in the Habit Editor swatch picker.
     */
    val allPalettes: List<HabitPaletteSpec> = listOf(
        EmeraldMatrix,
        CrimsonEmber,
        CobaltPulse,
        CyberAmber,
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
            "emerald", "emerald_matrix", "green", "lime", "forest" -> EmeraldMatrix
            "crimson", "crimson_ember", "terracotta", "red" -> CrimsonEmber
            "cobalt", "cobalt_pulse", "blue", "indigo", "slate" -> CobaltPulse
            "amber", "cyber_amber", "ochre", "orange", "gold" -> CyberAmber
            "violet", "violet_nebula", "purple", "fig" -> VioletNebula
            "cyan", "cyan_glacier", "glacier", "teal", "fjord" -> CyanGlacier
            "rose", "rose_quartz", "quartz", "pink", "clay" -> RoseQuartz
            "monochrome", "monochrome_ink", "mono", "ink", "espresso" -> MonochromeInk
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
