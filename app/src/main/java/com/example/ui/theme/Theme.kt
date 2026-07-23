package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CyberDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color(0xFF00325B),
    primaryContainer = Color(0xFF004880),
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = NeonPurple,
    onSecondary = Color(0xFF381E72),
    secondaryContainer = Color(0xFF4F378B),
    onSecondaryContainer = Color(0xEFE7F4D),
    tertiary = NeonEmerald,
    onTertiary = Color(0xFF003915),
    background = CyberBackground,
    onBackground = Color(0xFFE6EDF3),
    surface = CyberSurface,
    onSurface = Color(0xFFE6EDF3),
    surfaceVariant = CyberSurfaceVariant,
    onSurfaceVariant = Color(0xFF8B949E),
    outline = CyberBorder,
    error = NeonCoral
)

private val CyberLightColorScheme = lightColorScheme(
    primary = Color(0xFF0969DA),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDF4FF),
    onPrimaryContainer = Color(0xFF0969DA),
    secondary = Color(0xFF8250DF),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFBEFFF),
    onSecondaryContainer = Color(0xFF8250DF),
    tertiary = Color(0xFF1A7F37),
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = Color(0xFF1F2328),
    surface = LightSurface,
    onSurface = Color(0xFF1F2328),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF656D76),
    outline = LightBorder,
    error = Color(0xFFCF222E)
)

private val AmoledDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color.Black,
    secondary = NeonPurple,
    tertiary = NeonEmerald,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color(0xFF050505),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF121212),
    onSurfaceVariant = Color(0xFFB0B0B0),
    outline = Color(0xFF222222),
    error = NeonCoral
)

enum class ThemeMode {
    CYBER_DARK,
    LIGHT,
    AMOLED_DARK
}

@Composable
fun DevWorkspaceTheme(
    themeMode: ThemeMode = ThemeMode.CYBER_DARK,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        ThemeMode.CYBER_DARK -> CyberDarkColorScheme
        ThemeMode.LIGHT -> CyberLightColorScheme
        ThemeMode.AMOLED_DARK -> AmoledDarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
