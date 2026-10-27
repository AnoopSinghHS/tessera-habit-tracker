package app.tessera.habit.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ============================================================================
// 1. Warm Matte Carbon Dark Mode (Tactile Architectural Charcoal)
// ============================================================================
val SurfaceAbyss = Color(0xFF121210)
val SurfaceCard = Color(0xFF1A1917)
val SurfaceElevated = Color(0xFF23221F)
val SurfaceRecessed = Color(0xFF282622)
val BorderHairline = Color(0xFF33312C)
val BorderSpecular = Color(0xFF4A463F)
val TextPrimary = Color(0xFFF7F5F0)
val TextSecondary = Color(0xFFC4BFB6)
val TextMuted = Color(0xFF969086)

// ============================================================================
// 2. Warm Limestone & Paper Light Mode (Default Human-Crafted Surface)
// ============================================================================
val SurfaceAbyssLight = Color(0xFFF5F3EF)
val SurfaceCardLight = Color(0xFFFCFBF9)
val SurfaceElevatedLight = Color(0xFFEDEAE3)
val SurfaceRecessedLight = Color(0xFFEAE6DF)
val BorderHairlineLight = Color(0xFFDCD6CB)
val BorderSpecularLight = Color(0xFFC2B9AA)
val TextPrimaryLight = Color(0xFF141311)
val TextSecondaryLight = Color(0xFF4A4742)
val TextMutedLight = Color(0xFF6B665E)

// ============================================================================
// 3. Dune Arrakis Theme (Desert Basalt, Spiced Sandstone & Melange Amber)
// ============================================================================
val DuneAbyss = Color(0xFF13100D)
val DuneCard = Color(0xFF1B1612)
val DuneElevated = Color(0xFF261F1A)
val DuneRecessed = Color(0xFF2E251E)
val DuneBorder = Color(0xFF3E3228)
val DuneSpecular = Color(0xFF59483A)
val DuneTextPrimary = Color(0xFFFAF3EA)
val DuneTextSecondary = Color(0xFFD2C0AD)
val DuneTextMuted = Color(0xFFA38F7B)

// ============================================================================
// 4. Deep Space Theme (Celestial Starlight Titanium, Cosmic Void & Stellar Blue)
// ============================================================================
val SpaceAbyss = Color(0xFF090A0D)
val SpaceCard = Color(0xFF12141A)
val SpaceElevated = Color(0xFF191C24)
val SpaceRecessed = Color(0xFF20242E)
val SpaceBorder = Color(0xFF2D3446)
val SpaceSpecular = Color(0xFF434C63)
val SpaceTextPrimary = Color(0xFFF5F8FF)
val SpaceTextSecondary = Color(0xFFB8C2D8)
val SpaceTextMuted = Color(0xFF8893AB)

// ============================================================================
// 5. Tatooine Dusk Theme (Twin-Sun Sunset Terracotta & Weathered Sandstone)
// ============================================================================
val TatooineAbyss = Color(0xFF161212)
val TatooineCard = Color(0xFF201919)
val TatooineElevated = Color(0xFF2B2222)
val TatooineRecessed = Color(0xFF362B2B)
val TatooineBorder = Color(0xFF4A3A3A)
val TatooineSpecular = Color(0xFF665050)
val TatooineTextPrimary = Color(0xFFFAF2F2)
val TatooineTextSecondary = Color(0xFFD4BABA)
val TatooineTextMuted = Color(0xFFA68A8A)

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
