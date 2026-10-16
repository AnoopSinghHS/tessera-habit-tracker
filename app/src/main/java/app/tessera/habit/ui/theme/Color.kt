package app.tessera.habit.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ============================================================================
// 1. Warm Matte Carbon Dark Mode (Tactile Architectural Charcoal)
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
// 2. Warm Limestone & Paper Light Mode (Default Human-Crafted Surface)
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
// 3. Dune Arrakis Theme (Desert Basalt, Spiced Sandstone & Melange Amber)
// ============================================================================
val DuneAbyss = Color(0xFF13100D)
val DuneCard = Color(0xFF1B1612)
val DuneElevated = Color(0xFF261F1A)
val DuneRecessed = Color(0xFF2E251E)
val DuneBorder = Color(0xFF332921)
val DuneSpecular = Color(0xFF473A2E)
val DuneTextPrimary = Color(0xFFF4ECE1)
val DuneTextSecondary = Color(0xFFA69584)
val DuneTextMuted = Color(0xFF756758)

// ============================================================================
// 4. Deep Space Theme (Celestial Starlight Titanium, Cosmic Void & Stellar Blue)
// ============================================================================
val SpaceAbyss = Color(0xFF090A0D)
val SpaceCard = Color(0xFF12141A)
val SpaceElevated = Color(0xFF191C24)
val SpaceRecessed = Color(0xFF20242E)
val SpaceBorder = Color(0xFF242936)
val SpaceSpecular = Color(0xFF343A4C)
val SpaceTextPrimary = Color(0xFFEFF2F8)
val SpaceTextSecondary = Color(0xFF8A93A6)
val SpaceTextMuted = Color(0xFF5D6475)

// ============================================================================
// 5. Tatooine Dusk Theme (Twin-Sun Sunset Terracotta & Weathered Sandstone)
// ============================================================================
val TatooineAbyss = Color(0xFF161212)
val TatooineCard = Color(0xFF201919)
val TatooineElevated = Color(0xFF2B2222)
val TatooineRecessed = Color(0xFF362B2B)
val TatooineBorder = Color(0xFF3D3030)
val TatooineSpecular = Color(0xFF544242)
val TatooineTextPrimary = Color(0xFFF5EBEB)
val TatooineTextSecondary = Color(0xFFA68D8D)
val TatooineTextMuted = Color(0xFF756060)

// ============================================================================
// Semantic Color Tokens
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

val DuneArrakisColorTokens = TesseraColorTokens(
    isDark = true,
    surfaceAbyss = DuneAbyss,
    surfaceCard = DuneCard,
    surfaceElevated = DuneElevated,
    surfaceRecessed = DuneRecessed,
    borderHairline = DuneBorder,
    borderSpecular = DuneSpecular,
    textPrimary = DuneTextPrimary,
    textSecondary = DuneTextSecondary,
    textMuted = DuneTextMuted,
    statusSuccess = Color(0xFF5B8A68),
    statusWarning = Color(0xFFD97736),
    statusDanger = Color(0xFFC84E32)
)

val DeepSpaceColorTokens = TesseraColorTokens(
    isDark = true,
    surfaceAbyss = SpaceAbyss,
    surfaceCard = SpaceCard,
    surfaceElevated = SpaceElevated,
    surfaceRecessed = SpaceRecessed,
    borderHairline = SpaceBorder,
    borderSpecular = SpaceSpecular,
    textPrimary = SpaceTextPrimary,
    textSecondary = SpaceTextSecondary,
    textMuted = SpaceTextMuted,
    statusSuccess = Color(0xFF4E9675),
    statusWarning = Color(0xFF5A82A6),
    statusDanger = Color(0xFFC45A5A)
)

val TatooineDuskColorTokens = TesseraColorTokens(
    isDark = true,
    surfaceAbyss = TatooineAbyss,
    surfaceCard = TatooineCard,
    surfaceElevated = TatooineElevated,
    surfaceRecessed = TatooineRecessed,
    borderHairline = TatooineBorder,
    borderSpecular = TatooineSpecular,
    textPrimary = TatooineTextPrimary,
    textSecondary = TatooineTextSecondary,
    textMuted = TatooineTextMuted,
    statusSuccess = Color(0xFF5E8C70),
    statusWarning = Color(0xFFC85A3A),
    statusDanger = Color(0xFFB5453E)
)

val LocalTesseraColors = staticCompositionLocalOf { AlabasterLightColorTokens }
