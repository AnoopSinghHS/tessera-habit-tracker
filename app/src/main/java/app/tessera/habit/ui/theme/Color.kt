package app.tessera.habit.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ============================================================================
// Warm Matte Carbon Dark Mode (Tactile Architectural Charcoal)
// ============================================================================
val SurfaceAbyss = Color(0xFF121210)
val SurfaceCard = Color(0xFF1A1917)
val SurfaceElevated = Color(0xFF22211E)
val SurfaceRecessed = Color(0xFF262421)
val BorderHairline = Color(0xFF262421)
val BorderSpecular = Color(0xFF36332E)
val TextPrimary = Color(0xFFF2EFE9)
val TextSecondary = Color(0xFF8E8A82)
val TextMuted = Color(0xFF635F58)

// ============================================================================
// Warm Limestone & Paper Light Mode (Default Human-Crafted Surface)
// ============================================================================
val SurfaceAbyssLight = Color(0xFFF5F3EF)
val SurfaceCardLight = Color(0xFFFCFBF9)
val SurfaceElevatedLight = Color(0xFFEDEAE3)
val SurfaceRecessedLight = Color(0xFFEAE6DF)
val BorderHairlineLight = Color(0xFFE8E4DC)
val BorderSpecularLight = Color(0xFFD8D2C7)
val TextPrimaryLight = Color(0xFF1C1B19)
val TextSecondaryLight = Color(0xFF78746D)
val TextMutedLight = Color(0xFFA09B92)

// ============================================================================
// Semantic Color System
// ============================================================================
@Immutable
data class TesseraColorTokens(
    val isDark: Boolean,
    val surfaceAbyss: Color,
    val surfaceCard: Color,
    val surfaceElevated: Color,
    val surfaceRecessed: Color,
    val borderHairline: Color,
    val borderSpecular: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val statusSuccess: Color = Color(0xFF2D5A43),
    val statusWarning: Color = Color(0xFFC27D38),
    val statusDanger: Color = Color(0xFFB85435)
)

val ObsidianDarkColorTokens = TesseraColorTokens(
    isDark = true,
    surfaceAbyss = SurfaceAbyss,
    surfaceCard = SurfaceCard,
    surfaceElevated = SurfaceElevated,
    surfaceRecessed = SurfaceRecessed,
    borderHairline = BorderHairline,
    borderSpecular = BorderSpecular,
    textPrimary = TextPrimary,
    textSecondary = TextSecondary,
    textMuted = TextMuted,
    statusSuccess = Color(0xFF488B69),
    statusWarning = Color(0xFFDFA055),
    statusDanger = Color(0xFFD46A48)
)

val AlabasterLightColorTokens = TesseraColorTokens(
    isDark = false,
    surfaceAbyss = SurfaceAbyssLight,
    surfaceCard = SurfaceCardLight,
    surfaceElevated = SurfaceElevatedLight,
    surfaceRecessed = SurfaceRecessedLight,
    borderHairline = BorderHairlineLight,
    borderSpecular = BorderSpecularLight,
    textPrimary = TextPrimaryLight,
    textSecondary = TextSecondaryLight,
    textMuted = TextMutedLight,
    statusSuccess = Color(0xFF2D5A43),
    statusWarning = Color(0xFFC27D38),
    statusDanger = Color(0xFFB85435)
)

val LocalTesseraColors = staticCompositionLocalOf { AlabasterLightColorTokens }
