package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Maroon80,
    onPrimary = MaroonDark,
    primaryContainer = ChocoMedium,
    onPrimaryContainer = Color(0xFFF2E7DC),
    secondary = Gold80,
    onSecondary = Color(0xFF432C00),
    secondaryContainer = Gold40,
    onSecondaryContainer = Color(0xFFFFE0A4),
    tertiary = Rose80,
    background = Color(0xFF1F1610),
    surface = Color(0xFF281C15),
    onBackground = Color(0xFFEFE7E0),
    onSurface = Color(0xFFEFE7E0),
    surfaceVariant = Color(0xFF3B2B22),
    onSurfaceVariant = Color(0xFFD7C7BD)
)

private val LightColorScheme = lightColorScheme(
    primary = Maroon40,
    onPrimary = Color.White,
    primaryContainer = ChocoTonal,
    onPrimaryContainer = MaroonDark,
    secondary = GoldWarm,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFF0D4),
    onSecondaryContainer = Color(0xFF452D00),
    tertiary = Rose40,
    background = CreamBackground,
    surface = CreamSurface,
    surfaceVariant = CreamSurfaceVariant,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary
)

@Composable
fun DjandesTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Lock to LightColorScheme for clear POS readability regardless of system dark mode
    val colorScheme = LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                // Set status bar to dark chocolate brown matching app headers
                window.statusBarColor = ChocoBrown.toArgb()
                // Light icons (white battery, wifi, signal) on dark chocolate status bar
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    DjandesTheme(darkTheme = false, dynamicColor = false, content = content)
}
