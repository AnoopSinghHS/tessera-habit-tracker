package app.tessera.habit.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class ThemeMode(val key: String, val label: String) {
    ALABASTER_LIGHT("light", "Warm Limestone"),
    OBSIDIAN_DARK("dark", "Matte Carbon"),
    SYSTEM("system", "System");

    companion object {
        fun fromKey(key: String?): ThemeMode {
            return entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: ALABASTER_LIGHT
        }
    }
}

private val ObsidianMaterialColorScheme = darkColorScheme(
    primary = HabitPalette.EmeraldMatrix.level4,
    onPrimary = HabitPalette.EmeraldMatrix.onAccent,
    secondary = HabitPalette.CobaltPulse.level4,
    onSecondary = HabitPalette.CobaltPulse.onAccent,
    tertiary = HabitPalette.CyberAmber.level4,
    background = SurfaceAbyss,
    onBackground = TextPrimary,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = BorderHairline,
    outlineVariant = BorderSpecular,
    error = HabitPalette.CrimsonEmber.level4
)

private val AlabasterMaterialColorScheme = lightColorScheme(
    primary = HabitPalette.EmeraldMatrix.lightLevel4,
    onPrimary = SurfaceCardLight,
    secondary = HabitPalette.CobaltPulse.lightLevel4,
    onSecondary = SurfaceCardLight,
    tertiary = HabitPalette.CyberAmber.lightLevel4,
    background = SurfaceAbyssLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceCardLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceElevatedLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = BorderHairlineLight,
    outlineVariant = BorderSpecularLight,
    error = HabitPalette.CrimsonEmber.lightLevel4
)

object TesseraTheme {
    val colors: TesseraColorTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalTesseraColors.current
}

@Composable
fun TesseraTheme(
    themeMode: ThemeMode = ThemeMode.ALABASTER_LIGHT,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val useDark = when (themeMode) {
        ThemeMode.OBSIDIAN_DARK -> true
        ThemeMode.ALABASTER_LIGHT -> false
        ThemeMode.SYSTEM -> systemDark
    }

    val tesseraColors = if (useDark) ObsidianDarkColorTokens else AlabasterLightColorTokens
    val materialScheme = if (useDark) ObsidianMaterialColorScheme else AlabasterMaterialColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            @Suppress("DEPRECATION")
            window.statusBarColor = tesseraColors.surfaceAbyss.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = tesseraColors.surfaceCard.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !useDark
            controller.isAppearanceLightNavigationBars = !useDark
        }
    }

    CompositionLocalProvider(LocalTesseraColors provides tesseraColors) {
        MaterialTheme(
            colorScheme = materialScheme,
            typography = TesseraTypography,
            content = content
        )
    }
}
