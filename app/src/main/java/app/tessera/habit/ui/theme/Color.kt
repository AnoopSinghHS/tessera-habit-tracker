package app.tessera.habit.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ============================================================================
// 2.1 True AMOLED Obsidian Dark Theme (Default for Galaxy S26 Ultra)
// ============================================================================
val SurfaceAbyss = Color(0xFF07080B)
val SurfaceCard = Color(0xFF101217)
val SurfaceElevated = Color(0xFF161920)
val SurfaceRecessed = Color(0xFF181B22)
val BorderHairline = Color(0xFF1F242D)
val BorderSpecular = Color(0xFF2C3340)
val TextPrimary = Color(0xFFF3F4F6)
val TextSecondary = Color(0xFF8A919E)
val TextMuted = Color(0xFF525866)

// ============================================================================
// 2.2 Warm Alabaster Light Theme
// ============================================================================
val SurfaceAbyssLight = Color(0xFFF6F5F2)
val SurfaceCardLight = Color(0xFFFFFFFF)
val SurfaceElevatedLight = Color(0xFFEAE8E1)
val SurfaceRecessedLight = Color(0xFFEDEBE4)
val BorderHairlineLight = Color(0xFFE2DFD7)
val BorderSpecularLight = Color(0xFFC8C4B8)
val TextPrimaryLight = Color(0xFF111317)
val TextSecondaryLight = Color(0xFF686D76)
val TextMutedLight = Color(0xFF9CA3AF)

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
    val statusSuccess: Color = Color(0xFF34D399),
    val statusWarning: Color = Color(0xFFF59E0B),
    val statusDanger: Color = Color(0xFFF43F5E)
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
    textMuted = TextMuted
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
    textMuted = TextMutedLight
)

val LocalTesseraColors = staticCompositionLocalOf { ObsidianDarkColorTokens }
