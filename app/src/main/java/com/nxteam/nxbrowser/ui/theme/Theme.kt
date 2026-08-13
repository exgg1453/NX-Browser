package com.nxteam.nxbrowser.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

val NXBlue = Color(0xFF5B8CFF)
val NXViolet = Color(0xFF7C5CFF)
val NXTeal = Color(0xFF00C2A8)
val NXIncognito = Color(0xFF6E5AA8)

private val LightColors = lightColorScheme(
    primary = NXBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE6FF),
    onPrimaryContainer = Color(0xFF0B234F),
    secondary = NXViolet,
    onSecondary = Color.White,
    tertiary = NXTeal,
    background = Color(0xFFF3F5FA),
    onBackground = Color(0xFF12151C),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF12151C),
    surfaceVariant = Color(0xFFE6EAF3),
    onSurfaceVariant = Color(0xFF4A5062),
    outline = Color(0xFFB9C0D0),
    error = Color(0xFFD9455F)
)

private val DarkColors = darkColorScheme(
    primary = NXBlue,
    onPrimary = Color(0xFF06122B),
    primaryContainer = Color(0xFF1B2C55),
    onPrimaryContainer = Color(0xFFD6E2FF),
    secondary = NXViolet,
    onSecondary = Color(0xFF130A2E),
    tertiary = NXTeal,
    background = Color(0xFF0B0F1A),
    onBackground = Color(0xFFE6E9F2),
    surface = Color(0xFF131826),
    onSurface = Color(0xFFE6E9F2),
    surfaceVariant = Color(0xFF1E2436),
    onSurfaceVariant = Color(0xFFA9B1C6),
    outline = Color(0xFF39415A),
    error = Color(0xFFFF6B81)
)

private val NXTypography = Typography(
    headlineLarge = TextStyle(fontSize = 34.sp, fontWeight = FontWeight.Bold),
    headlineMedium = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(fontSize = 16.sp),
    bodyMedium = TextStyle(fontSize = 14.sp),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium)
)

@Composable
fun NXTheme(
    themeMode: String = "system",
    incognito: Boolean = false,
    content: @Composable () -> Unit
) {
    val dark = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    } || incognito

    val colors = if (dark) DarkColors else LightColors
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colors.background.toArgb()
            window.navigationBarColor = colors.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !dark
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !dark
        }
    }

    MaterialTheme(
        colorScheme = colors,
        typography = NXTypography,
        content = content
    )
}
