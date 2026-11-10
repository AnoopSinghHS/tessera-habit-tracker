package app.tessera.habit.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Homestead: the app's single theme. Sun-bleached sand under a clear blue sky.
 *
 * The theme owns all colour. There is one accent ([TesseraColorTokens.accent]) and one
 * five-step heatmap ramp ([TesseraColorTokens.heat]); habits do not bring their own colours.
 * Text colours pass WCAG AA on every surface they are used on.
 */
@Immutable
data class TesseraColorTokens(
    val isDark: Boolean,
    /** Page background. */
    val surfaceAbyss: Color,
    /** Cards and sheets. */
    val surfaceCard: Color,
    /** Raised or selected areas inside a card (chips, selected tab, input fill). */
    val surfaceElevated: Color,
    /** Recessed areas: empty heatmap cells, tracks. */
    val surfaceRecessed: Color,
    /** Dividers and quiet card edges. Decorative only. */
    val borderHairline: Color,
    /** Control boundaries (3:1 or better on surfaces). */
    val borderSpecular: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    /** The one accent: check-in buttons, selected states, links, key numbers. */
    val accent: Color,
    val onAccent: Color,
    /** Heatmap ramp, index 0 (empty) to 4 (target met). */
    val heat: List<Color>,
    val statusSuccess: Color,
    val statusWarning: Color,
    val statusDanger: Color
) {
    fun heatLevel(level: Int): Color = heat[level.coerceIn(0, 4)]
}

val HomesteadColorTokens = TesseraColorTokens(
    isDark = false,
    surfaceAbyss = Color(0xFFEFE3CE),
    surfaceCard = Color(0xFFFAF4E8),
    surfaceElevated = Color(0xFFE6D8BF),
    surfaceRecessed = Color(0xFFE6D9C2),
    borderHairline = Color(0xFFDCCBAE),
    borderSpecular = Color(0xFF8E7A61),
    textPrimary = Color(0xFF2A2016),
    textSecondary = Color(0xFF65533F),
    textMuted = Color(0xFF65533F),
    accent = Color(0xFF17599F),
    onAccent = Color(0xFFFFFFFF),
    heat = listOf(
        Color(0xFFE6D9C2),
        Color(0xFFA9CBE0),
        Color(0xFF74A9D3),
        Color(0xFF3B82C4),
        Color(0xFF17569C)
    ),
    statusSuccess = Color(0xFF17599F),
    statusWarning = Color(0xFF8A5A12),
    statusDanger = Color(0xFFA8321F)
)

val LocalTesseraColors = staticCompositionLocalOf { HomesteadColorTokens }
