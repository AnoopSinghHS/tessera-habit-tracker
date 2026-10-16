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
    ARRAKIS_DUNE("dune", "Dune Arrakis"),
    DEEP_SPACE("space", "Deep Space"),
    TATOOINE_DUSK("tatooine", "Tatooine Dusk"),
    SYSTEM("system", "System");

    companion object {
        fun fromKey(key: String?): ThemeMode {
            return entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: ALABASTER_LIGHT
        }
    }
}

@Composable
fun TesseraTheme(
    themeMode: ThemeMode = ThemeMode.ALABASTER_LIGHT,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val tokens = when (themeMode) {
        ThemeMode.ALABASTER_LIGHT -> AlabasterLightColorTokens
        ThemeMode.OBSIDIAN_DARK -> ObsidianDarkColorTokens
        ThemeMode.ARRAKIS_DUNE -> DuneArrakisColorTokens
        ThemeMode.DEEP_SPACE -> DeepSpaceColorTokens
        ThemeMode.TATOOINE_DUSK -> TatooineDuskColorTokens
        ThemeMode.SYSTEM -> if (isSystemDark) ObsidianDarkColorTokens else AlabasterLightColorTokens
    }

    val materialColorScheme = if (tokens.isDark) {
        darkColorScheme(
            primary = tokens.statusSuccess,
            background = tokens.surfaceAbyss,
            surface = tokens.surfaceCard,
            surfaceVariant = tokens.surfaceElevated,
            onBackground = tokens.textPrimary,
            onSurface = tokens.textPrimary,
            outline = tokens.borderHairline
        )
    } else {
        lightColorScheme(
            primary = tokens.statusSuccess,
            background = tokens.surfaceAbyss,
            surface = tokens.surfaceCard,
            surfaceVariant = tokens.surfaceElevated,
            onBackground = tokens.textPrimary,
            onSurface = tokens.textPrimary,
            outline = tokens.borderHairline
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = tokens.surfaceAbyss.toArgb()
                window.navigationBarColor = tokens.surfaceAbyss.toArgb()
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !tokens.isDark
                    isAppearanceLightNavigationBars = !tokens.isDark
                }
            }
        }
    }

    CompositionLocalProvider(LocalTesseraColors provides tokens) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            content = content
        )
    }
}

object TesseraTheme {
    val colors: TesseraColorTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalTesseraColors.current
}
