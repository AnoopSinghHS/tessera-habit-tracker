package app.tessera.habit.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun TesseraTheme(content: @Composable () -> Unit) {
    val tokens = HomesteadColorTokens

    val materialColorScheme = lightColorScheme(
        primary = tokens.accent,
        onPrimary = tokens.onAccent,
        primaryContainer = tokens.heat[1],
        onPrimaryContainer = tokens.textPrimary,
        secondary = tokens.accent,
        onSecondary = tokens.onAccent,
        secondaryContainer = tokens.surfaceElevated,
        onSecondaryContainer = tokens.textPrimary,
        tertiary = tokens.accent,
        onTertiary = tokens.onAccent,
        background = tokens.surfaceAbyss,
        onBackground = tokens.textPrimary,
        surface = tokens.surfaceCard,
        onSurface = tokens.textPrimary,
        surfaceVariant = tokens.surfaceElevated,
        onSurfaceVariant = tokens.textSecondary,
        surfaceTint = Color.Transparent,
        surfaceContainerLowest = tokens.surfaceCard,
        surfaceContainerLow = tokens.surfaceCard,
        surfaceContainer = tokens.surfaceCard,
        surfaceContainerHigh = tokens.surfaceElevated,
        surfaceContainerHighest = tokens.surfaceElevated,
        outline = tokens.borderSpecular,
        outlineVariant = tokens.borderHairline,
        error = tokens.statusDanger,
        onError = Color.White
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = tokens.surfaceAbyss.toArgb()
                window.navigationBarColor = tokens.surfaceCard.toArgb()
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = true
                    isAppearanceLightNavigationBars = true
                }
            }
        }
    }

    CompositionLocalProvider(LocalTesseraColors provides tokens) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = TesseraTypography,
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
